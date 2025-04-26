package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * This class initializes the system and starts the simulation by setting up
 * services, objects, configurations, and threading.
 */
public class GurionRockRunner {

    /**
     * Parses the configuration file to load simulation parameters.
     *
     * @param configFilePath The path to the JSON configuration file.
     * @return A {@link Config} object containing simulation configuration data.
     * @pre configFilePath != null && configFilePath points to a valid JSON file.
     * @post result == null || result contains valid simulation configuration.
     * @throws IOException If the configuration file cannot be read.
     * @throws JsonSyntaxException If the JSON format is invalid.
     */
    public static Config parseConfig(String configFilePath) {
        try (JsonReader reader = new JsonReader(new FileReader(configFilePath))) {
            Gson gson = new Gson();
            return gson.fromJson(reader, Config.class);
        } catch (IOException e) {
            System.err.println("Failed to load configuration file: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON format: " + e.getMessage());
        }
        return null;
    }

    /**
     * Parses camera data from a JSON file and converts it into a map structure.
     *
     * @param camerasFilePath The file path to the camera JSON data.
     * @return A map where each key is a camera identifier and each value is a list of {@link StampedDetectedObjects}.
     * @pre camerasFilePath != null && camerasFilePath points to a valid JSON file.
     * @post result == null || result.size() >= 0
     * @throws IOException If there's an issue reading the file.
     * @throws JsonSyntaxException If the JSON format is invalid.
     */
    public static Map<String, List<StampedDetectedObjects>> parseCameras(String camerasFilePath) {
        try (JsonReader cameraReader = new JsonReader(new FileReader(camerasFilePath))) {
            Gson gson = new Gson();
            Type cameraDataType = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();
            return gson.fromJson(cameraReader, cameraDataType);
        } catch (IOException e) {
            System.err.println("Failed to load file: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
        return null;
    }

    /**
     * The main entry point for the simulation.
     * <p>
     * Parses the configuration and camera data files, initializes services, and starts the simulation.
     * All services run in separate threads.
     * </p>
     *
     * @param args Command-line arguments. The first argument must be the path to the configuration file.
     * @pre args != null && args.length > 0 && args[0] points to a valid JSON configuration file.
     * @post Simulation starts with all services running in their respective threads.
     * @throws InterruptedException If a thread is interrupted while waiting for termination.
     */
    public static void main(String[] args) {
        AtomicInteger sensorsCounter = new AtomicInteger(0);


        if (args.length == 0) {
            System.out.println("Please provide the path to the configuration file as an argument.");
            return;
        }

        // Load configuration file
        String configFilePath = args[0];
        Config config = parseConfig(configFilePath);
        if (config == null) {
            System.err.println("Failed to load configuration file: " + configFilePath);
            return;
        }

        StatisticalFolder folder = new StatisticalFolder();

        // Load camera data
        Map<String, List<StampedDetectedObjects>> cameraData = parseCameras(config.Cameras.camera_datas_path);
        if (cameraData == null) {
            System.err.println("Failed to load cameras: " + config.Cameras.camera_datas_path);
            return;
        }


        // Initialize global latch
        int totalServices = config.Cameras.CamerasConfigurations.size() +
                config.LiDarWorkers.LidarConfigurations.size() + 2; // PoseService & FusionSlamService
        ServiceInitLatch.latch = new CountDownLatch(totalServices);


        // Initialize camera services
        List<CameraService> cameraServicesList = new ArrayList<>();
        for (Config.CameraConfig cam : config.Cameras.CamerasConfigurations) {
            List<StampedDetectedObjects> list = cameraData.getOrDefault(cam.camera_key, new ArrayList<>());
            Camera camera = new Camera(cam.id, cam.frequency, list);
            cameraServicesList.add(new CameraService(camera, folder));
            sensorsCounter.incrementAndGet();
        }

        // Start camera services in separate threads
        List<Thread> cameraThreads = new ArrayList<>();
        for (CameraService cam : cameraServicesList) {
            Thread thread = new Thread(cam);
            thread.start();
            cameraThreads.add(thread);
        }

        // Load LiDAR data
        LiDarDataBase lidarDataBase = LiDarDataBase.getInstance();
        lidarDataBase.loadFromJson(config.LiDarWorkers.lidars_data_path);

        // Initialize LiDAR services
        List<LiDarService> liDarServicesList = new ArrayList<>();
        for (Config.LidarConfig lidar : config.LiDarWorkers.LidarConfigurations) {
            liDarServicesList.add(new LiDarService(new LiDarWorkerTracker(lidar.id, lidar.frequency, new ArrayList<>()), folder));
            sensorsCounter.incrementAndGet();
        }

        // Start LiDAR services in separate threads
        List<Thread> lidarThreads = new ArrayList<>();
        for (LiDarService liDarService : liDarServicesList) {
            Thread thread = new Thread(liDarService);
            thread.start();
            lidarThreads.add(thread);
        }

        // Initialize and start PoseService
        GPSIMU gpsimu = new GPSIMU(config.poseJsonFile);
        PoseService poseService = new PoseService(gpsimu);
        sensorsCounter.incrementAndGet();
        Thread poseThread = new Thread(poseService);
        poseThread.start();

        // Initialize and start FusionSlamService
        FusionSlamService fusionSlamService = new FusionSlamService(folder);
        Thread fusionSlamThread = new Thread(fusionSlamService);
        fusionSlamThread.start();

        // Wait until all services signal readiness
        try {
            ServiceInitLatch.latch.await();
            System.out.println("All services initialized. Starting TimeService...");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for services to initialize.");
        }


        // Initialize and start TimeService
        TimeService timeService = new TimeService(config.TickTime, config.Duration, folder, sensorsCounter);
        Thread timeThread = new Thread(timeService);
        timeThread.start();

        // Wait for TimeService to finish
        try {
            timeThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for TimeService to finish.");
        }
    }
}
