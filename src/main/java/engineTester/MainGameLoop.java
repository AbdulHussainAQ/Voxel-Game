package engineTester;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import lombok.Getter;
import models.RawModelPool;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import textures.ModelTexture;
import world.Location;
import world.World;
import world.chunk.Chunk;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainGameLoop {

    @Getter
    private static ModelTexture modelTexture;

    public static void main(String[] args) {

        DisplayManager.createDisplay();
        Loader loader = new Loader();
        modelTexture = new ModelTexture(loader.loadTexture("atlas"));
        modelTexture.setUseFakeLighting(true);
        modelTexture.setNumberOfRows(16);


        RawModelPool rawModelPool = new RawModelPool(loader);



        CopyOnWriteArrayList<Entity> entities = new CopyOnWriteArrayList<>();
        List<Entity> normalMapEntities = new ArrayList<>();



        List<Light> lights = new ArrayList<>();
        Light sun = new Light(new Location(100, 100000, -100), new Vector3f(1f, 1f, 1f));



        lights.add(sun);

        MasterRenderer renderer = new MasterRenderer(loader);

        Player player = new Player(null, new Location(5, 3, 5), 0, 0, 0, 1f);
        World world = new World(player, entities, rawModelPool, loader);

        final double[] x = {0};
        final double[] z = {0};
        new Thread(() ->{
            for (x[0] = player.getMinXValue() - (Chunk.getCHUNK_LENGTH() * 5); x[0] < player.getMinXValue() + (Chunk.getCHUNK_LENGTH() * 5); x[0] += Chunk.getCHUNK_LENGTH()) {
                for (z[0] = player.getMinZValue() - (Chunk.getCHUNK_WIDTH() * 5); z[0] < player.getMinZValue() + (Chunk.getCHUNK_WIDTH() * 5); z[0] += Chunk.getCHUNK_WIDTH()) {
                    Chunk chunk = World.getChunkAt((int) x[0], (int) z[0]);

                    if (chunk != null) {
                        World.addChunk(chunk);
                        chunk.lazyLoadSurroundingChunks();

                    } else {


                        Chunk chunk1 = new Chunk(World.getNoise(),(int) x[0], (int) z[0]);
                        if (!chunk1.blocksComputed) {

                            chunk1.computeBlocks();
                        }

                        World.addChunk(chunk1);
                        chunk1.lazyLoadSurroundingChunks();

                    }

                }
            }
        }).start();

        Camera camera = new Camera(player);
        List<GuiTexture> guiTextures = new ArrayList<>();
        GuiRenderer guiRenderer = new GuiRenderer(loader);
        //MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);

        //**********Water Renderer Set-up************************

		/*WaterFrameBuffers buffers = new WaterFrameBuffers();
		WaterShader waterShader = new WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
		List<WaterTile> waters = new ArrayList<WaterTile>();
		WaterTile water = new WaterTile(75, -75, 0);
		waters.add(water);*/

        //****************Game Loop Below*********************

		/*
		for(Chunk chunk : chunks){
			chunk.updateChunk();
			break;
		}

		 */


        World.MeshRenderer meshRenderer = new World.MeshRenderer();
        World.ChunkUnloader chunkUnloader = new World.ChunkUnloader();
        long prevTime = System.currentTimeMillis();
        int counter = 60;

        long timeOut = 10000;
        long lastTime =System.currentTimeMillis();
        while (!Display.isCloseRequested()) {

            player.move();
            camera.move();
            chunkUnloader.run();

            // Measure speed
            if(!meshRenderer.isEmpty()){
                meshRenderer.run();
            }

            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - prevTime;

            counter++;
            if (counter == DisplayManager.FPS_CAP) {
                int fps = (int) ((timeDiff / 1000f) * DisplayManager.FPS_CAP);
                //System.out.println("FPS: "+fps);;
                prevTime = currentTime;
                counter = 0;
            }












			
			/*//render reflection texture
			buffers.bindReflectionFrameBuffer();
			float distance = 2 * (camera.getPosition().y - water.getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, 1, 0, -water.getHeight()+1));
			camera.getPosition().y += distance;
			camera.invertPitch();
			
			//render refraction texture
			buffers.bindRefractionFrameBuffer();
			renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, -1, 0, water.getHeight()));
			a
			//render to screen
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			buffers.unbindCurrentFrameBuffer();
			*/


            renderer.renderScene(entities, normalMapEntities, lights, camera, new Vector4f(0, -1, 0, 100000));
            //waterRenderer.render(waters, camera, sun);
            //guiRenderer.render(guiTextures);

            DisplayManager.updateDisplay();

        }

        //*********Clean Up Below**************

        guiRenderer.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();

    }


}
