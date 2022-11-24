package textures;

import java.io.Serializable;

public class ModelTexture implements Serializable {

    private final int textureID;

    private int numberOfRows = 1;

    public ModelTexture(int texture) {
        this.textureID = texture;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }


    public int getID() {
        return textureID;
    }


}
