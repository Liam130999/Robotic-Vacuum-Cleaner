package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 * Each landmark has a unique identifier, a description, and a list of 3D coordinates.
 */
public class LandMark {
    private final String id;
    private final String description;
    private final List<CloudPoint> coordinates;

    /**
     * Constructs a new LandMark with the specified ID, description, and coordinates.
     *
     * @param id          The unique identifier for the landmark. Must not be null or empty.
     * @param description A textual description of the landmark. Must not be null.
     * @param coordinates A list of {@link CloudPoint} representing the coordinates of the landmark. Must not be null.
     * @pre id != null && !id.isEmpty() && description != null && coordinates != null
     * @post this.id == id && this.description == description && this.coordinates == coordinates
     */
    public LandMark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    /**
     * Retrieves the unique identifier of the landmark.
     *
     * @return The ID of the landmark.
     * @pre none
     * @post result == this.id
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the description of the landmark.
     *
     * @return The description of the landmark.
     * @pre none
     * @post result == this.description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves the list of coordinates associated with the landmark.
     *
     * @return A list of {@link CloudPoint} representing the landmark's coordinates.
     * @pre none
     * @post result == this.coordinates
     */
    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    /**
     * Provides a JSON-like string representation of the landmark.
     * The string includes the landmark's ID, description, and coordinates.
     *
     * @return A JSON-like formatted string representing the landmark.
     * @pre none
     * @post result != null
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(id).append("\",");
        sb.append("\"description\":\"").append(description).append("\",");
        sb.append("\"coordinates\":[");

        for (int i = 0; i < coordinates.size(); i++) {
            sb.append(coordinates.get(i).toString());
            if (i < coordinates.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]}");
        return sb.toString();
    }
}
