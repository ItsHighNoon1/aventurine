package us.itshighnoon.aventurine.render.shader;

import org.joml.Matrix4f;

public class TerrainShader extends ShaderProgram {
  private int location_mvpMatrix;
  
  private int location_heightmapSampler;
  
  public TerrainShader() {
    super("res/terrain_v.glsl", "res/terrain_tc.glsl", "res/terrain_te.glsl", "res/terrain_f.glsl");
    
    location_mvpMatrix = getUniformLocation("u_mvpMatrix");
    location_heightmapSampler = getUniformLocation("u_heightmapSampler");
  }
  
  public void setMvpMatrix(Matrix4f matrix) {
    uniformMat4(location_mvpMatrix, matrix);
  }
  
  public void setHeightmapSampler(int textureId) {
    uniform1i(location_heightmapSampler, textureId);
  }
}
