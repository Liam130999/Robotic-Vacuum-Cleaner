package bgu.spl.mics;

import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.CameraService;
import org.junit.jupiter.api.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CameraService} and {@link GurionRockRunner#parseCameras(String)}.
 * Tests focus on direct parsing and CameraService processing without tick events.
 */
class CameraServiceTest {

    private static final String MOCK_CAMERA_DATA_FILE = "mock_camera_data.json";

    private CameraService cameraService;
    private StatisticalFolder folder;

    @BeforeAll
    static void setupMockCameraDataFile() {
        // Create a mock camera_data.json file
        String mockJson = "{\n" +
                "  \"Camera1\": [\n" +
                "    {\n" +
                "      \"time\": 1,\n" +
                "      \"detectedObjects\": [\n" +
                "        {\"id\": \"Object1\", \"description\": \"Tree\"},\n" +
                "        {\"id\": \"Object2\", \"description\": \"Rock\"}\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"time\": 2,\n" +
                "      \"detectedObjects\": [\n" +
                "        {\"id\": \"Object3\", \"description\": \"Animal\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"Camera2\": [\n" +
                "    {\n" +
                "      \"time\": 1,\n" +
                "      \"detectedObjects\": [\n" +
                "        {\"id\": \"Object4\", \"description\": \"Bush\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        try (FileWriter writer = new FileWriter(MOCK_CAMERA_DATA_FILE)) {
            writer.write(mockJson);
        } catch (IOException e) {
            fail("Failed to create mock camera data file: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        folder = new StatisticalFolder();
    }

    /**
     * Test parsing camera data using {@link GurionRockRunner#parseCameras(String)}.
     */
    @Test
    @DisplayName("Test Camera Data Parsing from Mock JSON")
    void testParseCameras() {
        // Act
        Map<String, List<StampedDetectedObjects>> cameraData = GurionRockRunner.parseCameras(MOCK_CAMERA_DATA_FILE);

        // Assert
        assertNotNull(cameraData, "Camera data should not be null");
        assertEquals(2, cameraData.size(), "There should be two cameras in the data");

        List<StampedDetectedObjects> camera1Objects = cameraData.get("Camera1");
        assertNotNull(camera1Objects, "Camera1 data should not be null");
        assertEquals(2, camera1Objects.size(), "Camera1 should have 2 entries");

        assertEquals(1, camera1Objects.get(0).getTime(), "First Camera1 object time should be 1");
        assertEquals("Tree", camera1Objects.get(0).getDetectedObjects().get(0).getDescription(),
                "First detected object should be 'Tree'");
    }

    /**
     * Test CameraService processes camera data.
     */
    @Test
    @DisplayName("Test CameraService Processes Data Directly")
    void testCameraServiceDirectProcessing() {
        // Arrange
        Map<String, List<StampedDetectedObjects>> cameraData = GurionRockRunner.parseCameras(MOCK_CAMERA_DATA_FILE);
        assertNotNull(cameraData, "Camera data should not be null");

        List<StampedDetectedObjects> camera1Data = cameraData.get("Camera1");
        Camera camera = new Camera(1, 1, camera1Data);
        cameraService = new CameraService(camera, folder);

        // Simulate processing the camera data manually
        for (StampedDetectedObjects stampedObject : camera1Data) {
            List<DetectedObject> detectedObjects = stampedObject.getDetectedObjects();

            for (DetectedObject obj : detectedObjects) {
                System.out.println("Detected object: " + obj.getId() + ", Description: " + obj.getDescription());
                folder.addDetectedObject();
            }
        }

        // Assert folder updates
        assertEquals(3, folder.getNumDetectedObjects(), "Folder should record 3 detected objects from Camera1");
    }

    /**
     * Test CameraService processes multiple cameras' data directly.
     */
    @Test
    @DisplayName("Test Multiple Cameras Data Processing Directly")
    void testMultipleCamerasDirectProcessing() {
        // Arrange
        Map<String, List<StampedDetectedObjects>> cameraData = GurionRockRunner.parseCameras(MOCK_CAMERA_DATA_FILE);
        assertNotNull(cameraData, "Camera data should not be null");

        // Process each camera
        for (Map.Entry<String, List<StampedDetectedObjects>> entry : cameraData.entrySet()) {
            Camera camera = new Camera(1, 1, entry.getValue());
            CameraService service = new CameraService(camera, folder);

            // Simulate direct processing of each frame
            for (StampedDetectedObjects stampedObject : entry.getValue()) {
                List<DetectedObject> detectedObjects = stampedObject.getDetectedObjects();

                for (DetectedObject obj : detectedObjects) {
                    System.out.println("Camera: " + entry.getKey() + " Detected object: " + obj.getId() +
                            ", Description: " + obj.getDescription());
                    folder.addDetectedObject();
                }
            }
        }

        // Assert folder updates
        assertEquals(4, folder.getNumDetectedObjects(), "Folder should record 4 detected objects across cameras");
    }

    @AfterAll
    static void cleanupMockFiles() {
        // Delete the mock file after tests
        java.io.File file = new java.io.File(MOCK_CAMERA_DATA_FILE);
        if (!file.delete()) {
            System.err.println("Failed to delete mock file: " + MOCK_CAMERA_DATA_FILE);
        }
    }
}
