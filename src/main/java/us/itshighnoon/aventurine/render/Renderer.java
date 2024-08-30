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
    GL11.glEnable(GL11.GL_CULL_FACE);
    GL11.glCullFace(GL11.GL_BACK);
  }
  
  public void prepare() {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
  }
  
  public void render(Camera camera, Model model) {
    testShader.bind();
    testShader.setDiffuseSampler(0);
    testShader.setMvpMatrix(camera.getCameraMatrix());
    model.render();
    GL30.glBindVertexArray(0);
  }
  
  public void cleanup() {
    testShader.cleanup();
  }
}
