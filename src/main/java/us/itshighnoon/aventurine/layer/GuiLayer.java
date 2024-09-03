package us.itshighnoon.aventurine.layer;

import us.itshighnoon.aventurine.layer.event.Event;
import us.itshighnoon.aventurine.layer.event.MouseEvent;
import us.itshighnoon.aventurine.ui.GuiNode;

public class GuiLayer extends Layer {
  private GuiNode root;
  private GuiNode previouslyHovered;

  public GuiLayer(GuiNode root) {
    super("GUI");
    this.root = root;
    this.previouslyHovered = null;
    super.subscribeTo(Event.EventType.MOUSE_CLICK);
    super.subscribeTo(Event.EventType.MOUSE_SCROLL);
    super.subscribeTo(Event.EventType.MOUSE_MOVE);
  }

  @Override
  protected boolean handleEvent(Event e) {
    MouseEvent me = (MouseEvent)e;
    GuiNode mouseIsOver = root.find(me.getX(), me.getY());
    switch (me.getType()) {
    case MOUSE_CLICK:
      if (mouseIsOver != null && mouseIsOver.getListener() != null) {
        mouseIsOver.getListener().onClick(me.getMouseButton(), me.getX(), me.getY());
      }
      break;
    case MOUSE_SCROLL:
      if (mouseIsOver != null && mouseIsOver.getListener() != null) {
        mouseIsOver.getListener().onScroll(me.getScrollX(), me.getScrollY(), me.getX(), me.getY());
      }
      break;
    case MOUSE_MOVE:
      if (mouseIsOver != previouslyHovered) {
        if (mouseIsOver != null && mouseIsOver.getListener() != null) {
          mouseIsOver.getListener().onHover(me.getX(), me.getY());
        }
        if (previouslyHovered != null && previouslyHovered.getListener() != null) {
          previouslyHovered.getListener().onUnhover(me.getX(), me.getY());
        }
        previouslyHovered = mouseIsOver;
      }
      break;
    default:
      break;
    }
    return mouseIsOver != null;
  }
}
