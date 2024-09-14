package us.itshighnoon.aventurine.map.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Inflater;

import org.joml.Vector2L;

/**
 * Kafka.java - step 1 of reformatting osm.pbf
 */
public class Kafka {
  public static void main(String[] args) {
    System.out.println("Started phase 1");
    phase1(args);
    System.out.println("Started phase 2");
    Map<Long, Long[]> wayToNodes = new HashMap<Long, Long[]>();
    Map<Long, Set<Long>> nodeToWays = new HashMap<Long, Set<Long>>();
    phase2(args, wayToNodes, nodeToWays);
    System.out.println("Started phase 3");
    phase3(args, wayToNodes, nodeToWays);
    System.out.println("Done");
  }
  
  /**
   * Phase 1 - turn an osm.pbf file into a bunch of chunk files and one big ways file
   */
  private static void phase1(String[] args) {
    Map<Vector2L, DataOutputStream> files;
    DataOutputStream wayFile;
    try {
      files = new HashMap<Vector2L, DataOutputStream>();
      Path folder = Path.of(args[1]);
      wayFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(folder.toString() + "/all.ways")));
      long chunkSize = Long.parseLong(args[2]);
      RandomAccessFile file = new RandomAccessFile(args[0], "r");
      ProtobufReader protoReader = new ProtobufReader(file);

      while (file.getFilePointer() < file.length()) {
        // 4 bytes of length which tells us when we are out of the BlobHeader
        int blobHeaderLength = file.readInt();
        long expectedHeaderEnd = file.getFilePointer() + blobHeaderLength;

        // BlobHeader which tells us the type and size of the following Blob
        boolean isOsmData = false;
        int blobSize = 0;
        while (file.getFilePointer() < expectedHeaderEnd) {
          long identifier = protoReader.readVarint();
          switch ((int) (identifier >> 3)) {
          case 1:
            String type = protoReader.readString();
            if (type.equals("OSMHeader")) {
              isOsmData = false;
            } else if (type.equals("OSMData")) {
              isOsmData = true;
            } else {
              file.close();
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
        long expectedDataEnd = file.getFilePointer() + blobSize;

        // Blob which may be compressed
        byte[] compressedData = null;
        byte[] uncompressedData = null;
        int rawDataSize = 0;
        while (file.getFilePointer() < expectedDataEnd) {
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
      file.close();
      for (DataOutputStream dos : files.values()) {
        dos.close();
      }
      wayFile.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Phase 2 - extract way information into faster data structures
   */
  private static void phase2(String[] args, Map<Long, Long[]> wayToNodes, Map<Long, Set<Long>> nodeToWays) {
    Path folder = Path.of(args[1]);
    try {
      DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(folder.toString() + "/all.ways")));
      while (dis.available() > 0) {
        long wayId = dis.readLong();
        int numberNodes = (int) dis.readLong();
        Long[] wayNodes = new Long[numberNodes];
        for (int nodeIdx = 0; nodeIdx < wayNodes.length; nodeIdx++) {
          long node = dis.readLong();
          Set<Long> containingWays = nodeToWays.get(node);
          if (containingWays == null) {
            containingWays = new HashSet<Long>();
            nodeToWays.put(node, containingWays);
          }
          containingWays.add(wayId);
          wayNodes[nodeIdx] = node;
        }
        wayToNodes.put(wayId, wayNodes);
      }
      dis.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Phase 3 - bake node files into complete data files
   */
  private static void phase3(String[] args, Map<Long, Long[]> wayToNodes, Map<Long, Set<Long>> nodeToWays) {
    File folder = new File(args[1]);
    File[] files = folder.listFiles();
    for (File inFile : files) {
      if (inFile.getName().endsWith(".dat")) {
        String outFile = "/" + inFile.getName().substring(0, inFile.getName().length() - 4) + ".kfk";
        outFile = folder.getPath() + outFile;
        try {
          DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
          long nodesInFile = inFile.length() / 24;
          DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(inFile)));
          dos.writeLong(nodesInFile);
          Set<Long> waysInChunk = new HashSet<Long>();
          for (int nodeIdx = 0; nodeIdx < nodesInFile; nodeIdx++) {
            long nodeId = dis.readLong();
            dos.writeLong(nodeId);
            Set<Long> waysForNode = nodeToWays.get(nodeId);
            if (waysForNode != null) {
              waysInChunk.addAll(waysForNode);
            }
            dos.writeLong(dis.readLong()); // lat
            dos.writeLong(dis.readLong()); // lon
          }
          dis.close();
          dos.writeLong(waysInChunk.size());
          for (Long wayId : waysInChunk) {
            Long[] way = wayToNodes.get(wayId);
            dos.writeLong(wayId);
            dos.writeLong(way.length);
            for (long nodeIdInWay : way) {
              dos.writeLong(nodeIdInWay);
            }
          }
          dos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  private static void readBlob(ProtobufReader reader, boolean isOsmData, Map<Vector2L, DataOutputStream> files,
      Path folder, long chunkSize, DataOutputStream wayFile) throws Exception {
    if (!isOsmData) {
      return;
    }

    while (!reader.finished()) {
      int granularity = 100;
      long latOffset = 0;
      long lonOffset = 0;
      long identifier = reader.readVarint();
      List<String> stringTable = new ArrayList<String>();
      switch ((int) (identifier >> 3)) {
      case 1:
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
              default:
                reader.skipThisField(denseIdentifier);
              }
            }
            if (denseIds.size() != denseLats.size() || denseLats.size() != denseLons.size()) {
              throw new Exception();
            } else {
              for (int denseIdx = 0; denseIdx < denseIds.size(); denseIdx++) {
                long realLat = latOffset + (granularity * denseLats.get(denseIdx));
                long realLon = lonOffset + (granularity * denseLons.get(denseIdx));
                Vector2L chunkIdx = new Vector2L(realLon / chunkSize, realLat / chunkSize);
                DataOutputStream dos = files.get(chunkIdx);
                if (dos == null) {
                  dos = new DataOutputStream(new BufferedOutputStream(
                      new FileOutputStream(folder.toString() + "/" + chunkIdx.x + "." + chunkIdx.y + ".dat")));
                  files.put(chunkIdx, dos);
                }
                dos.writeLong(denseIds.get(denseIdx));
                dos.writeLong(realLat);
                dos.writeLong(realLon);
              }
            }
            break;
          case 3:
            // 3 is "Way"
            List<Long> nodeIds = new ArrayList<Long>();
            long wayEnd = reader.readVarint() + reader.getCursor();
            long wayId = 0;
            while (reader.getCursor() < wayEnd) {
              long wayIdentifier = reader.readVarint();
              switch ((int) (wayIdentifier >> 3)) {
              case 1:
                wayId = reader.readVarint();
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
              wayFile.writeLong(wayId);
              wayFile.writeLong(nodeIds.size());
              for (Long node : nodeIds) {
                wayFile.writeLong(node);
              }
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
