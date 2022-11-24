package blocks;

import engineTester.MainGameLoop;
import entities.ChunkEntity;
import lombok.Getter;
import lombok.Setter;
import models.TexturedModel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import textures.ModelTexture;
import world.Location;
import world.World;
import world.chunk.Chunk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;


public class BlockRenderer implements Serializable {

    /* TODO
     -Optimise blocksInChunk no need for all blocks even air blocks
     - Optimise possitions float array. Suitable for each face, useless space
     - Implement greedy meshing algorithm. Faces are merged less space and memory consumed
     */




    public static Block[] blocks = new Block[Chunk.getCHUNK_LENGTH()*Chunk.getCHUNK_WIDTH()*Chunk.getCHUNK_HEIGHT()];

    private final int minX;
    private final int minZ;

    @Getter
    private static final ModelTexture texture = MainGameLoop.getModelTexture();

    @Getter @Setter
    private TexturedModel texturedModel;

    private final Chunk chunk;

    int index = 0;

    @Getter
    public float[] positions;
    static float[][] positionss = new float[][]{{0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0}};
    static float[][][] pos = new float[Chunk.getCHUNK_LENGTH()*Chunk.getCHUNK_WIDTH()*Chunk.getCHUNK_HEIGHT()][6][13];

    private static final float[] TEXTURECOORDS = new float[]{
            4f/16f, 0.f, //TOP LEFT
            4f/16f, 0.0625f, // BOTTOM LEFT
            5f/16f, 0.0625f,
            5f/16f, 0.f,
    };

    private static final float[] TOPFACE = new float[]{
            -0.5f,0.5f,0f,
            -0.5f,0.5f,1f,
            0.5f,0.5f,1f,
            0.5f,0.5f,0f,
    };
    private static final float[] BOTTOMFACE = new float[]{
            -0.5f,-0.5f,1f,
            -0.5f,-0.5f,0f,
            0.5f,-0.5f,0f,
            0.5f,-0.5f,1f
    };
    private static final float[] LEFTFACE = new float[]{
            0.5f,0.5f,0f,
            0.5f,-0.5f,0f,
            -0.5f,-0.5f,0f,
            -0.5f,0.5f,0f,
    };
    private static final float[] RIGHTFACE = new float[]{
            -0.5f,0.5f,1f,
            -0.5f,-0.5f,1f,
            0.5f,-0.5f,1f,
            0.5f,0.5f,1f,

    };
    private static final float[] FRONTFACE = new float[]{
            0.5f,0.5f,1f,
            0.5f,-0.5f,1f,
            0.5f,-0.5f,0f,
            0.5f,0.5f,0f,
    };
    private static  final float[] BACKFACE = new float[]{
            -0.5f,0.5f,0f,
            -0.5f,-0.5f,0f,
            -0.5f,-0.5f,1f,
            -0.5f,0.5f,1f,
    };


    private int faces;

    int positionsLength;
    public BlockRenderer(int minX, int minZ,Chunk chunk){
        this.minX = minX;
        this.minZ = minZ;
        this.chunk = chunk;
        positionsLength = 0;
        
    }
    LinkedList<Float> textureCoordsList = new LinkedList<>();




    public synchronized void render(Block block, Block.FACE @NotNull [] faces){
        int integer;
        for(Block.FACE face : faces){
            if(face == null)continue;
            switch (face){
                case TOP -> {
                    integer = 1;
                    positionss[1][0] = 1;
                    for(float position : TOPFACE){
                        positionss[1][integer++] = position;
                    }
                    positionsLength+=12;
                }
                case BOTTOM -> {
                    integer = 1;
                    positionss[0][0] = 1;
                    for(float position : BOTTOMFACE){
                        positionss[0][integer++] = position;
                    }
                    positionsLength+=12;
                }
                case LEFT -> {
                    integer = 1;
                    positionss[4][0] = 1;
                    for(float position : LEFTFACE){
                        positionss[4][integer++] = position;
                    }
                    positionsLength+=12;
                }
                case RIGHT -> {
                    integer = 1;
                    positionss[5][0] = 1;
                    for(float position : RIGHTFACE){
                        positionss[5][integer++] = position;
                    }
                    positionsLength+=12;
                }
                case FRONT -> {
                    integer = 1;
                    positionss[3][0] = 1;
                    for(float position : FRONTFACE){
                        positionss[3][integer++] = position;
                    }
                    positionsLength+=12;
                }
                case BACK -> {
                    integer = 1;
                    positionss[2][0] = 1;
                    for(float position : BACKFACE){
                        positionss[2][integer++] = position;
                    }
                    positionsLength+=12;
                }
            }
        }

        block.setIndices(generateIndices(faces.length));
        block.setPositions(positionss);
        textureCoords(faces, block);


        blocks[index] = block;
        positionss = new float[][]{{0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0}};
        index++;

    }

    @Getter
    public int[] indices;

    public synchronized void computeMesh(){

        long start = System.nanoTime();

        positions = new float[positionsLength];
        int[] index = {0, 0, 0};

        for (Block block : blocks) {
            if(block == null)continue;
            pos[index[2]] = (block.getPositions());
            index[2]++;
        }


        int xincrement = 1;
        int yincrement = 1;
        int zincrement = 1;

        for (float[][] numberOfFaces : pos) {
            int facesToRender = 0;
            for (float[] face : numberOfFaces) {

                if (face[0] != 1) continue;
                facesToRender++;
                for (int vertex = 1; vertex < face.length; vertex++) {

                    switch (vertex % 3){
                        case 1 ->{
                            positions[index[1]] = (face[vertex] + xincrement);
                            index[1]++;
                        }
                        case 2->{
                            positions[index[1]] = (face[vertex] + yincrement);
                            index[1]++;
                        }
                        case 0 ->{
                            positions[index[1]] = (face[vertex] + zincrement);
                            index[1]++;
                        }
                    }
                }
            }
            faces += facesToRender;

            if (xincrement < Chunk.getCHUNK_WIDTH()) {
                xincrement++;
            } else if (yincrement < Chunk.getCHUNK_HEIGHT()) {
                xincrement = 1;
                yincrement++;
            } else if (zincrement < Chunk.getCHUNK_WIDTH()) {
                xincrement = 1;
                yincrement = 1;
                zincrement++;
            }
        }
        indices = generateIndices(faces);


        textureCoords = new float[textureCoordsList.size()];
        int i = 0;
        for(Float num : textureCoordsList){
            this.textureCoords[i] = num;
            i++;
        }

        System.out.println("FINISHED COMPUTING MESH: "+(System.nanoTime() - start )/ Math.pow(10, 9));
    }
    @Getter
    private float[] textureCoords;





    public synchronized void renderChunk(){
        Location location = new Location(minX,0,minZ);
        ChunkEntity entity = new ChunkEntity(texturedModel, 0, location, 0,0,0,Block.getBLOCK_WIDTH(), chunk);
        chunk.setEntity(entity);
        World.renderChunk(entity);

    }



    @Contract(pure = true)
    public synchronized void textureCoords(Block.FACE[] faces, Block block){

        if(block == null)return;

        Block.Material material = block.getMaterial();
        BlockTextureHandler textureHandler = new BlockTextureHandler();
        textureHandler.setType(material);
        LinkedList<Float> coords = new LinkedList<>();
        for(Block.FACE face : faces){
            if(face == null)continue;
            switch (face){
                case TOP -> coords.addAll(textureHandler.topFace);
                case BACK -> coords.addAll(textureHandler.backFace);
                case LEFT -> coords.addAll(textureHandler.leftFace);
                case RIGHT -> coords.addAll(textureHandler.rightFace);
                case FRONT -> coords.addAll(textureHandler.frontFace);
                case BOTTOM -> coords.addAll(textureHandler.bottomFace);
            }
        }
        textureCoordsList.addAll(coords);
    }




    @Contract(mutates = "this")
    public synchronized int @NotNull [] generateIndices(int num){

        int[] nums = new int[num*6];

        int index = 0;

        for(int i = 1; i<=num;i++){

            int t = (4 * i) -3;
            nums[index] = (t-1);
            index++;
            nums[index] = (t);
            index++;
            nums[index] = (t+2);
            index++;
            nums[index] = (t+2);
            index++;
            nums[index] = (t);
            index++;
            nums[index] = (t+1);
            index++;

        }

        return nums;

    }




}
