package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * Represents a broadcast message that signals the passage of a single time tick
 * in the system's global clock.
 */
public class TickBroadcast implements Broadcast {
    private final int tick;

    /**
     * Constructs a new TickBroadcast with the given tick value.
     *
     * @param tick The current tick count in the system.
     * @pre tick >= 0
     * @post this.tick == tick
     */
    public TickBroadcast(int tick) {
        this.tick = tick;
    }

    /**
     * Retrieves the current tick value.
     *
     * @return The tick count associated with this broadcast.
     * @pre none
     * @post result == this.tick
     */
    public int getTick() {
        return tick;
    }
}
