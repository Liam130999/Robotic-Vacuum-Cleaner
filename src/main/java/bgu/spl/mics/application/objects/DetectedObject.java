package bgu.spl.mics.application.objects;

/**
 * Represents an object detected by the camera.
 * It contains information such as the object's unique identifier and a textual description.
 */
public class DetectedObject {
    private final String id;
    private final String description;

    /**
     * Constructs a new DetectedObject with a unique identifier and a description.
     *
     * @param id          The unique identifier for the detected object. Must not be null or empty.
     * @param description A textual description of the detected object. Must not be null.
     * @pre id != null && !id.isEmpty() && description != null
     * @post this.id == id && this.description == description
     * @throws IllegalArgumentException if id is null, empty, or if description is null.
     */
    public DetectedObject(String id, String description) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }

        this.id = id;
        this.description = description;
    }

    /**
     * Retrieves the unique identifier of the detected object.
     *
     * @return The object's unique identifier.
     * @pre none
     * @post result == this.id
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the textual description of the detected object.
     *
     * @return The object's description.
     * @pre none
     * @post result == this.description
     */
    public String getDescription() {
        return description;
    }
}
