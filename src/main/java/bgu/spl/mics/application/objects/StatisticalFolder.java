package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aggregates and manages statistical information about the system's operation.
 * This class keeps track of runtime ticks, the number of detected and tracked objects,
 * and the number of identified landmarks.
 */
public class StatisticalFolder {
    private final AtomicInteger systemRuntime;
    private final AtomicInteger numDetectedObjects;
    private final AtomicInteger numTrackedObjects;
    private final AtomicInteger numLandmarks;

    /**
     * Constructs a new StatisticalFolder with initial zero values for all statistics.
     *
     * @pre none
     * @post systemRuntime.get() == 0 && numDetectedObjects.get() == 0 &&
     *       numTrackedObjects.get() == 0 && numLandmarks.get() == 0
     */
    public StatisticalFolder() {
        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
    }

    /**
     * Retrieves the total runtime of the system in ticks.
     *
     * @return The system runtime in ticks.
     * @pre none
     * @post result == systemRuntime.get()
     */
    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    /**
     * Retrieves the total number of detected objects.
     *
     * @return The number of detected objects.
     * @pre none
     * @post result == numDetectedObjects.get()
     */
    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    /**
     * Retrieves the total number of tracked objects.
     *
     * @return The number of tracked objects.
     * @pre none
     * @post result == numTrackedObjects.get()
     */
    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    /**
     * Retrieves the total number of identified landmarks.
     *
     * @return The number of identified landmarks.
     * @pre none
     * @post result == numLandmarks.get()
     */
    public int getNumLandmarks() {
        return numLandmarks.get();
    }

    /**
     * Increments the runtime tick counter by one.
     *
     * @pre none
     * @post systemRuntime.get() == systemRuntime.get()@pre + 1
     */
    public void addTick() {
        this.systemRuntime.incrementAndGet();
    }

    /**
     * Increments the detected objects counter by one.
     *
     * @pre none
     * @post numDetectedObjects.get() == numDetectedObjects.get()@pre + 1
     */
    public void addDetectedObject() {
        this.numDetectedObjects.incrementAndGet();
    }

    /**
     * Increments the tracked objects counter by one.
     *
     * @pre none
     * @post numTrackedObjects.get() == numTrackedObjects.get()@pre + 1
     */
    public void addTrackedObject() {
        this.numTrackedObjects.incrementAndGet();
    }

    /**
     * Increments the landmarks counter by one.
     *
     * @pre none
     * @post numLandmarks.get() == numLandmarks.get()@pre + 1
     */
    public void addLandmark() {
        this.numLandmarks.incrementAndGet();
    }

    /**
     * Provides a JSON-like string representation of the statistical data.
     *
     * @return A JSON-formatted string containing runtime, detected objects, tracked objects, and landmarks.
     * @pre none
     * @post result != null
     */
    @Override
    public String toString() {
        return "{" +
                "\"systemRuntime\":" + systemRuntime.get() + "," +
                "\"numDetectedObjects\":" + numDetectedObjects.get() + "," +
                "\"numTrackedObjects\":" + numTrackedObjects.get() + "," +
                "\"numLandmarks\":" + numLandmarks.get() +
                "}";
    }
}
