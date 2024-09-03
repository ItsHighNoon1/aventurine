package us.itshighnoon.aventurine.ui;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

public class GuiNode {
  private List<GuiNode> children;
  private GuiListener listener;
  private int x;
  private int y;
  private int width;
  private int height;
  private int texture;
  private Vector2f texOffset;
  private Vector2f texSize;
  
  public GuiNode(int x, int y, int width, int height, int texture, Vector2f texOffset, Vector2f texSize) {
    this.children = new ArrayList<GuiNode>();
    this.listener = null;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.texture = texture;
    this.texOffset = texOffset;
    this.texSize = texSize;
  }
  
  public GuiNode(int x, int y, int width, int height, int texture) {
    this(x, y, width, height, texture, new Vector2f(0.0f, 0.0f), new Vector2f(1.0f, 1.0f));
  }

  public List<GuiNode> getChildren() {
    return children;
  }
  
  public void addChild(GuiNode child) {
    children.add(child);
  }
  
  public GuiNode find(int mouseX, int mouseY) {
    GuiNode foundNode = null;
    if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
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
}
