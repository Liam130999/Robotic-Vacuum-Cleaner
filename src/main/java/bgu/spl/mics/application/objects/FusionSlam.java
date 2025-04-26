package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the fusion of sensor data for Simultaneous Localization and Mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private final List<LandMark> landmarks;
    private final List<Pose> poses;

    /**
     * Inner static class responsible for holding the singleton instance of FusionSlam.
     * This approach ensures thread safety and lazy initialization.
     */
    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();
    }

    /**
     * Private constructor to enforce the Singleton pattern.
     * Initializes the landmarks and poses lists.
     *
     * @pre none
     * @post landmarks.isEmpty() && poses.isEmpty()
     */
    private FusionSlam() {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
    }

    /**
     * Retrieves the single instance of FusionSlam.
     *
     * @return The singleton instance of {@link FusionSlam}.
     * @pre none
     * @post result != null
     */
    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    /**
     * Retrieves the list of landmarks in the current SLAM map.
     *
     * @return A list of {@link LandMark} representing detected landmarks.
     * @pre none
     * @post result == this.landmarks
     */
    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    /**
     * Retrieves the list of recorded poses during SLAM processing.
     *
     * @return A list of {@link Pose} representing the robot's positions and orientations.
     * @pre none
     * @post result == this.poses
     */
    public List<Pose> getPoses() {
        return poses;
    }
}
