package entities;

import lombok.Getter;
import models.TexturedModel;
import world.Location;
import world.chunk.Chunk;

import java.io.Serializable;
import java.util.UUID;

public class ChunkEntity extends Entity implements Serializable {

    @Getter
    private final UUID chunkUUID;

    @Getter
    private final Chunk chunk;

    public ChunkEntity(TexturedModel model, int index, Location position, float rotX, float rotY, float rotZ, float scale, Chunk chunk) {
        super(model, index, position, rotX, rotY, rotZ, scale);
        this.chunkUUID = chunk.getChunkUUID();
        this.chunk = chunk;
    }
}
