package us.itshighnoon.aventurine.map;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joml.Vector2f;
import org.joml.Vector3f;

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
  private long latCenter;
  private long lonCenter;
  private double latScale;
  private double lonScale;

  public MapChunk(Path dataFile, long west, long south, long east, long north, long latCenter, long lonCenter,
      double latScale, double lonScale) {
    this.dataFile = dataFile;
    this.roads = new ArrayList<Mesh>();
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

  public List<Mesh> getRoads() {
    return roads;
  }

  public boolean isLoaded() {
    return loaded;
  }

  public void load() {
    Logger.log(String.format("0050 Loading chunk at %.4f, %.4f", eastEdge, northEdge));

    Map<Long, Vector3f> nodes = new HashMap<Long, Vector3f>();
    Map<Long, Long[]> ways = new HashMap<Long, Long[]>();

    try {
      DataInputStream inputStream = new DataInputStream(
          new BufferedInputStream(new FileInputStream(dataFile.toString())));
      long nodesLen = inputStream.readLong();
      for (int nodeIdx = 0; nodeIdx < nodesLen; nodeIdx++) {
        long id = inputStream.readLong();
        long lat = inputStream.readLong() - latCenter;
        long lon = inputStream.readLong() - lonCenter;

        nodes.put(id, new Vector3f((float) (0.000000001 * lon * lonScale), 0.0f, -(float) (0.000000001 * lat * latScale)));
      }

      long waysLen = inputStream.readLong();
      for (int wayIdx = 0; wayIdx < waysLen; wayIdx++) {
        long id = inputStream.readLong();
        int numberNodes = (int) inputStream.readLong();
        Long[] wayNodes = new Long[numberNodes];
        for (int nodeIdx = 0; nodeIdx < wayNodes.length; nodeIdx++) {
          wayNodes[nodeIdx] = inputStream.readLong();
        }
        ways.put(id, wayNodes);
      }
      inputStream.close();
    } catch (IOException e) {
      Logger.log("0053 Failed to load map " + dataFile.toString(), Logger.Severity.ERROR);
    }

    List<Vector3f[]> roadArrays = new ArrayList<Vector3f[]>();
    for (Entry<Long, Long[]> way : ways.entrySet()) {
      List<Vector3f> roadNodes = new ArrayList<Vector3f>();
      for (Long nodeId : way.getValue()) {
        Vector3f nodePos = nodes.get(nodeId);
        if (nodePos == null) {
          continue;
        }
        roadNodes.add(nodePos);
      }
      if (!roadNodes.isEmpty()) {
        Vector3f[] roadArray = new Vector3f[roadNodes.size()];
        for (int roadNodeIdx = 0; roadNodeIdx < roadNodes.size(); roadNodeIdx++) {
          roadArray[roadNodeIdx] = roadNodes.get(roadNodeIdx);
        }
        roadArrays.add(roadArray);
      }
    }
    roads.addAll(Mesh.awaitLineMeshMulti(roadArrays));

    Logger.log(String.format("0054 Finished loading chunk at %.4f, %.4f", eastEdge, northEdge));
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
