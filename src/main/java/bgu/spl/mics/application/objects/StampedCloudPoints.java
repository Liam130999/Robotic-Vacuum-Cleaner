package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private final String id;
    private int time;
    private final List<List<Double>> cloudPoints;

    /**
     * Constructs a new StampedCloudPoints instance with an ID, timestamp, and cloud point data.
     *
     * @param id          The unique identifier of the tracked object. Must not be null or empty.
     * @param time        The timestamp when the object was tracked. Must not be negative.
     * @param cloudPoints A list of lists representing the 3D cloud points. Must not be null.
     * @pre id != null && !id.isEmpty() && time >= 0 && cloudPoints != null
     * @post this.id == id && this.time == time && this.cloudPoints == cloudPoints
     * @throws IllegalArgumentException if id is null or empty, time is negative, or cloudPoints is null.
     */
    public StampedCloudPoints(String id, int time, List<List<Double>> cloudPoints) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id cannot be null or empty");
        }

        if (cloudPoints == null) {
            throw new IllegalArgumentException("cloudPoints cannot be null");
        }

        if (time < 0) {
            throw new IllegalArgumentException("time cannot be negative");
        }

        this.id = id;
        this.time = time;
        this.cloudPoints = cloudPoints;
    }

    /**
     * Retrieves the unique identifier of the tracked object.
     *
     * @return The object's unique ID.
     * @pre none
     * @post result == this.id
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the timestamp when the object was tracked.
     *
     * @return The timestamp of the object tracking.
     * @pre none
     * @post result == this.time
     */
    public int getTime() {
        return time;
    }

    /**
     * Retrieves the cloud point data associated with the tracked object.
     *
     * @return A list of lists of {@link Double} representing the cloud points.
     * @pre none
     * @post result == this.cloudPoints
     */
    public List<List<Double>> getCloudPoints() {
        return cloudPoints;
    }
}
