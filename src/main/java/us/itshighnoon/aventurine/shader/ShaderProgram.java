package us.itshighnoon.aventurine.shader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import us.itshighnoon.aventurine.util.Logger;

public class ShaderProgram {
  private int shaderProgram;
  
  protected ShaderProgram(String vertexShaderPath, String fragmentShaderPath) {
    this.shaderProgram = GL20.glCreateProgram();
    int vertexShader = createShader(vertexShaderPath, GL20.GL_VERTEX_SHADER);
    int fragmentShader = createShader(fragmentShaderPath, GL20.GL_FRAGMENT_SHADER);
    GL20.glAttachShader(this.shaderProgram, vertexShader);
    GL20.glAttachShader(this.shaderProgram, fragmentShader);
    GL20.glLinkProgram(this.shaderProgram);
    int[] intPtr = new int[1];
    GL20.glGetProgramiv(this.shaderProgram, GL20.GL_LINK_STATUS, intPtr);
    if (intPtr[0] != GL11.GL_TRUE) {
      String err = GL20.glGetProgramInfoLog(this.shaderProgram);
      Logger.log("0016 Shader program link error:\n" + err, Logger.Severity.ERROR);
    }
    GL20.glDeleteShader(vertexShader);
    GL20.glDeleteShader(fragmentShader);
  }
  
  private int createShader(String filePath, int type) {
    int shader = GL20.glCreateShader(type);
    String shaderSource = null;
    try {
      shaderSource = Files.readString(Path.of(filePath));
    } catch (IOException e) {
      Logger.log("0015 Unable to open shader source file " + filePath, Logger.Severity.ERROR);
      GL20.glDeleteShader(shader);
      return -1;
    }
    GL20.glShaderSource(shader, shaderSource);
    GL20.glCompileShader(shader);
    int[] intPtr = new int[1];
    GL20.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, intPtr);
    if (intPtr[0] != GL11.GL_TRUE) {
      String err = GL20.glGetShaderInfoLog(shader);
      Logger.log("0014 Shader compile error:\n" + err, Logger.Severity.ERROR);
      GL20.glDeleteShader(shader);
      return -1;
    }
    return shader;
  }
  
  public void bind() {
    GL20.glUseProgram(shaderProgram);
  }
  
  public void cleanup() {
    GL20.glDeleteProgram(shaderProgram);
  }
  
  protected int getUniformLocation(String uniformName) {
    return GL20.glGetUniformLocation(this.shaderProgram, uniformName);
  }
  
  protected void uniform1i(int location, int value) {
    GL20.glUniform1i(location, value);
  }
  
  protected void uniformMat4(int location, Matrix4f mat) {
    float[] matData = new float[16];
    mat.get(matData);
    GL20.glUniformMatrix4fv(location, false, matData);
  }
}
