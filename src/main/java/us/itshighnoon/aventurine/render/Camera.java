package us.itshighnoon.aventurine.render;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import us.itshighnoon.aventurine.util.Logger;

public class Camera {
  public Vector3f position;
  public Vector3f rotation;
  
  private Matrix4f projectionMatrix;
  private FrustumIntersection frustum;
  private float aspectRatio;
  private float nearPlane;
  private float farPlane;
  private float fov;
  
  public Camera(float nearPlane, float farPlane, float fov) {
    this.nearPlane = nearPlane;
    this.farPlane = farPlane;
    this.fov = fov;
    this.aspectRatio = 1.0f;
    this.position = new Vector3f(0.0f, 0.0f, 0.0f);
    this.rotation = new Vector3f(0.0f, 0.0f, 0.0f);
    this.projectionMatrix = new Matrix4f();
    recalculateFrustum();
  }
  
  public void setNearPlane(float nearPlane) {
    Logger.log("0018 Adjusting near plane not recommended", Logger.Severity.WARN);
    this.nearPlane = nearPlane;
  }
  
  public void setFarPlane(float farPlane) {
    Logger.log("0018 Adjusting far plane not recommended", Logger.Severity.WARN);
    this.farPlane = farPlane;
  }
  
  public void setFov(float fov) {
    this.fov = fov;
  }
  
  public void recalculateProjection(int screenWidth, int screenHeight) {
    this.aspectRatio = (float)screenWidth / (float)screenHeight;
    this.projectionMatrix.setPerspective(fov, aspectRatio, nearPlane, farPlane);
  }
  
  public Matrix4f getCameraMatrix() {
    Matrix4f viewMatrix = new Matrix4f();
    viewMatrix.rotate(-rotation.z, 0.0f, 0.0f, 1.0f);
    viewMatrix.rotate(-rotation.x, 1.0f, 0.0f, 0.0f);
    viewMatrix.rotate(-rotation.y, 0.0f, 1.0f, 0.0f);
    viewMatrix.translate(-position.x, -position.y, -position.z);
    projectionMatrix.mul(viewMatrix, viewMatrix); // avoid recalculating proj matrix every frame
    return viewMatrix;
  }
  
  public void recalculateFrustum() {
    this.frustum = new FrustumIntersection(getCameraMatrix());
  }
  
  public boolean canSeeAab(Vector3f min, Vector3f max) {
    return frustum.testAab(min, max);
  }
  
  public boolean canSeeAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    return frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ);
  }
}
