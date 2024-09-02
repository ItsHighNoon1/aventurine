package us.itshighnoon.aventurine.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.render.mem.Mesh;
import us.itshighnoon.aventurine.render.shader.GuiShader;
import us.itshighnoon.aventurine.util.DisplayManager;

public class GuiRenderer {
  private GuiShader guiShader;
  private Mesh quad;
  private int screenWidth;
  private int screenHeight;

  public GuiRenderer() {
    this.guiShader = new GuiShader();
    this.quad = new Mesh();
  }

  public void prepare() {
    this.screenWidth = DisplayManager.getWidth();
    this.screenHeight = DisplayManager.getHeight();
    GL11.glDisable(GL11.GL_DEPTH_TEST);
  }

  public void render(int x, int y, int width, int height) {
    Matrix4f mMatrix = new Matrix4f();
    float xPos = (float) x / screenWidth * 2.0f - 1.0f;
    float yPos = (float) -y / screenHeight * 2.0f + 1.0f;
    float xScale = (float) width / screenWidth * 2.0f;
    float yScale = (float) height / screenHeight * 2.0f;
    mMatrix.setTranslation(xPos, yPos, 0.0f);
    mMatrix.scale(xScale, yScale, 0.0f);
    guiShader.bind();
    guiShader.setMMatrix(mMatrix);
    guiShader.setTexture(0);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL30.glBindVertexArray(quad.getVao());
    GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
  }

  public void cleanup() {
    guiShader.cleanup();
    quad.cleanup();
  }
}
