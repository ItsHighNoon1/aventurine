package us.itshighnoon.aventurine;

import us.itshighnoon.aventurine.render.Camera;
import us.itshighnoon.aventurine.render.RawModel;
import us.itshighnoon.aventurine.render.Renderer;
import us.itshighnoon.aventurine.util.DisplayManager;

public class Game {
  public static final float[] TRI_VERTS = {-0.5f, -0.5f, 0.0f,
                                            0.5f, -0.5f, 0.0f,
                                            0.0f,  0.5f, 0.0f};
  
  public void run() {
    Renderer renderer = new Renderer();
    RawModel model = new RawModel(TRI_VERTS);
    RawModel qq = new RawModel("res/ignore/qq/qq.obj");
    Camera camera = new Camera(0.1f, 100.0f, (float)Math.toRadians((double)90.0f));
    camera.position.z = 2.0f;
    camera.position.x = 1.0f;
    camera.rotation.y = -1.0f;
    DisplayManager.setCamera(camera);
    while (!DisplayManager.closeRequested()) {
      renderer.prepare();
      renderer.render(camera, model);
      renderer.render(camera, qq);
      DisplayManager.refresh();
    }
    model.cleanup();
    renderer.cleanup();
  }
}
