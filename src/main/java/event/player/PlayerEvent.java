package event.player;

import entities.Player;
import event.Event;
import org.jetbrains.annotations.NotNull;

public class PlayerEvent extends Event {

    protected Player player;

    public PlayerEvent(@NotNull Player who) {
        this.player = who;
    }

    PlayerEvent(@NotNull Player who, boolean async) {
        super(async);
        this.player = who;
    }

    @NotNull
    public final Player getPlayer() {
        return this.player;
    }

}
