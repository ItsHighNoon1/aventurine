package us.itshighnoon.aventurine.layer.event;

public class Event {
  public static enum EventType {
    WINDOW_MOVE,
    WINDOW_RESIZE,
    WINDOW_CLOSE,
    MOUSE_CLICK, 
    MOUSE_SCROLL,
    MOUSE_MOVE, 
  };
  
  private EventType type;

  public Event(EventType type) {
    this.type = type;
  }
  
  public EventType getType() {
    return type;
  }
}
