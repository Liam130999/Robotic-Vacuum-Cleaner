package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment at regular intervals.
 */
public class Camera {
    private final int id;
    private final int frequency;
    private final List<StampedDetectedObjects> detectedObjectsList; // Detected objects at specific times
    private STATUS status; // Status of the camera (Up, Down, Error)

    /**
     * Constructs a new Camera instance.
     *
     * @param id                  The unique identifier of the camera. Must be non-negative.
     * @param frequency           The frequency at which the camera detects objects. Must be non-negative.
     * @param detectedObjectsList List of detected objects with timestamps. Must not be null.
     * @pre id >= 0 && frequency >= 0 && detectedObjectsList != null
     * @post this.id == id && this.frequency == frequency && this.detectedObjectsList == detectedObjectsList
     * @throws IllegalArgumentException if id or frequency are negative, or if detectedObjectsList is null.
     */
    public Camera(int id, int frequency, List<StampedDetectedObjects> detectedObjectsList) {
        if (id < 0) {
            throw new IllegalArgumentException("id cannot be negative");
        }

        if (frequency < 0) {
            throw new IllegalArgumentException("frequency cannot be negative");
        }

        if (detectedObjectsList == null) {
            throw new IllegalArgumentException("detectedObjectsList cannot be null");
        }

        this.id = id;
        this.frequency = frequency;
        this.detectedObjectsList = detectedObjectsList;
        this.status = STATUS.UP;
    }

    /**
     * Retrieves the unique identifier of the camera.
     *
     * @return The camera's unique ID.
     * @pre none
     * @post result == this.id
     */
    public int getId() {
        return id;
    }

    /**
     * Retrieves the frequency at which the camera operates.
     *
     * @return The frequency of the camera.
     * @pre none
     * @post result == this.frequency
     */
    public int getFreq() {
        return frequency;
    }

    /**
     * Retrieves the list of detected objects with timestamps.
     *
     * @return A list of {@link StampedDetectedObjects} representing detected objects.
     * @pre none
     * @post result == this.detectedObjectsList
     */
    public List<StampedDetectedObjects> getDetectedObjects() {
        return detectedObjectsList;
    }

    /**
     * Sets the status of the camera.
     *
     * @param status The new status of the camera.
     * @pre status != null
     * @post this.status == status
     */
    public void SetStatus(STATUS status) {
        this.status = status;
    }

    /**
     * Retrieves the current status of the camera.
     *
     * @return The current {@link STATUS} of the camera.
     * @pre none
     * @post result == this.status
     */
    public STATUS getStatus() {
        return status;
    }
}
