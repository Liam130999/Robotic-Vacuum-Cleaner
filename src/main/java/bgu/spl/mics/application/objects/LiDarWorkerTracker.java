package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Manages a single LiDAR worker responsible for detecting and tracking objects.
 * The worker processes {@link DetectObjectsEvent} and generates {@link TrackedObjectsEvent}
 * using data from the {@link LiDarDataBase}.
 * Each worker tracks objects and sends observations to the {@link FusionSlam} service.
 */
public class LiDarWorkerTracker {
    private final int id;
    private final int frequency;
    private STATUS status; // Status of the LiDAR (Up, Down, Error)
    private final List<TrackedObject> lastTrackedObjects; // Detected objects at specific times

    /**
     * Constructs a new LiDarWorkerTracker with a unique identifier, frequency, and tracked objects list.
     *
     * @param id                  The unique identifier of the LiDAR worker. Must be non-negative.
     * @param frequency           The frequency at which the LiDAR detects objects. Must be non-negative.
     * @param lastTrackedObjects  A list of tracked objects with timestamps. Must not be null.
     * @pre id >= 0 && frequency >= 0 && lastTrackedObjects != null
     * @post this.id == id && this.frequency == frequency && this.lastTrackedObjects == lastTrackedObjects
     * @throws IllegalArgumentException if id or frequency are negative, or if lastTrackedObjects is null.
     */
    public LiDarWorkerTracker(int id, int frequency, List<TrackedObject> lastTrackedObjects) {
        if (id < 0) {
            throw new IllegalArgumentException("id cannot be negative");
        }

        if (frequency < 0) {
            throw new IllegalArgumentException("frequency cannot be negative");
        }

        if (lastTrackedObjects == null) {
            throw new IllegalArgumentException("lastTrackedObjects cannot be null");
        }

        this.id = id;
        this.frequency = frequency;
        this.lastTrackedObjects = lastTrackedObjects;
        this.status = STATUS.UP;
    }

    /**
     * Retrieves the unique identifier of the LiDAR worker.
     *
     * @return The worker's unique ID.
     * @pre none
     * @post result == this.id
     */
    public int getId() {
        return id;
    }

    /**
     * Retrieves the frequency at which the LiDAR worker operates.
     *
     * @return The frequency of operation.
     * @pre none
     * @post result == this.frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Retrieves the current status of the LiDAR worker.
     *
     * @return The current {@link STATUS} of the worker.
     * @pre none
     * @post result == this.status
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * Updates the status of the LiDAR worker.
     *
     * @param status The new {@link STATUS} to set.
     * @pre status != null
     * @post this.status == status
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }

    /**
     * Retrieves the last set of tracked objects detected by the LiDAR worker.
     *
     * @return A list of {@link TrackedObject} representing the last tracked objects.
     * @pre none
     * @post result == this.lastTrackedObjects
     */
    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }
}
