package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents the configuration settings parsed from the system's configuration file.
 * This class encapsulates the configurations for cameras, LiDAR workers, pose data, and timing parameters.
 */
public class Config {
    /** Configuration for cameras. */
    public Cameras Cameras;

    /** Configuration for LiDAR workers. */
    public LiDarWorkers LiDarWorkers;

    /** Path to the pose data JSON file. */
    public String poseJsonFile;

    /** Duration of a single system tick, in milliseconds. */
    public int TickTime;

    /** Total duration of the simulation, in ticks. */
    public int Duration;

    /**
     * Represents the configuration settings for cameras.
     */
    public static class Cameras {
        /** List of individual camera configurations. */
        public List<CameraConfig> CamerasConfigurations;

        /** Path to the camera data JSON file. */
        public String camera_datas_path;
    }

    /**
     * Represents the configuration of a single camera.
     */
    public static class CameraConfig {
        /** Unique identifier for the camera. */
        public int id;

        /** Frequency at which the camera captures data (in ticks). */
        public int frequency;

        /** Unique key associated with the camera in the JSON file. */
        public String camera_key;
    }

    /**
     * Represents the configuration settings for LiDAR workers.
     */
    public static class LiDarWorkers {
        /** List of individual LiDAR worker configurations. */
        public List<LidarConfig> LidarConfigurations;

        /** Path to the LiDAR data JSON file. */
        public String lidars_data_path;
    }

    /**
     * Represents the configuration of a single LiDAR worker.
     */
    public static class LidarConfig {
        /** Unique identifier for the LiDAR worker. */
        public int id;

        /** Frequency at which the LiDAR worker captures data (in ticks). */
        public int frequency;
    }
}
