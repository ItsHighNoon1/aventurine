package us.itshighnoon.aventurine.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.joml.FrustumIntersection;
import org.joml.Intersectionf;
import org.joml.Vector3f;

import us.itshighnoon.aventurine.map.io.ProtobufReader;
import us.itshighnoon.aventurine.map.io.ProtobufWriter;
import us.itshighnoon.aventurine.render.Camera;
import us.itshighnoon.aventurine.render.mem.Mesh;
import us.itshighnoon.aventurine.util.Logger;

public class Way {
  // Fields from OSM
  private long id;
  private Map<String, String> attributes;
  private Node[] nodes;
  private long[] nodesRaw;
  
  // Fields for rendering
  private Mesh debugMesh;
  float minX;
  float minY;
  float minZ;
  float maxX;
  float maxY;
  float maxZ;
  
  public Way(long id, Map<String, String> attributes, Node[] nodes, long[] nodeIds) {
    this.id = id;
    this.attributes = attributes;
    this.nodes = nodes;
    this.nodesRaw = nodeIds;
    this.debugMesh = null;
    if (nodes != null) {
      boolean setPos = false;
      for (Node node : nodes) {
        if (node != null) {
          Vector3f nodePos = node.getPosition();
          if (!setPos) {
            this.minX = nodePos.x;
            this.minY = nodePos.y;
            this.minZ = nodePos.z;
            this.maxX = nodePos.x;
            this.maxY = nodePos.y;
            this.maxZ = nodePos.z;
            setPos = true;
          } else {
            if (nodePos.x < minX) {
              this.minX = nodePos.x;
            }
            if (nodePos.y < minY) {
              this.minY = nodePos.y;
            }
            if (nodePos.z < minZ) {
              this.minZ = nodePos.z;
            }
            if (nodePos.x > maxX) {
              this.maxX = nodePos.x;
            }
            if (nodePos.y > maxY) {
              this.maxY = nodePos.y;
            }
            if (nodePos.z > maxZ) {
              this.maxZ = nodePos.z;
            }
          }
        }
      }
    } else {
      this.minX = 0.0f;
      this.minY = 0.0f;
      this.minZ = 0.0f;
      this.maxX = 0.0f;
      this.maxY = 0.0f;
      this.maxZ = 0.0f;
    }
  }
  
  public Way(long id, Map<String, String> attributes, Node[] nodes) {
    this(id, attributes, nodes, null);
  }
  
  public Way(long id, Map<String, String> attributes, long[] nodes) {
    this(id, attributes, null, nodes);
  }
  
  public static Way readFromKafka(ProtobufReader reader, Map<Long, Node> knownNodes) {
    try {
      long id = reader.readVarint();
      long attribsEnd = reader.getCursor() + reader.readVarint();
      Map<String, String> attributes = new HashMap<String, String>();
      while (reader.getCursor() < attribsEnd) {
        String key = reader.readString();
        String value = reader.readString();
        attributes.put(key, value);
      }
      int numNodes = (int) reader.readVarint();
      Node[] nodes = new Node[numNodes];
      long[] nodeIds = new long[numNodes];
      long lastNodeId = 0;
      for (int nodeIdx = 0; nodeIdx < nodes.length; nodeIdx++) {
        long nodeId = reader.readVarintSigned() + lastNodeId;
        lastNodeId = nodeId;
        Node node = knownNodes.get(nodeId);
        nodes[nodeIdx] = node;
        nodeIds[nodeIdx] = nodeId;
      }
      Way way = new Way(id, attributes, nodes);
      return way;
    } catch (IOException e) {
      Logger.log("0057 IO exception while reading way", Logger.Severity.ERROR);
    }
    return null;
  }
  
  public static Way readRawFromKafka(ProtobufReader reader) {
    try {
      long id = reader.readVarint();
      long attribsEnd = reader.getCursor() + reader.readVarint();
      Map<String, String> attributes = new HashMap<String, String>();
      while (reader.getCursor() < attribsEnd) {
        String key = reader.readString();
        String value = reader.readString();
        attributes.put(key, value);
      }
      int numNodes = (int) reader.readVarint();
      long[] nodes = new long[numNodes];
      long lastNodeId = 0;
      for (int nodeIdx = 0; nodeIdx < nodes.length; nodeIdx++) {
        long nodeId = reader.readVarintSigned() + lastNodeId;
        lastNodeId = nodeId;
        nodes[nodeIdx] = nodeId;
      }
      Way way = new Way(id, attributes, nodes);
      return way;
    } catch (IOException e) {
      Logger.log("0057 IO exception while reading way", Logger.Severity.ERROR);
    }
    return null;
  }
  
  public void writeToKafka(ProtobufWriter writer) {
    try {
      writer.writeVarint(id);
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
      long lastNode = 0;
      if (nodes != null) {
        writer.writeVarint(nodes.length);
        for (Node node : nodes) {
          writer.writeVarintSigned(node.getId() - lastNode);
          lastNode = node.getId();
        }
      } else {
        writer.writeVarint(nodesRaw.length);
        for (long nodeId : nodesRaw) {
          writer.writeVarintSigned(nodeId - lastNode);
          lastNode = nodeId;
        }
      }
    } catch (IOException e) {
      Logger.log("0058 IO exception while writing way", Logger.Severity.ERROR);
    }
  }
  
  public void buildMesh() {
    if (nodes == null) {
      Logger.log("0061 Cannot build way mesh from raw nodes", Logger.Severity.ERROR);
      return;
    }
    List<Vector3f> verts = new ArrayList<Vector3f>();
    for (int vertIdx = 0; vertIdx < nodes.length; vertIdx++) {
      if (nodes[vertIdx] != null) {
        verts.add(nodes[vertIdx].getPosition());
      }
    }
    Vector3f[] meshData = new Vector3f[verts.size()];
    for (int vertIdx = 0; vertIdx < meshData.length; vertIdx++) {
      meshData[vertIdx] = verts.get(vertIdx);
    }
    this.debugMesh = Mesh.loadLineMesh(meshData);
  }
  
  public void buildMeshAsync() {
    if (nodes == null) {
      Logger.log("0061 Cannot build way mesh from raw nodes", Logger.Severity.ERROR);
      return;
    }
    List<Vector3f> verts = new ArrayList<Vector3f>();
    for (int vertIdx = 0; vertIdx < nodes.length; vertIdx++) {
      if (nodes[vertIdx] != null) {
        verts.add(nodes[vertIdx].getPosition());
      }
    }
    Vector3f[] meshData = new Vector3f[verts.size()];
    for (int vertIdx = 0; vertIdx < meshData.length; vertIdx++) {
      meshData[vertIdx] = verts.get(vertIdx);
    }
    CompletableFuture.runAsync(() -> {
      this.debugMesh = Mesh.awaitLineMesh(meshData);
    });
  }
  
  public long getId() {
    return id;
  }
  
  public Set<Long> getNodeSet() {
    Set<Long> nodeSet = new HashSet<Long>();
    if (nodes != null) {
      for (Node node : nodes) {
        nodeSet.add(node.getId());
      }
    } else {
      for (long node : nodesRaw) {
        nodeSet.add(node);
      }
    }
    return nodeSet;
  }
  
  public Map<String, String> getAttributes() {
    return attributes;
  }
  
  public Mesh getDebugMesh() {
    return debugMesh;
  }
  
  public boolean intersectsAab(float otherMinX, float otherMinY, float otherMinZ, float otherMaxX, float otherMaxY, float otherMaxZ) {
    return Intersectionf.testAabAab(minX, minY, minZ, maxX, maxY, maxZ, otherMinX, otherMinY, otherMinZ, otherMaxX, otherMaxY, otherMaxZ);
  }
  
  public boolean visible(Camera camera) {
    int intersectionTest = camera.canSeeAab(minX, minY, minZ, maxX, maxY, maxZ);
    return intersectionTest == FrustumIntersection.INSIDE || intersectionTest == FrustumIntersection.INTERSECT;
  }
  
  public void cleanup() {
    if (debugMesh != null) {
      debugMesh.cleanup();
    }
  }
  
  @Override
  public int hashCode() {
    return (int) id;
  }
}
