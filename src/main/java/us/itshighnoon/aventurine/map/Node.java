package us.itshighnoon.aventurine.map;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joml.Vector3f;

import us.itshighnoon.aventurine.map.io.ProtobufReader;
import us.itshighnoon.aventurine.map.io.ProtobufWriter;
import us.itshighnoon.aventurine.util.Logger;

public class Node {
  // Fields from OSM
  private long id;
  private long sourceLat;
  private long sourceLon;
  private Map<String, String> attributes;

  // Fields for rendering
  private Vector3f position;

  public Node(long id, long lat, long lon, Map<String, String> attributes, long latCenter, long lonCenter, double latScale, double lonScale) {
    this.id = id;
    this.sourceLat = lat;
    this.sourceLon = lon;
    this.attributes = attributes;
    this.position = new Vector3f((float) ((lon - lonCenter) * 0.000000001 * lonScale), 0.0f, -(float) ((lat - latCenter) * 0.000000001 * latScale));
  }
  
  public Node(long id, long lat, long lon, Map<String, String> attributes) {
    this(id, lat, lon, attributes, 0, 0, 0.0, 0.0);
  }
  
  public static Node readFromKafka(ProtobufReader reader, Node lastNode, long latCenter, long lonCenter, double latScale, double lonScale) {
    try {
      long id = reader.readVarintSigned();
      long sourceLat = reader.readVarintSigned();
      long sourceLon = reader.readVarintSigned();
      if (lastNode != null) {
        id += lastNode.getId();
        sourceLat += lastNode.getLat();
        sourceLon += lastNode.getLon();
      }
      long attribsEnd = reader.getCursor() + reader.readVarint();
      Map<String, String> attributes = new HashMap<String, String>();
      while (reader.getCursor() < attribsEnd) {
        String key = reader.readString();
        String value = reader.readString();
        attributes.put(key, value);
      }
      Node node = new Node(id, sourceLat, sourceLon, attributes, latCenter, lonCenter, latScale, lonScale);
      return node;
    } catch (IOException e) {
      Logger.log("0055 IO exception while reading node", Logger.Severity.ERROR);
    }
    return null;
  }
  
  public static Node readFromKafka(ProtobufReader reader, Node lastNode) {
    return readFromKafka(reader, lastNode, 0, 0, 0.0, 0.0);
  }
  
  public void writeToKafka(ProtobufWriter writer, Node lastNode) {
    try {
      long lastId = 0;
      long lastLat = 0;
      long lastLon = 0;
      if (lastNode != null) {
        lastId = lastNode.getId();
        lastLat = lastNode.getLat();
        lastLon = lastNode.getLon();
      }
      writer.writeVarintSigned(id - lastId);
      writer.writeVarintSigned(sourceLat - lastLat);
      writer.writeVarintSigned(sourceLon - lastLon);
      if (attributes != null) {
        long attribsLength = 0;
        for (Entry<String, String> attrib : attributes.entrySet()) {
          attribsLength += ProtobufWriter.sizeAsBytes(attrib.getKey().getBytes());
          attribsLength += ProtobufWriter.sizeAsBytes(attrib.getValue().getBytes());
        }
        writer.writeVarint(attribsLength);
        for (Entry<String, String> keyValue : attributes.entrySet()) {
          writer.writeString(keyValue.getKey());
          writer.writeString(keyValue.getValue());
        }
      } else {
        writer.writeVarint(0);
      }
    } catch (IOException e) {
      Logger.log("0056 IO exception while writing node", Logger.Severity.ERROR);
    }
  }
  
  public long getId() {
    return id;
  }
  
  public long getLat() {
    return sourceLat;
  }
  
  public long getLon() {
    return sourceLon;
  }
  
  public Vector3f getPosition() {
    return position;
  }
  
  public Map<String, String> getAttributes() {
    return attributes;
  }
  
  @Override
  public int hashCode() {
    return (int) id;
  }
}
