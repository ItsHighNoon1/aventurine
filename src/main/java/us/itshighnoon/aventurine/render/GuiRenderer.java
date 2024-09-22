package us.itshighnoon.aventurine.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.render.mem.Font;
import us.itshighnoon.aventurine.render.mem.Font.Glyph;
import us.itshighnoon.aventurine.render.mem.Mesh;
import us.itshighnoon.aventurine.render.shader.GuiShader;
import us.itshighnoon.aventurine.ui.GuiNode;
import us.itshighnoon.aventurine.util.DisplayManager;
import us.itshighnoon.aventurine.util.Logger;

public class GuiRenderer {
  private GuiShader guiShader;
  private Mesh quad;
  private int screenWidth;
  private int screenHeight;

  public GuiRenderer() {
    this.guiShader = new GuiShader();
    this.quad = Mesh.loadQuad();
  }

  public void prepare() {
    this.screenWidth = DisplayManager.getWidth();
    this.screenHeight = DisplayManager.getHeight();
    GL11.glDisable(GL11.GL_DEPTH_TEST);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
  }

  public void render(GuiNode node) {
    guiShader.bind();
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL30.glBindVertexArray(quad.getVao());
    renderRecursive(node);
  }

  private void renderRecursive(GuiNode node) {
    float xPos = (float) node.getX() / screenWidth * 2.0f - 1.0f;
    float yPos = (float) -node.getY() / screenHeight * 2.0f + 1.0f;
    float xSize = (float) node.getWidth() / screenWidth * 2.0f;
    float ySize = (float) node.getHeight() / screenHeight * 2.0f;
    float aspectRatio = (float) screenHeight / screenWidth;
    Matrix4f mMatrix = new Matrix4f();
    if (node.getTexture() != 0) {
      mMatrix.identity();
      mMatrix.setTranslation(xPos, yPos, 0.0f);
      mMatrix.scale(xSize, ySize, 0.0f);
      guiShader.setMMatrix(mMatrix);
      GL13.glBindTexture(GL11.GL_TEXTURE_2D, node.getTexture());
      guiShader.setTextureParms(0, node.getTexOffset(), node.getTexSize());
      GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
    }
    if (node.getText() != null) {
      Font font = node.getFont();
      GL13.glBindTexture(GL11.GL_TEXTURE_2D, font.getTexture());
      String[] lines = node.getText().split("\n");
      for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
        float alignAdjust = 0.0f;
        if (node.getAlignment() != GuiNode.TextAlignment.LEFT) {
          for (int charIdx = 0; charIdx < lines[lineIdx].length(); charIdx++) {
            Glyph glyph = font.getGlyph(lines[lineIdx].charAt(charIdx));
            if (glyph == null) {
              Logger.log(
                  "0068 Bad character '" + lines[lineIdx].charAt(charIdx) + "' as part of \"" + node.getText() + "\"",
                  Logger.Severity.WARN);
              continue;
            }
            alignAdjust += glyph.getxAdvance() * node.getTextSize() * aspectRatio;
          }
        }
        switch (node.getAlignment()) {
        case CENTER:
          alignAdjust = xSize - alignAdjust;
          alignAdjust /= 2.0f;
          break;
        case RIGHT:
          alignAdjust = xSize - alignAdjust;
          break;
        default:
          break;
        }
        float runningX = xPos + alignAdjust;
        float runningY = yPos - font.getLineHeight() * node.getTextSize() * lineIdx;
        for (int charIdx = 0; charIdx < lines[lineIdx].length(); charIdx++) {
          Glyph glyph = font.getGlyph(lines[lineIdx].charAt(charIdx));
          if (glyph == null) {
            continue;
          }
          mMatrix.identity();
          mMatrix.setTranslation(runningX + glyph.getxOffset() * node.getTextSize() * aspectRatio,
              runningY - glyph.getyOffset() * node.getTextSize(), 0.0f);
          mMatrix.scale(glyph.getTexSize().x * node.getTextSize() * aspectRatio,
              glyph.getTexSize().y * node.getTextSize(), 0.0f);
          guiShader.setMMatrix(mMatrix);
          guiShader.setTextureParms(0, glyph.getTexLocation(), glyph.getTexSize());
          GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
          runningX += glyph.getxAdvance() * node.getTextSize() * aspectRatio;
        }
      }
    }
    for (GuiNode child : node.getChildren()) {
      render(child);
    }
  }

  public void cleanup() {
    guiShader.cleanup();
    quad.cleanup();
  }
}
