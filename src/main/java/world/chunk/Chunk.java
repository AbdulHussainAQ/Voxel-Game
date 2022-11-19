package world.chunk;

import blocks.Block;
import blocks.Block.FACE;
import blocks.Block.Material;
import blocks.BlockRenderer;
import entities.ChunkEntity;
import lombok.Getter;
import lombok.Setter;
import world.Location;
import world.World;
import world.worldgen.Noise;

import java.io.Serializable;
import java.util.UUID;

import static blocks.Block.FACE.*;
import static blocks.Block.Material.AIR;

public class Chunk implements Serializable{

    @Getter
    public UUID chunkUUID;
    @Getter
    private static final int CHUNK_LENGTH = 16;
    @Getter
    private static final int CHUNK_WIDTH = 16;
    @Getter
    private static final int CHUNK_HEIGHT = 16;

    @Getter
    public final int MIN_X;
    @Getter
    public final int MIN_Z;

    private int[][][] blocksInChunk;

    @Getter @Setter
    private ChunkEntity entity;

    private Noise noise;

    @Getter
    private boolean isLazy;

    public boolean blocksComputed;

    private BlockRenderer blockRenderer;
    @Getter
    private int maxBlockHeight;

    @Getter @Setter
    private boolean isRendered;

    public Chunk(Noise noise, int min_x, int min_z) {


        MIN_X = min_x;
        MIN_Z = min_z;
        chunkUUID = UUID.randomUUID();
        maxBlockHeight = 0;
        blocksComputed = false;
        isRendered = false;
        this.noise = noise;
        this.isLazy = true;
        computeBlocks();
        World.createChunk(this);








    }





    public void lazyLoadSurroundingChunks() {


        if (!World.chunkExists(MIN_X - CHUNK_LENGTH, MIN_Z)) {



            if (World.getChunkAt(MIN_X - CHUNK_LENGTH, MIN_Z) == null) {
                new Chunk(noise, MIN_X - CHUNK_LENGTH, MIN_Z);
            }
        }


        if (!World.chunkExists(MIN_X, MIN_Z - CHUNK_WIDTH)) {



            if (World.getChunkAt(MIN_X, MIN_Z - CHUNK_WIDTH) == null) {
                new Chunk(noise, MIN_X, MIN_Z - CHUNK_WIDTH);
            }
        }


        if (!World.chunkExists(MIN_X + CHUNK_LENGTH, MIN_Z)) {

            if (World.getChunkAt(MIN_X + CHUNK_LENGTH, MIN_Z) == null) {
                new Chunk(noise, MIN_X + CHUNK_LENGTH, MIN_Z);
            }
        }

        if (!World.chunkExists(MIN_X, MIN_Z + CHUNK_WIDTH)) {


            if (World.getChunkAt(MIN_X, MIN_Z + CHUNK_WIDTH) == null) {
                new Chunk(noise, MIN_X, MIN_Z + CHUNK_WIDTH);

            }
        }

        //System.out.println("FINISHED LOADING LAZY CHUNKS: "+(System.nanoTime() - start/ Math.pow(10, 9)));




    }


    public void addBlocks() {
        this.isLazy = false;
        updateChunk();



        //System.out.println(BlockRenderer.Faces);


    }


    public int getHeight(int x, int z){
        x = Math.min(x - MIN_X, 15);
        z = Math.min(z - MIN_Z, 15);
        int height = 0;
        for(int y = 0;y<CHUNK_HEIGHT;y++){
            if(blocksInChunk[x][y][z] != 0){
                height = y;
            }
        }
        return height;
    }

    public synchronized void computeBlocks() {

        blocksInChunk = new int[CHUNK_LENGTH][CHUNK_HEIGHT + 1][CHUNK_WIDTH];

        long start = System.nanoTime();


        for (int x = 0; x < CHUNK_LENGTH; x++) {

            for (int y = 0; y < CHUNK_HEIGHT; y++) {

                for (int z = 0; z < CHUNK_WIDTH; z++) {

                    //int TERRAIN_HEIGHT = (int) Math.max(1, (CHUNK_HEIGHT*noise.noise(MIN_X+x, MIN_Z+z)));

                    int TERRAIN_HEIGHT = 10;

                    /*
                    if(y >= TERRAIN_HEIGHT -1 && x == CHUNK_LENGTH / 2 && z == CHUNK_WIDTH / 2){
                        blocksInChunk[x][y][z] = 0;
                    }

                     */
                    if (y >= TERRAIN_HEIGHT) {
                        blocksInChunk[x][y][z] = 0;
                    } else {
                        blocksInChunk[x][y][z] = 1;
                    }
                    if(TERRAIN_HEIGHT > maxBlockHeight){
                        maxBlockHeight = TERRAIN_HEIGHT;
                    }
                }
            }
        }
        blocksComputed = true;
        //System.out.println("FINISHED COMPUTING CHUNK: "+(System.nanoTime() - start/ Math.pow(10, 9)));

    }


    public synchronized void updateChunk() {

        long start = System.nanoTime();

        blockRenderer = new BlockRenderer(MIN_X, MIN_Z, this);


        for (int x = 0; x < CHUNK_LENGTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                for (int z = 0; z < CHUNK_WIDTH; z++) {
                    FACE[] facesToRender = new FACE[6];

                    int id = blocksInChunk[x][y][z];

                    Material material = Material.getByID(id);

                    Block block = new Block(new Location(MIN_X + x, y, MIN_Z + z), "dirt", material);

                    if (material == AIR) {

                        blockRenderer.render(block, new FACE[]{});
                        continue;
                    }

                    try {
                        if (blocksInChunk[x][y + 1][z] == 0) {
                            facesToRender[0] = (FACE.TOP);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {
                        facesToRender[0] = (TOP);
                    }

                    try {
                        if (blocksInChunk[x][y - 1][z] == 0) {


                            facesToRender[1] = (BOTTOM);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {


                    }
                    // MAX Z
                    try {
                        if (blocksInChunk[x + 1][y][z] == 0) {

                            facesToRender[2] = (RIGHT);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {


                        Chunk chunk = World.getChunkAt(MIN_X, MIN_Z + CHUNK_WIDTH);
                        if (chunk == null || chunk.isBlockAir(0, y, z)) {
                            facesToRender[2] = (RIGHT);
                        }

                    }
                    try {
                        if (blocksInChunk[x - 1][y][z] == 0) {
                            facesToRender[3] = (LEFT);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {


                        Chunk chunk = World.getChunkAt(MIN_X, MIN_Z - CHUNK_WIDTH);
                        if (chunk == null || chunk.isBlockAir(CHUNK_WIDTH - 1, y, z)) {
                            facesToRender[3] = (LEFT);
                        }




                    }
                    try {
                        if (blocksInChunk[x][y][z - 1] == 0) {

                            facesToRender[4] = (BACK);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {

                        Chunk chunk = World.getChunkAt(MIN_X - CHUNK_LENGTH, MIN_Z);
                        if (chunk == null || chunk.isBlockAir(x, y, CHUNK_LENGTH - 1)) {

                            facesToRender[4] = (BACK);
                        }



                    }
                    try {
                        if (blocksInChunk[x][y][z + 1] == 0) {

                            facesToRender[5] = (FRONT);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {


                        Chunk chunk = World.getChunkAt(MIN_X + CHUNK_LENGTH, MIN_Z);

                        if (chunk == null || chunk.isBlockAir(x, y, 0)) {

                            facesToRender[5] = (FRONT);
                        }



                    }


                    blockRenderer.render(block, facesToRender);


                }
            }
        }
        //System.out.println("FINISHED UPDATING CHUNK: "+(System.nanoTime() - start/ Math.pow(10, 9)));
        blockRenderer.computeMesh();





        World.chunksToMesh.add(this);


    }

    public synchronized BlockRenderer getBlockRenderer() {
        return blockRenderer;
    }






    public boolean isBlockAir(int x, int y, int z) {

        return blocksInChunk[x][y][z] == 0;

    }


    public void setBlock(int xLoc, int yLoc, int zLoc, Material material){
        int x = Math.min((xLoc - MIN_X), 15) ;
        int z = Math.min((zLoc - MIN_Z), 15);
        blocksInChunk[x][yLoc][z] =Material.toID(material);
        System.out.println("Set block at: X: "+x+" Y: "+yLoc+" Z: "+z);
    }

    public boolean containsCoords(float x, float z){
        return x>=MIN_X && z>= MIN_Z && z<=MIN_Z+CHUNK_WIDTH && x<=MIN_X+CHUNK_LENGTH;
    }


}
