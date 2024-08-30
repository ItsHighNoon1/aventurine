package us.itshighnoon.aventurine.render;

import java.nio.IntBuffer;

import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.util.Logger;

public class RawModel {
  private int vao;
  private int vbo;
  private int ebo;
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
  
  private RawModel(AIMesh aiMesh) {
    float[] vertexData = new float[aiMesh.mNumVertices() * 8];
    AIVector3D.Buffer aiPositions = aiMesh.mVertices();
    AIVector3D.Buffer aiTexCoords = aiMesh.mTextureCoords(0);
    AIVector3D.Buffer aiNormals = aiMesh.mNormals();
    for (int vertexIdx = 0; vertexIdx < aiMesh.mNumVertices(); vertexIdx++) {
      AIVector3D position = aiPositions.get(vertexIdx);
      AIVector3D texCoords = aiTexCoords.get(vertexIdx);
      AIVector3D normal = aiNormals.get(vertexIdx);
      vertexData[vertexIdx * 8 + 0] = position.x();
      vertexData[vertexIdx * 8 + 1] = position.y();
      vertexData[vertexIdx * 8 + 2] = position.z();
      vertexData[vertexIdx * 8 + 3] = texCoords.x();
      vertexData[vertexIdx * 8 + 4] = texCoords.y();
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
  
  public static RawModel[] loadAll(String modelPath) {
    AIScene aiScene = Assimp.aiImportFile(modelPath,
        Assimp.aiProcess_JoinIdenticalVertices | Assimp.aiProcess_Triangulate | Assimp.aiProcess_FixInfacingNormals);
    if (aiScene == null) {
      Logger.log("0017 Assimp failed to load file " + modelPath, Logger.Severity.ERROR);
      return null;
    }
    if (aiScene.mNumMeshes() <= 0) {
      Logger.log("0019 File " + modelPath + " contains no meshes", Logger.Severity.ERROR);
      return new RawModel[0];
    }
    RawModel[] models = new RawModel[aiScene.mNumMeshes()];
    for (int meshIdx = 0; meshIdx < aiScene.mNumMeshes(); meshIdx++) {
      models[meshIdx] = new RawModel(AIMesh.create(aiScene.mMeshes().get(meshIdx)));
    }
    return models;
  }
  
  public static RawModel loadBiggest(String modelPath) {
    AIScene aiScene = Assimp.aiImportFile(modelPath,
        Assimp.aiProcess_JoinIdenticalVertices | Assimp.aiProcess_Triangulate | Assimp.aiProcess_FixInfacingNormals);
    if (aiScene == null) {
      Logger.log("0017 Assimp failed to load file " + modelPath, Logger.Severity.ERROR);
      return null;
    }
    if (aiScene.mNumMeshes() <= 0) {
      Logger.log("0019 File " + modelPath + " contains no meshes", Logger.Severity.ERROR);
      return null;
    }
    AIMesh aiMesh = null;
    if (aiScene.mNumMeshes() > 1) {
      Logger.log("0020 File " + modelPath + " contains more than 1 mesh", Logger.Severity.WARN);
      int maxVertices = -1;
      for (int meshIdx = 0; meshIdx < aiScene.mNumMeshes(); meshIdx++) {
        AIMesh candidateMesh = AIMesh.create(aiScene.mMeshes().get(meshIdx));
        if (candidateMesh.mNumVertices() > maxVertices) {
          aiMesh = candidateMesh;
          maxVertices = candidateMesh.mNumVertices();
        }
      }
    }
    return new RawModel(aiMesh);
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
    GL15.glDeleteBuffers(this.ebo);
  }
  
  @Override
  public int hashCode() {
    return vao;
  }
}
