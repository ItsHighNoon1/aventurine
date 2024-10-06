package us.itshighnoon.aventurine.map;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;

import us.itshighnoon.aventurine.util.Logger;
import us.itshighnoon.aventurine.util.ResourceManager;

public class Terrain {
  private float[][] heights;
  private float scale;
  private float north;
  private float west;
  private int heightTexture;

  public Terrain(Path terrainFile) {
    /*
  }
    try {
      
      File imageFile = new File(terrainFile.toString());
      BufferedImage image = Imaging.getBufferedImage(imageFile);
      Raster imageData = image.getData();
      int width = imageData.getWidth();
      int height = imageData.getHeight();
      this.heights = new float[width][height];
      for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
          this.heights[x][height - 1 - y] = imageData.getSampleFloat(x, y, 0);
        }
      }
      */
      CompletableFuture.runAsync(() -> {
        this.heightTexture = ResourceManager.awaitLoadTexture(terrainFile);
      });
      /*
    } catch (ImagingException e) {
      Logger.log("0069 Failed to load terrain file " + terrainFile.toString(), Logger.Severity.ERROR);
    } catch (IOException e) {
      Logger.log("0069 Failed to load terrain file " + terrainFile.toString(), Logger.Severity.ERROR);
    } */
  }
  
  public float getWest() {
    return west;
  }
  
  public float getNorth() {
    return north;
  }
  
  public float getWidth() {
    return 10000.0f;
  }
  
  public float getDepth() {
    return 10000.0f;
  }
  
  public int getHeightmapTexture() {
    return heightTexture;
  }

  public float getHeight(float x, float z) {
    float floatGridX = (x - west) / scale;
    float floatGridZ = (z - north) / scale;
    int gridX = (int) ((x - west) / scale);
    int gridZ = (int) ((z - north) / scale);
    if (gridX < 0 || gridX >= heights.length - 1 || gridZ < 0 || gridZ >= heights[0].length - 1) {
      return 0.0f;
    }
    float gridOffsetX = floatGridX % 1.0f;
    float gridOffsetZ = floatGridZ % 1.0f;
    float height = 0.0f;
    if (x + z < 1.0f) {
      height = interpolate(gridOffsetX, gridOffsetZ, heights[gridX][gridZ], heights[gridX][gridZ + 1],
          heights[gridX + 1][gridZ]);
    } else {
      height = interpolate(1.0f - gridOffsetX, 1.0f - gridOffsetZ, heights[gridX + 1][gridZ + 1],
          heights[gridX + 1][gridZ], heights[gridX][gridZ + 1]);
    }
    return height;
  }

  private float interpolate(float x, float y, float v1, float v2, float v3) {
    if (x < 0.0f || y < 0.0f || x + y > 1.0f) {
      return 0.0f;
    }
    return v1 * (1 - x - y) + v2 * y + v3 * x;
  }
  
  private int loadFloatTexture(Path texturePath) {
    int texture;
    int[] textureWidth = { 0 };
    int[] textureHeight = { 0 };
    int[] textureChannels = { 0 };
    STBImage.stbi_set_flip_vertically_on_load(true);
    ByteBuffer textureData = STBImage.stbi_load(texturePath.toString(), textureWidth, textureHeight, textureChannels, 1);
    if (textureData == null) {
      Logger.log("A " + GL11.glGetError());
      return 0;
    }
    textureData.flip();
    texture = GL11.glGenTextures();
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R32F, textureWidth[0], textureHeight[0],
        0, GL11.GL_RED, GL11.GL_FLOAT, textureData);
    Logger.log("A " + GL11.glGetError());
    STBImage.stbi_image_free(textureData);
    
    return texture;
  }
  
  public void cleanup() {
    ResourceManager.unloadTexture(heightTexture);
  }
}
