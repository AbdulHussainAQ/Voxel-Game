package event.player;

import entities.Player;
import org.jetbrains.annotations.NotNull;
import world.chunk.Chunk;

public class PlayerChunkSwitchEvent extends PlayerEvent{

    public Chunk chunkFrom;
    public Chunk chunkTo;



    public PlayerChunkSwitchEvent(@NotNull Player who, Chunk chunkFrom, Chunk chunkTo) {
        super(who, true);
        this.chunkFrom = chunkFrom;
        this.chunkTo = chunkTo;
    }
}
