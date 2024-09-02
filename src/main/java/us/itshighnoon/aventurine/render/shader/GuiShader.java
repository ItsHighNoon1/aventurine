package us.itshighnoon.aventurine.render.shader;

import org.joml.Matrix4f;

public class GuiShader extends ShaderProgram {
  private int location_mMatrix;
  
  private int location_sampler;
  
  public GuiShader() {
    super("res/gui_v.glsl", "res/gui_f.glsl");
    
    location_mMatrix = getUniformLocation("u_mMatrix");
    
    location_sampler = getUniformLocation("u_sampler");
  }
  
  public void setMMatrix(Matrix4f matrix) {
    uniformMat4(location_mMatrix, matrix);
  }
  
  public void setTexture(int textureId) {
    uniform1i(location_sampler, textureId);
  }
}
