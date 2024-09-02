package us.itshighnoon.aventurine.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.render.mem.Model;
import us.itshighnoon.aventurine.render.shader.TestShader;

public class TestRenderer {
  private TestShader testShader;
  
  public TestRenderer() {
    this.testShader = new TestShader();
  }
  
  public void prepare() {
    GL11.glEnable(GL11.GL_DEPTH_TEST);
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
