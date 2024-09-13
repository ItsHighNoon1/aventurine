package us.itshighnoon.aventurine.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.render.mem.Mesh;
import us.itshighnoon.aventurine.render.shader.LineShader;

public class LineRenderer {
  private LineShader lineShader;

  public LineRenderer() {
    this.lineShader = new LineShader();
  }

  public void prepare() {
    GL11.glDisable(GL11.GL_DEPTH_TEST);
  }

  public void render(Camera camera, Vector3f position, Vector3f rotation, Mesh line, Vector3f color) {
    Matrix4f mvpMatrix = new Matrix4f();
    mvpMatrix.setTranslation(position);
    mvpMatrix.rotate(rotation.z, 0.0f, 0.0f, 1.0f);
    mvpMatrix.rotate(rotation.x, 1.0f, 0.0f, 0.0f);
    mvpMatrix.rotate(rotation.y, 0.0f, 1.0f, 0.0f);
    camera.getCameraMatrix().mul(mvpMatrix, mvpMatrix);
    lineShader.bind();
    lineShader.setColor(color);
    lineShader.setMvpMatrix(mvpMatrix);
    GL30.glBindVertexArray(line.getVao());
    GL11.glDrawArrays(line.getMode(), 0, line.getVertexCount());
    GL30.glBindVertexArray(0);
  }

  public void cleanup() {
    lineShader.cleanup();
  }
}
