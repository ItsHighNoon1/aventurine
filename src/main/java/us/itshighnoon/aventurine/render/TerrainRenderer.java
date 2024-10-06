package us.itshighnoon.aventurine.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL40;

import us.itshighnoon.aventurine.map.Terrain;
import us.itshighnoon.aventurine.render.shader.TerrainShader;
import us.itshighnoon.aventurine.util.ResourceManager;

public class TerrainRenderer {
  private TerrainShader terrainShader;
  
  public TerrainRenderer() {
    this.terrainShader = new TerrainShader();
  }
  
  public void prepare() {
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    //GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    GL40.glPatchParameteri(GL40.GL_PATCH_VERTICES, 4);
  }
  
  public void render(Camera camera, Terrain terrain) {
    Matrix4f mvpMatrix = new Matrix4f();
    mvpMatrix.setTranslation(terrain.getWest(), 0.0f, terrain.getNorth());
    mvpMatrix.rotate((float) -Math.toRadians(90.0), 1.0f, 0.0f, 0.0f);
    mvpMatrix.scale(terrain.getWidth(), terrain.getDepth(), 1.0f);
    camera.getCameraMatrix().mul(mvpMatrix, mvpMatrix);
    terrainShader.bind();
    terrainShader.setHeightmapSampler(0);
    terrainShader.setMvpMatrix(mvpMatrix);
    GL13.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getHeightmapTexture());
    GL11.glDrawArrays(GL40.GL_PATCHES, 0, ResourceManager.getQuad().getVertexCount());
  }
  
  public void cleanup() {
    terrainShader.cleanup();
  }
}
