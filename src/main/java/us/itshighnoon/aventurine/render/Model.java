package us.itshighnoon.aventurine.render;

import java.nio.file.Path;
import java.util.Arrays;

import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import us.itshighnoon.aventurine.util.Logger;

public class Model {
  private Mesh[] meshes;
  private Material[] materials;

  private Model(Mesh[] mesh, Material[] material) {
    this.meshes = mesh;
    this.materials = material;
    Arrays.sort(this.meshes);
  }

  public static Model loadAll(String modelPath) {
    String texturePath = Path.of(modelPath).getParent().toString();
    AIScene aiScene = Assimp.aiImportFile(modelPath,
        Assimp.aiProcess_JoinIdenticalVertices | Assimp.aiProcess_Triangulate | Assimp.aiProcess_FixInfacingNormals);
    if (aiScene == null) {
      Logger.log("0017 Assimp failed to load file " + modelPath, Logger.Severity.ERROR);
      return null;
    }
    if (aiScene.mNumMeshes() <= 0) {
      Logger.log("0019 File " + modelPath + " contains no meshes", Logger.Severity.ERROR);
    }
    if (aiScene.mNumMaterials() <= 0) {
      Logger.log("0022 File " + modelPath + " contains no materials", Logger.Severity.WARN);
    }
    Mesh[] meshes = new Mesh[aiScene.mNumMeshes()];
    for (int meshIdx = 0; meshIdx < aiScene.mNumMeshes(); meshIdx++) {
      meshes[meshIdx] = new Mesh(AIMesh.create(aiScene.mMeshes().get(meshIdx)));
    }
    Material[] materials = new Material[aiScene.mNumMaterials()];
    for (int matIdx = 0; matIdx < aiScene.mNumMaterials(); matIdx++) {
      materials[matIdx] = new Material(AIMaterial.create(aiScene.mMaterials().get(matIdx)), texturePath);
    }

    return new Model(meshes, materials);
  }

  public void render() {
    int currentMaterial = -1;
    for (Mesh mesh : meshes) {
      if (materials[mesh.getMaterialIdx()].getTexture() == 0) {
        // TODO remove... test model has some nonsense going on
        continue;
      }
      if (mesh.getMaterialIdx() != currentMaterial) {
        currentMaterial = mesh.getMaterialIdx();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, materials[currentMaterial].getTexture());
      }
      GL30.glBindVertexArray(mesh.getVao());
      GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
    }
  }

  public void cleanup() {
    for (Mesh mesh : meshes) {
      mesh.cleanup();
    }
    for (Material material : materials) {
      material.cleanup();
    }
  }
}
