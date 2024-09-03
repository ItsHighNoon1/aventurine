package us.itshighnoon.aventurine.layer;

import java.util.HashSet;
import java.util.Set;

import us.itshighnoon.aventurine.layer.event.Event;

public abstract class Layer {
  private Set<Event.EventType> subscribedEvents;
  private String name;
  
  public Layer(String name) {
    this.subscribedEvents = new HashSet<Event.EventType>();
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean raiseEvent(Event e) {
    if (subscribedEvents.contains(e.getType())) {
      return handleEvent(e);
    }
    return false;
  }
  
  protected void subscribeTo(Event.EventType type) {
    subscribedEvents.add(type);
  }
  
  protected void unsubscribeFrom(Event.EventType type) {
    subscribedEvents.remove(type);
  }
  
  protected abstract boolean handleEvent(Event e);
}
