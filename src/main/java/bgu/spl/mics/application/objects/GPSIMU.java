package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Represents the robot's GPS and IMU (Inertial Measurement Unit) system.
 * Provides information about the robot's position, movement, and orientation.
 * Responsible for loading pose data from a JSON file and managing runtime pose information.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private List<Pose> poseList;

    /**
     * Constructs a GPSIMU instance and initializes it with pose data from a JSON file.
     *
     * @param posePath The path to the JSON file containing pose data.
     * @pre posePath != null && !posePath.isEmpty()
     * @post this.currentTick == 0 && this.status == STATUS.UP && this.poseList != null
     */
    public GPSIMU(String posePath) {
        this.currentTick = 0;
        this.status = STATUS.UP;
        this.poseList = null;
        loadPosesFromJson(posePath);
    }

    /**
     * Loads Pose data from a JSON file into the pose list.
     *
     * @param jsonFilePath The path to the JSON file containing pose data.
     * @pre jsonFilePath != null && !jsonFilePath.isEmpty()
     * @post this.poseList != null
     * @throws IOException If there is an issue reading the JSON file.
     */
    public void loadPosesFromJson(String jsonFilePath) {
        try (FileReader reader = new FileReader(jsonFilePath)) {
            Gson gson = new Gson();
            Type poseListType = new TypeToken<List<Pose>>() {}.getType();
            this.poseList = gson.fromJson(reader, poseListType);
            System.out.println("Pose data loaded successfully. Total poses: " + (poseList != null ? poseList.size() : 0));
        } catch (IOException e) {
            System.err.println("Failed to load Pose data from JSON: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while loading Pose data: " + e.getMessage());
        }
    }

    /**
     * Retrieves the current status of the GPSIMU system.
     *
     * @return The current {@link STATUS} of the GPSIMU system.
     * @pre none
     * @post result == this.status
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * Updates the status of the GPSIMU system.
     *
     * @param status The new {@link STATUS} to set.
     * @pre status != null
     * @post this.status == status
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }

    /**
     * Retrieves the list of robot poses.
     *
     * @return A list of {@link Pose} objects representing the robot's historical positions.
     * @pre none
     * @post result == this.poseList
     */
    public List<Pose> getPoseList() {
        return poseList;
    }

    /**
     * Sets the current tick value for the GPSIMU system.
     *
     * @param currentTick The current tick value to set.
     * @pre currentTick >= 0
     * @post this.currentTick == currentTick
     */
    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    /**
     * Retrieves the current tick value of the GPSIMU system.
     *
     * @return The current tick value.
     * @pre none
     * @post result == this.currentTick
     */
    public int getCurrentTick() {
        return currentTick;
    }
}
