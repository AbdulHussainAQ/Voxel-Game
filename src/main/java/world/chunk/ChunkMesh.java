package world.chunk;

import blocks.Block;

public class ChunkMesh {

    private final Chunk chunk;

    public ChunkMesh(Chunk chunk) {

        this.chunk = chunk;
    }

    private void createGreedyMesh() {

        int maxX = chunk.getMIN_X() + Chunk.getCHUNK_WIDTH();
        int maxZ = chunk.getMIN_Z() + Chunk.getCHUNK_WIDTH();
        int maxY = Chunk.getCHUNK_HEIGHT();

        for (int x = chunk.getMIN_X(); x < maxX; x += Block.getBLOCK_WIDTH()) {
            for (int y = 0; y < maxY; y += Block.getBLOCK_WIDTH()) {
                for (int z = chunk.getMIN_Z(); z < maxZ; z += Block.getBLOCK_WIDTH()) {


                }
            }
        }


    }


}
