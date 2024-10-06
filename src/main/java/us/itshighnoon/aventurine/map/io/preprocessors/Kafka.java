package us.itshighnoon.aventurine.map.io.preprocessors;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.Inflater;

import org.joml.Vector2L;

import us.itshighnoon.aventurine.map.Node;
import us.itshighnoon.aventurine.map.Way;
import us.itshighnoon.aventurine.map.io.ProtobufReader;
import us.itshighnoon.aventurine.map.io.ProtobufWriter;

/**
 * Kafka.java - convert osm.pbf files to .kfk files
 */
public class Kafka {
  public static void main(String[] args) {
    System.out.println("Started phase 1");
    phase1(args);
    System.out.println("Started phase 2");
    phase2(args);
    System.out.println("Done");
  }

  /**
   * Phase 1 - turn an osm.pbf file into a bunch of chunk files and one big ways
   * file
   */
  private static void phase1(String[] args) {
    Map<Vector2L, DataOutputStream> files;
    DataOutputStream wayFile;
    try {
      files = new HashMap<Vector2L, DataOutputStream>();
      Path folder = Path.of(args[1]);
      wayFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(folder.toString() + "/all.ways")));
      long chunkSize = Long.parseLong(args[2]);
      ProtobufReader protoReader = new ProtobufReader(new File(args[0]));

      while (!protoReader.finished()) {
        // 4 bytes of length which tells us when we are out of the BlobHeader
        int blobHeaderLength = protoReader.read();
        blobHeaderLength = (blobHeaderLength << 8) + protoReader.read();
        blobHeaderLength = (blobHeaderLength << 8) + protoReader.read();
        blobHeaderLength = (blobHeaderLength << 8) + protoReader.read();
        
        long expectedHeaderEnd = protoReader.getCursor() + blobHeaderLength;

        // BlobHeader which tells us the type and size of the following Blob
        boolean isOsmData = false;
        int blobSize = 0;
        while (protoReader.getCursor() < expectedHeaderEnd) {
          long identifier = protoReader.readVarint();
          switch ((int) (identifier >> 3)) {
          case 1:
            String type = protoReader.readString();
            if (type.equals("OSMHeader")) {
              isOsmData = false;
            } else if (type.equals("OSMData")) {
              isOsmData = true;
            } else {
              throw new IOException();
            }
            break;
          case 3:
            blobSize = (int) protoReader.readVarint();
            break;
          default:
            protoReader.skipThisField(identifier);
          }
        }
        long expectedDataEnd = protoReader.getCursor() + blobSize;

        // Blob which may be compressed
        byte[] compressedData = null;
        byte[] uncompressedData = null;
        int rawDataSize = 0;
        while (protoReader.getCursor() < expectedDataEnd) {
          long identifier = protoReader.readVarint();
          switch ((int) (identifier >> 3)) {
          case 2:
            rawDataSize = (int) protoReader.readVarint();
            break;
          case 1:
            uncompressedData = protoReader.readBytes();
            break;
          case 3:
            compressedData = protoReader.readBytes();
            break;
          default:
            compressedData = protoReader.readBytes();
          }
        }

        // Inflate if necessary
        if (uncompressedData == null) {
          int inflatedBytes = 0;
          if (rawDataSize != 0) {
            uncompressedData = new byte[rawDataSize];
          } else {
            uncompressedData = new byte[compressedData.length * 2];
          }
          Inflater inflater = new Inflater();
          inflater.setInput(compressedData);
          while (!inflater.finished()) {
            inflatedBytes += inflater.inflate(uncompressedData, inflatedBytes, uncompressedData.length - inflatedBytes);
            if (!inflater.finished()) {
              byte[] biggerData = new byte[uncompressedData.length * 2];
              uncompressedData = biggerData;
            }
          }
          if (inflatedBytes != uncompressedData.length) {
            byte[] trimmedData = new byte[inflatedBytes];
            System.arraycopy(uncompressedData, 0, trimmedData, 0, inflatedBytes);
            uncompressedData = trimmedData;
          }
        }

        // Defer to blob reader
        readBlob(new ProtobufReader(uncompressedData), isOsmData, files, folder, chunkSize, wayFile);
      }
      protoReader.close();
      for (DataOutputStream dos : files.values()) {
        dos.close();
      }
      wayFile.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Phase 2 - build kfk files
   */
  private static void phase2(String[] args) {
    File folder = new File(args[1]);
    File[] files = folder.listFiles();
    List<DataOutputStream> kfkFiles = new ArrayList<DataOutputStream>();
    Map<Long, Integer> nodeOwner = new HashMap<Long, Integer>();
    for (int fileIdx = 0; fileIdx < files.length; fileIdx++) {
      File inFile = files[fileIdx];
      if (inFile.getName().endsWith(".dat")) {
        try {
          // Read in all nodes from dat
          List<Node> fileNodes = new ArrayList<Node>();
          Set<Long> nodesInChunk = new HashSet<Long>();
          ProtobufReader reader = new ProtobufReader(inFile);
          while (!reader.finished()) {
            Node node = Node.readFromKafka(reader, null);
            fileNodes.add(node);
            nodesInChunk.add(node.getId());
          }
          
          // Write nodes to start of kfk file
          String outFileName = Path.of(args[1]).toString() + "/" + inFile.getName().substring(0, inFile.getName().length() - 4) + ".kfk";
          DataOutputStream outFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFileName)));
          ProtobufWriter writer = new ProtobufWriter(outFile);
          writer.writeVarint(fileNodes.size());
          Node lastNode = null;
          for (Node node : fileNodes) {
            node.writeToKafka(writer, lastNode);
            lastNode = node;
            nodeOwner.put(node.getId(), kfkFiles.size());
          }
          
          // Add file to list for ways step
          kfkFiles.add(outFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      System.out.printf("Nodes progress: %d / %d\n", fileIdx, files.length);
    }
    
    // Read in all ways from all.ways
    Map<Integer, Set<Way>> waysInFiles = new HashMap<Integer, Set<Way>>();
    String waysFileName = Path.of(args[1]).toString() + "/all.ways";
    try {
      File waysFile = new File(waysFileName);
      ProtobufReader reader = new ProtobufReader(waysFile);
      long nextCheckpoint = waysFile.length() / 20;
      while (!reader.finished()) {
        if (reader.getCursor() > nextCheckpoint) {
          System.out.printf("Progress through ways file: %d / %d\n", reader.getCursor(), waysFile.length());
          nextCheckpoint += waysFile.length() / 20;
        }
        Way way = Way.readRawFromKafka(reader);
        Set<Long> nodesInWay = way.getNodeSet();
        for (long nodeId : nodesInWay) {
          Integer fileIdx = nodeOwner.get(nodeId);
          if (fileIdx == null) {
            System.err.println("Node not in file");
            continue;
          }
          if (waysInFiles.containsKey(fileIdx)) {
            waysInFiles.get(fileIdx).add(way);
          } else {
            Set<Way> newWaysSet = new HashSet<Way>();
            newWaysSet.add(way);
            waysInFiles.put(fileIdx, newWaysSet);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Done with ways, finalizing files");
    
    // Write ways to kfk files
    for (Entry<Integer, Set<Way>> fileWays : waysInFiles.entrySet()) {
      int fileIdx = fileWays.getKey();
      Set<Way> ways = fileWays.getValue();
      DataOutputStream outFile = kfkFiles.get(fileIdx);
      ProtobufWriter writer = new ProtobufWriter(outFile);
      try {
        writer.writeVarint(ways.size());
        for (Way way : ways) {
          way.writeToKafka(writer);
        }
        outFile.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void readBlob(ProtobufReader reader, boolean isOsmData, Map<Vector2L, DataOutputStream> files,
      Path folder, long chunkSize, DataOutputStream wayFile) throws Exception {
    if (!isOsmData) {
      return;
    }
    
    List<String> stringTable = null;
    
    while (!reader.finished()) {
      int granularity = 100;
      long latOffset = 0;
      long lonOffset = 0;
      long identifier = reader.readVarint();
      switch ((int) (identifier >> 3)) {
      case 1:
        stringTable = new ArrayList<String>();
        long stringTableEnd = reader.readVarint() + reader.getCursor();
        while (reader.getCursor() < stringTableEnd) {
          long stringIdentifier = reader.readVarint();
          if (stringIdentifier >> 3 == 1) {
            stringTable.add(reader.readString());
          } else {
            reader.skipThisField(stringIdentifier);
          }
        }
        break;
      case 2:
        long primitiveGroupEnd = reader.readVarint() + reader.getCursor();
        while (reader.getCursor() < primitiveGroupEnd) {
          long primitiveIdentifier = reader.readVarint();
          switch ((int) (primitiveIdentifier >> 3)) {
          case 1:
            // 1 is "Node"
            throw new IOException();
          case 2:
            // 2 is "DenseNodes"
            List<Long> denseIds = new ArrayList<Long>();
            List<Long> denseLats = new ArrayList<Long>();
            List<Long> denseLons = new ArrayList<Long>();
            List<Map<String, String>> denseAttribs = new ArrayList<Map<String, String>>();
            long denseNodesEnd = reader.readVarint() + reader.getCursor();
            while (reader.getCursor() < denseNodesEnd) {
              long denseIdentifier = reader.readVarint();
              switch ((int) (denseIdentifier >> 3)) {
              case 1:
                long denseIdsEnd = reader.readVarint() + reader.getCursor();
                long lastId = 0;
                while (reader.getCursor() < denseIdsEnd) {
                  long currentId = reader.readVarintSigned() + lastId;
                  denseIds.add(currentId);
                  lastId = currentId;
                }
                break;
              case 8:
                long denseLatsEnd = reader.readVarint() + reader.getCursor();
                long lastLat = 0;
                while (reader.getCursor() < denseLatsEnd) {
                  long currentLat = reader.readVarintSigned() + lastLat;
                  denseLats.add(currentLat);
                  lastLat = currentLat;
                }
                break;
              case 9:
                long denseLonsEnd = reader.readVarint() + reader.getCursor();
                long lastLon = 0;
                while (reader.getCursor() < denseLonsEnd) {
                  long currentLon = reader.readVarintSigned() + lastLon;
                  denseLons.add(currentLon);
                  lastLon = currentLon;
                }
                break;
              case 10:
                long keysValsEnd = reader.readVarint() + reader.getCursor();
                Map<String, String> currentAttribs = null;
                while (reader.getCursor() < keysValsEnd) {
                  int keyStringIdx = (int) reader.readVarint();
                  if (keyStringIdx == 0) {
                    denseAttribs.add(currentAttribs);
                    currentAttribs = null;
                    continue;
                  }
                  int valueStringIdx = (int) reader.readVarint();
                  if (currentAttribs == null) {
                    currentAttribs = new HashMap<String, String>();
                  }
                  currentAttribs.put(stringTable.get(keyStringIdx), stringTable.get(valueStringIdx));
                }
                break;
              default:
                reader.skipThisField(denseIdentifier);
              }
            }
            if (denseIds.size() != denseLats.size() || denseLats.size() != denseLons.size()
                || (denseIds.size() != denseAttribs.size() && !denseAttribs.isEmpty())) {
              throw new Exception();
            } else {
              for (int denseIdx = 0; denseIdx < denseIds.size(); denseIdx++) {
                long realLat = latOffset + (granularity * denseLats.get(denseIdx));
                long realLon = lonOffset + (granularity * denseLons.get(denseIdx));
                Map<String, String> attribs = null;
                if (!denseAttribs.isEmpty()) {
                  attribs = denseAttribs.get(denseIdx);
                }
                Node node = new Node(denseIds.get(denseIdx), realLat, realLon, attribs);
                Vector2L chunkIdx = new Vector2L(realLon / chunkSize, realLat / chunkSize);
                DataOutputStream dos = files.get(chunkIdx);
                if (dos == null) {
                  dos = new DataOutputStream(new BufferedOutputStream(
                      new FileOutputStream(folder.toString() + "/" + chunkIdx.x + "." + chunkIdx.y + ".dat")));
                  files.put(chunkIdx, dos);
                }
                ProtobufWriter writer = new ProtobufWriter(dos);
                node.writeToKafka(writer, null);
              }
            }
            break;
          case 3:
            // 3 is "Way"
            List<Long> nodeIds = new ArrayList<Long>();
            List<Integer> keyStrings = new ArrayList<Integer>();
            List<Integer> valueStrings = new ArrayList<Integer>();
            long wayEnd = reader.readVarint() + reader.getCursor();
            long wayId = 0;
            while (reader.getCursor() < wayEnd) {
              long wayIdentifier = reader.readVarint();
              switch ((int) (wayIdentifier >> 3)) {
              case 1:
                wayId = reader.readVarint();
                break;
              case 2:
                long keyStringsEnd = reader.readVarint() + reader.getCursor();
                while (reader.getCursor() < keyStringsEnd) {
                  keyStrings.add((int) reader.readVarint());
                }
                break;
              case 3:
                long valueStringsEnd = reader.readVarint() + reader.getCursor();
                while (reader.getCursor() < valueStringsEnd) {
                  valueStrings.add((int) reader.readVarint());
                }
                break;
              case 8:
                long nodeId = 0;
                long nodeIdsEnd = reader.readVarint() + reader.getCursor();
                while (reader.getCursor() < nodeIdsEnd) {
                  nodeId = reader.readVarintSigned() + nodeId;
                  nodeIds.add(nodeId);
                }
                break;
              default:
                reader.skipThisField(wayIdentifier);
              }
            }
            if (nodeIds == null || nodeIds.size() == 0) {
              throw new Exception();
            } else {
              long[] nodeIndices = new long[nodeIds.size()];
              for (int nodeIdx = 0; nodeIdx < nodeIndices.length; nodeIdx++) {
                nodeIndices[nodeIdx] = nodeIds.get(nodeIdx);
              }
              Map<String, String> attribs = null;
              if (!keyStrings.isEmpty()) {
                if (keyStrings.size() != valueStrings.size()) {
                  throw new Exception();
                }
                attribs = new HashMap<String, String>();
                for (int attribIdx = 0; attribIdx < keyStrings.size(); attribIdx++) {
                  int keyStringIdx = keyStrings.get(attribIdx);
                  int valueStringIdx = valueStrings.get(attribIdx);
                  if (keyStringIdx == 0 || valueStringIdx == 0) {
                    throw new Exception();
                  }
                  attribs.put(stringTable.get(keyStringIdx), stringTable.get(valueStringIdx));
                }
              }
              Way way = new Way(wayId, attribs, nodeIndices);
              ProtobufWriter writer = new ProtobufWriter(wayFile);
              way.writeToKafka(writer);
            }
            break;
          case 4:
            // 4 is "Relation"
            reader.skipThisField(identifier);
            break;
          case 5:
            // 5 is "ChangeSet"
            reader.skipThisField(identifier);
            break;
          default:
            reader.skipThisField(identifier);
            break;
          }
        }
        break;
      case 17:
        granularity = (int) reader.readVarint();
        break;
      case 19:
        latOffset = reader.readVarint();
        break;
      case 20:
        lonOffset = reader.readVarint();
        break;
      default:
        reader.skipThisField(identifier);
      }
    }
  }
}
