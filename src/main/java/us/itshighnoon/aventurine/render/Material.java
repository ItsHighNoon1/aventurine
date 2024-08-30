package us.itshighnoon.aventurine.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import us.itshighnoon.aventurine.util.Logger;

public class Material {
  private int texture;
  
  public Material(String texturePath) {
    this.texture = 0;
    loadTexture(texturePath);
  }
  
  public Material(AIMaterial aiMaterial, String textureDir) {
    AIString path = AIString.calloc();
    Assimp.aiGetMaterialTexture(aiMaterial, Assimp.aiTextureType_DIFFUSE, 0, path, (IntBuffer)null, null, null, null, null, null);
    this.texture = 0;
    if (path.length() > 0) {
      loadTexture(textureDir + "/" + path.dataString());
    }
  }
  
  private void loadTexture(String texturePath) {
    // TODO have a texture cache... materials sharing textures more common than expected
    int[] textureWidth = { 0 };
    int[] textureHeight = { 0 };
    int[] textureChannels = { 0 };
    STBImage.stbi_set_flip_vertically_on_load(true);
    ByteBuffer textureData = STBImage.stbi_load(texturePath, textureWidth, textureHeight, textureChannels, 4);
    if (textureData == null) {
      Logger.log("0023 Texture " + texturePath + " does not exist", Logger.Severity.WARN);
      return;
    }
    textureData.flip();
    this.texture = GL11.glGenTextures();
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureWidth[0], textureHeight[0],
        0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureData);
    STBImage.stbi_image_free(textureData);
  }
  
  public int getTexture() {
    return texture;
  }
  
  public void cleanup() {
    GL11.glDeleteTextures(texture);
  }
}
