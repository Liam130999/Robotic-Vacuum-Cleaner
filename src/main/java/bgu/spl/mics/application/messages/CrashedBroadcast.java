package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * A broadcast message indicating that a crash has occurred in the system.
 */
public class CrashedBroadcast implements Broadcast {
    private final String sender;
    private final String errorMaker;
    private final String message;

    /**
     * Constructs a CrashedBroadcast with the specified details.
     *
     * @param sender     The sender of the broadcast.
     * @param errorMaker The component that caused the error.
     * @param message    Additional information about the crash.
     */
    public CrashedBroadcast(String sender, String errorMaker, String message) {
        this.sender = sender;
        this.errorMaker = errorMaker;
        this.message = message;
    }

    /**
     * Gets the sender of the broadcast.
     *
     * @return The sender's identifier.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Gets the component that caused the error.
     *
     * @return The error-causing component's identifier.
     */
    public String getErrorMaker() {
        return errorMaker;
    }

    /**
     * Gets the message containing additional information about the crash.
     *
     * @return The crash message.
     */
    public String getMessage() {
        return message;
    }
}
