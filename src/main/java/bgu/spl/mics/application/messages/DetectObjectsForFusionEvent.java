package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

/**
 * Event representing an object detection event from a camera.
 * It contains detected objects at a specific timestamp.
 */
public class DetectObjectsForFusionEvent implements Event<Boolean> {
    private final StampedDetectedObjects stampedDetectedObjects; // Immutable reference to detected objects
    private final String sender;

    /**
     * Constructor for DetectObjectsEvent.
     *
     * @param stampedDetectedObjects The detected objects with a timestamp.
     * @param sender The sender name
     */
    public DetectObjectsForFusionEvent(StampedDetectedObjects stampedDetectedObjects, String sender) {
        if (stampedDetectedObjects == null) {
            throw new IllegalArgumentException("stampedDetectedObjects cannot be null");
        }
        this.stampedDetectedObjects = stampedDetectedObjects;
        this.sender = sender;
    }

    /**
     * @return The detected objects with their associated timestamp.
     */
    public StampedDetectedObjects getStampedDetectedObjects() {
        return stampedDetectedObjects;
    }

    /**
     * @return The sender name.
     */
    public String getSender() {
        return sender;
    }
}