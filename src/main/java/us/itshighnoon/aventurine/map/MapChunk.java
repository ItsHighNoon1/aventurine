package us.itshighnoon.aventurine.map;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Vector2f;
import org.joml.Vector3f;

import us.itshighnoon.aventurine.map.io.OsmPbfReader;
import us.itshighnoon.aventurine.util.Logger;

public class MapChunk {
  private Path dataFile;
  private List<Vector3f> nodes;
  private float northEdge;
  private float southEdge;
  private float eastEdge;
  private float westEdge;
  private boolean loaded;
  
  public MapChunk(Path dataFile, float west, float south, float east, float north) {
    this.dataFile = dataFile;
    this.nodes = new ArrayList<Vector3f>();
    this.northEdge = north;
    this.southEdge = south;
    this.eastEdge = east;
    this.westEdge = west;
    this.loaded = false;
  }
  
  public List<Vector3f> getNodes() {
    return nodes;
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
    OsmPbfReader.readMap(dataFile, nodes, latMin, latMax, lonMin, lonMax, latScale, lonScale, latCenter, lonCenter);
    Random rand = new Random();
    List<Vector3f> reducedNodes = new ArrayList<Vector3f>();
    for (Vector3f node : nodes) {
      if (rand.nextFloat() < 0.0003f) {
        reducedNodes.add(node);
      }
    }
    nodes = reducedNodes;
    loaded = true;
  }
  
  public void unload() {
    if (loaded) {
      loaded = false;
    } else {
      Logger.log("0049 Double-unload on chunk", Logger.Severity.WARN);
    }
    nodes.clear();
  }
  
  public float distance2(float x, float z) {
    float chunkCenterX = (westEdge + eastEdge) / 2.0f;
    float chunkCenterZ = (northEdge + southEdge) / 2.0f;
    return Vector2f.distanceSquared(chunkCenterX, chunkCenterZ, x, z);
  }
}
