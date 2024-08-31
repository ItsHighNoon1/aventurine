package us.itshighnoon.aventurine.render;

import java.nio.IntBuffer;

import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.Assimp;

import us.itshighnoon.aventurine.util.ResourceManager;

public class Material {
  private int texture;
  
  public Material(String texturePath) {
    this.texture = ResourceManager.loadTexture(texturePath);
  }
  
  public Material(AIMaterial aiMaterial, String textureDir) {
    AIString path = AIString.calloc();
    Assimp.aiGetMaterialTexture(aiMaterial, Assimp.aiTextureType_DIFFUSE, 0, path, (IntBuffer)null, null, null, null, null, null);
    this.texture = 0;
    if (path.length() > 0) {
      this.texture = ResourceManager.loadTexture(textureDir + "/" + path.dataString());
    }
  }
  
  public int getTexture() {
    return texture;
  }
  
  public void cleanup() {
    ResourceManager.unloadTexture(this.texture);
  }
}
