package us.itshighnoon.aventurine.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Logger {
  public static enum Severity {
    INFO, WARN, ERROR
  };

  private static Logger singleton = null;

  private LoggerThread logThread;
  private ConcurrentLinkedQueue<String> messages;
  private PrintWriter fileOut;
  private boolean stdout;
  private boolean ignoreInfos;

  public Logger(String filepath, boolean stdout, boolean ignoreInfos) {
    if (singleton != null) {
      log("0001 Attempted second Logger initialization", Severity.WARN);
      return;
    }
    singleton = this;
    
    this.ignoreInfos = ignoreInfos;
    this.logThread = new LoggerThread();
    this.messages = new ConcurrentLinkedQueue<String>();
    if (filepath != null) {
      try {
        this.fileOut = new PrintWriter(filepath);
      } catch (FileNotFoundException e) {
        this.fileOut = null;
        log("0002 Could not open log output file", Severity.WARN);
      }
    } else {
      this.fileOut = null;
    }
    this.stdout = stdout;
    
    this.logThread.start();
  }

  public static void log(String message, Severity sev) {
    if (singleton == null) {
      System.out.println(message);
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(new Date().toString());
    switch (sev) {
    case INFO:
      if (singleton.ignoreInfos) {
        return;
      }
      sb.append(" [INFO]  ");
      break;
    case WARN:
      sb.append(" [WARN]  ");
      break;
    case ERROR:
      sb.append(" [ERROR] ");
    }
    sb.append(message);
    singleton.messages.add(sb.toString());
  }
  
  public static void log(String message) {
    log(message, Severity.INFO);
  }
  
  public static void logStacktrace(Exception exception) {
    log("0037 Stacktrace display: " + exception.getMessage(), Severity.ERROR);
    StackTraceElement[] stackFrames = exception.getStackTrace();
    String whitespace = "";
    for (int frameIdx = stackFrames.length - 1; frameIdx >= 0; frameIdx--) {
      log(String.format("0037 Stacktrace display: %s%s:%d", whitespace, stackFrames[frameIdx].getFileName(), 
          stackFrames[frameIdx].getLineNumber()), Severity.ERROR);
      whitespace += "  ";
    }
  }
  
  public void stop() {
    logThread.shouldStop = true;
    try {
      logThread.join();
    } catch (InterruptedException e) {
      // oh well
    }
    fileOut.close();
  }
  
  private class LoggerThread extends Thread {
    private boolean shouldStop;
    
    public LoggerThread() {
      shouldStop = false;
    }
    
    @Override
    public void run() {
      while (true) {
        String message = messages.poll();
        if (message == null) {
          try {
            if (shouldStop) {
              return;
            }
            Thread.sleep(100);
          } catch (InterruptedException e) {
            log("0003 Log monitor sleep interrupted", Severity.WARN);
          }
          continue;
        }
        if (stdout) {
          System.out.println(message);
        }
        if (fileOut != null) {
          fileOut.println(message);
        }
      }
    }
  }
}
