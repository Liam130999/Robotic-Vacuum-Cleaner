package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Manages the termination output of the system and generates an `output_file.json` file.
 * The class aggregates final statistics and results collected during the system's runtime,
 * including runtime ticks, detected and tracked objects, landmarks, and their details.
 */
public class TerminationOutput {
    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    private List<LandMark> landMarks;

    /**
     * Constructs a new TerminationOutput instance with final statistical results.
     *
     * @param systemRuntime      The total runtime of the system in ticks.
     * @param numDetectedObjects The total number of detected objects.
     * @param numTrackedObjects  The total number of tracked objects.
     * @param numLandmarks       The total number of identified landmarks.
     * @param landMarks          A list of {@link LandMark} representing identified landmarks.
     * @pre systemRuntime >= 0 && numDetectedObjects >= 0 && numTrackedObjects >= 0 &&
     *      numLandmarks >= 0 && landMarks != null
     * @post this.systemRuntime == systemRuntime && this.numDetectedObjects == numDetectedObjects &&
     *       this.numTrackedObjects == numTrackedObjects && this.numLandmarks == numLandmarks &&
     *       this.landMarks == landMarks
     */
    public TerminationOutput(int systemRuntime,
                             int numDetectedObjects,
                             int numTrackedObjects,
                             int numLandmarks,
                             List<LandMark> landMarks) {
        this.systemRuntime = systemRuntime;
        this.numDetectedObjects = numDetectedObjects;
        this.numTrackedObjects = numTrackedObjects;
        this.numLandmarks = numLandmarks;
        this.landMarks = landMarks;
    }

    /**
     * Generates an `output_file.json` file containing the termination statistics and details.
     * The file includes runtime ticks, detected and tracked objects, landmarks, and other relevant information.
     *
     * @pre none
     * @post A file named 'output_file.json' is created with serialized termination data.
     * @throws IOException If there is an error creating or writing to the JSON file.
     */
    public void generateJsonFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("output_file.json")) {
            gson.toJson(this, writer);
            System.out.println("output_file.json has been successfully created.");
        } catch (IOException e) {
            System.err.println("Failed to create output_file.json: " + e.getMessage());
        }
    }
}
