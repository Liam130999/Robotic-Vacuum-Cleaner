package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents an object tracked by the LiDAR system.
 * This object contains information such as the object's unique identifier,
 * the time it was tracked, a textual description, and its coordinates in the environment.
 */
public class TrackedObject {
    private final String id;
    private int time;
    private final String description;
    private final List<CloudPoint> coordinates;

    /**
     * Constructs a new TrackedObject with the given parameters.
     *
     * @param id          The unique identifier for the tracked object. Must not be null or empty.
     * @param time        The timestamp indicating when the object was tracked. Must not be negative.
     * @param description A textual description of the object. Must not be null.
     * @param coordinates A list of {@link CloudPoint} representing the object's coordinates. Must not be null.
     * @pre id != null && !id.isEmpty() && time >= 0 && description != null && coordinates != null
     * @post this.id == id && this.time == time && this.description == description && this.coordinates == coordinates
     * @throws IllegalArgumentException if id is null or empty, time is negative, description is null, or coordinates is null.
     */
    public TrackedObject(String id, int time, String description, List<CloudPoint> coordinates) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        if (time < 0) {
            throw new IllegalArgumentException("Time cannot be negative");
        }

        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
        this.time = time;
    }

    /**
     * Retrieves the unique identifier of the tracked object.
     *
     * @return The unique identifier of the tracked object.
     * @pre none
     * @post result == this.id
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the timestamp when the object was tracked.
     *
     * @return The time the object was tracked.
     * @pre none
     * @post result == this.time
     */
    public int getTime() {
        return time;
    }

    /**
     * Retrieves the textual description of the tracked object.
     *
     * @return The description of the tracked object.
     * @pre none
     * @post result == this.description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves the list of coordinates of the tracked object.
     *
     * @return A list of {@link CloudPoint} representing the object's coordinates.
     * @pre none
     * @post result == this.coordinates
     */
    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    /**
     * Provides a JSON-like string representation of the tracked object.
     * Includes its ID, time, description, and coordinates.
     *
     * @return A JSON-formatted string representing the tracked object.
     * @pre none
     * @post result != null
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[{\"id\":\"").append(id).append("\",")
                .append("\"time\":").append(time).append(",")
                .append("\"description\":\"").append(description).append("\",")
                .append("\"coordinates\":[");

        // Iterate through coordinates
        for (int i = 0; i < coordinates.size(); i++) {
            sb.append(coordinates.get(i).toString());
            if (i < coordinates.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]}]");

        return sb.toString();
    }
}
