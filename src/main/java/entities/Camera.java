package entities;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;
import world.Location;

public class Camera {

    private float distanceFromPlayer = 0;
    private float angleAroundPlayer = 0;

    private final Location position = new Location(0, 0, 0);
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
        checkClick();
        float horizontalDistance = calculateHorizontalDistance();
        float verticalDistance = calculateVerticalDistance();
        calculateCameraPosition(horizontalDistance, verticalDistance);
        this.yaw = 180 - (angleAroundPlayer);
        yaw %= 360;
    }

    public void invertPitch() {
        this.pitch = -pitch;
    }

    public Location getPosition() {
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
        position.y = player.getPosition().getY();
    }

    private float calculateHorizontalDistance() {
        return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
    }

    private float calculateVerticalDistance() {
        return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
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
        } else if (pitch > 90) {
            pitch = 90;
        }


    }

    private void calculateAngleAroundPlayer() {

        float angleChange = Mouse.getDX();
        angleAroundPlayer -= angleChange;
        player.increaseRotation(0, -angleChange, 0);

    }



    private static long lastClicked = System.currentTimeMillis();

    private void checkClick(){

        if(Mouse.isButtonDown(0) && (System.currentTimeMillis() - lastClicked > 100)){

            player.breakBlock();
            lastClicked = System.currentTimeMillis();
        }
        if(Mouse.isButtonDown(1) && (System.currentTimeMillis()) - lastClicked > 100){
            player.placeBlock();
            lastClicked = System.currentTimeMillis();
        }
    }


}
