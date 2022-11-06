package models;

import textures.ModelTexture;

import java.io.Serializable;

public class TexturedModel implements Serializable {

    private RawModel rawModel;
    private ModelTexture texture;


    public TexturedModel(RawModel model, ModelTexture texture) {
        this.rawModel = model;
        this.texture = texture;
    }

    public void cleanUp(){
        rawModel = null;
        texture = null;

    }

    public RawModel getRawModel() {
        return rawModel;
    }

    public ModelTexture getTexture() {
        return texture;
    }

}
