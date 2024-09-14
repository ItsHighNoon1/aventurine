package us.itshighnoon.aventurine.render.mem;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import org.joml.Vector3f;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.util.Logger;

public class Mesh implements Comparable<Mesh> {
  private static ConcurrentLinkedQueue<LineLoadRequest> lineLoadRequests = new ConcurrentLinkedQueue<LineLoadRequest>();

  private int vao;
  private int vbo;
  private int ebo;
  private int vCount;
  private int materialIdx;
  private int mode;

  private Mesh() {
    // All meshes have vao and vbo but some do not have ebo
    this.vao = GL30.glGenVertexArrays();
    this.vbo = GL15.glGenBuffers();
    this.ebo = 0;
    this.vCount = 0;
    this.materialIdx = -1;
    this.mode = GL11.GL_TRIANGLES;
  }

  public static Mesh loadQuad() {
    Mesh mesh = new Mesh();
    mesh.ebo = GL15.glGenBuffers();
    mesh.vCount = 4;
    mesh.mode = GL11.GL_TRIANGLE_STRIP;
    float vertices[] = { 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f,
        0.0f };
    GL30.glBindVertexArray(mesh.vao);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.vbo);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
    GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 16, 0);
    GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 16, 8);
    GL20.glEnableVertexAttribArray(0);
    GL20.glEnableVertexAttribArray(1);
    GL30.glBindVertexArray(0);
    return mesh;
  }

  public static Mesh loadAssimpMesh(AIMesh aiMesh) {
    Mesh mesh = new Mesh();

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

    mesh.vCount = indexData.length;
    mesh.materialIdx = aiMesh.mMaterialIndex();
    mesh.ebo = GL15.glGenBuffers();
    GL30.glBindVertexArray(mesh.vao);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.vbo);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 32, 0); // 32 is sizeof(float) * 8
    GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 32, 12);
    GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 32, 20);
    GL20.glEnableVertexAttribArray(0);
    GL20.glEnableVertexAttribArray(1);
    GL20.glEnableVertexAttribArray(2);
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.ebo);
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexData, GL15.GL_STATIC_DRAW);
    GL30.glBindVertexArray(0);

    return mesh;
  }

  public static Mesh awaitLineMesh(Vector3f[] points) {
    LineLoadRequest llq = new LineLoadRequest(points);
    lineLoadRequests.add(llq);
    llq.await();
    return llq.getMesh();
  }
  
  public static List<Mesh> awaitLineMeshMulti(List<Vector3f[]> lines) {
    List<LineLoadRequest> llqs = new ArrayList<LineLoadRequest>();
    for (Vector3f[] line : lines) {
      LineLoadRequest llq = new LineLoadRequest(line);
      llqs.add(llq);
      lineLoadRequests.add(llq);
    }
    List<Mesh> meshes = new ArrayList<Mesh>();
    for (LineLoadRequest llq : llqs) {
      llq.await();
      meshes.add(llq.getMesh());
    }
    return meshes;
  }

  public static Mesh loadLineMesh(Vector3f[] points) {
    Mesh mesh = new Mesh();
    mesh.vCount = points.length;
    mesh.mode = GL11.GL_LINE_STRIP;

    float[] vertexData = new float[points.length * 3];
    for (int pointIdx = 0; pointIdx < points.length; pointIdx++) {
      vertexData[pointIdx * 3 + 0] = points[pointIdx].x;
      vertexData[pointIdx * 3 + 1] = points[pointIdx].y;
      vertexData[pointIdx * 3 + 2] = points[pointIdx].z;
    }
    GL30.glBindVertexArray(mesh.vao);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.vbo);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12, 0);
    GL20.glEnableVertexAttribArray(0);
    GL30.glBindVertexArray(0);

    return mesh;
  }

  public static void serviceOutstandingLoads() {
    while (true) {
      LineLoadRequest llq = lineLoadRequests.poll();
      if (llq == null) {
        break;
      }
      Mesh line = loadLineMesh(llq.getPoints());
      llq.setMesh(line);
      llq.flagComplete();
    }
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

  public int getMode() {
    return this.mode;
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

  private static class LineLoadRequest {
    private Vector3f[] points;
    private Semaphore lock;
    private Mesh loadedMesh;

    private LineLoadRequest(Vector3f[] points) {
      this.points = points;
      this.lock = new Semaphore(0);
      this.loadedMesh = null;
    }

    private void await() {
      try {
        this.lock.acquire();
      } catch (InterruptedException e) {
        Logger.log("0053 Interrupted while awaiting line load request", Logger.Severity.WARN);
      }
    }

    private Vector3f[] getPoints() {
      return points;
    }

    private Mesh getMesh() {
      return loadedMesh;
    }

    private void setMesh(Mesh mesh) {
      this.loadedMesh = mesh;
    }

    private void flagComplete() {
      this.lock.release();
    }
  }
}
