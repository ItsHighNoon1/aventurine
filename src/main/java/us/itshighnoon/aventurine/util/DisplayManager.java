package us.itshighnoon.aventurine.util;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import us.itshighnoon.aventurine.render.Camera;

public class DisplayManager {
  private static DisplayManager singleton;

  private Camera camera;
  private long window;
  private int currentWidth;
  private int currentHeight;

  public DisplayManager(int width, int height, String title, long monitor, int vsync) {
    if (singleton != null) {
      Logger.log("0005 Attempted second DisplayManager initialization", Logger.Severity.WARN);
      return;
    }
    singleton = this;

    this.camera = null;
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
    glfwSetFramebufferSizeCallback(this.window, (long cbWin, int newWidth, int newHeight) -> {
      if (this.camera != null) {
        this.camera.recalculateProjection(newWidth, newHeight);
      }
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
  
  public static void setCamera(Camera camera) {
    singleton.camera = camera;
    camera.recalculateProjection(singleton.currentWidth, singleton.currentHeight);
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
