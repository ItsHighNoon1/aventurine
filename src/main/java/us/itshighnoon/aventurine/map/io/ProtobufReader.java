package us.itshighnoon.aventurine.map.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import us.itshighnoon.aventurine.util.Logger;

public class ProtobufReader {
  private BufferedInputStream file;
  private long fileIdx;
  private long fileLength;
  private byte[] bytes;
  private int byteIdx;

  public ProtobufReader(File file) throws IOException {
    this.file = new BufferedInputStream(new FileInputStream(file));
    this.fileIdx = 0;
    this.fileLength = file.length();
    this.bytes = null;
    this.byteIdx = 0;
  }

  public ProtobufReader(byte[] bytes, int byteIdx) {
    this.file = null;
    this.fileIdx = 0;
    this.fileLength = 0;
    this.bytes = bytes;
    this.byteIdx = byteIdx;
  }

  public ProtobufReader(byte[] bytes) {
    this(bytes, 0);
  }
  
  public boolean finished() throws IOException {
    if (file != null) {
      return fileIdx >= fileLength;
    } else {
      return byteIdx >= bytes.length;
    }
  }
  
  public byte read() throws IOException {
    byte b;
    if (file != null) {
      b = (byte) file.read();
      fileIdx++;
    } else {
      b = bytes[byteIdx++];
    }
    return b;
  }

  public long readVarint() throws IOException {
    long currentInt = 0;
    long multiplier = 1;
    byte b;
    do {
      b = read();
      currentInt += (b & 0x7F) * multiplier;
      multiplier *= 128;
    } while ((b & 0x80) != 0);
    return currentInt;
  }

  public long readVarintSigned() throws IOException {
    long varint = readVarint();
    if ((varint & 0x01) != 0) {
      varint = -varint - 1;
    }
    return varint >> 1;
  }

  public String readString() throws IOException {
    int stringLength = (int) readVarint();
    if (file != null) {
      byte[] stringBytes = new byte[stringLength];
      file.read(stringBytes);
      fileIdx += stringLength;
      return new String(stringBytes);
    } else {
      String toReturn = new String(bytes, byteIdx, stringLength);
      byteIdx += stringLength;
      return toReturn;
    }
  }

  public byte[] readBytes() throws IOException {
    int bytesLength = (int) readVarint();
    byte[] bytes = new byte[bytesLength];
    if (file != null) {
      file.read(bytes);
      fileIdx += bytesLength;
      return bytes;
    } else {
      System.arraycopy(this.bytes, this.byteIdx, bytes, 0, bytesLength);
      byteIdx += bytesLength;
      return bytes;
    }
  }
  
  public void skipArbitrary(long bytesToSkip) throws IOException {
    if (file != null) {
      file.skip(bytesToSkip);
      fileIdx += bytesToSkip;
    } else {
      byteIdx += bytesToSkip;
    }
  }
  
  public long getCursor() throws IOException {
    if (file != null) {
      return fileIdx;
    } else {
      return byteIdx;
    }
  }
  
  public void close() throws IOException {
    if (file != null) {
      file.close();
      file = null;
    }
  }

  public void skipThisField(long identifier) throws IOException {
    // A string especially might be an expensive read compared to skipping it
    int bytesToSkip = 0;
    switch ((int) identifier & 0x07) {
    case 0:
      readVarint();
      break;
    case 1:
      bytesToSkip = 8;
      break;
    case 2:
      long stringLength = readVarint();
      bytesToSkip = (int) stringLength;
      break;
    case 5:
      bytesToSkip = 4;
      break;
    default:
      Logger.log("0032 Protobuf reader encountered type " + (identifier & 0x07), Logger.Severity.ERROR);
    }
    skipArbitrary(bytesToSkip);
  }
  
  public void dumpHex() throws IOException {
    StringBuilder hexBuilder = new StringBuilder();
    StringBuilder asciiBuilder = new StringBuilder();
    StringBuilder totalBuilder = new StringBuilder();
    int bytesToDisplay = 256;
    byte[] hexBytes = new byte[bytesToDisplay];
    if (file != null) {
      file.mark(bytesToDisplay);
      if (file.available() < bytesToDisplay) {
        bytesToDisplay = (int) (file.available());
      }
      file.read(hexBytes, 0, bytesToDisplay);
      file.reset();
    } else {
      if (bytes.length - byteIdx < bytesToDisplay) {
        bytesToDisplay = bytes.length - byteIdx;
      }
      System.arraycopy(bytes, byteIdx, hexBytes, 0, bytesToDisplay);
    }
    
    int byteCount = 0;
    while (byteCount < bytesToDisplay) {
      for (int i = 0; i < 16; i++) {
        if (byteCount < bytesToDisplay) {
          byte b = hexBytes[byteCount++];
          hexBuilder.append(String.format("%02X ", b));
          if (Character.isWhitespace(b)) {
            asciiBuilder.append(' ');
          } else if (Character.isISOControl(b)) {
            asciiBuilder.append('?');
          } else {
            asciiBuilder.append((char) b);
          }
        } else {
          hexBuilder.append("   ");
          asciiBuilder.append(" ");
        }
      }
      totalBuilder.append(hexBuilder);
      totalBuilder.append(asciiBuilder);
      totalBuilder.append('\n');
      hexBuilder.setLength(0);
      asciiBuilder.setLength(0);
    }
    Logger.log("0031 Hex dump:\n" + totalBuilder.toString().trim());
  }
}