package us.itshighnoon.aventurine;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import us.itshighnoon.aventurine.render.Camera;
import us.itshighnoon.aventurine.render.Model;
import us.itshighnoon.aventurine.render.Renderer;
import us.itshighnoon.aventurine.util.DisplayManager;
import us.itshighnoon.aventurine.util.Logger;

public class Game {
  public void run() {
    Renderer renderer = new Renderer();
    Model model = Model.loadAll("res/ignore/qq/qq.fbx");
    Camera camera = new Camera(0.1f, 100.0f, (float)Math.toRadians((double)90.0f));
    camera.position.x = -78.67357f * 10000.0f / 0.819f;
    camera.position.z = -35.77233f * 10000.0f;
    camera.position.y = 30.0f;
    camera.rotation.x = (float)Math.toRadians(-90.0);
    DisplayManager.setCamera(camera);
    Vector3f modelRotation = new Vector3f((float)Math.toRadians(-90.0f), 0.0f, 0.0f);
    
    List<Vector3f> positions = new ArrayList<Vector3f>();
    try {
      InputStream ncIn = new FileInputStream("res/ignore/map.osm");
      XMLInputFactory factory = XMLInputFactory.newFactory();
      XMLStreamReader xmlReader = factory.createXMLStreamReader(ncIn);
      while (xmlReader.hasNext()) {
        int eventType = xmlReader.next();
        if (eventType == XMLStreamConstants.START_ELEMENT && xmlReader.getLocalName().equals("node")) {
          float latitude = Float.parseFloat(xmlReader.getAttributeValue(null, "lat"));
          float longitude = Float.parseFloat(xmlReader.getAttributeValue(null, "lon"));
          positions.add(new Vector3f(longitude * 10000.0f / 0.819f, 0.0f, -latitude * 10000.0f));
        }
      }
    } catch (Exception e) {
      Logger.log("0030 Problem loading map", Logger.Severity.ERROR);
      e.printStackTrace();
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
      for (Vector3f position : positions) {
        renderer.render(camera, position, modelRotation, model);
      }
      DisplayManager.refresh();
    }
    model.cleanup();
    renderer.cleanup();
  }
}
