package renderEngine;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.*;

public class DisplayManager {

	private static final int WIDTH = 1600;
	private static final int HEIGHT = 900;

	public static final int FPS_CAP = Integer.MAX_VALUE;

	private static long lastFrameTime;
	private static float delta;

	public static void createDisplay(){
		ContextAttribs attribs = new ContextAttribs(3,2)
		.withForwardCompatible(true)
		.withProfileCore(true).withDebug(true);

		try {
			Display.setDisplayMode(new DisplayMode(WIDTH,HEIGHT));
			Display.create(new PixelFormat(), attribs);
			Display.setTitle("Minecraft");
			Display.setResizable(true);

		} catch (LWJGLException e) {
			e.printStackTrace();
		}


		lastFrameTime = getCurrentTime();
	}

	public static void updateDisplay(){
		Display.sync(FPS_CAP);
		GL11.glViewport(0,0, Display.getWidth(), Display.getHeight());
		Display.update();
		long currentFrameTime = getCurrentTime();
		delta = (currentFrameTime - lastFrameTime)/1000f;
		lastFrameTime = currentFrameTime;
	}

	public static float getFrameTimeSeconds(){
		return delta;
	}

	public static void closeDisplay(){
		Display.destroy();
	}

	private static long getCurrentTime(){
		return Sys.getTime()*1000/Sys.getTimerResolution();
	}




}
