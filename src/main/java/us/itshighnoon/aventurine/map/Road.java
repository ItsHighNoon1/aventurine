package us.itshighnoon.aventurine.map;

import org.joml.Vector3f;

public class Road {
  private Vector3f[] path;
  private String name;
  
  public Road(Vector3f[] path, String name) {
    this.path = path;
    this.name = name;
  }
  
  public Vector3f[] getPath() {
    return path;
  }
  
  public String getName() {
    return name;
  }
  
  public void cleanup() {
    
  }
}
