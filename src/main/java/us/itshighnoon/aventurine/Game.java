package us.itshighnoon.aventurine;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import us.itshighnoon.aventurine.layer.GuiLayer;
import us.itshighnoon.aventurine.layer.LayerManager;
import us.itshighnoon.aventurine.map.MapChunk;
import us.itshighnoon.aventurine.map.io.MapStreamer;
import us.itshighnoon.aventurine.render.Camera;
import us.itshighnoon.aventurine.render.MasterRenderer;
import us.itshighnoon.aventurine.render.mem.Mesh;
import us.itshighnoon.aventurine.render.mem.Model;
import us.itshighnoon.aventurine.ui.GuiListener;
import us.itshighnoon.aventurine.ui.GuiNode;
import us.itshighnoon.aventurine.util.DisplayManager;
import us.itshighnoon.aventurine.util.Logger;
import us.itshighnoon.aventurine.util.ResourceManager;

public class Game {
  public void run() {
    MasterRenderer renderer = new MasterRenderer();
    Model model = Model.loadAll("res/ignore/qq/qq.fbx");

    LayerManager eventHandler = new LayerManager();
    GuiNode gui = new GuiNode(100, 100, 100, 100, ResourceManager.loadTexture("res/ignore/qq/1.png"));
    GuiNode gui2 = new GuiNode(150, 150, 100, 100, ResourceManager.loadTexture("res/ignore/qq/a4.bmp"));
    gui.addChild(gui2);
    gui.setListener(new TestListener());
    GuiLayer guiEventHandler = new GuiLayer(gui);
    eventHandler.addFirst(guiEventHandler);
    DisplayManager.setLayerManager(eventHandler);

    Camera camera = new Camera(0.1f, 1000.0f, (float) Math.toRadians((double) 90.0f));
    renderer.setCamera(camera);
    camera.position.y = 100.0f;
    DisplayManager.setCamera(camera);
    MapStreamer mapStreamer = new MapStreamer("res/map.meta");

    while (!DisplayManager.closeRequested()) {
      mapStreamer.loadChunksAround(camera.position.x, camera.position.z, true);
      Mesh.serviceOutstandingLoads();

      float speed = DisplayManager.getLastFrameTime() * 100.0f;
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
        speed *= 3.0f;
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_W)) {
        camera.position.z -= speed * Math.cos(camera.rotation.y) * Math.cos(camera.rotation.x);
        camera.position.x -= speed * Math.sin(camera.rotation.y) * Math.cos(camera.rotation.x);
        camera.position.y += speed * Math.sin(camera.rotation.x);
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_S)) {
        camera.position.z += speed * Math.cos(camera.rotation.y) * Math.cos(camera.rotation.x);
        camera.position.x += speed * Math.sin(camera.rotation.y) * Math.cos(camera.rotation.x);
        camera.position.y -= speed * Math.sin(camera.rotation.x);
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_A)) {
        camera.position.x -= speed * Math.cos(camera.rotation.y);
        camera.position.z += speed * Math.sin(camera.rotation.y);
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_D)) {
        camera.position.x += speed * Math.cos(camera.rotation.y);
        camera.position.z -= speed * Math.sin(camera.rotation.y);
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_UP)) {
        camera.rotation.x += DisplayManager.getLastFrameTime() * 3.0f;
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
        camera.rotation.x -= DisplayManager.getLastFrameTime() * 3.0f;
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
        camera.rotation.y += DisplayManager.getLastFrameTime() * 3.0f;
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
        camera.rotation.y -= DisplayManager.getLastFrameTime() * 3.0f;
      }

      renderer.prepare();
      for (MapChunk chunk : mapStreamer.getMapChunks()) {
        if (chunk.isLoaded()) {
          for (Mesh road : chunk.getRoads()) {
            renderer.submitLine(road, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
            //renderer.submitTest(model, node, new Vector3f((float) Math.toRadians(-90), 0.0f, 0.0f));
          }
        }
      }
      renderer.submitGui(gui);
      DisplayManager.refresh();
    }
    mapStreamer.cleanup();
    model.cleanup();
    renderer.cleanup();
  }

  private static class TestListener implements GuiListener {
    @Override
    public void onHover(int x, int y) {
      Logger.log("1001 Hovered");
    }

    @Override
    public void onUnhover(int x, int y) {
      Logger.log("1002 Unhovered");
    }

    @Override
    public void onClick(int mouseButton, int x, int y) {
      Logger.log("1003 Clicked");
    }

    @Override
    public void onScroll(int scrollX, int scrollY, int x, int y) {
      Logger.log("1004 Scrolled");
    }
  }
}
