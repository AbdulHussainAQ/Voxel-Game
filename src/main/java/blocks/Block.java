package blocks;

import lombok.Getter;
import lombok.Setter;
import world.Location;

import java.io.Serializable;

public class Block implements Serializable {
    private final Location location;

    @Getter
    private static final float BLOCK_WIDTH = 1f;
    public int[] indices;

    public float[] textureCoords;
    public float[][] positions;


    @Getter @Setter
    public float[] uvs;

    private final Material material;

    public static int instanceCount = 0;


    public Block(Location location, String texture, Material material) {

        this.location = location;
        this.material = material;
        instanceCount++;
    }


    public int[] getIndices() {
        return indices;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }

    public void setTextureCoords(float[] textureCoords) {
        this.textureCoords = textureCoords;
    }

    public float[][] getPositions() {
        return positions;
    }

    public void setPositions(float[][] positions) {
        this.positions = positions;
    }

    public Location getLocation() {
        return location;
    }

    public Material getMaterial() {
        return material;
    }


    public enum FACE {
        FRONT, BACK, RIGHT, LEFT, TOP, BOTTOM
    }


    public enum Material {

        GRASS, AIR, STONE, DIRT, OAK_LOG, OAK_LEAVES, SAND, WATER;


        public static Material getByID(int id) {
            if (id == 0) {
                return AIR;
            }
            if (id == 1) {
                return GRASS;
            }
            if(id == 2){
                return DIRT;
            }
            if(id == 3){
                return STONE;
            }
            if(id == 4){
                return OAK_LOG;
            }
            if(id == 5){
                return OAK_LEAVES;
            }
            if(id == 6){
                return SAND;
            }
            if(id == 7){
                return WATER;
            }
            return null;
        }

        public static boolean isTransparent(int id){
            return id == 0 || id == 5;
        }


        public static int toID(Material material){
            if(material == AIR){
                return 0;
            }
            if(material == GRASS){
                return 1;
            }
            if(material == SAND){
                return 6;
            }
            if(material == OAK_LEAVES){
                return 5;
            }
            if(material == WATER){
                return 7;
            }
            return -1;
        }


    }


}
