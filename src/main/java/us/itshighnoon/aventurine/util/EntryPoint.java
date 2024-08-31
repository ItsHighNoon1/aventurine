package us.itshighnoon.aventurine.util;

import us.itshighnoon.aventurine.Game;

// highest log #: 0030

public class EntryPoint {
  public static void main(String[] args) {
    String logFile = null;
    boolean logStdout = false;
    if (args.length >= 1) {
      logStdout = Boolean.parseBoolean(args[0]);
    }
    if (args.length >= 2) {
      logFile = args[1];
    }
    Logger logger = null;
    if (logStdout || logFile != null) {
      logger = new Logger(logFile, logStdout, false);
    }
    DisplayManager displayManager = new DisplayManager();
    ResourceManager resourceManager = new ResourceManager();
    
    Logger.log("0004 Starting game", Logger.Severity.INFO);
    Game game = new Game();
    try {
      game.run();
      Logger.log("0011 Stopped game", Logger.Severity.INFO);
    } catch (Exception e) {
      Logger.log("0012 Stopped game with exception", Logger.Severity.ERROR);
      e.printStackTrace();
    }
    
    resourceManager.cleanup();
    displayManager.destroy();
    if (logger != null) {
      logger.stop();
    }
  }
}
