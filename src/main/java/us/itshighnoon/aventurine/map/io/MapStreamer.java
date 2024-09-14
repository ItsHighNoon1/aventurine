package us.itshighnoon.aventurine.map.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import us.itshighnoon.aventurine.map.MapChunk;
import us.itshighnoon.aventurine.util.Logger;

public class MapStreamer {
  private Set<MapChunk> chunks;
  private Set<MapChunk> loadedLoading;
  private ConcurrentLinkedQueue<MapChunk> loadRequests;
  private LoadThread loadThread;
  private long latCenter;
  private long lonCenter;
  private long chunkSize;
  private double unitsPerLat;
  private double unitsPerLon;
  private float loadDistance;

  public MapStreamer(String mapFile) {
    this.chunks = new HashSet<MapChunk>();
    this.loadedLoading = new HashSet<MapChunk>();
    this.loadRequests = new ConcurrentLinkedQueue<MapChunk>();
    this.loadThread = new LoadThread();

    Map<String, String> fileData = new HashMap<String, String>();
    Path filePath = Path.of(mapFile);
    try {
      BufferedReader fileReader = new BufferedReader(new FileReader(filePath.toString()));
      String line;
      int lineNumber = 0;
      while ((line = fileReader.readLine()) != null) {
        lineNumber++;
        if (line.isBlank() || line.startsWith("#")) {
          continue;
        }
        String[] parts = line.split("=", 2);
        if (parts.length < 2) {
          Logger.log("0045 Syntax error in map file: " + filePath.toString() + ":" + lineNumber, Logger.Severity.WARN);
        } else {
          fileData.put(parts[0], parts[1]);
        }
      }
      fileReader.close();
    } catch (IOException e) {
      Logger.log("0046 Failed to load map metadata " + filePath.toString(), Logger.Severity.ERROR);
    }

    latCenter = 0;
    if (fileData.containsKey("centerLat")) {
      try {
        latCenter = Long.parseLong(fileData.get("centerLat"));
      } catch (NumberFormatException e) {
        Logger.log("0047 centerLat not a number", Logger.Severity.WARN);
      }
    }
    lonCenter = 0;
    if (fileData.containsKey("centerLon")) {
      try {
        lonCenter = Long.parseLong(fileData.get("centerLon"));
      } catch (NumberFormatException e) {
        Logger.log("0047 centerLon not a number", Logger.Severity.WARN);
      }
    }
    chunkSize = 100000000;
    if (fileData.containsKey("chunkSize")) {
      try {
        chunkSize = Long.parseLong(fileData.get("chunkSize"));
      } catch (NumberFormatException e) {
        Logger.log("0047 chunkSize not a number", Logger.Severity.WARN);
      }
    }
    unitsPerLat = 1.0;
    if (fileData.containsKey("latMul")) {
      try {
        unitsPerLat = Double.parseDouble(fileData.get("latMul"));
      } catch (NumberFormatException e) {
        Logger.log("0047 latMul not a number", Logger.Severity.WARN);
      }
    }
    unitsPerLon = 1.0;
    if (fileData.containsKey("lonMul")) {
      try {
        unitsPerLon = Double.parseDouble(fileData.get("lonMul"));
      } catch (NumberFormatException e) {
        Logger.log("0047 lonMul not a number", Logger.Severity.WARN);
      }
    }
    loadDistance = 5000.0f;
    if (fileData.containsKey("loadDistance")) {
      try {
        loadDistance = Float.parseFloat(fileData.get("loadDistance"));
      } catch (NumberFormatException e) {
        Logger.log("0047 loadDistance not a number", Logger.Severity.WARN);
      }
    }
    loadDistance += Math.sqrt(chunkSize * unitsPerLat  * 0.000000001 * chunkSize * unitsPerLon * 0.000000001 / 2.0f);

    File mapDirectory = new File(filePath.getParent().resolve(Path.of(fileData.get("mapdir"))).toString());
    for (File child : mapDirectory.listFiles()) {
      if (!child.getName().endsWith(".kfk")) {
        continue;
      }
      String[] splitPath = child.getName().split("\\.");
      long lat = Long.parseLong(splitPath[1]);
      long lon = Long.parseLong(splitPath[0]);
      Path nodesPath = Path.of(child.getPath());
      MapChunk newChunk = new MapChunk(nodesPath, (lon - 1) * chunkSize, lat * chunkSize, lon * chunkSize,
          (lat + 1) * chunkSize, latCenter, lonCenter, unitsPerLat, unitsPerLon);
      chunks.add(newChunk);
    }

    this.loadThread.start();
  }

  public Set<MapChunk> getMapChunks() {
    return chunks;
  }

  public int getLoadingStats(int[] loaded, int[] inProgress) {
    int totalLoaded = 0;
    for (MapChunk chunk : loadedLoading) {
      if (chunk.isLoaded()) {
        totalLoaded++;
      }
    }
    if (loaded != null) {
      loaded[0] = totalLoaded;
    }
    if (inProgress != null) {
      inProgress[0] = loadedLoading.size() - totalLoaded;
    }
    return totalLoaded;
  }

  public void loadChunksAround(float x, float z, boolean shouldUnload) {
    for (MapChunk chunk : chunks) {
      if (chunk.distance2(x, z) < loadDistance * loadDistance) {
        if (!chunk.isLoaded() && !loadedLoading.contains(chunk)) {
          loadRequests.add(chunk);
          loadedLoading.add(chunk);
        }
      } else if (chunk.distance2(x, z) > loadDistance * loadDistance + chunkSize * 2.0f) {
        if (shouldUnload && chunk.isLoaded()) {
          chunk.unload();
          loadedLoading.remove(chunk);
        }
      }
    }
  }

  public void cleanup() {
    loadThread.shouldStop = true;
    for (MapChunk chunk : chunks) {
      if (chunk.isLoaded()) {
        chunk.unload();
      }
    }
    try {
      loadThread.join();
    } catch (InterruptedException e) {
      Logger.log("0049 Interrupted while waiting for load thread termination", Logger.Severity.WARN);
    }
  }

  private class LoadThread extends Thread {
    private boolean shouldStop;

    public LoadThread() {
      shouldStop = false;
    }

    @Override
    public void run() {
      while (true) {
        if (shouldStop) {
          return;
        }
        MapChunk toLoad = loadRequests.poll();
        if (toLoad == null) {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            Logger.log("0048 Load thread sleep interrupted", Logger.Severity.WARN);
          }
          continue;
        }
        toLoad.load();
      }
    }
  }
}
