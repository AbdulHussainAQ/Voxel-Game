package models;

import java.io.Serializable;

public class RawModel implements Serializable {

    private Integer vaoID;
    private Integer vertexCount;

    public RawModel(int vaoID, int vertexCount) {
        this.vaoID = vaoID;
        this.vertexCount = vertexCount;
    }

    public int getVaoID() {
        return vaoID;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void setVaoID(Integer num){
        this.vaoID = num;
    }

    public void setVertexCount(Integer num){
        this.vaoID = num;
    }


}
