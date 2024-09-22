package us.itshighnoon.aventurine.ui;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

import us.itshighnoon.aventurine.render.mem.Font;

public class GuiNode {
  public enum TextAlignment {
    LEFT, CENTER, RIGHT
  };

  private List<GuiNode> children;
  private GuiListener listener;
  private int x;
  private int y;
  private int width;
  private int height;
  private int texture;
  private boolean interactible;
  private Vector2f texOffset;
  private Vector2f texSize;
  private String text;
  private TextAlignment textAlignment;
  private float textSize;
  private Font font;

  private GuiNode(int x, int y, int width, int height, int texture, boolean interactible, Vector2f texOffset,
      Vector2f texSize, String text, TextAlignment alignment, float textSize, Font font) {
    this.children = new ArrayList<GuiNode>();
    this.listener = null;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.texture = texture;
    this.interactible = interactible;
    this.texOffset = texOffset;
    this.texSize = texSize;
    this.text = text;
    this.textAlignment = alignment;
    this.textSize = textSize;
    this.font = font;
  }

  public GuiNode(int x, int y, int width, int height) {
    this(x, y, width, height, 0, false, null, null, null, TextAlignment.LEFT, 0.0f, null);
  }
  
  public GuiNode(int x, int y, int width, int height, int textureId, Vector2f texOffset, Vector2f texSize) {
    this(x, y, width, height, textureId, true, texOffset, texSize, null, TextAlignment.LEFT, 0.0f, null);
  }
  
  public GuiNode(int x, int y, int width, int height, String text, TextAlignment alignment, float textSize, Font font) {
    this(x, y, width, height, 0, false, null, null, text, alignment, textSize, font);
  }

  public List<GuiNode> getChildren() {
    return children;
  }

  public void addChild(GuiNode child) {
    children.add(child);
  }

  public GuiNode find(int mouseX, int mouseY) {
    GuiNode foundNode = null;
    if (interactible && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
      foundNode = this;
    }
    for (GuiNode node : children) {
      if (node.find(mouseX, mouseY) != null) {
        foundNode = node;
      }
    }
    return foundNode;
  }

  public GuiListener getListener() {
    return listener;
  }

  public void setListener(GuiListener listener) {
    this.listener = listener;
  }

  public int getTexture() {
    return texture;
  }

  public Vector2f getTexOffset() {
    return texOffset;
  }

  public Vector2f getTexSize() {
    return texSize;
  }

  public void setTextureParams(int texture, Vector2f texOffset, Vector2f texSize) {
    this.texture = texture;
    this.texOffset = texOffset;
    this.texSize = texSize;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public void move(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public void resize(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public TextAlignment getAlignment() {
    return textAlignment;
  }

  public void setTextAlignmnt(TextAlignment alignment) {
    this.textAlignment = alignment;
  }

  public float getTextSize() {
    return textSize;
  }

  public void setTextSize(float size) {
    this.textSize = size;
  }
  
  public Font getFont() {
    return font;
  }
  
  public void setFont(Font font) {
    this.font = font;
  }
}
