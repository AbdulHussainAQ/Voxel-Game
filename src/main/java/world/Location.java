package world;

import blocks.Block;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector3f;
import world.chunk.Chunk;

import java.io.Serializable;


public class Location implements Cloneable, Serializable {

    private final float BLOCK_WIDTH = Block.getBLOCK_WIDTH();


    public float x;
    public float y;


    public float z;

    public Location(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z ;

    }


    public float getX() {
        return this.x ;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z ;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }


    public Vector3f toVector() {
        return new Vector3f(x, y, z);
    }


    public boolean isNextTo(Location loc) {


        return this.x == loc.x - 1 || this.x == loc.x + 1 || this.z == loc.z - 1 || this.z == loc.z + 1 || this.y == loc.y - 1 || this.y == loc.y + 1;
    }

    public Location clone() {

        return new Location(x,y,z);


    }

    @NotNull
    public Location add(@NotNull Location vec) {

        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;

    }

    @NotNull
    public Location multiply(float m) {
        this.x *= m;
        this.y *= m;
        this.z *= m;
        return this;
    }


    public Location add(@NotNull Vector3f vec) {
        this.x += vec.getX();
        this.y += vec.getY();
        this.z += vec.getZ();
        return this;
    }

    @NotNull
    public Location add(int x, int y, int z) {

        this.x += x + BLOCK_WIDTH;
        this.y += y + BLOCK_WIDTH;
        this.z += z + BLOCK_WIDTH;
        return this;
    }

    @NotNull
    public Location subtract(@NotNull Location vec) {

        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;

    }

    @NotNull
    public Location subtract(@NotNull Vector3f vec) {
        this.x -= vec.getX();
        this.y -= vec.getY();
        this.z -= vec.getZ();
        return this;
    }

    @NotNull
    public Location subtract(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector3f getDirection(float yaw, float pitch) {
        Vector3f vector = new Vector3f();
        vector.setY((float) -Math.sin(Math.toRadians(pitch)));
        double xz = Math.cos(Math.toRadians(pitch));
        vector.setX((float) (xz * Math.sin(Math.toRadians(yaw))));
        vector.setZ((float) (xz * Math.cos(Math.toRadians(yaw))));
        return vector;
    }


}
