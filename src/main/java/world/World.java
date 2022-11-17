package world;

import engineTester.MainGameLoop;
import entities.ChunkEntity;
import entities.Entity;
import entities.Player;
import lombok.Getter;
import models.RawModelPool;
import models.TexturedModel;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import world.chunk.Chunk;
import world.worldgen.Noise;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class World {


    private static ConcurrentLinkedQueue<Chunk> chunks;
    private static ConcurrentLinkedQueue<Chunk> chunksLoaded;
    private static ConcurrentLinkedQueue<Chunk> chunksUnLoaded;
    protected static ArrayBlockingQueue<ChunkEntity> chunksToRender;
    private static ArrayBlockingQueue<Chunk> chunksToLoad;

    private static ArrayBlockingQueue<Chunk> chunksToCompute;

    public static LinkedBlockingQueue<Chunk> chunksToMesh;

    private static ArrayBlockingQueue<Chunk> chunksToSave;

    private static ArrayBlockingQueue<Chunk> chunksToUnload;


    public static boolean done = false;
    public static boolean doneRendering;

    private RawModelPool rawModelPool;

    private static Loader loader;

    private static Player player;

    @Getter
    private static Noise noise;


    protected static Entity[] entities;

    private static int index = 0;

    private BlockRenderer renderer;

    private BlockComputer blockComputer;

    private ChunkComputer chunkComputer;
    private ChunkSaver chunkSaver;
    private ChunkLoadingDistributor chunkLoader;


    public World(Player player, Entity[] entities, RawModelPool pool, Loader loader) {
        World.loader = loader;
        World.player = player;
        World.entities = entities;
        this.rawModelPool = pool;
        noise = new Noise(new Random().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE));
        chunksLoaded = new ConcurrentLinkedQueue<>();
        chunksToSave = new ArrayBlockingQueue<>(5000);
        chunksUnLoaded = new ConcurrentLinkedQueue<>();
        chunksToLoad = new ArrayBlockingQueue<>(5000);
        chunks = new ConcurrentLinkedQueue<>();
        chunksToMesh = new LinkedBlockingQueue<>();
        chunksToRender = new ArrayBlockingQueue<>(5000);
        chunksToCompute = new ArrayBlockingQueue<>(5000);
        chunksToUnload = new ArrayBlockingQueue<>(5000);
        renderer = new BlockRenderer();
        blockComputer = new BlockComputer();
        chunkComputer = new ChunkComputer();
        chunkSaver = new ChunkSaver();
        chunkLoader = new ChunkLoadingDistributor();
        doneRendering = false;





        //System.out.println("E");
    }

    private void render(){
        if(chunksToRender.size() > 0){
            renderer.run();
        }
        if(chunksToLoad.size() > 0){
            blockComputer.run();
        }
        if(chunksToCompute.size() > 0){
            chunkComputer.run();
        }
        if(chunksToSave.size() > 0){
            chunkSaver.run();
        }
        if(!World.MeshRenderer.isEmpty()){
            World.MeshRenderer.run();
        }
        //chunkLoader.run();
    }

    public void update(){
        render();
    }


    public static void createChunk(Chunk chunk) {

        chunks.add(chunk);
    }

    public static void computeChunk(Chunk chunk){
        chunksToCompute.add(chunk);
    }

    public static void addChunk(Chunk chunk) {
        chunksToLoad.add(chunk);
    }

    public static void renderChunk(ChunkEntity entity) {
        chunksToRender.add(entity);
    }

    public static Chunk getChunkAt(int x, int z) {

        Chunk[] chunkCollected = new Chunk[1];
        chunks.stream().filter(c -> (c.MIN_X == x && c.MIN_Z == z)).forEach(c -> chunkCollected[0] = c);


        return chunkCollected[0];
    }

    public static Chunk getChunk(float x, float z){
        final Chunk[] chunk = new Chunk[1];

        chunks.forEach(c -> {
            if(c.containsCoords(x,z)){
                chunk[0] = c;
            }
        });
        return chunk[0];
    }


    public static boolean unloadChunk(ChunkEntity entity){
        for(int i =0; i<entities.length;i++){
            if(entities[i] == entity){
                entities[i] = null;
                return true;
            }
        }
        return false;
    }





    public static class MeshRenderer {

        private static final long TIMEOUT = 0;
        private static long lastTime = System.currentTimeMillis();
        private static int done = 0;
        private static long s = System.currentTimeMillis();
        public static void run() {


                if(System.currentTimeMillis() - lastTime > 2000 && chunksToRender.size() == 0){
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }


            if(System.currentTimeMillis() - lastTime > TIMEOUT){
                Chunk chunk = chunksToMesh.poll();
                if (chunk != null) {
                    //System.out.println("RENDERING");
                    long start = System.nanoTime();
                    blocks.BlockRenderer renderer = chunk.getBlockRenderer();
                    if(!(renderer.getPositions() == null || renderer.getTextureCoords() == null || renderer.getIndices() == null)){
                        renderer.setTexturedModel(new TexturedModel(loader.loadToVAO(renderer.getPositions(), renderer.getTextureCoords(),renderer.getIndices(), chunk), blocks.BlockRenderer.getTexture()));

                        computeChunk(chunk);
                        done++;
                        if(done == 1){
                            s = System.currentTimeMillis();
                        }
                        if (done == ((MainGameLoop.getRENDERDISTANCE()*2)*((MainGameLoop.getRENDERDISTANCE()*2)))){
                            System.out.println("Rendered "+(MainGameLoop.getRENDERDISTANCE()*2)*((MainGameLoop.getRENDERDISTANCE()*2))+" chunks in "+((System.currentTimeMillis() - s) / 1000)+" seconds");

                        }
                        lastTime = System.currentTimeMillis();

                    }


                }

            }


        }


        public static boolean isEmpty(){
            return chunksToMesh.size() == 0;
        }

    }


    private static class BlockComputer implements Runnable{
        private static final long TIMEOUT = 0;
        private long lastTime = System.currentTimeMillis();

        @Override
        public void run() {

                if(System.currentTimeMillis() - lastTime > 2000 && chunksToRender.size() == 0){
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }


                if(System.currentTimeMillis() - lastTime > TIMEOUT){

                        Chunk chunk = chunksToLoad.poll();
                        if(chunk != null){
                            chunk.addBlocks();



                        lastTime = System.currentTimeMillis();
                    }
                }
        }
    }

    public static boolean chunkExists(int minX, int minZ) {
        List<Chunk> chunk = chunks.stream().filter(c -> (c.MIN_X == minX && c.MIN_Z == minZ)).toList();
        return chunk.size() != 0;
    }


    private static class BlockRenderer implements Runnable{

        private long lastTime = System.currentTimeMillis();
        private static final long TIMEOUT = 10;

        private int c = 0;

        @Override
        public void run() {

                if(System.currentTimeMillis() - lastTime > 2000 && chunksToRender.size() == 0){
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }


                if(System.currentTimeMillis() - lastTime > TIMEOUT){
                            ChunkEntity entity = chunksToRender.poll();
                            if (entity != null) {
                                entity.getChunk().setRendered(true);
                                for(int j=0; j<entities.length;j++){
                                    if(entities[j] == null){
                                        entities[j] = entity;
                                        c++;
                                        break;
                                    }
                                }
                                if(c == ((MainGameLoop.getRENDERDISTANCE()*2)*((MainGameLoop.getRENDERDISTANCE()*2)))){
                                    System.out.println("DONE");;
                                    doneRendering = true;
                                }
                                for (Chunk chunk : chunks) {
                                    if(chunksLoaded.contains(chunk)) continue;
                                    if (chunksUnLoaded.contains(chunk)) continue;
                                    if (chunk.getChunkUUID() == entity.getChunkUUID()) {
                                        chunksLoaded.add(chunk);

                                    }
                                }
                                Arrays.stream(entities).forEach(e ->{
                                    if(e != null){
                                        ChunkEntity chunkEntity = (ChunkEntity) e;
                                        Chunk chunk = chunkEntity.getChunk();
                                        if(!chunks.contains(chunk)){
                                            chunks.add(chunk);
                                        }
                                    }
                                });
                                chunksToSave.add(entity.getChunk());




                    }
                    lastTime = System.currentTimeMillis();
                }





        }
    }

    private static class ChunkComputer implements Runnable{

        private long lastTime = System.currentTimeMillis();
        private static final long TIMEOUT = 0;

        @Override
        public void run() {

                if(System.currentTimeMillis() - lastTime > 2000 && chunksToRender.size() == 0){
                    try {
                        Thread.sleep(80);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }


                if(System.currentTimeMillis() - lastTime > TIMEOUT){
                    Chunk chunk = chunksToCompute.poll();
                    if(chunk != null){
                        chunk.getBlockRenderer().renderChunk();
                        lastTime = System.currentTimeMillis();
                    }
                }


        }
    }

    public static class ChunkUnloader{

        public void run(){
            Chunk chunk = chunksToUnload.poll();
            if(chunk != null){
                loader.unloadChunk(chunk);
            }
        }

        public boolean isEmpty(){
            return chunksToUnload.size() == 0;
        }




    }

    //
    public static class ChunkSaver implements Runnable{

        //private static final long TIMEOUT = 10;

        //private long lastTime = System.currentTimeMillis();

        @Override
        public void run() {
            /*


                if(System.currentTimeMillis() - lastTime > 2000 && chunksToSave.size() == 0){
                    try {
                        Thread.sleep(90);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(System.currentTimeMillis() - lastTime >= TIMEOUT){
                    Chunk chunk = chunksToSave.poll();
                    if(chunk != null){
                        ChunkHandler.saveChunk(chunk);
                        lastTime = System.currentTimeMillis();
                    }

                }

        }

             */
        }
    }


    public static boolean enableChunkLoading = false;


    private static class ChunkLoadingDistributor implements Runnable{

        private static final long INITIALTIMEOUT = 3000;
        private long lastTime = System.currentTimeMillis();
        private static final long TIMEOUT = 0;
        private final long initialTime = System.currentTimeMillis();
        @Override
        public void run() {
                    if (System.currentTimeMillis() - initialTime > INITIALTIMEOUT){
                        if(System.currentTimeMillis() - lastTime > TIMEOUT){
                            double minX = player.getMinXValue();
                            double minZ = player.getMinZValue();
                            CopyOnWriteArrayList<Chunk> newChunks = new CopyOnWriteArrayList<>();
                            for (int x = (int) (minX - (Chunk.getCHUNK_LENGTH() * 8)); x < minX + (Chunk.getCHUNK_LENGTH() * 8); x += Chunk.getCHUNK_LENGTH()) {
                                for (int z = (int) (minZ - (Chunk.getCHUNK_WIDTH() * 8)); z < minZ + (Chunk.getCHUNK_WIDTH() * 8); z += Chunk.getCHUNK_WIDTH()) {
                                    Chunk chunk = World.getChunkAt(x, z);



                                    if(chunk == null){

                                        Chunk chunk1 = new Chunk(noise, x, z);


                                        chunk1.lazyLoadSurroundingChunks();
                                        newChunks.add(chunk1);
                                        addChunk(chunk1);

                                    }else{

                                        if(chunk.isLazy()){
                                            chunk.lazyLoadSurroundingChunks();
                                            chunk.computeBlocks();

                                            addChunk(chunk);
                                        }

                                        newChunks.add(chunk);

                                    }

                                }
                            }

                            chunks.forEach(chunk -> {
                                if(chunk != null){
                                    if(!newChunks.contains(chunk) && !chunk.isLazy()){
                                        for(int i =0; i<entities.length;i++){
                                            if(entities[i] == chunk.getEntity()){
                                                entities[i] = null;
                                                chunks.remove(chunk);
                                                chunksLoaded.remove(chunk);
                                                chunksToUnload.add(chunk);
                                            }
                                        }
                                    }
                                }
                            });

                            lastTime = System.currentTimeMillis();
                        }


                    }



            }
        }



}









