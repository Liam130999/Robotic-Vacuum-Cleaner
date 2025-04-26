package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.Map;

/**
 * Represents a mapping of camera identifiers to their detections.
 * This class is responsible for managing and storing data parsed from camera sources.
 */
public class CameraData {
    private Map<String, List<StampedDetectedObjects>> cameras;

    /**
     * Constructs an empty CameraData object.
     * The camera mapping can later be initialized using {@link #setCameras(Map)}.
     *
     * @pre none
     * @post cameras == null
     */
    public CameraData() {
    }

    /**
     * Retrieves the mapping of camera identifiers to their detected objects.
     *
     * @return A map where each key represents a camera ID, and each value is a list of
     * {@link StampedDetectedObjects} detected by that camera.
     * @pre none
     * @post result == this.cameras
     */
    public Map<String, List<StampedDetectedObjects>> getCameras() {
        return cameras;
    }

    /**
     * Sets the mapping of camera identifiers to their detected objects.
     *
     * @param cameras A map where each key represents a camera ID, and each value is a list of
     *                {@link StampedDetectedObjects} detected by that camera.
     * @pre cameras != null
     * @post this.cameras == cameras
     */
    public void setCameras(Map<String, List<StampedDetectedObjects>> cameras) {
        this.cameras = cameras;
    }

    /**
     * Provides a string representation of the CameraData object.
     *
     * @return A string representation containing camera mappings and their detections.
     * @pre none
     * @post result != null
     */
    @Override
    public String toString() {
        return "CameraData{" +
                "cameras=" + cameras +
                '}';
    }
}
