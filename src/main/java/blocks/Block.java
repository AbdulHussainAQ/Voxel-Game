package blocks;

import lombok.Getter;
import world.Location;

import java.io.Serializable;

public class Block implements Serializable {
    @Getter
    private final Location location;

    @Getter
    private static final float BLOCK_WIDTH = 1f;
    public int[] indices;

    public float[] textureCoords;


    public float[][] positions;
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

        GRASS, AIR;


        public static Material getByID(int id) {
            if (id == 0) {
                return AIR;
            }
            if (id == 1) {
                return GRASS;
            }
            return null;
        }


    }


}
