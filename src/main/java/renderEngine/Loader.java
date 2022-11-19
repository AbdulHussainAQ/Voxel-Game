package renderEngine;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import entities.ChunkEntity;
import models.RawModel;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import textures.TextureData;
import world.chunk.Chunk;

import java.io.FileInputStream;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Loader{

    protected ConcurrentHashMap<Chunk,Integer> vaos = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<Chunk, CopyOnWriteArrayList<Integer>> vbos = new ConcurrentHashMap<>();
    protected List<Integer> textures = new ArrayList<>();


    public RawModel loadToVAO(float[] positions, float[] textureCoords,
                              int[] indices, Chunk chunk) {
        int vaoID = createVAO(chunk);
        CopyOnWriteArrayList<Integer> v = new CopyOnWriteArrayList<>();
        bindIndicesBuffer(indices, v);
        storeDataInAttributeList(0, 3, positions, v);
        storeDataInAttributeList(1, 2, textureCoords, v);
        vbos.put(chunk, v);
        unbindVAO();
        return new RawModel(vaoID, indices.length);
    }


    public void unloadChunk(Chunk chunk){
        List<Integer> ids = vbos.get(chunk);

        if(!vbos.containsKey(chunk)){
            for(Chunk c : vbos.keySet()){

                if(c.MIN_X == chunk.MIN_X && c.MIN_Z == chunk.MIN_Z){
                    ids = vbos.get(c);
                }
            }
        }
        if(ids == null)return;
        for(int vbo : ids){
            GL15.glDeleteBuffers(vbo);
        }
        int id = vaos.get(chunk);
        GL30.glDeleteVertexArrays(id);


        vaos.remove(chunk);
        vbos.remove(chunk);
    }



    public RawModel loadToVAO(float[] positions, int dimensions) {
        int vaoID = createVAO();
        this.storeDataInAttributeList(0, dimensions, positions);
        unbindVAO();
        return new RawModel(vaoID, positions.length / dimensions);
    }



    public int loadTexture(String fileName) {
        Texture texture = null;
        try {
            texture = TextureLoader.getTexture("PNG", new FileInputStream("res/" + fileName
                    + ".png"));
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
                    GL11.GL_LINEAR_MIPMAP_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.4f);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Tried to load texture " + fileName + ".png , didn't work");
            System.exit(-1);
        }
        textures.add(texture.getTextureID());
        return texture.getTextureID();
    }

    public void cleanUp() {
        for (int vao : vaos.values()) {
            GL30.glDeleteVertexArrays(vao);
        }
        for (List<Integer> vbosIn : vbos.values()) {
            for(int vbo : vbosIn){
                GL15.glDeleteBuffers(vbo);
            }
        }
        for (int texture : textures) {
            GL11.glDeleteTextures(texture);
        }
    }

    public int loadCubeMap(String[] textureFiles) {
        int texID = GL11.glGenTextures();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texID);

        for (int i = 0; i < textureFiles.length; i++) {
            TextureData data = decodeTextureFile("res/" + textureFiles[i] + ".png");
            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
        }

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        textures.add(texID);
        return texID;
    }

    protected TextureData decodeTextureFile(String fileName) {
        int width = 0;
        int height = 0;
        ByteBuffer buffer = null;
        try {
            FileInputStream in = new FileInputStream(fileName);
            PNGDecoder decoder = new PNGDecoder(in);
            width = decoder.getWidth();
            height = decoder.getHeight();
            buffer = ByteBuffer.allocateDirect(4 * width * height);
            decoder.decode(buffer, width * 4, Format.RGBA);
            buffer.flip();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Tried to load texture " + fileName + ", didn't work");
            System.exit(-1);
        }
        return new TextureData(buffer, width, height);
    }

    protected int createVAO(Chunk chunk) {
        int vaoID = GL30.glGenVertexArrays();
        vaos.put(chunk,vaoID);
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }
    protected int createVAO() {
        int vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }

    protected void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data, List<Integer> vbos) {
        int vboID = GL15.glGenBuffers();

        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    protected void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
        int vboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    protected void unbindVAO() {
        GL30.glBindVertexArray(0);
    }

    protected void bindIndicesBuffer(int[] indices, List<Integer> vbos) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = storeDataInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
    }

    protected IntBuffer storeDataInIntBuffer(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    protected FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

}
