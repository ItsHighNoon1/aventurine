package us.itshighnoon.aventurine;

import org.joml.Vector3f;

import us.itshighnoon.aventurine.render.Camera;
import us.itshighnoon.aventurine.render.Model;
import us.itshighnoon.aventurine.render.Renderer;
import us.itshighnoon.aventurine.util.DisplayManager;

public class Game {
  public void run() {
    Renderer renderer = new Renderer();
    Model model = Model.loadAll("res/ignore/qq/qq.fbx");
    Camera camera = new Camera(0.1f, 100.0f, (float)Math.toRadians((double)90.0f));
    camera.position.z = 1.0f;
    camera.position.y = -1.0f;
    camera.rotation.x = (float)Math.toRadians(-90.0);
    DisplayManager.setCamera(camera);
    Vector3f modelPosition = new Vector3f();
    Vector3f modelRotation = new Vector3f();
    while (!DisplayManager.closeRequested()) {
      modelRotation.z += 0.01f;
      renderer.prepare();
      renderer.render(camera, modelPosition, modelRotation, model);
      DisplayManager.refresh();
    }
    model.cleanup();
    renderer.cleanup();
  }
}
