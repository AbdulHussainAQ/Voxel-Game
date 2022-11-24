package world.chunk;

import blocks.Block;
import blocks.Block.FACE;
import blocks.Block.Material;
import blocks.BlockRenderer;
import blocks.BlockTextureHandler;
import entities.ChunkEntity;
import lombok.Getter;
import lombok.Setter;
import world.Location;
import world.World;
import world.worldgen.Noise;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static blocks.Block.FACE.*;
import static blocks.Block.Material.AIR;
import static blocks.Block.Material.isTransparent;

public class Chunk implements Serializable{

    @Getter
    public UUID chunkUUID;
    @Getter
    private static final int CHUNK_LENGTH = 16;
    @Getter
    private static final int CHUNK_WIDTH = 16;
    @Getter
    private static final int CHUNK_HEIGHT = 256;

    @Getter
    public final int MIN_X;
    @Getter
    public final int MIN_Z;

    private int[][][] blocksInChunk;

    @Getter @Setter
    private ChunkEntity entity;
    @Getter@Setter
    private ChunkEntity oldChunkEntity = null;

    private Noise noise;

    @Getter
    private boolean isLazy;

    public boolean blocksComputed;

    private BlockRenderer blockRenderer;

    private BlockTextureHandler blockHandler = new BlockTextureHandler();;

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
        Random treeGenerator = new Random();

        for (int x = 0; x < CHUNK_LENGTH; x++) {

            for (int y = 0; y < CHUNK_HEIGHT; y++) {

                for (int z = 0; z < CHUNK_WIDTH; z++) {

                    int TERRAIN_HEIGHT = (int) Math.max(1, (10*noise.noise(MIN_X+x, MIN_Z+z)));



                    //int TERRAIN_HEIGHT = 1;

                    /*
                    if(y >= TERRAIN_HEIGHT -1 && x == CHUNK_LENGTH / 2 && z == CHUNK_WIDTH / 2){
                        blocksInChunk[x][y][z] = 0;
                    }

                     */
                    if(blocksInChunk[x][y][z] != 0) continue;
                    if(ThreadLocalRandom.current().nextInt(1,500) == 5 && y == TERRAIN_HEIGHT + 1){
                        generateTree(x,y,z);
                    }
                    else if (y > TERRAIN_HEIGHT) {
                        blocksInChunk[x][y][z] = 0;
                    }
                    else if(y == TERRAIN_HEIGHT){
                        blocksInChunk[x][y][z] = 1;
                    }else if(y == TERRAIN_HEIGHT - 1){
                        blocksInChunk[x][y][z] = 2;
                    }else{
                        blocksInChunk[x][y][z] = 3;
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

    private void generateTree(int x, int y, int z) {

        blocksInChunk[x][y][z] = 4;
        blocksInChunk[x][y+1][z] = 4;
        blocksInChunk[x][y+2][z] = 4;
        blocksInChunk[x][y+3][z] = 4;//LEAVES FROM THIS Y
        blocksInChunk[x][y+4][z] = 4;
        blocksInChunk[x][y+5][z] = 4;

        for(int xC = x-2; xC < x+3;xC++){
            for(int zC = z-2; zC < z+3;zC++){
                for(int yC = y+3;yC<y+7;yC++){
                    if(xC == x && (yC == y+3 || yC == y+4) && zC == z)continue;
                    try{
                        blocksInChunk[xC][yC][zC] = 5;
                    }catch (Exception ignore){

                    }
                }
            }
        }





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

                    blockHandler.setType(block.getMaterial());

                    if (material == AIR) {

                        blockRenderer.render(block, new FACE[]{});
                        continue;
                    }

                    try {
                        if (isTransparent(blocksInChunk[x][y + 1][z])) {
                            facesToRender[0] = (FACE.TOP);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {
                        facesToRender[0] = (TOP);
                    }

                    try {
                        if (isTransparent(blocksInChunk[x][y - 1][z])) {


                            facesToRender[1] = (BOTTOM);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {


                    }
                    // MAX Z
                    try {
                        if (isTransparent(blocksInChunk[x + 1][y][z])) {

                            facesToRender[2] = (RIGHT);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {


                        Chunk chunk = World.getChunkAt(MIN_X, MIN_Z + CHUNK_WIDTH);
                        if (chunk == null || chunk.isBlockTranparent(0, y, z)) {
                            facesToRender[2] = (RIGHT);
                        }

                    }
                    try {
                        if (isTransparent(blocksInChunk[x - 1][y][z])) {
                            facesToRender[3] = (LEFT);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {


                        Chunk chunk = World.getChunkAt(MIN_X, MIN_Z - CHUNK_WIDTH);
                        if (chunk == null || chunk.isBlockTranparent(CHUNK_WIDTH - 1, y, z)) {
                            facesToRender[3] = (LEFT);
                        }




                    }
                    try {
                        if (isTransparent(blocksInChunk[x][y][z - 1])) {

                            facesToRender[4] = (BACK);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {

                        Chunk chunk = World.getChunkAt(MIN_X - CHUNK_LENGTH, MIN_Z);
                        if (chunk == null || chunk.isBlockTranparent(x, y, CHUNK_LENGTH - 1)) {

                            facesToRender[4] = (BACK);
                        }



                    }
                    try {
                        if (isTransparent(blocksInChunk[x][y][z + 1])) {

                            facesToRender[5] = (FRONT);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignore) {


                        Chunk chunk = World.getChunkAt(MIN_X + CHUNK_LENGTH, MIN_Z);

                        if (chunk == null || chunk.isBlockTranparent(x, y, 0)) {

                            facesToRender[5] = (FRONT);
                        }



                    }


                    blockRenderer.render(block, facesToRender);


                }
            }
        }
        //System.out.println("FINISHED UPDATING CHUNK: "+(System.nanoTime() - start/ Math.pow(10, 9)));
        blockRenderer.computeMesh();





        //World.chunksToMesh.add(this);


    }

    public synchronized BlockRenderer getBlockRenderer() {
        return blockRenderer;
    }






    public boolean isBlockTranparent(int x, int y, int z) {

        return isTransparent(blocksInChunk[x][y][z]);

    }


    public void setBlock(int xLoc, int yLoc, int zLoc, Material material){
        System.out.println("CHUNK MINX: "+MIN_X);
        System.out.println("CHUNK MINZ: "+MIN_Z);
        System.out.println("Set world block at: X: "+xLoc+" Y: "+yLoc+" Z: "+zLoc);
        int x = Math.min((zLoc - MIN_Z), 15) ;
        int z = Math.min((xLoc - MIN_X), 15);
        x = Math.max(x-1, 0);
        z = Math.max(z-1,0);

        System.out.println("Set chunk block at: X: "+x+" Y: "+yLoc+" Z: "+z);

        try{
            blocksInChunk[x][yLoc][z] =Material.toID(material);
            System.out.println("SUCCESS");

        }catch (ArrayIndexOutOfBoundsException ignore){

        }

    }

    public int getBlock(int xLoc, int yLoc, int zLoc){
        int x = Math.min((zLoc - MIN_Z), 15) ;
        int z = Math.min((xLoc - MIN_X), 15);
        x = Math.max(x-1, 0);
        z = Math.max(z-1,0);

        try{
            return blocksInChunk[x][yLoc][z];
        }catch (ArrayIndexOutOfBoundsException ignore){

        }
        return -1;

    }


    public boolean containsCoords(float x, float z){
        return x>=MIN_X && z>= MIN_Z && z<=MIN_Z+CHUNK_WIDTH && x<=MIN_X+CHUNK_LENGTH;
    }


}
