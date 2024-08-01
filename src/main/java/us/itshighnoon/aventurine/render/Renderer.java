package us.itshighnoon.aventurine.render;

import org.lwjgl.opengl.GL11;

import us.itshighnoon.aventurine.shader.TestShader;

public class Renderer {
  private TestShader testShader;
  
  public Renderer() {
    this.testShader = new TestShader();
    GL11.glClearColor(0.05f, 0.05f, 0.05f, 1.0f);
  }
  
  public void prepare() {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
  }
  
  public void render(RawModel model) {
    testShader.bind();
    model.bind();
    GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.getVertexCount());
  }
  
  public void cleanup() {
    testShader.cleanup();
  }
}
