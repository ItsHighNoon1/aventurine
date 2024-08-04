package us.itshighnoon.aventurine.render;

import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_FixInfacingNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_JoinIdenticalVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;

import org.lwjgl.assimp.AIScene;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.util.Logger;

public class RawModel {
  private int vao;
  private int vbo;
  private int vCount;
  
  public RawModel(float[] vertices) {
    this.vbo = GL15.glGenBuffers();
    this.vao = GL30.glGenVertexArrays();
    this.vCount = vertices.length / 3;
    if (vertices.length % 3 != 0) {
      Logger.log("0013 Uneven number of vertices specified for model", Logger.Severity.WARN);
    }
    GL30.glBindVertexArray(this.vao);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12, 0); // 12 is sizeof(float) * 3
    GL20.glEnableVertexAttribArray(0);
    GL30.glBindVertexArray(0);
  }
  
  public RawModel(String modelPath) {
    AIScene aiScene = aiImportFile(modelPath,
        aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_FixInfacingNormals);
    if (aiScene == null) {
      Logger.log("0017 Assimp failed to load file " + modelPath, Logger.Severity.ERROR);
      this.vao = 0;
      this.vbo = 0;
      this.vCount = 0;
      return;
    }
  }
  
  public void bind() {
    GL30.glBindVertexArray(this.vao);
  }
  
  public int getVertexCount() {
    return this.vCount;
  }
  
  public void cleanup() {
    GL30.glDeleteVertexArrays(this.vao);
    GL15.glDeleteBuffers(this.vbo);
  }
  
  @Override
  public int hashCode() {
    return vao;
  }
}
