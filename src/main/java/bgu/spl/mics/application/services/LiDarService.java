package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending {@link TrackedObjectsEvent} to the FusionSLAM service.
 * <p>
 * It tracks detected objects, handles time synchronization using {@link TickBroadcast},
 * and manages error scenarios via {@link CrashedBroadcast}.
 */
public class LiDarService extends MicroService {
    private final LiDarWorkerTracker liDarWorkerTracker;
    private final LiDarDataBase liDarDataBase; // Singleton instance
    private int currentTick = 0;
    private final StatisticalFolder folder;
    private final ConcurrentHashMap<StampedDetectedObjects, Boolean> waitingList ;

    /**
     * Constructs a LiDarService instance.
     *
     * @param liDarWorkerTracker A {@link LiDarWorkerTracker} object used to process LiDAR data. Must not be null.
     * @param folder             The {@link StatisticalFolder} used to store statistical data. Must not be null.
     * @pre liDarWorkerTracker != null && folder != null
     * @post this.liDarWorkerTracker == liDarWorkerTracker && this.folder == folder
     */
    public LiDarService(LiDarWorkerTracker liDarWorkerTracker, StatisticalFolder folder) {
        super("LidarService-" + liDarWorkerTracker.getId());
        this.liDarWorkerTracker = liDarWorkerTracker;
        this.liDarDataBase = LiDarDataBase.getInstance();
        this.folder = folder;
        this.waitingList = new ConcurrentHashMap<>();
    }

    /**
     * Initializes the LiDarService.
     *   <li>Subscribes to {@link TerminatedBroadcast} for graceful shutdown.</li>
     *   <li>Subscribes to {@link CrashedBroadcast} to handle critical errors.</li>
     *   <li>Subscribes to {@link TickBroadcast} for time synchronization.</li>
     *   <li>Handles {@link DetectObjectsEvent} for object detection events.</li>
     *
     * @pre liDarWorkerTracker != null && liDarDataBase != null && folder != null
     * @post Service is subscribed to the necessary broadcasts and events.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");

        // Handle Termination Broadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if(broadcast.getSender().equals("TimeService")) {
                System.out.println(getName() + " received TerminatedBroadcast. Shutting down.");
                terminate();
            }
        });

        // Handle Crashed Broadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            if(broadcast.getSender().equals("TimeService")) {
                System.out.println(getName() + " received CrashedBroadcast from TimeService Shutting down.");
                terminate();
            }
        });

        // Handle TickBroadcast to update the current tick
        subscribeBroadcast(TickBroadcast.class, broadcast -> {
            if(this.liDarWorkerTracker.getStatus() == STATUS.DOWN || this.liDarWorkerTracker.getStatus() == STATUS.ERROR) {
                System.out.println(getName() + " has status " + this.liDarWorkerTracker.getStatus());
                return;
            }

            currentTick = broadcast.getTick();
            System.out.println(getName() + " updated currentTick to: " + currentTick);

            //check if finished
            List<StampedCloudPoints> dbList = this.liDarDataBase.getCloudPoints();
            StampedCloudPoints lastPoint = dbList.get(dbList.size()-1);
            if (currentTick > lastPoint.getTime()) {
                System.out.println(getName() + " finished getting updates");
                this.liDarWorkerTracker.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                return;
            }

            handleWaitingList();
        });

        // Handle DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, event -> {
            System.out.println(getName() + " received DetectObjectsEvent.");
            int eventTime = event.getStampedDetectedObjects().getTime();
            int frequency = liDarWorkerTracker.getFrequency();
            StampedDetectedObjects stamp = event.getStampedDetectedObjects();

            // Check alignment
            if ((eventTime + frequency) <= currentTick) { // can take care
                System.out.println(getName() + " processing event with time: " + eventTime);
                handleStamp(stamp);
            } else { //cant take care YET
                System.out.println(getName() + " waiting event due to time misalignment.");
                this.waitingList.putIfAbsent(stamp, false);
            }
        });

        ServiceInitLatch.latch.countDown(); // Signal readiness
    }

    /**
     * Handles events in the waiting list and processes them when ready.
     *
     * @pre waitingList != null
     * @post All eligible events in the waiting list are processed.
     */
    private void handleWaitingList() {
        for(StampedDetectedObjects stamp: this.waitingList.keySet()) {
            if (!this.waitingList.get(stamp)) {
                handleStamp(stamp);
                this.waitingList.replace(stamp, true);
            }
        }
    }

    /**
     * Processes a stamped detection object and converts it into tracked objects.
     *
     * @param stamp A {@link StampedDetectedObjects} containing detected object data. Must not be null.
     * @pre stamp != null
     * @post Detected objects are processed and converted to {@link TrackedObject}.
     */
    private void handleStamp(StampedDetectedObjects stamp) {
        // Map DetectedObjects to TrackedObjects using the latest valid LiDAR data
        List<TrackedObject> trackedObjects = new ArrayList<>();
        int eventTime = stamp.getTime();
        List<DetectedObject> detectedObjects = stamp.getDetectedObjects();
        List<StampedCloudPoints> dbList = this.liDarDataBase.getCloudPoints();

        // Iterate efficiently using indexPointer
        for (DetectedObject detectedObject : detectedObjects) {
            StampedCloudPoints latestEntry = null;

            for (int i = 0; i < dbList.size() ; i++) { //Assuming db is sorted
                StampedCloudPoints entry = dbList.get(i);

                if (entry.getTime() > eventTime) {
                    System.out.println("BREAK");
                    break;
                    // Future entries, stop checking further
                }

                //Look for error
                if(Objects.equals(entry.getId(), "ERROR")){
                    System.err.println(getName() + " encountered an ERROR object. Sending CrashedBroadcast.");
                    sendBroadcast(new CrashedBroadcast(this.getName(), this.getName(), "LiDar Error"));
                    this.liDarWorkerTracker.setStatus(STATUS.ERROR);
                    return;
                }


                if (entry.getId().equals(detectedObject.getId()) && entry.getTime() <= eventTime) {
                    latestEntry = entry;
                    if (entry.getTime() == eventTime) {
                        // Exact match, no need to continue
                        break;
                    }
                }

            }

            if (latestEntry != null) {
                // Convert cloud points to TrackedObject
                List<CloudPoint> cloudPoints = new ArrayList<>();
                for (List<Double> point : latestEntry.getCloudPoints()) {
                    cloudPoints.add(new CloudPoint(point.get(0), point.get(1)));
                }

                TrackedObject trackedObject = new TrackedObject(
                        detectedObject.getId(),
                        eventTime,
                        detectedObject.getDescription(),
                        cloudPoints
                );

                trackedObjects.add(trackedObject);
                System.out.println(getName() + " tracked object: " + trackedObject.getId());
            } else {
                System.out.println(getName() + " No valid LiDAR data found for object ID: " + detectedObject.getId());
            }
        }

        liDarWorkerTracker.getLastTrackedObjects().clear();
        liDarWorkerTracker.getLastTrackedObjects().addAll(trackedObjects);


        // Send TrackedObjectsEvent to FusionSLAM Service
        if (!trackedObjects.isEmpty()) {
            sendEvent(new TrackedObjectsEvent(trackedObjects, "LiDarTrackerWorker"+this.liDarWorkerTracker.getId()));
            System.out.println(getName() + " sent TrackedObjectsEvent to FusionSLAM Service.");

            //update folder
            for (TrackedObject trackedObject : trackedObjects) {
                this.folder.addTrackedObject();
            }
        }
    }
}
