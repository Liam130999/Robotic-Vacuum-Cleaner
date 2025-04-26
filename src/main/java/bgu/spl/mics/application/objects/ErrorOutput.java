package bgu.spl.mics.application.utils;

import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Represents the error output structure and is responsible for generating an error_output.json file.
 * This class captures critical information about system errors and provides a clear snapshot
 * of the system's state during failure.
 */
public class ErrorOutput {

    private String error;
    private String faultySensor;
    private Map<String, StampedDetectedObjects> lastCamerasFrame;
    private Map<String, List<TrackedObject>> lastLiDarWorkerTrackersFrame;
    private List<Pose> poses;
    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    private List<LandMark> landmarks;

    /**
     * Constructs an ErrorOutput instance with details of the system's error state.
     *
     * @param error                      A description of the encountered error.
     * @param faultySensor               The identifier of the sensor responsible for the error.
     * @param lastCamerasFrame           The most recent frame detected by the cameras.
     * @param lastLiDarWorkerTrackersFrame The most recent data tracked by LiDAR workers.
     * @param poses                      A list of robot poses recorded up to the error point.
     * @param systemRuntime              The total runtime of the system in ticks before failure.
     * @param numDetectedObjects         The number of objects detected by cameras.
     * @param numTrackedObjects          The number of objects tracked by LiDAR workers.
     * @param numLandmarks               The total number of unique landmarks detected.
     * @param landmarks                  A list of landmarks recorded in the system.
     * @pre error != null && faultySensor != null && lastCamerasFrame != null &&
     *      lastLiDarWorkerTrackersFrame != null && poses != null && landmarks != null
     * @post All parameters are assigned to their corresponding fields.
     */
    public ErrorOutput(String error,
                       String faultySensor,
                       Map<String, StampedDetectedObjects> lastCamerasFrame,
                       Map<String, List<TrackedObject>> lastLiDarWorkerTrackersFrame,
                       List<Pose> poses,
                       int systemRuntime,
                       int numDetectedObjects,
                       int numTrackedObjects,
                       int numLandmarks,
                       List<LandMark> landmarks) {
        this.error = error;
        this.faultySensor = faultySensor;
        this.lastCamerasFrame = lastCamerasFrame;
        this.lastLiDarWorkerTrackersFrame = lastLiDarWorkerTrackersFrame;
        this.poses = poses;
        this.systemRuntime = systemRuntime;
        this.numDetectedObjects = numDetectedObjects;
        this.numTrackedObjects = numTrackedObjects;
        this.numLandmarks = numLandmarks;
        this.landmarks = landmarks;
    }

    /**
     * Generates an `error_output.json` file containing the error details and system state at failure.
     * The generated JSON file is formatted for readability.
     *
     * @pre none
     * @post A file named 'error_output.json' is created with the error details serialized in JSON format.
     * @throws IOException If there is an issue creating or writing to the file.
     */
    public void generateJsonFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("error_output.json")) {
            gson.toJson(this, writer);
            System.out.println("error_output.json has been successfully created.");
        } catch (IOException e) {
            System.err.println("Failed to create error_output.json: " + e.getMessage());
        }
    }
}
