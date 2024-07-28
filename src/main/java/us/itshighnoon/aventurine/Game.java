package us.itshighnoon.aventurine;

import us.itshighnoon.aventurine.util.DisplayManager;

public class Game {
  public Game() {
    
  }
  
  public void run() {
    while (!DisplayManager.closeRequested()) {
      DisplayManager.refresh();
    }
  }
}
