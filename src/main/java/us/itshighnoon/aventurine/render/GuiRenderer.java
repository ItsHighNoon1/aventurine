package us.itshighnoon.aventurine.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.render.mem.Mesh;
import us.itshighnoon.aventurine.render.shader.GuiShader;
import us.itshighnoon.aventurine.ui.GuiNode;
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

  public void render(GuiNode node) {
    guiShader.bind();
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL30.glBindVertexArray(quad.getVao());
    renderRecursive(node);
  }
  
  private void renderRecursive(GuiNode node) {
    Matrix4f mMatrix = new Matrix4f();
    float xPos = (float) node.getX() / screenWidth * 2.0f - 1.0f;
    float yPos = (float) -node.getY() / screenHeight * 2.0f + 1.0f;
    float xScale = (float) node.getWidth() / screenWidth * 2.0f;
    float yScale = (float) node.getHeight() / screenHeight * 2.0f;
    mMatrix.setTranslation(xPos, yPos, 0.0f);
    mMatrix.scale(xScale, yScale, 0.0f);
    guiShader.setMMatrix(mMatrix);
    GL13.glBindTexture(GL11.GL_TEXTURE_2D, node.getTexture());
    guiShader.setTextureParms(0, node.getTexOffset(), node.getTexSize());
    GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
    for (GuiNode child : node.getChildren()) {
      render(child);
    }
  }

  public void cleanup() {
    guiShader.cleanup();
    quad.cleanup();
  }
}
