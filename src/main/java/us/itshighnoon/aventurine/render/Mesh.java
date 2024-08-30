package us.itshighnoon.aventurine.render;

import java.nio.IntBuffer;

import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.util.Logger;

public class Mesh implements Comparable<Mesh> {
  private int vao;
  private int vbo;
  private int ebo;
  private int vCount;
  private int materialIdx;
  
  public Mesh(float[] vertices) {
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
  
  public Mesh(AIMesh aiMesh) {
    float[] vertexData = new float[aiMesh.mNumVertices() * 8];
    boolean hasTexture = aiMesh.mNumUVComponents().get() >= 2;
    AIVector3D.Buffer aiPositions = aiMesh.mVertices();
    AIVector3D.Buffer aiTexCoords = aiMesh.mTextureCoords(0);
    AIVector3D.Buffer aiNormals = aiMesh.mNormals();
    for (int vertexIdx = 0; vertexIdx < aiMesh.mNumVertices(); vertexIdx++) {
      AIVector3D position = aiPositions.get(vertexIdx);
      vertexData[vertexIdx * 8 + 0] = position.x();
      vertexData[vertexIdx * 8 + 1] = position.y();
      vertexData[vertexIdx * 8 + 2] = position.z();
      
      if (hasTexture) {
        AIVector3D texCoords = aiTexCoords.get(vertexIdx);
        vertexData[vertexIdx * 8 + 3] = texCoords.x();
        vertexData[vertexIdx * 8 + 4] = texCoords.y();
      } else {
        vertexData[vertexIdx * 8 + 3] = 0.0f;
        vertexData[vertexIdx * 8 + 4] = 0.0f;
      }
      
      AIVector3D normal = aiNormals.get(vertexIdx);
      vertexData[vertexIdx * 8 + 5] = normal.x();
      vertexData[vertexIdx * 8 + 6] = normal.y();
      vertexData[vertexIdx * 8 + 7] = normal.z();
    }
    
    int[] indexData = new int[aiMesh.mNumFaces() * 3];
    AIFace.Buffer aiFaces = aiMesh.mFaces();
    for (int faceIdx = 0; faceIdx < aiMesh.mNumFaces(); faceIdx++) {
      IntBuffer faceIndices = aiFaces.get(faceIdx).mIndices();
      indexData[faceIdx * 3 + 0] = faceIndices.get(0);
      indexData[faceIdx * 3 + 1] = faceIndices.get(1);
      indexData[faceIdx * 3 + 2] = faceIndices.get(2);
    }
    
    this.vCount = indexData.length;
    this.materialIdx = aiMesh.mMaterialIndex();
    this.vbo = GL15.glGenBuffers();
    this.ebo = GL15.glGenBuffers();
    this.vao = GL30.glGenVertexArrays();
    GL30.glBindVertexArray(this.vao);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 32, 0); // 32 is sizeof(float) * 8
    GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 32, 12);
    GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 32, 20);
    GL20.glEnableVertexAttribArray(0);
    GL20.glEnableVertexAttribArray(1);
    GL20.glEnableVertexAttribArray(2);
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ebo);
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexData, GL15.GL_STATIC_DRAW);
    GL30.glBindVertexArray(0);
  }
  
  public int getVao() {
    return this.vao;
  }
  
  public int getVertexCount() {
    return this.vCount;
  }
  
  public int getMaterialIdx() {
    return this.materialIdx;
  }
  
  public void cleanup() {
    GL30.glDeleteVertexArrays(this.vao);
    GL15.glDeleteBuffers(this.vbo);
    GL15.glDeleteBuffers(this.ebo);
  }
  
  @Override
  public int hashCode() {
    return vao;
  }

  @Override
  public int compareTo(Mesh other) {
    return this.materialIdx - other.materialIdx;
  }
}
