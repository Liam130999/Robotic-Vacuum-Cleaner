package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * Represents a broadcast message signaling that a service or sensor
 * has completed its operations and is terminating gracefully.
 */
public class TerminatedBroadcast implements Broadcast {
    private final String sender;

    /**
     * Constructs a new TerminatedBroadcast with the specified sender.
     *
     * @param sender The identifier of the service or sensor sending the termination broadcast.
     * @pre sender != null
     * @post this.sender == sender
     */
    public TerminatedBroadcast(String sender) {
        this.sender = sender;
    }

    /**
     * Retrieves the sender of this broadcast.
     *
     * @return The identifier of the service or sensor that sent the termination broadcast.
     * @pre none
     * @post result == this.sender
     */
    public String getSender() {
        return sender;
    }
}
