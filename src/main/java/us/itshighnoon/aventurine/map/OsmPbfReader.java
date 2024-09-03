package us.itshighnoon.aventurine.map;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.joml.Vector3f;

import us.itshighnoon.aventurine.util.Logger;

public class OsmPbfReader {
  private static enum OsmBlobType {
    OSM_HEADER, OSM_DATA
  };

  public static void readMap(String filepath, List<Vector3f> outList) {
    // https://wiki.openstreetmap.org/wiki/PBF_Format
    try {
      RandomAccessFile file = new RandomAccessFile(filepath, "r");
      ProtobufReader protoReader = new ProtobufReader(file);

      while (file.getFilePointer() < file.length()) {
        // 4 bytes of length which tells us when we are out of the BlobHeader
        int blobHeaderLength = file.readInt();
        long expectedHeaderEnd = file.getFilePointer() + blobHeaderLength;

        // BlobHeader which tells us the type and size of the following Blob
        OsmBlobType blobType = null;
        int blobSize = 0;
        while (file.getFilePointer() < expectedHeaderEnd) {
          long identifier = protoReader.readVarint();
          switch ((int) (identifier >> 3)) {
          case 1:
            String type = protoReader.readString();
            if (type.equals("OSMHeader")) {
              blobType = OsmBlobType.OSM_HEADER;
            } else if (type.equals("OSMData")) {
              blobType = OsmBlobType.OSM_DATA;
            } else {
              Logger.log("0033 Invalid BlobHeader.type", Logger.Severity.ERROR);
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
            Logger.log("0034 Blob.Data is not raw or zlib", Logger.Severity.WARN);
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
          try {
            while (!inflater.finished()) {
              inflatedBytes += inflater.inflate(uncompressedData, inflatedBytes,
                  uncompressedData.length - inflatedBytes);
              if (!inflater.finished()) {
                Logger.log("0036 Blob.Data required an expansion", Logger.Severity.INFO);
                byte[] biggerData = new byte[uncompressedData.length * 2];
                System.arraycopy(uncompressedData, 0, biggerData, 0, inflatedBytes);
                uncompressedData = biggerData;
              }
            }
          } catch (DataFormatException e) {
            Logger.log("0035 Failed to inflate compressed Blob.Data", Logger.Severity.WARN);
          }
          if (inflatedBytes != uncompressedData.length) {
            byte[] trimmedData = new byte[inflatedBytes];
            System.arraycopy(uncompressedData, 0, trimmedData, 0, inflatedBytes);
            uncompressedData = trimmedData;
          }
        }

        // Defer to blob reader
        readBlob(new ProtobufReader(uncompressedData), blobType, outList);
      }
      file.close();
    } catch (IOException e) {
      Logger.log("0030 Failed to load map " + filepath, Logger.Severity.ERROR);
    }
    Logger.log("0041 Successfully loaded map " + filepath, Logger.Severity.INFO);
  }

  private static void readBlob(ProtobufReader reader, OsmBlobType type, List<Vector3f> outList) throws IOException {
    if (type == OsmBlobType.OSM_HEADER || type == null) {
      // Wow! This is useless!
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
            Logger.log("0039 StringTable had something other than strings");
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
            Logger.log("0038 Node found, unsure what to do", Logger.Severity.WARN);
            throw new IOException();
          case 2:
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
              Logger.log("0040 Dense arrays not all same size");
            } else {
              for (int denseIdx = 0; denseIdx < denseIds.size(); denseIdx++) {
                float realLatitude = (float)(0.000000001 * (latOffset + (granularity * denseLats.get(denseIdx))));
                float realLongitude = (float)(0.000000001 * (lonOffset + (granularity * denseLons.get(denseIdx))));
                outList.add(new Vector3f(realLongitude, realLatitude, 0.0f));
              }
            }
            break;
          case 3:
            reader.skipThisField(identifier);
            break;
          case 4:
            reader.skipThisField(identifier);
            break;
          case 5:
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
