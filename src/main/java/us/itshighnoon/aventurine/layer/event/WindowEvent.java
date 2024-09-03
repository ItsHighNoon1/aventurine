package us.itshighnoon.aventurine.layer.event;

public class WindowEvent extends Event {
  private int x;
  private int y;
  private int width;
  private int height;
  
  private WindowEvent(EventType type, int x, int y, int width, int height) {
    super(type);
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
  
  public int getX() {
    return x;
  }
  
  public int getY() {
    return y;
  }
  
  public int getWidth() {
    return width;
  }
  
  public int getHeight() {
    return height;
  }
  
  public static WindowEvent newMoveEvent(int x, int y) {
    return new WindowEvent(EventType.WINDOW_MOVE, x, y, 0, 0);
  }
  
  public static WindowEvent newResizeEvent(int width, int height) {
    return new WindowEvent(EventType.WINDOW_RESIZE, 0, 0, width, height);
  }
}
