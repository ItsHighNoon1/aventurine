package us.itshighnoon.aventurine.render.shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class LineShader extends ShaderProgram {
  int location_mvpMatrix;
  
  int location_color;
  
  public LineShader() {
    super("res/line_v.glsl", "res/line_f.glsl");
    
    location_mvpMatrix = getUniformLocation("u_mvpMatrix");
    
    location_color = getUniformLocation("u_color");
  }
  
  public void setMvpMatrix(Matrix4f matrix) {
    uniformMat4(location_mvpMatrix, matrix);
  }
  
  public void setColor(Vector3f color) {
    uniform3f(location_color, color.x, color.y, color.z);
  }
}
