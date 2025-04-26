package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private int time;
    private final List<DetectedObject> detectedObjects;

    /**
     * Constructs a new StampedDetectedObjects instance with a timestamp and a list of detected objects.
     *
     * @param time            The time of detection in simulation ticks. Must not be negative.
     * @param detectedObjects A list of {@link DetectedObject} representing objects detected by the camera. Must not be null.
     * @pre time >= 0 && detectedObjects != null
     * @post this.time == time && this.detectedObjects == detectedObjects
     * @throws IllegalArgumentException if time is negative or detectedObjects is null.
     */
    public StampedDetectedObjects(int time, List<DetectedObject> detectedObjects) {
        if (detectedObjects == null) {
            throw new IllegalArgumentException("List cannot be null");
        }

        if (time < 0) {
            throw new IllegalArgumentException("Timestamp cannot be negative");
        }

        this.time = time;
        this.detectedObjects = detectedObjects;
    }

    /**
     * Retrieves the time at which the objects were detected.
     *
     * @return The detection timestamp in simulation ticks.
     * @pre none
     * @post result == this.time
     */
    public int getTime() {
        return time;
    }

    /**
     * Retrieves the list of detected objects.
     *
     * @return A list of {@link DetectedObject} representing objects detected at the specified time.
     * @pre none
     * @post result == this.detectedObjects
     */
    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }

    /**
     * Provides a JSON-like string representation of the detected objects and their timestamp.
     *
     * @return A JSON-formatted string containing the time and detected objects.
     * @pre none
     * @post result != null
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"time\":").append(time).append(",\"detectedObjects\":[");

        for (int i = 0; i < detectedObjects.size(); i++) {
            sb.append("{\"id\":\"").append(detectedObjects.get(i).getId()).append("\",")
                    .append("\"description\":\"").append(detectedObjects.get(i).getDescription()).append("\"}");

            if (i < detectedObjects.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]}");
        return sb.toString();
    }
}
