package us.itshighnoon.aventurine.render.shader;

import org.joml.Matrix4f;
import org.joml.Vector2f;

public class GuiShader extends ShaderProgram {
  private int location_mMatrix;
  private int location_texOffset;
  private int location_texSize;
  
  private int location_sampler;
  
  public GuiShader() {
    super("res/gui_v.glsl", "res/gui_f.glsl");
    
    location_mMatrix = getUniformLocation("u_mMatrix");
    location_texOffset = getUniformLocation("u_texOffset");
    location_texSize = getUniformLocation("u_texSize");
    
    location_sampler = getUniformLocation("u_sampler");
  }
  
  public void setMMatrix(Matrix4f matrix) {
    uniformMat4(location_mMatrix, matrix);
  }
  
  public void setTextureParms(int textureSlot, Vector2f texOffset, Vector2f texSize) {
    uniform1i(location_sampler, textureSlot);
    uniform2f(location_texOffset, texOffset.x, texOffset.y);
    uniform2f(location_texSize, texSize.x, texSize.y);
  }
}
