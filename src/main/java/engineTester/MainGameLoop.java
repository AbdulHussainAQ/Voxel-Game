package engineTester;

import entities.Camera;
import entities.ChunkEntity;
import entities.Light;
import entities.Player;
import lombok.Getter;
import models.RawModelPool;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import textures.ModelTexture;
import toolbox.MousePicker;
import world.Location;
import world.World;
import world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public class MainGameLoop {

    @Getter
    private static ModelTexture modelTexture;
    @Getter
    private static final int RENDERDISTANCE = 16;

    public static void main(String[] args) {

        DisplayManager.createDisplay();
        Loader loader = new Loader();
        modelTexture = new ModelTexture(loader.loadTexture("atlas"));
        modelTexture.setNumberOfRows(32);


        RawModelPool rawModelPool = new RawModelPool(loader);



        ChunkEntity[] chunks = new ChunkEntity[(MainGameLoop.getRENDERDISTANCE()*2)*((MainGameLoop.getRENDERDISTANCE()*2))];


        List<Light> lights = new ArrayList<>();
        Light sun = new Light(new Location(100, 100000, -100), new Vector3f(1f, 1f, 1f));



        lights.add(sun);

        MasterRenderer renderer = new MasterRenderer(loader);

        Player player = new Player(null, new Location(5, 3, 5), 0, 0, 0, 1f);
        World world = new World(player, chunks, rawModelPool, loader);


        final double[] x = {0};
        final double[] z = {0};
        new Thread(() ->{
            for (x[0] = player.getMinXValue() - (Chunk.getCHUNK_LENGTH() * RENDERDISTANCE); x[0] < player.getMinXValue() + (Chunk.getCHUNK_LENGTH() * RENDERDISTANCE); x[0] += Chunk.getCHUNK_LENGTH()) {
                for (z[0] = player.getMinZValue() - (Chunk.getCHUNK_WIDTH() * RENDERDISTANCE); z[0] < player.getMinZValue() + (Chunk.getCHUNK_WIDTH() * RENDERDISTANCE); z[0] += Chunk.getCHUNK_WIDTH()) {
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
        player.setCamera(camera);
        MousePicker mousePicker = new MousePicker(camera, renderer.getProjectionMatrix());
        int num = 0;
        long lastTime =System.currentTimeMillis();
        while (!Display.isCloseRequested()) {



            player.move();
            camera.move();
            mousePicker.update();

            for(ChunkEntity c: chunks){
                if(c == null)continue;
                renderer.processEntity(c);

            }
            world.update();
            num++;
            if(System.currentTimeMillis() - lastTime >= 1000){
                //Display.setTitle("FPS: "+num);
                lastTime = System.currentTimeMillis();
                num = 0;

            }
            // Measure speed



            renderer.renderScene(lights, camera);

            DisplayManager.updateDisplay();
        }

        //*********Clean Up Below**************
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();

    }


}
