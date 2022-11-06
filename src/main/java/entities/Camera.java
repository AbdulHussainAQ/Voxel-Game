package entities;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

    private float distanceFromPlayer = 0;
    private float angleAroundPlayer = 0;

    private final Vector3f position = new Vector3f(0, 0, 0);
    private float pitch = 20;
    private float yaw = 0;
    private float roll;

    private final Player player;

    public Camera(Player player) {
        this.player = player;
        this.angleAroundPlayer = player.getRotY();
        this.pitch = player.getRotX();
        Mouse.setGrabbed(true);
    }

    public void move() {
        calculateZoom();
        calculatePitch();
        calculateAngleAroundPlayer();
        float horizontalDistance = calculateHorizontalDistance();
        float verticalDistance = calculateVerticalDistance();
        calculateCameraPosition(horizontalDistance, verticalDistance);
        this.yaw = 180 - (angleAroundPlayer);
        yaw %= 360;
    }

    public void invertPitch() {
        this.pitch = -pitch;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }

    private void calculateCameraPosition(float horizDistance, float verticDistance) {

        position.x = player.getPosition().getX();
        position.z = player.getPosition().getZ();
        position.y = player.getPosition().getY() + 6;
    }

    private float calculateHorizontalDistance() {
        return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch + 4)));
    }

    private float calculateVerticalDistance() {
        return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch + 4)));
    }

    private void calculateZoom() {
        float zoomLevel = Mouse.getDWheel() * 0.03f;
        distanceFromPlayer -= zoomLevel;
        if (distanceFromPlayer < 5) {
            distanceFromPlayer = 5;
        }
    }

    private void calculatePitch() {

        float pitchChange = Mouse.getDY() * 0.2f;
        pitch -= pitchChange;
        if (pitch < -90) {
            pitch = -90;
        } else if (pitch > 80) {
            pitch = 80;
        }


    }

    private void calculateAngleAroundPlayer() {

        float angleChange = Mouse.getDX();
        angleAroundPlayer -= angleChange;
        player.increaseRotation(0, -angleChange, 0);

    }


}
