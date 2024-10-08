package us.itshighnoon.aventurine.map;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Vector2f;

import us.itshighnoon.aventurine.map.io.ProtobufReader;
import us.itshighnoon.aventurine.render.Camera;
import us.itshighnoon.aventurine.util.Logger;

public class MapChunk {
  private Path kafkaFile;
  private Path tifFile;
  private Terrain terrain;
  private List<Way> ways;
  private float northEdge;
  private float southEdge;
  private float eastEdge;
  private float westEdge;
  private boolean loaded;
  private long latCenter;
  private long lonCenter;
  private double latScale;
  private double lonScale;

  public MapChunk(Path wayFile, Path terrainFile, long west, long south, long east, long north, long latCenter,
      long lonCenter, double latScale, double lonScale) {
    this.kafkaFile = wayFile;
    this.tifFile = terrainFile;
    this.ways = new ArrayList<Way>();
    this.terrain = null;
    this.northEdge = -(float) (0.000000001 * (north - latCenter) * latScale);
    this.southEdge = -(float) (0.000000001 * (south - latCenter) * latScale);
    this.eastEdge = (float) (0.000000001 * (east - lonCenter) * lonScale);
    this.westEdge = (float) (0.000000001 * (west - lonCenter) * lonScale);
    this.loaded = false;
    this.latCenter = latCenter;
    this.lonCenter = lonCenter;
    this.latScale = latScale;
    this.lonScale = lonScale;
  }

  public Set<Way> getVisibleWays(Camera camera) {
    Set<Way> builtSet = new HashSet<Way>();
    for (Way way : ways) {
      if (way.visible(camera)) {
        builtSet.add(way);
      }
    }
    return builtSet;
  }

  public Terrain getTerrain() {
    return terrain;
  }

  public boolean isLoaded() {
    return loaded;
  }

  public void load() {
    Logger.log(String.format("0050 Loading chunk at %.4f, %.4f", eastEdge, northEdge));

    Map<Long, Node> nodes = new HashMap<Long, Node>();

    try {
      ProtobufReader reader = new ProtobufReader(new File(kafkaFile.toString()));
      int numNodes = (int) reader.readVarint();
      Node lastNode = null;
      for (int nodeIdx = 0; nodeIdx < numNodes; nodeIdx++) {
        Node node = Node.readFromKafka(reader, lastNode, latCenter, lonCenter, latScale, lonScale);
        lastNode = node;
        nodes.put(node.getId(), node);
      }
      int numWays = (int) reader.readVarint();
      for (int wayIdx = 0; wayIdx < numWays; wayIdx++) {
        Way way = Way.readFromKafka(reader, nodes);
        ways.add(way);
      }
    } catch (IOException e) {
      Logger.log("0060 Failed to load chunk file " + kafkaFile.toString());
    }

    for (Way way : ways) {
      way.buildMeshAsync();
    }
    terrain = new Terrain(tifFile);
    Logger.log(String.format("0054 Finished loading chunk at %.4f, %.4f", eastEdge, northEdge));
    loaded = true;
  }

  public void unload() {
    if (loaded) {
      loaded = false;
    } else {
      Logger.log("0049 Double-unload on chunk", Logger.Severity.WARN);
    }
    for (Way way : ways) {
      way.cleanup();
    }
    ways.clear();
    terrain.cleanup();
    terrain = null;
  }

  public float distance2(float x, float z) {
    float chunkCenterX = (westEdge + eastEdge) / 2.0f;
    float chunkCenterZ = (northEdge + southEdge) / 2.0f;
    return Vector2f.distanceSquared(chunkCenterX, chunkCenterZ, x, z);
  }
}
