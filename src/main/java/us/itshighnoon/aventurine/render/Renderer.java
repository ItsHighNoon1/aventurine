package us.itshighnoon.aventurine.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.shader.TestShader;

public class Renderer {
  private TestShader testShader;
  
  public Renderer() {
    this.testShader = new TestShader();
    GL11.glClearColor(0.05f, 0.05f, 0.05f, 1.0f);
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    GL11.glEnable(GL11.GL_CULL_FACE);
    GL11.glCullFace(GL11.GL_BACK);
    GL11.glEnable(GL13.GL_MULTISAMPLE);
  }
  
  public void prepare() {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
  }
  
  public void render(Camera camera, Vector3f position, Vector3f rotation, Model model) {
    Matrix4f mvpMatrix = new Matrix4f();
    mvpMatrix.setTranslation(position);
    mvpMatrix.rotate(rotation.z, 0.0f, 0.0f, 1.0f);
    mvpMatrix.rotate(rotation.x, 1.0f, 0.0f, 0.0f);
    mvpMatrix.rotate(rotation.y, 0.0f, 1.0f, 0.0f);
    camera.getCameraMatrix().mul(mvpMatrix, mvpMatrix);
    testShader.bind();
    testShader.setDiffuseSampler(0);
    testShader.setMvpMatrix(mvpMatrix);
    model.render();
    GL30.glBindVertexArray(0);
  }
  
  public void cleanup() {
    testShader.cleanup();
  }
}
