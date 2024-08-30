package us.itshighnoon.aventurine;

import us.itshighnoon.aventurine.render.Camera;
import us.itshighnoon.aventurine.render.RawModel;
import us.itshighnoon.aventurine.render.Renderer;
import us.itshighnoon.aventurine.util.DisplayManager;

public class Game {
  public void run() {
    Renderer renderer = new Renderer();
    RawModel[] models = RawModel.loadAll("res/ignore/qq/qq.obj");
    Camera camera = new Camera(0.1f, 100.0f, (float)Math.toRadians((double)90.0f));
    camera.position.z = 1.0f;
    camera.position.y = 1.0f;
    DisplayManager.setCamera(camera);
    while (!DisplayManager.closeRequested()) {
      renderer.prepare();
      for (int i = 0; i < models.length; i++) {
        renderer.render(camera, models[i]);
      }
      DisplayManager.refresh();
    }
    for (int i = 0; i < models.length; i++) {
      models[i].cleanup();
    }
    renderer.cleanup();
  }
}
