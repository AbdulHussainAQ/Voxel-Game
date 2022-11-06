package entities;

import lombok.Getter;
import models.TexturedModel;
import world.Location;

public class Entity {

    @Getter
    private TexturedModel model;
    private Location position;
    private float rotX, rotY, rotZ;
    private float scale;

    private int textureIndex = 0;

    public Entity(TexturedModel model, Location position, float rotX, float rotY, float rotZ,
                  float scale) {
        this.model = model;
        this.rotX = rotX;
        this.position = position;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.scale = scale;
    }

    public Entity(TexturedModel model, int index, Location position, float rotX, float rotY, float rotZ,
                  float scale) {
        this.textureIndex = index;
        this.model = model;
        this.rotX = rotX;
        this.position = position;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.scale = scale;
    }

    public float getTextureXOffset() {
        int column = textureIndex % model.getTexture().getNumberOfRows();
        return (float) column / (float) model.getTexture().getNumberOfRows();
    }

    public float getTextureYOffset() {
        int row = textureIndex / model.getTexture().getNumberOfRows();
        return (float) row / (float) model.getTexture().getNumberOfRows();
    }

    public void increasePosition(float dx, float dy, float dz) {
        this.position.x += dx;
        this.position.y += dy;
        this.position.z += dz;
    }

    public void increaseRotation(float dx, float dy, float dz) {
        this.rotX += dx;
        this.rotY += dy;
        this.rotZ += dz;
    }

    public TexturedModel getModel() {
        return model;
    }

    public void setModel(TexturedModel model) {
        this.model = model;
    }

    public Location getPosition() {
        return position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public float getRotX() {
        return rotX;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

}
