package blocks;

import blocks.impl.*;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Arrays;

public class BlockTextureHandler {
        private Block.Material type;

        public ArrayList<Float> frontFace;
        public ArrayList<Float> backFace;
        public ArrayList<Float> topFace;
        public ArrayList<Float> bottomFace;
        public ArrayList<Float> leftFace;
        public ArrayList<Float> rightFace;

        public void setType(Block.Material material) {
            this.type = material;
            frontFace = new ArrayList<>();
            bottomFace = new ArrayList<>();
            leftFace = new ArrayList<>();
            rightFace = new ArrayList<>();
            topFace = new ArrayList<>();
            backFace = new ArrayList<>();

            getFaces();
        }

        public Block.Material getType(){
            return type;
        }

        private void getFaces() {
            switch (type) {
                case GRASS -> setFaces(BlockGrass.UV_FRONT, BlockGrass.UV_BACK, BlockGrass.UV_TOP, BlockGrass.UV_BOTTOM,
                        BlockGrass.UV_LEFT, BlockGrass.UV_RIGHT);
                case DIRT ->
                        setFaces(BlockDirt.UV_FRONT, BlockDirt.UV_BACK, BlockDirt.UV_TOP, BlockDirt.UV_BOTTOM, BlockDirt.UV_LEFT,
                                BlockDirt.UV_RIGHT);
                case STONE -> setFaces(BlockStone.UV_FRONT, BlockStone.UV_BACK, BlockStone.UV_TOP, BlockStone.UV_BOTTOM,
                        BlockStone.UV_LEFT, BlockStone.UV_RIGHT);
                case OAK_LOG -> setFaces(BlockOakLog.UV_FRONT, BlockOakLog.UV_BACK, BlockOakLog.UV_TOP, BlockOakLog.UV_BOTTOM,
                        BlockOakLog.UV_LEFT, BlockOakLog.UV_RIGHT);
                case OAK_LEAVES -> setFaces(BlockOakLeaves.UV_FRONT, BlockOakLeaves.UV_BACK, BlockOakLeaves.UV_TOP, BlockOakLeaves.UV_BOTTOM,
                        BlockOakLeaves.UV_LEFT, BlockOakLeaves.UV_RIGHT);
                case SAND -> setFaces(BlockSand.UV_FRONT, BlockSand.UV_BACK, BlockSand.UV_TOP, BlockSand.UV_BOTTOM, BlockSand.UV_LEFT, BlockSand.UV_RIGHT);
                case WATER ->setFaces(BlockWater.UV_FRONT, BlockWater.UV_BACK, BlockWater.UV_TOP, BlockWater.UV_BOTTOM, BlockWater.UV_LEFT, BlockWater.UV_RIGHT);
            }
        }

        private void setFaces(float[] front, float[] back, float[] top, float[] bottom, float[] left,
                              float[] right) {
            long start = System.nanoTime();
            for(float face : front){
                frontFace.add(face);
            }
            for(float face : back){
                backFace.add(face);
            }
            for(float face : left){
                leftFace.add(face);
            }
            for(float face : right){
                rightFace.add(face);
            }
            for(float face : top){
                topFace.add(face);
            }
            for(float face : bottom){
                bottomFace.add(face);
            }

        }
    }
    

