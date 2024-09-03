package us.itshighnoon.aventurine.layer.event;

public class MouseEvent extends Event {
  private int x;
  private int y;
  private int button;
  private int scrollX;
  private int scrollY;
  
  private MouseEvent(EventType type, int x, int y, int button, int scrollX, int scrollY) {
    super(type);
    this.x = x;
    this.y = y;
    this.button = button;
    this.scrollX = scrollX;
    this.scrollY = scrollY;
  }
  
  public int getX() {
    return x;
  }
  
  public int getY() {
    return y;
  }
  
  public int getMouseButton() {
    return button;
  }
  
  public int getScrollX() {
    return scrollX;
  }
  
  public int getScrollY() {
    return scrollY;
  }
  
  public static MouseEvent newClickEvent(int x, int y, int button) {
    return new MouseEvent(EventType.MOUSE_CLICK, x, y, button, 0, 0);
  }
  
  public static MouseEvent newScrollEvent(int x, int y, int scrollX, int scrollY) {
    return new MouseEvent(EventType.MOUSE_SCROLL, x, y, 0, scrollX, scrollY);
  }
  
  public static MouseEvent newMoveEvent(int x, int y) {
    return new MouseEvent(EventType.MOUSE_MOVE, x, y, 0, 0, 0);
  }
}
