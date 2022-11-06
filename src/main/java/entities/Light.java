package entities;

import org.lwjgl.util.vector.Vector3f;
import world.Location;

public class Light {

    private Location position;
    private Vector3f colour;
    private Vector3f attenuation = new Vector3f(1, 0, 0);

    public Light(Location position, Vector3f colour) {
        this.position = position;
        this.colour = colour;
    }

    public Light(Location position, Vector3f colour, Vector3f attenuation) {
        this.position = position;
        this.colour = colour;
        this.attenuation = attenuation;
    }

    public Vector3f getAttenuation() {
        return attenuation;
    }

    public Location getPosition() {
        return position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public Vector3f getColour() {
        return colour;
    }

    public void setColour(Vector3f colour) {
        this.colour = colour;
    }


}
