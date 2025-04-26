package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.List;

/**
 * Handles data processing from the camera sensor and interacts with the system through events and broadcasts.
 * Responsible for detecting objects, tracking errors, and updating the system's statistics folder.
 * This service processes incoming TickBroadcasts and triggers relevant events based on the current state.
 */
public class CameraService extends MicroService {

    private final Camera camera;
    private int startTick = -1; // Records the first tick received
    private int indexPointer = 0; // Tracks the last processed object index
    private final StatisticalFolder folder;

    /**
     * Constructs a CameraService with the specified camera and statistical folder.
     *
     * @param camera The {@link Camera} object responsible for detecting objects. Must not be null.
     * @param folder The {@link StatisticalFolder} to record detected object data. Must not be null.
     * @pre camera != null && folder != null
     * @post this.camera == camera && this.folder == folder
     */
    public CameraService(Camera camera, StatisticalFolder folder) {
        super("CameraService-" + camera.getId());
        this.camera = camera;
        this.folder = folder;
    }

    /**
     * Initializes the CameraService by subscribing to necessary broadcasts and processing incoming ticks.
     * Subscribes to:
     * <ul>
     *   <li>{@link TerminatedBroadcast} – Handles system termination.</li>
     *   <li>{@link CrashedBroadcast} – Handles sensor or system crashes.</li>
     *   <li>{@link TickBroadcast} – Processes sensor data at each tick.</li>
     * </ul>
     *
     * @pre The {@link Camera} object associated with this service must be fully initialized.
     * @post The service is subscribed to TickBroadcast, TerminatedBroadcast, and CrashedBroadcast.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");

        // Handle Termination Broadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if (broadcast.getSender().equals("TimeService")) {
                System.out.println(getName() + " received TerminatedBroadcast from " + broadcast.getSender() + ". Shutting down.");
                terminate();
            }
        });

        // Handle Crashed Broadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            if (broadcast.getSender().equals("TimeService")) {
                System.out.println(getName() + " received CrashedBroadcast from TimeService. Shutting down.");
                terminate();
            }
        });

        // Handle Tick Broadcast
        subscribeBroadcast(TickBroadcast.class, broadcast -> {
            if (this.camera.getStatus() == STATUS.DOWN || this.camera.getStatus() == STATUS.ERROR) {
                System.out.println("Camera " + this.camera.getId() + " has status " + this.camera.getStatus());
                return;
            }

            int currentTick = broadcast.getTick();
            System.out.println(getName() + " received TickBroadcast at tick: " + currentTick);

            // Record the first tick if not already set
            if (startTick == -1) {
                startTick = currentTick;
            }

            // Retrieve detected objects
            List<StampedDetectedObjects> stampedObjects = camera.getDetectedObjects();

            if (stampedObjects == null || stampedObjects.isEmpty()) {
                System.out.println(getName() + ": No detected objects in the camera data.");
                return;
            }

            // Check if no more data - terminate
            if (indexPointer >= stampedObjects.size()) {
                System.out.println(getName() + ": No more detected objects in the camera data.");
                this.camera.SetStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                return;
            }

            // Start from the last processed object
            while (indexPointer < stampedObjects.size()) {
                StampedDetectedObjects stampedObject = stampedObjects.get(indexPointer);
                System.out.println(stampedObject.toString());
                int detectionTime = stampedObject.getTime();
                int availableTime = detectionTime + camera.getFreq();

                // Error detection happens at detectionTime
                if (currentTick == detectionTime) {
                    List<DetectedObject> list = stampedObject.getDetectedObjects();
                    for (DetectedObject detectedObject : list) {
                        if (detectedObject.getId().equals("ERROR")) {
                            System.err.println(getName() + " encountered an ERROR object. Sending CrashedBroadcast.");
                            sendBroadcast(new CrashedBroadcast(this.getName(), this.getName(), detectedObject.getDescription()));
                            indexPointer++;
                            this.camera.SetStatus(STATUS.ERROR);
                            return;
                        }
                    }
                }

                // Process available detections
                if (currentTick == availableTime) {
                    System.out.println(getName() + " detected objects available at tick: " + currentTick);
                    sendEvent(new DetectObjectsEvent(stampedObject, "Camera" + this.camera.getId()));
                    sendEvent(new DetectObjectsForFusionEvent(stampedObject, "Camera" + this.camera.getId()));

                    indexPointer++;

                    // Update folder with detected objects count
                    for (int i = 0; i < stampedObject.getDetectedObjects().size(); i++) {
                        this.folder.addDetectedObject();
                    }

                } else if (currentTick < availableTime) {
                    // If current tick is before the next available time, stop checking
                    break;
                } else {
                    // Skip past outdated entries
                    indexPointer++;
                }
            }
        });

        ServiceInitLatch.latch.countDown(); // Signal readiness

    }
}
