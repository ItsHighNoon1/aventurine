package us.itshighnoon.aventurine.util;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.opengl.ATIMeminfo;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
import org.lwjgl.stb.STBImage;

import us.itshighnoon.aventurine.util.Logger.Severity;

public class ResourceManager {
  private static ResourceManager singleton;
  
  private HashMap<Path, Integer> loadedTextures;
  private HashMap<Integer, Integer> textureRefCount;
  
  public ResourceManager() {
    if (singleton != null) {
      Logger.log("0024 Attempted second ResourceManager initialization", Severity.WARN);
      return;
    }
    singleton = this;
    
    this.loadedTextures = new HashMap<Path, Integer>();
    this.textureRefCount = new HashMap<Integer, Integer>();
  }
  
  public static int getFreeMemoryKB() {
    int[] params = new int[4];
    params[0] = -1;
    GL11.glGetIntegerv(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX, params);
    GL11.glGetIntegerv(ATIMeminfo.GL_TEXTURE_FREE_MEMORY_ATI, params);
    GL11.glGetError(); // One of the two above will return an error
    return params[0];
  }
  
  public static int loadTexture(String pathString) {
    if (singleton == null) {
      Logger.log("0024 Attempted texture load before ResourceManager init", Logger.Severity.ERROR);
      return 0;
    }
    
    Path texturePath = Path.of(pathString).normalize();
    Integer possibleTexture = singleton.loadedTextures.get(texturePath);
    if (possibleTexture != null) {
      Integer possibleReferences = singleton.textureRefCount.get(possibleTexture.intValue());
      if (possibleReferences != null) {
        int newReferences = possibleReferences.intValue() + 1;
        singleton.textureRefCount.put(possibleTexture, newReferences);
        Logger.log("0029 Loading already cached texture " + texturePath.toString());
        return possibleTexture.intValue();
      }
      Logger.log("0028 Reloading previously unloaded texture " + texturePath.toString());
    } else {
      Logger.log("0027 Loading new texture " + texturePath.toString());
    }
    
    int texture;
    int[] textureWidth = { 0 };
    int[] textureHeight = { 0 };
    int[] textureChannels = { 0 };
    STBImage.stbi_set_flip_vertically_on_load(true);
    ByteBuffer textureData = STBImage.stbi_load(texturePath.toString(), textureWidth, textureHeight, textureChannels, 4);
    if (textureData == null) {
      Logger.log("0023 Texture " + texturePath.toString() + " does not exist", Logger.Severity.ERROR);
      return 0;
    }
    textureData.flip();
    texture = GL11.glGenTextures();
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureWidth[0], textureHeight[0],
        0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureData);
    STBImage.stbi_image_free(textureData);
    
    singleton.loadedTextures.put(texturePath, texture);
    singleton.textureRefCount.put(texture, 1);
    return texture;
  }
  
  public static void unloadTexture(int texture) {
    if (singleton == null) {
      Logger.log("0024 Attempted texture unload before ResourceManager init", Logger.Severity.ERROR);
    }
    if (texture == 0) {
      return;
    }
    Integer possibleReferences = singleton.textureRefCount.get(texture);
    if (possibleReferences == null) {
      Logger.log("0025 Attempted texture unload of not-loaded texture");
    }
    int newReferences = possibleReferences.intValue() - 1;
    if (newReferences == 0) {
      singleton.textureRefCount.remove(texture);
      GL11.glDeleteTextures(texture);
      Logger.log("0026 Texture " + texture + " hit refcount 0 and unloaded");
    } else {
      singleton.textureRefCount.put(texture, newReferences);
    }
  }
  
  public void cleanup() {
    for (Entry<Integer, Integer> textureRef : textureRefCount.entrySet()) {
      if (textureRef.getValue().intValue() > 0) {
        GL11.glDeleteTextures(textureRef.getKey().intValue());
      }
    }
  }
}
