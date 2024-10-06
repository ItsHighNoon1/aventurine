package us.itshighnoon.aventurine.layer;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import us.itshighnoon.aventurine.layer.event.Event;
import us.itshighnoon.aventurine.util.Logger;

public class LayerManager {
  private List<Layer> layers;
  
  public LayerManager() {
    layers = new LinkedList<Layer>();
  }
  
  public void raiseEvent(Event e) {
    for (Layer layer : layers) {
      boolean eventConsumed = layer.raiseEvent(e);
      if (eventConsumed) {
        return;
      }
    }
  }
  
  public void addFirst(Layer layer) {
    layers.add(0, layer);
  }
  
  public void addLast(Layer layer) {
    layers.add(layers.size(), layer);
  }
  
  public void addAfter(Layer layer, String layerName) {
    ListIterator<Layer> it = layers.listIterator();
    while (it.hasNext()) {
      Layer current = it.next();
      if (current.getName().equals(layerName)) {
        it.add(layer);
        return;
      }
    }
    Logger.log("0042 Tried adding layer after " + layerName + " but no such layer existed", Logger.Severity.WARN);
    layers.add(0, layer);
  }
}
