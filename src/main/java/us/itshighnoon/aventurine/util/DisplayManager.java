package us.itshighnoon.aventurine.util;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class DisplayManager {
  private static DisplayManager singleton;
  
  private long window;
  private int currentWidth;
  private int currentHeight;
  
  public DisplayManager(int width, int height, String title, long monitor, int vsync) {
    if (singleton != null) {
      Logger.log("0005 Attempted second DisplayManager initialization", Logger.Severity.WARN);
      return;
    }
    singleton = this;
    
    this.currentWidth = width;
    this.currentHeight = height;
    
    if (!glfwInit()) {
      Logger.log("0006 Failed to initialize GLFW", Logger.Severity.ERROR);
      return;
    }
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    this.window = glfwCreateWindow(width, height, title, monitor, 0);
    if (this.window == 0) {
      Logger.log("0007 Failed to create GLFW window", Logger.Severity.ERROR);
    }
    
    glfwMakeContextCurrent(this.window);
    GL.createCapabilities();
    glfwSetFramebufferSizeCallback(this.window, (long cbWin, int newHeight, int newWidth) -> {
      this.currentWidth = newWidth;
      this.currentHeight = newHeight;
      GL11.glViewport(0, 0, newWidth, newHeight);
      Logger.log("0010 Window size changed");
    });
    GL11.glViewport(0, 0, width, height);
    glfwSwapInterval(vsync);
    glfwShowWindow(this.window);
  }
  
  public DisplayManager() {
    this(800, 500, "Aventurine", 0, 1);
  }
  
  public static boolean closeRequested() {
    if (singleton == null) {
      Logger.log("0009 Window status queried before window init", Logger.Severity.WARN);
      return false;
    }
    return glfwWindowShouldClose(singleton.window);
  }
  
  public static void refresh() {
    if (singleton == null) {
      Logger.log("0008 Refresh attempted before window init", Logger.Severity.WARN);
      return;
    }
    glfwSwapBuffers(singleton.window);
    glfwPollEvents();
  }
  
  public static int getWidth() {
    if (singleton == null) {
      Logger.log("0009 Window status queried before window init", Logger.Severity.WARN);
      return 0;
    }
    return singleton.currentWidth;
  }
  
  public static int getHeight() {
    if (singleton == null) {
      Logger.log("0009 Window status queried before window init", Logger.Severity.WARN);
      return 0;
    }
    return singleton.currentHeight;
  }
  
  public void destroy() {
    glfwDestroyWindow(this.window);
    glfwTerminate();
  }
}
