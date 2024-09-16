package us.itshighnoon.aventurine.map.io;

import java.io.DataOutput;
import java.io.IOException;

public class ProtobufWriter {
  private DataOutput file;

  public ProtobufWriter(DataOutput file) {
    this.file = file;
  }

  public void writeVarint(long varint) throws IOException {
    long remaining = varint;
    do {
      byte low7 = (byte) (remaining & 0x7F);
      remaining = remaining >>> 7;
      if (remaining > 0) {
        low7 |= 0x80;
      }
      file.writeByte(low7);
    } while (remaining > 0);
  }

  public void writeVarintSigned(long varint) throws IOException {
    if (varint >= 0) {
      writeVarint(varint << 1);
    } else {
      writeVarint((-varint << 1) - 1);
    }
  }
  
  public static int sizeAsVarint(long varint) {
    int bytes = 0;
    long remaining = varint;
    do {
      remaining = remaining >>> 7;
      bytes++;
    } while (remaining > 0);
    return bytes;
  }

  public void writeString(String string) throws IOException {
    byte[] stringBytes = string.getBytes();
    writeBytes(stringBytes);
  }
  
  public void writeBytes(byte[] bytes) throws IOException {
    writeVarint(bytes.length);
    file.write(bytes);
  }
  
  public static int sizeAsBytes(byte[] bytes) {
    int size = sizeAsVarint(bytes.length);
    size += bytes.length;
    return size;
  }
}
