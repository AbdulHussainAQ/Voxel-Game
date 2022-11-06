package world;

import entities.ChunkEntity;
import entities.Entity;
import entities.Player;
import lombok.Getter;
import models.RawModelPool;
import models.TexturedModel;
import renderEngine.Loader;
import world.chunk.Chunk;
import world.worldgen.Noise;
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

    private RawModelPool rawModelPool;

    private static Loader loader;

    private static Player player;

    @Getter
    private static Noise noise;


    protected static CopyOnWriteArrayList<Entity> entities;

    public World(Player player, CopyOnWriteArrayList<Entity> entities, RawModelPool pool, Loader loader) {
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
        BlockRenderer renderer = new BlockRenderer();
        Thread t1 = new Thread(renderer);
        t1.start();
        BlockComputer blockComputer = new BlockComputer();
        Thread t2 = new Thread(blockComputer);
        t2.start();
        ChunkComputer chunkComputer = new ChunkComputer();
        Thread t3 = new Thread(chunkComputer);
        t3.start();
        ChunkSaver chunkSaver = new ChunkSaver();
        Thread t4 = new Thread(chunkSaver);
        t4.start();

        ChunkLoadingDistributor chunkLoader = new ChunkLoadingDistributor();
        Thread t5 = new Thread(chunkLoader);
        t5.start();



        //System.out.println("E");
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





    public static class MeshRenderer {

        private static final long TIMEOUT = 50;
        private long lastTime = System.currentTimeMillis();
        private int done = 0;
        private long s = System.currentTimeMillis();
        public void run() {


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
                    renderer.setTexturedModel(new TexturedModel(loader.loadToVAO(renderer.getPositions(), renderer.getTextureCoords(),renderer.getIndices(), chunk), blocks.BlockRenderer.getTexture()));

                    computeChunk(chunk);
                    done++;
                    if(done == 1){
                        s = System.currentTimeMillis();
                    }
                    if (done == 100) {
                        System.out.println("Renderer 100 chunks in "+((System.currentTimeMillis() - s) / 1000)+" seconds");
                    }
                    lastTime = System.currentTimeMillis();
                    System.out.println("DONE "+done+": "+(System.nanoTime()-start)/1000000000f);

                }

            }


        }


        public boolean isEmpty(){
            return chunksToMesh.size() == 0;
        }

    }


    private static class BlockComputer implements Runnable {
        private static final long TIMEOUT = 0;
        private long lastTime = System.currentTimeMillis();
        @Override
        public void run() {



            while (true) {



                if(System.currentTimeMillis() - lastTime > 2000 && chunksToRender.size() == 0){
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }


                if(System.currentTimeMillis() - lastTime > TIMEOUT){
                    if(chunksToLoad.size() >= 10){
                        for(int i =0; i<10;i++){
                            Chunk chunk = chunksToLoad.poll();
                            if(chunk != null){
                                chunk.addBlocks();
                            }
                        }

                        lastTime = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    public static boolean chunkExists(int minX, int minZ) {
        List<Chunk> chunk = chunks.stream().filter(c -> (c.MIN_X == minX && c.MIN_Z == minZ)).toList();
        return chunk.size() != 0;
    }


    private static class BlockRenderer implements Runnable {

        private long lastTime = System.currentTimeMillis();
        private static final long TIMEOUT = 30;
        @Override
        public void run() {


            while (true) {


                if(System.currentTimeMillis() - lastTime > 2000 && chunksToRender.size() == 0){
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }


                if(System.currentTimeMillis() - lastTime > TIMEOUT){
                    if(chunksToRender.size() >= 10){
                        for(int i =0; i<10;i++){
                            ChunkEntity entity = chunksToRender.poll();
                            if (entity != null) {
                                entity.getChunk().setRendered(true);
                                entities.add(entity);
                                for (Chunk chunk : chunks) {
                                    if(chunksLoaded.contains(chunk)) continue;
                                    if (chunksUnLoaded.contains(chunk)) continue;
                                    if (chunk.getChunkUUID() == entity.getChunkUUID()) {
                                        chunksLoaded.add(chunk);

                                    }
                                }
                                entities.forEach(e ->{
                                    ChunkEntity chunkEntity = (ChunkEntity) e;
                                    Chunk chunk = chunkEntity.getChunk();
                                    if(!chunks.contains(chunk)){
                                        chunks.add(chunk);
                                    }
                                });
                                chunksToSave.add(entity.getChunk());


                            }
                        }
                        lastTime = System.currentTimeMillis();
                    }
                }


            }


        }
    }

    private static class ChunkComputer implements Runnable{

        private long lastTime = System.currentTimeMillis();
        private static final long TIMEOUT = 20;

        @Override
        public void run() {
            while (true){



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
    }

    public static class ChunkUnloader{

        public void run(){
            Chunk chunk = chunksToUnload.poll();
            if(chunk != null){
                loader.unloadChunk(chunk);
            }
        }

    }

    //
    public static class ChunkSaver implements Runnable {

        //private static final long TIMEOUT = 10;

        //private long lastTime = System.currentTimeMillis();

        @Override
        public void run() {
            /*
            while (true){

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
        }

             */
        }
    }


    public static boolean enableChunkLoading = false;


    private static class ChunkLoadingDistributor implements Runnable{

        private static final long INITIALTIMEOUT = 10000;
        private long lastTime = System.currentTimeMillis();
        private static final long TIMEOUT = 0;
        private final long initialTime = System.currentTimeMillis();

        @Override
        public void run() {
            while (true){
                    if (System.currentTimeMillis() - initialTime > INITIALTIMEOUT){
                        if(System.currentTimeMillis() - lastTime > TIMEOUT){
                            double minX = player.getMinXValue();
                            double minZ = player.getMinZValue();
                            CopyOnWriteArrayList<Chunk> newChunks = new CopyOnWriteArrayList<>();
                            for (int x = (int) (minX - (Chunk.getCHUNK_LENGTH() * 5)); x < minX + (Chunk.getCHUNK_LENGTH() * 5); x += Chunk.getCHUNK_LENGTH()) {
                                for (int z = (int) (minZ - (Chunk.getCHUNK_WIDTH() * 5)); z < minZ + (Chunk.getCHUNK_WIDTH() * 5); z += Chunk.getCHUNK_WIDTH()) {
                                    Chunk chunk = World.getChunkAt(x, z);



                                    if(chunk == null){

                                        //Chunk chunk1 = new Chunk(noise, x, z);
                                       // newChunks.add(chunk1);


                                        //chunk1.lazyLoadSurroundingChunks();
                                        //chunk1.computeBlocks();
                                        //addChunk(chunk1);



                                    }else{

                                        if(chunk.isLazy()){
                                            //chunk.computeBlocks();
                                            //addChunk(chunk);
                                            //System.out.println("SLA");
                                        }

                                        newChunks.add(chunk);

                                    }

                                }
                            }

                            chunks.forEach(chunk -> {
                                if(chunk != null){
                                    if(!newChunks.contains(chunk) && !chunk.isLazy()){
                                        entities.remove(chunk.getEntity());
                                        chunks.remove(chunk);
                                        chunksLoaded.remove(chunk);
                                        chunksToUnload.add(chunk);

                                    }
                                }

                            });
                            lastTime = System.currentTimeMillis();
                        }


                    }


                }
            }
        }



}









