package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Represents a singleton database for managing LiDAR data.
 * It is responsible for loading LiDAR cloud point data from a JSON file and providing access to it.
 */
public class LiDarDataBase {
    private List<StampedCloudPoints> cloudPoints;

    /**
     * Inner static class responsible for holding the singleton instance of {@link LiDarDataBase}.
     * Ensures thread-safe, lazy initialization of the instance.
     */
    private static class SingletonHolder {
        private static final LiDarDataBase instance = new LiDarDataBase();
    }

    /**
     * Private constructor to enforce the Singleton pattern.
     * Initialization is deferred until the first call to {@link #getInstance()}.
     *
     * @pre none
     * @post this.cloudPoints == null
     */
    private LiDarDataBase() {
    }

    /**
     * Retrieves the singleton instance of {@link LiDarDataBase}.
     *
     * @return The singleton instance of {@link LiDarDataBase}.
     * @pre none
     * @post result != null
     */
    public static LiDarDataBase getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Loads cloud point data from a JSON file.
     * The data is parsed into a list of {@link StampedCloudPoints}.
     *
     * @param filePath The path to the JSON file containing LiDAR cloud point data.
     * @pre filePath != null && !filePath.isEmpty()
     * @post this.cloudPoints != null
     * @throws RuntimeException If an error occurs while reading or parsing the JSON file.
     */
    public void loadFromJson(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<StampedCloudPoints>>() {}.getType();
            cloudPoints = gson.fromJson(reader, listType);
            System.out.println("LiDar database loaded successfully from JSON.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load LiDar data from JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the loaded LiDAR cloud point data.
     *
     * @return A list of {@link StampedCloudPoints} representing the loaded LiDAR data.
     * @pre none
     * @post result == this.cloudPoints
     */
    public List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }
}
