package toolbox;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector3f;

import java.io.Serializable;

public class BoundingBox implements Serializable {

    @Getter
    private double minX;
    @Getter
    private double minY;
    @Getter
    private double minZ;
    @Getter
    private double maxX;
    @Getter
    private double maxY;
    @Getter
    private double maxZ;

    public BoundingBox() {
        this.resize(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.resize(x1, y1, z1, x2, y2, z2);
    }
    @NotNull
    public BoundingBox resize(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
        return this;
    }

    public double getCenterX() {
        return this.minX + this.getWidthX() * 0.5;
    }

    @NotNull
    public Vector3f getMin() {
        return new Vector3f((float) this.minX, (float) this.minY, (float) this.minZ);
    }


    @NotNull
    public Vector3f getMax() {
        return new Vector3f((float) this.maxX, (float) this.maxY, (float) this.maxZ);
    }

    public double getWidthX() {
        return this.maxX - this.minX;
    }

    public double getWidthZ() {
        return this.maxZ - this.minZ;
    }

    public double getHeight() {
        return this.maxY - this.minY;
    }

    public double getVolume() {
        return this.getHeight() * this.getWidthX() * this.getWidthZ();
    }



    public double getCenterY() {
        return this.minY + this.getHeight() * 0.5;
    }

    public double getCenterZ() {
        return this.minZ + this.getWidthZ() * 0.5;
    }

    @NotNull
    public Vector3f getCenter() {
        return new Vector3f((float) this.getCenterX(), (float) this.getCenterY(), (float) this.getCenterZ());
    }

    public boolean contains(double x, double y, double z) {
        return x >= this.minX && x < this.maxX && y >= this.minY && y < this.maxY && z >= this.minZ && z < this.maxZ;
    }

    public boolean contains(@NotNull Vector3f position) {
        return this.contains(position.getX(), position.getY(), position.getZ());
    }

    private boolean contains(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.minX <= minX && this.maxX >= maxX && this.minY <= minY && this.maxY >= maxY && this.minZ <= minZ && this.maxZ >= maxZ;
    }

    public boolean contains(@NotNull BoundingBox other) {
        return this.contains(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean contains(@NotNull Vector3f min, @NotNull Vector3f max) {
        double x1 = min.getX();
        double y1 = min.getY();
        double z1 = min.getZ();
        double x2 = max.getX();
        double y2 = max.getY();
        double z2 = max.getZ();
        return this.contains(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }


}
