package event;

import org.jetbrains.annotations.NotNull;

public abstract class Event {

    private String name;
    private final boolean async;

    public Event() {
        this(false);
    }

    public Event(boolean isAsync) {
        this.async = isAsync;
    }

    @NotNull
    public String getEventName() {
        if (this.name == null) {
            this.name = this.getClass().getSimpleName();
        }

        return this.name;
    }



    public final boolean isAsynchronous() {
        return this.async;
    }

    public static enum Result {
        DENY,
        DEFAULT,
        ALLOW;

        private Result() {
        }
    }

}
