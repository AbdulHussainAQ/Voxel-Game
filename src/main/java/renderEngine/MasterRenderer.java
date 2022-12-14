package renderEngine;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import shaders.StaticShader;

import java.util.*;


public class MasterRenderer {

	private static final float FOV = 110;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 500f;

	//SKY COLOURS
	public static final float RED = 0f;
	public static final float GREEN = 0.764f;
	public static final float BLUE = 1f;

	private Matrix4f projectionMatrix;

	private StaticShader shader = new StaticShader();
	private Renderer renderer;







	private Map<TexturedModel, List<Entity>> entities = new HashMap<>();
	private Map<TexturedModel, List<Entity>> normalMapEntities = new HashMap<>();


	public MasterRenderer(Loader loader) {
		enableCulling();
		createProjectionMatrix();
		renderer = new Renderer(shader, projectionMatrix);


	}

	public Matrix4f getProjectionMatrix() {
		return this.projectionMatrix;
	}

	public void renderScene(List<Light> lights,
							Camera camera) {
		render(lights, camera);
	}

	public void render(List<Light> lights, Camera camera) {
		prepare();

		shader.start();

		shader.loadLights(lights);
		shader.loadViewMatrix(camera);
		renderer.render(entities);
		shader.stop();
		entities.clear();
	}

	public static void enableCulling() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);

	}

	public static void disableCulling() {
		GL11.glDisable(GL11.GL_CULL_FACE);
	}


	public void processEntity(Entity entity) {

		TexturedModel entityModel = entity.getModel();
		LinkedList<Entity> batch = (LinkedList<Entity>) entities.get(entityModel);
		if (batch != null) {
			batch.add(entity);
		} else {
			LinkedList<Entity> newBatch = new LinkedList<>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}

	}



	public void cleanUp() {
		shader.cleanUp();
	}

	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(RED, GREEN, BLUE, 1);

	}

	private void createProjectionMatrix() {
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
	}

}
