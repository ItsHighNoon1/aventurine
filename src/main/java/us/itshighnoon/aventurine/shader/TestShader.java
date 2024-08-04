package us.itshighnoon.aventurine.shader;

import org.joml.Matrix4f;

public class TestShader extends ShaderProgram {
  private int location_mvpMatrix;
  
  public TestShader() {
    super("res/test_v.glsl", "res/test_f.glsl");
    location_mvpMatrix = getUniformLocation("u_mvpMatrix");
  }
  
  public void setMvpMatrix(Matrix4f matrix) {
    uniformMat4(location_mvpMatrix, matrix);
  }
}
