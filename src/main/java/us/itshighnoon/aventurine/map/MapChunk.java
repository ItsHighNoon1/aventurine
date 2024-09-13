package us.itshighnoon.aventurine.map;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joml.Vector2f;
import org.joml.Vector3f;

import us.itshighnoon.aventurine.map.io.OsmPbfReader;
import us.itshighnoon.aventurine.render.mem.Mesh;
import us.itshighnoon.aventurine.util.Logger;

public class MapChunk {
  private Path dataFile;
  private List<Mesh> roads;
  private float northEdge;
  private float southEdge;
  private float eastEdge;
  private float westEdge;
  private boolean loaded;
  
  public MapChunk(Path dataFile, float west, float south, float east, float north) {
    this.dataFile = dataFile;
    this.roads = new ArrayList<Mesh>();
    this.northEdge = north;
    this.southEdge = south;
    this.eastEdge = east;
    this.westEdge = west;
    this.loaded = false;
  }
  
  public List<Mesh> getRoads() {
    return roads;
  }
  
  public boolean isLoaded() {
    return loaded;
  }
  
  public void load(double latCenter, double lonCenter, double latScale, double lonScale) {
    double latMin = -southEdge / latScale + latCenter;
    double latMax = -northEdge / latScale + latCenter;
    double lonMin = westEdge / lonScale + lonCenter;
    double lonMax = eastEdge / lonScale + lonCenter;
    Logger.log(String.format("0050 Loading chunk at %.4fN %.4fE", (latMin + latMax) / 2.0f, (lonMin + lonMax) / 2.0f));
    
    Map<Long, Vector3f> nodes = new HashMap<Long, Vector3f>();
    Map<Long, Long[]> ways = new HashMap<Long, Long[]>();
    OsmPbfReader.readMap(dataFile, nodes, ways, latMin, latMax, lonMin, lonMax, latScale, lonScale, latCenter, lonCenter);
    
    for (Entry<Long, Long[]> way : ways.entrySet()) {
      List<Vector3f> roadNodes = new ArrayList<Vector3f>();
      for (Long nodeId : way.getValue()) {
        Vector3f nodePos = nodes.get(nodeId);
        if (nodePos == null) {
          continue;
        }
        if (nodePos.x >= westEdge && nodePos.x < eastEdge && nodePos.z >= northEdge && nodePos.z < southEdge) {
          roadNodes.add(nodePos);
        }
      }
      if (!roadNodes.isEmpty()) {
        Vector3f[] roadArray = new Vector3f[roadNodes.size()];
        for (int roadNodeIdx = 0; roadNodeIdx < roadNodes.size(); roadNodeIdx++) {
          roadArray[roadNodeIdx] = roadNodes.get(roadNodeIdx);
        }
        Mesh road = Mesh.awaitLineMesh(roadArray);
        roads.add(road);
      }
    }
    
    loaded = true;
  }
  
  public void unload() {
    if (loaded) {
      loaded = false;
    } else {
      Logger.log("0049 Double-unload on chunk", Logger.Severity.WARN);
    }
    for (Mesh road : roads) {
      road.cleanup();
    }
    roads.clear();
  }
  
  public float distance2(float x, float z) {
    float chunkCenterX = (westEdge + eastEdge) / 2.0f;
    float chunkCenterZ = (northEdge + southEdge) / 2.0f;
    return Vector2f.distanceSquared(chunkCenterX, chunkCenterZ, x, z);
  }
}
