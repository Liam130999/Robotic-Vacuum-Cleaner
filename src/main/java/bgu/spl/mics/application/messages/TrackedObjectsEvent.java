package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;

/**
 * Represents an event containing a list of tracked objects detected by a LiDAR service or sensor.
 * This event is used to communicate processed tracking data to the Fusion-SLAM service.
 *
 * @param <Boolean> The expected result type when processing this event.
 */
public class TrackedObjectsEvent implements Event<Boolean> {
    private final List<TrackedObject> trackedObjects;
    private final String sender;

    /**
     * Constructs a new TrackedObjectsEvent with a list of tracked objects and the sender's identifier.
     *
     * @param trackedObjects The list of tracked objects detected. Must not be null.
     * @param sender         The identifier of the service or sensor sending this event.
     * @pre trackedObjects != null
     * @post this.trackedObjects == trackedObjects && this.sender == sender
     * @throws IllegalArgumentException if trackedObjects is null.
     */
    public TrackedObjectsEvent(List<TrackedObject> trackedObjects, String sender) {
        if (trackedObjects == null) {
            throw new IllegalArgumentException("trackedObjects cannot be null");
        }
        this.trackedObjects = trackedObjects;
        this.sender = sender;
    }

    /**
     * Retrieves the list of tracked objects associated with this event.
     *
     * @return The list of tracked objects.
     * @pre none
     * @post result == this.trackedObjects
     */
    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    /**
     * Retrieves the sender's identifier.
     *
     * @return The name of the service or sensor that sent this event.
     * @pre none
     * @post result == this.sender
     */
    public String getSender() {
        return sender;
    }
}
