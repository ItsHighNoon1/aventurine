package us.itshighnoon.aventurine.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.shader.TestShader;

public class Renderer {
  private TestShader testShader;
  
  public Renderer() {
    this.testShader = new TestShader();
    GL11.glClearColor(0.05f, 0.05f, 0.05f, 1.0f);
    GL11.glEnable(GL11.GL_DEPTH_TEST);
  }
  
  public void prepare() {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
  }
  
  public void render(Camera camera, RawModel model) {
    testShader.bind();
    testShader.setMvpMatrix(camera.getCameraMatrix());
    model.bind();
    GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
    GL30.glBindVertexArray(0);
  }
  
  public void cleanup() {
    testShader.cleanup();
  }
}
