package us.itshighnoon.aventurine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import us.itshighnoon.aventurine.layer.GuiLayer;
import us.itshighnoon.aventurine.layer.LayerManager;
import us.itshighnoon.aventurine.map.OsmPbfReader;
import us.itshighnoon.aventurine.render.Camera;
import us.itshighnoon.aventurine.render.MasterRenderer;
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
    camera.position.x = -40.0f * 10.0f;
    camera.position.z = 25.0f;
    camera.position.y = 18.0f * 10.0f / 0.819f;
    DisplayManager.setCamera(camera);
    
    Vector3f modelRotation = new Vector3f((float) Math.toRadians(-90.0f), 0.0f, 0.0f);
    List<Vector3f> positions = new ArrayList<Vector3f>();
    OsmPbfReader.readMap("res/ignore/map.osm.pbf", positions);
    
    Random rand = new Random();
    List<Vector3f> lessPositions = new ArrayList<Vector3f>();
    for (int i = 0; i < 1000; i++) {
      Vector3f scaledVector = new Vector3f(positions.get(rand.nextInt(positions.size())));
      lessPositions.add(scaledVector.mul(10.0f, 10.0f / 0.819f, 0.0f));
    }

    while (!DisplayManager.closeRequested()) {
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_W)) {
        camera.position.z -= Math.cos(camera.rotation.y) * Math.cos(camera.rotation.x);
        camera.position.x -= Math.sin(camera.rotation.y) * Math.cos(camera.rotation.x);
        camera.position.y += Math.sin(camera.rotation.x);
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_S)) {
        camera.position.z += Math.cos(camera.rotation.y) * Math.cos(camera.rotation.x);
        camera.position.x += Math.sin(camera.rotation.y) * Math.cos(camera.rotation.x);
        camera.position.y -= Math.sin(camera.rotation.x);
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_A)) {
        camera.position.x -= Math.cos(camera.rotation.y);
        camera.position.z += Math.sin(camera.rotation.y);
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_D)) {
        camera.position.x += Math.cos(camera.rotation.y);
        camera.position.z -= Math.sin(camera.rotation.y);
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_UP)) {
        camera.rotation.x += 0.1f;
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
        camera.rotation.x -= 0.1f;
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
        camera.rotation.y += 0.1f;
      }
      if (DisplayManager.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
        camera.rotation.y -= 0.1f;
      }

      renderer.prepare();
      for (Vector3f position : lessPositions) {
        renderer.submitTest(model, position, modelRotation);
      }
      renderer.submitGui(gui);
      DisplayManager.refresh();
    }
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
