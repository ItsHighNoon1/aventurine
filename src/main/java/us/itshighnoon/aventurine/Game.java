package us.itshighnoon.aventurine;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import us.itshighnoon.aventurine.layer.GuiLayer;
import us.itshighnoon.aventurine.layer.LayerManager;
import us.itshighnoon.aventurine.map.MapChunk;
import us.itshighnoon.aventurine.map.Way;
import us.itshighnoon.aventurine.map.io.MapStreamer;
import us.itshighnoon.aventurine.render.Camera;
import us.itshighnoon.aventurine.render.MasterRenderer;
import us.itshighnoon.aventurine.render.mem.Font;
import us.itshighnoon.aventurine.render.mem.Mesh;
import us.itshighnoon.aventurine.render.mem.Model;
import us.itshighnoon.aventurine.ui.GuiListener;
import us.itshighnoon.aventurine.ui.GuiNode;
import us.itshighnoon.aventurine.util.DisplayManager;
import us.itshighnoon.aventurine.util.Logger;

public class Game {
  public void run() {
    MasterRenderer renderer = new MasterRenderer();
    Model model = Model.loadAll("res/ignore/qq/qq.fbx");
    Font font = new Font("res/ignore/font/b/2048.fnt");
    

    LayerManager eventHandler = new LayerManager();
    GuiNode gui = new GuiNode(300, 100, 100, 100, "Short\nAnd a very, very long line!", GuiNode.TextAlignment.CENTER, 1.0f, font);
    gui.setListener(new TestListener());
    GuiLayer guiEventHandler = new GuiLayer(gui);
    eventHandler.addFirst(guiEventHandler);
    DisplayManager.setLayerManager(eventHandler);

    Camera camera = new Camera(100.0f, 25000.0f, (float) Math.toRadians((double) 90.0f));
    renderer.setCamera(camera);
    camera.position.y = 1000.0f;
    DisplayManager.setCamera(camera);
    MapStreamer mapStreamer = new MapStreamer("res/map.meta");

    while (!DisplayManager.closeRequested()) {
      mapStreamer.loadChunksAround(camera.position.x, camera.position.z, true);
      Mesh.serviceOutstandingLoads();

      float speed = DisplayManager.getLastFrameTime() * 5000.0f;
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
      camera.recalculateFrustum();

      renderer.prepare();
      for (MapChunk chunk : mapStreamer.getMapChunks()) {
        if (chunk.isLoaded()) {
          for (Way way : chunk.getVisibleWays(camera)) {
            if (way.getDebugMesh() != null) {
              Vector3f color = null;
              String highwayType = way.getAttributes().get("highway");
              if ("motorway".equals(highwayType)) {
                // Interstate, US route, etc
                color = new Vector3f(1.0f, 0.5f, 0.5f);
              } else if (highwayType != null) {
                // All other roads
                color = new Vector3f(1.0f, 1.0f, 0.5f);
              } else if (way.getAttributes().get("building") != null) {
                // Building
                color = new Vector3f(0.9f, 0.9f, 0.9f);
              } else if (way.getAttributes().get("waterway") != null){
                // Waterway
                color = new Vector3f(0.2f, 0.5f, 1.0f);
              } else if (way.getAttributes().get("railway") != null){
                // Railroad
                color = new Vector3f(1.0f, 0.5f, 1.0f);
              } else if (way.getAttributes().get("power") != null){
                // Long distance electrical line
                color = new Vector3f(1.0f, 0.5f, 0.2f);
              }
              if (color != null) {
                renderer.submitLine(way.getDebugMesh(), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), color);
              }
            }
          }
        }
      }
      renderer.submitGui(gui);
      DisplayManager.refresh();
    }
    font.cleanup();
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
