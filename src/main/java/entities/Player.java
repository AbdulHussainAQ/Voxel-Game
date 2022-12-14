package entities;

import blocks.Block;
import lombok.Getter;
import lombok.Setter;
import models.TexturedModel;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import org.lwjgl.util.vector.Vector3f;
import renderEngine.DisplayManager;
import world.Location;

import world.World;
import world.chunk.Chunk;

import static org.lwjgl.opengl.GL11.*;

public class Player extends Entity {
    //private final float RUN_SPEED = 1f; //Test speed
    private final float RUN_SPEED = 4.317f; //Walk speed
    //private final float RUN_SPEED = 10.8f; //Fly speed
    //private final float RUN_SPEED = 20f;
    private final float GRAVITY = 0;

    private final int REACHDISTANCE = 7;

    private final float JUMP_POWER = RUN_SPEED;

    private boolean togglePolygon = false;
    private long cooldown = 0L;

    private float currentSpeed = 0;
    private float currentTurnSpeed = 0;
    private float upwardsSpeed = 0;

    private boolean jumping = false;

    private final boolean isInAir = false;

    private boolean horizontal = false;

    private Chunk currentChunk;

    @Setter @Getter
    private Camera camera;

    public Player(TexturedModel model, Location position, float rotX, float rotY, float rotZ,
                  float scale) {
        super(model, position, rotX, rotY, rotZ, scale);

    }
	/*
	public void move(Terrain terrain) {
		checkInputs();

		super.increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		float distance = currentSpeed * DisplayManager.getFrameTimeSeconds();
		float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
		super.increasePosition(dx, 0, dz);
		upwardsSpeed += GRAVITY * DisplayManager.getFrameTimeSeconds();
		super.increasePosition(0, upwardsSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		float terrainHeight = terrain.getHeightOfTerrain(getPosition().x, getPosition().z);

		/*
		if (super.getPosition().y < terrainHeight) {
			upwardsSpeed = 0;
			isInAir = false;
			super.getPosition().y = terrainHeight;
		}


	}
	*/

    private long lastTime = System.currentTimeMillis();
    private final long TIMEOUT = 50;
    public void move() {

        jumping = false;
        horizontal = false;
        checkInputs();
        if(currentChunk == null){
            currentChunk = getCurrentChunk();
        }
        Location location = this.getPosition();
        Display.setTitle("X: "+location.getX()+" Y: "+location.getY()+" Z: "+location.getZ()+" Pitch: "+camera.getPitch()+" Yaw: "+camera.getYaw());


        super.increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0);
        float distance = currentSpeed * DisplayManager.getFrameTimeSeconds();
        float dx;
        float dz;
        dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
        dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));

        super.increasePosition(dx, 0, dz);

        if (jumping) {
            upwardsSpeed += GRAVITY * DisplayManager.getFrameTimeSeconds();
            super.increasePosition(0, upwardsSpeed * DisplayManager.getFrameTimeSeconds(), 0);
        }
        super.increasePosition(0, 0, 0);


		/*
		float terrainHeight = 1;

		if (super.getPosition().y < terrainHeight) {
			upwardsSpeed = 0;
			isInAir = false;
			super.getPosition().y = terrainHeight;
		}

		 */
    }

    private void jump() {

        jumping = true;
        this.upwardsSpeed = JUMP_POWER;


    }

    private void flyDown() {

        jumping = true;
        this.upwardsSpeed = -JUMP_POWER;


    }

    private void checkInputs() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            this.currentSpeed = RUN_SPEED;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            this.currentSpeed = -RUN_SPEED;
//		}else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
//			this.currentSpeed = RUN_SPEED;
//			this.currentTurnSpeed = TURN_SPEED;
//			this.horizontal = true;
//		} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
//			this.currentSpeed = -RUN_SPEED;
//			this.currentTurnSpeed = -TURN_SPEED;
//			this.horizontal = true;
        } else {
            this.currentSpeed = 0;
            this.currentTurnSpeed = 0;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
            if (!togglePolygon && System.currentTimeMillis() - cooldown > 1000) {
                cooldown = System.currentTimeMillis();
                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                togglePolygon = true;
            } else if (togglePolygon && System.currentTimeMillis() - cooldown > 1000) {
                cooldown = System.currentTimeMillis();
                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                togglePolygon = false;
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            flyDown();
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            jump();
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
            Mouse.setGrabbed(false);
        }
        if(Mouse.isButtonDown(0) && !Mouse.isGrabbed()){
            Mouse.setGrabbed(true);
        }

    }

    public Chunk getCurrentChunk(){
        Location location = getPosition();
        return World.getChunk(location.x, location.z);
    }



    public double getMinXValue(){
        Location location = getPosition();
        return 16*(Math.floor(location.x/16f));
    }
    public double getMinZValue(){
        Location location = getPosition();
        return 16*(Math.floor(location.z/16f));
    }

    public Location getEyeLocation(){

        Location loc = camera.getPosition().clone();
        return loc;

    }

    private void rayTracePlace(int maxDistance) {

        Location eyeLocation = this.getEyeLocation();
        Vector3f direction = eyeLocation.getDirection(this.camera.getYaw(), this.camera.getPitch());
        Vector3f directionNormalized = (Vector3f) direction.normalise();
        Location finalLoc = eyeLocation.clone();


        double distance = 0;


        while (distance < maxDistance) {
            finalLoc.add(directionNormalized);
            Chunk chunk = World.getChunk(finalLoc.x, finalLoc.z);
            if (chunk != null) {
                System.out.println("BLOCK ID: " + chunk.getBlock((int) Math.floor(finalLoc.x), (int) Math.floor(finalLoc.y), (int) Math.floor(finalLoc.z)));
                if (chunk.getBlock((int) Math.floor(finalLoc.x), (int) Math.floor(finalLoc.y), (int) Math.floor(finalLoc.z)) != 0) {


                    for(double i = distance; i>=0;i--){
                        finalLoc.subtract(directionNormalized);
                        if(chunk.getBlock((int) Math.floor(finalLoc.x), (int) Math.floor(finalLoc.y), (int) Math.floor(finalLoc.z)) == 0){
                            chunk.setBlock((int) Math.floor(finalLoc.x), (int) Math.floor(finalLoc.y), (int) Math.floor(finalLoc.z), Block.Material.GRASS);
                            World.chunksToUpdate.add(chunk);
                            break;
                        }
                    }
                    break;

                }
            }
            distance+=1;
        }

    }

    public void rayTraceBreak(double maxDistance){
        Location eyeLocation = this.getEyeLocation();
        Vector3f direction = eyeLocation.getDirection(this.camera.getYaw(), this.camera.getPitch());
        Vector3f directionNormalized = (Vector3f) direction.normalise();
        Location finalLoc = eyeLocation.clone();


        double distance = 0;


        while (distance < maxDistance){
            finalLoc.add(directionNormalized);
            Chunk chunk = World.getChunk(finalLoc.x, finalLoc.z);
            if(chunk != null){
                System.out.println("BLOCK ID: "+chunk.getBlock((int) Math.floor(finalLoc.x), (int) Math.floor(finalLoc.y), (int) Math.floor(finalLoc.z)));
                if(chunk.getBlock((int) Math.floor(finalLoc.x), (int) Math.floor(finalLoc.y), (int) Math.floor(finalLoc.z)) != 0){
                    chunk.setBlock((int) Math.floor(finalLoc.x), (int) Math.floor(finalLoc.y), (int) Math.floor(finalLoc.z), Block.Material.AIR);
                    World.chunksToUpdate.add(chunk);
                    break;
                }
            }
            distance+=1;
        }


    }

    public void breakBlock() {

        rayTraceBreak(REACHDISTANCE);

    }

    public void placeBlock() {

        rayTracePlace(REACHDISTANCE);

    }


}
