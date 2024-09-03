package us.itshighnoon.aventurine.util;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import us.itshighnoon.aventurine.layer.LayerManager;
import us.itshighnoon.aventurine.layer.event.MouseEvent;
import us.itshighnoon.aventurine.layer.event.WindowEvent;
import us.itshighnoon.aventurine.render.Camera;

public class DisplayManager {
  private static DisplayManager singleton;

  private Camera camera;
  private LayerManager layerManager;
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
    this.layerManager = null;
    this.currentWidth = width;
    this.currentHeight = height;

    if (!GLFW.glfwInit()) {
      Logger.log("0006 Failed to initialize GLFW", Logger.Severity.ERROR);
      return;
    }
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
    GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
    GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
    GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
    this.window = GLFW.glfwCreateWindow(width, height, title, monitor, 0);
    if (this.window == 0) {
      Logger.log("0007 Failed to create GLFW window", Logger.Severity.ERROR);
    }

    GLFW.glfwMakeContextCurrent(this.window);
    GL.createCapabilities();
    GLFW.glfwSetFramebufferSizeCallback(this.window, (long cbWin, int newWidth, int newHeight) -> {
      if (this.camera != null) {
        this.camera.recalculateProjection(newWidth, newHeight);
      }
      this.currentWidth = newWidth;
      this.currentHeight = newHeight;
      GL11.glViewport(0, 0, newWidth, newHeight);
      Logger.log("0010 Window size changed");
      if (layerManager != null) {
        layerManager.raiseEvent(WindowEvent.newResizeEvent(newWidth, newHeight));
      }
    });
    GLFW.glfwSetCursorPosCallback(this.window, (window, xpos, ypos) -> {
      layerManager.raiseEvent(MouseEvent.newMoveEvent((int) xpos, (int) ypos));
    });
    GLFW.glfwSetMouseButtonCallback(this.window, (window, button, action, mods) -> {
      if (action != GLFW.GLFW_PRESS) {
        return;
      }
      double[] xPosReturn = new double[1];
      double[] yPosReturn = new double[1];
      GLFW.glfwGetCursorPos(window, xPosReturn, yPosReturn);
      layerManager.raiseEvent(MouseEvent.newClickEvent((int) xPosReturn[0], (int) yPosReturn[0], button));
    });
    GLFW.glfwSetScrollCallback(this.window, (window, xoffset, yoffset) -> {
      double[] xPosReturn = new double[1];
      double[] yPosReturn = new double[1];
      GLFW.glfwGetCursorPos(window, xPosReturn, yPosReturn);
      layerManager.raiseEvent(
          MouseEvent.newScrollEvent((int) xPosReturn[0], (int) yPosReturn[0], (int) xoffset, (int) yoffset));
    });
    GL11.glViewport(0, 0, width, height);
    GLFW.glfwSwapInterval(vsync);
    GLFW.glfwShowWindow(this.window);
  }

  public DisplayManager() {
    this(800, 500, "Aventurine", 0, 1);
  }

  public static void setCamera(Camera camera) {
    if (singleton == null) {
      Logger.log("0043 Tried to set camera before window init", Logger.Severity.WARN);
    }
    singleton.camera = camera;
    camera.recalculateProjection(singleton.currentWidth, singleton.currentHeight);
  }

  public static void setLayerManager(LayerManager layerManager) {
    if (singleton == null) {
      Logger.log("0044 Tried to set layer manager before window init", Logger.Severity.WARN);
    }
    singleton.layerManager = layerManager;
  }

  public static boolean closeRequested() {
    if (singleton == null) {
      Logger.log("0009 Window status queried before window init", Logger.Severity.WARN);
      return false;
    }
    return GLFW.glfwWindowShouldClose(singleton.window);
  }

  public static void refresh() {
    if (singleton == null) {
      Logger.log("0008 Refresh attempted before window init", Logger.Severity.WARN);
      return;
    }
    GLFW.glfwSwapBuffers(singleton.window);
    GLFW.glfwPollEvents();
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

  public static boolean isKeyDown(int keyCode) {
    if (singleton == null) {
      Logger.log("0021 Input status queried before window init", Logger.Severity.WARN);
      return false;
    }
    return GLFW.glfwGetKey(singleton.window, keyCode) == GLFW.GLFW_PRESS;
  }

  public void destroy() {
    GLFW.glfwDestroyWindow(this.window);
    GLFW.glfwTerminate();
  }
}
