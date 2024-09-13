package us.itshighnoon.aventurine.render;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import us.itshighnoon.aventurine.render.mem.Mesh;
import us.itshighnoon.aventurine.render.mem.Model;
import us.itshighnoon.aventurine.ui.GuiNode;

public class MasterRenderer {
  private TestRenderer testRenderer;
  private GuiRenderer guiRenderer;
  private LineRenderer lineRenderer;
  private Camera camera;
  
  public MasterRenderer() {
    this.testRenderer = new TestRenderer();
    this.guiRenderer = new GuiRenderer();
    this.lineRenderer = new LineRenderer();
    this.camera = new Camera(0.1f, 100.0f, (float) Math.toRadians(90.0));
    GL11.glClearColor(0.05f, 0.05f, 0.05f, 1.0f);
    GL11.glEnable(GL11.GL_CULL_FACE);
    GL11.glCullFace(GL11.GL_BACK);
    GL11.glEnable(GL13.GL_MULTISAMPLE);
  }
  
  public void setCamera(Camera camera) {
    this.camera = camera;
  }
  
  public void prepare() {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
  }
  
  public void submitTest(Model model, Vector3f position, Vector3f rotation) {
    testRenderer.prepare();
    testRenderer.render(camera, position, rotation, model);
  }
  
  public void submitGui(GuiNode gui) {
    guiRenderer.prepare();
    guiRenderer.render(gui);
  }
  
  public void submitLine(Mesh road, Vector3f position, Vector3f rotation) {
    submitLine(road, position, rotation, new Vector3f(0.0f, 0.0f, 1.0f));
  }
  
  public void submitLine(Mesh line, Vector3f position, Vector3f rotation, Vector3f color) {
    lineRenderer.prepare();
    lineRenderer.render(camera, position, rotation, line, color);
  }
  
  public void cleanup() {
    testRenderer.cleanup();
    guiRenderer.cleanup();
    lineRenderer.cleanup();
  }
}
