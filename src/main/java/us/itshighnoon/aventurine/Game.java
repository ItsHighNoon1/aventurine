package us.itshighnoon.aventurine;

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
    while (!DisplayManager.closeRequested()) {
      renderer.prepare();
      renderer.render(model);
      DisplayManager.refresh();
    }
    model.cleanup();
    renderer.cleanup();
  }
}
