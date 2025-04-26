
package bgu.spl.mics.application.services;

import java.util.List;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * FusionSlamService integrates data from multiple sensors to build and update the robot's global map.
 * <p>
 * This service processes:
 * <ul>
 *   <li>{@link TrackedObjectsEvent} from LiDAR workers</li>
 *   <li>{@link PoseEvent} from the PoseService</li>
 *   <li>{@link DetectObjectsForFusionEvent} for camera-based detections</li>
 * </ul>
 * It maintains the system's global state, manages landmarks, tracks sensor statuses,
 * and produces outputs on termination or crash events.
 */
public class FusionSlamService extends MicroService {

    private final FusionSlam fusionSlam; // Singleton instance
    private int currentTick = 0; // Tracks the current simulation tick
    private final StatisticalFolder folder;
    private final ConcurrentHashMap<String, StampedDetectedObjects> lastCamerasFrame;
    private final ConcurrentHashMap<String, List<TrackedObject>> lastLiDarWorkerTrackersFrame;


    /**
     * Constructs a FusionSlamService instance.
     *
     * @param folder The {@link StatisticalFolder} to record statistical data. Must not be null.
     * @pre folder != null
     * @post this.folder == folder
     */
    public FusionSlamService(StatisticalFolder folder) {
        super("FusionSlamService");
        this.fusionSlam = FusionSlam.getInstance();
        this.folder = folder;
        this.lastCamerasFrame = new ConcurrentHashMap<>();
        this.lastLiDarWorkerTrackersFrame = new ConcurrentHashMap<>();
    }


    /**
     * Initializes the FusionSlamService by subscribing to events and broadcasts.
     * <ul>
     *   <li>{@link TerminatedBroadcast}: Handles normal termination.</li>
     *   <li>{@link CrashedBroadcast}: Handles abnormal termination due to crashes.</li>
     *   <li>{@link TickBroadcast}: Updates the simulation tick.</li>
     *   <li>{@link PoseEvent}: Updates robot pose information.</li>
     *   <li>{@link TrackedObjectsEvent}: Updates tracked objects and landmarks.</li>
     *   <li>{@link DetectObjectsForFusionEvent}: Updates detected objects from cameras.</li>
     * </ul>
     *
     * @pre none
     * @post Service is subscribed to the listed events and broadcasts.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");

        // Handle termination broadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if(Objects.equals(broadcast.getSender(), "TimeService")) {
                System.out.println(getName() + " shutting down.");

                // Gather statistics and landmarks
                TerminationOutput terminationOutput = new TerminationOutput(
                        folder.getSystemRuntime(),
                        folder.getNumDetectedObjects(),
                        folder.getNumTrackedObjects(),
                        folder.getNumLandmarks(),
                        fusionSlam.getLandmarks()
                );

                // Generate JSON file
                terminationOutput.generateJsonFile();

                //OUTPUT - Terminate
                System.out.println(this.folder.toString());
                for (LandMark landmark : fusionSlam.getLandmarks()) {
                    System.out.println(landmark.toString());
                }

                terminate();
            }
        });

        // Handle crash broadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            if(Objects.equals(broadcast.getSender(), "TimeService")) {
                System.out.println(getName() + " shutting down due to crash in " + broadcast.getErrorMaker());

                // Generate error output JSON
                bgu.spl.mics.application.utils.ErrorOutput errorOutput = new bgu.spl.mics.application.utils.ErrorOutput(
                        broadcast.getMessage(),
                        "Camera1",
                        lastCamerasFrame,
                        lastLiDarWorkerTrackersFrame,
                        fusionSlam.getPoses(),
                        currentTick,
                        this.folder.getNumDetectedObjects(),
                        this.folder.getNumTrackedObjects(),
                        fusionSlam.getLandmarks().size(),
                        fusionSlam.getLandmarks()
                );

                errorOutput.generateJsonFile();

                System.out.println("=== Last Cameras Frame ===");
                for (Map.Entry<String, StampedDetectedObjects> entry : lastCamerasFrame.entrySet()) {
                    String cameraName = entry.getKey();
                    StampedDetectedObjects stampedObjects = entry.getValue();

                    System.out.println("\"" + cameraName + "\": " + stampedObjects.toString());
                }

                System.out.println("=== Last LiDAR Worker Trackers Frame ===");
                for (Map.Entry<String, List<TrackedObject>> entry : lastLiDarWorkerTrackersFrame.entrySet()) {
                    String workerName = entry.getKey();
                    List<TrackedObject> trackedObjects = entry.getValue();

                    System.out.println("\"" + workerName + "\": [");
                    for (int i = 0; i < trackedObjects.size(); i++) {
                        System.out.print(trackedObjects.get(i).toString());
                        if (i < trackedObjects.size() - 1) {
                            System.out.print(",");
                        }
                    }
                    System.out.println("]");
                }


                for(Pose pose : fusionSlam.getPoses()) {
                    System.out.println(pose.toString());
                }
                System.out.println("statistics: " + this.folder.toString());
                for (LandMark landmark : fusionSlam.getLandmarks()) {
                    System.out.println(landmark.toString());
                }

                terminate();
            }
        });

        // Update current tick on TickBroadcast
        subscribeBroadcast(TickBroadcast.class, broadcast -> {
            currentTick = broadcast.getTick();
            System.out.println(getName() + " Tick: " + currentTick);
        });

        // Handle Pose updates
        subscribeEvent(PoseEvent.class, event -> {
            Pose pose = event.getPose();
            fusionSlam.getPoses().add(pose);
            System.out.println(getName() + " Pose added at tick: " + pose.getTime());
            complete(event, true);
        });

        // Handle Tracked Objects + for the error case
        subscribeEvent(TrackedObjectsEvent.class, event -> {
            System.out.println("Fusion tracked from " + event.getSender());

            //update for error
            if(this.lastLiDarWorkerTrackersFrame.containsKey(event.getSender())){
                this.lastLiDarWorkerTrackersFrame.replace(event.getSender(), event.getTrackedObjects());
            }
            else {
                this.lastLiDarWorkerTrackersFrame.put(event.getSender(), event.getTrackedObjects());
            }

            System.out.println(getName() + " Processing TrackedObjectsEvent.");
            updateLandmarks(event.getTrackedObjects());
            complete(event, true);
        });

        // Handle Detected Object - for the error case
        subscribeEvent(DetectObjectsForFusionEvent.class, event -> {
            System.out.println("Fusion detected from " + event.getSender());
            //update for error
            if(this.lastCamerasFrame.containsKey(event.getSender())){
                this.lastCamerasFrame.replace(event.getSender(), event.getStampedDetectedObjects());
            }
            else {
                this.lastCamerasFrame.put(event.getSender(), event.getStampedDetectedObjects());
            }
        });

        ServiceInitLatch.latch.countDown(); // Signal readiness
    }

    /**
     * Updates landmarks with tracked object data, adding new ones or refining existing ones.
     *
     * @param trackedObjects List of tracked objects.
     * @pre trackedObjects != null
     * @post fusionSlam landmarks are updated based on tracked objects.
     */
    public void updateLandmarks(List<TrackedObject> trackedObjects) {
        for (TrackedObject trackedObject : trackedObjects) {
            Pose pose = getPoseForTime(trackedObject.getTime());

            if (pose == null) {
                System.out.println(getName() + ": No pose available for object " + trackedObject.getId());
                continue;
            }

            // Transform cloud points to global coordinates
            List<CloudPoint> globalCoordinates = transformToGlobal(trackedObject.getCoordinates(), pose);

            // Check if landmark already exists
            LandMark existingLandmark = null;
            for (LandMark landmark : fusionSlam.getLandmarks()) {
                if (landmark.getId().equals(trackedObject.getId())) {
                    existingLandmark = landmark;
                    break;
                }
            }

            if (existingLandmark != null) { //meaning found
                // Update existing landmark by averaging coordinates
                List<CloudPoint> averagedPoints = averageCoordinates(existingLandmark.getCoordinates(), globalCoordinates);

                fusionSlam.getLandmarks().remove(existingLandmark);
                fusionSlam.getLandmarks().add(new LandMark(trackedObject.getId(), trackedObject.getDescription(), averagedPoints));
                System.out.println(getName() + ": Averaged and updated landmark " + trackedObject.getId());
            }
            else {
                // Add new landmark
                fusionSlam.getLandmarks().add(new LandMark(trackedObject.getId(), trackedObject.getDescription(), globalCoordinates));
                System.out.println(getName() + ": Added new landmark " + trackedObject.getId());

                //update folder
                this.folder.addLandmark();
            }
        }
    }


    /**
     * Retrieves the pose corresponding to a specific tick time.
     *
     * @param time The tick time to match.
     * @return The {@link Pose} corresponding to the given tick time, or {@code null} if no match is found.
     * @pre time >= 0
     * @post result == null || fusionSlam.getPoses().contains(result)
     */
    public Pose getPoseForTime(int time) {
        for (Pose pose : fusionSlam.getPoses()) {
            if (pose.getTime() == time) {
                return pose;
            }
        }
        return null; // Return null if no matching pose is found
    }

    /**
     * Transforms local cloud points into global coordinates using the robot's pose.
     *
     * @param cloudPoints A list of local {@link CloudPoint} representing object coordinates.
     * @param pose        The current {@link Pose} of the robot used for transformation.
     * @return A list of transformed global {@link CloudPoint}.
     * @pre cloudPoints != null && pose != null
     * @post result.size() == cloudPoints.size()
     */
    public List<CloudPoint> transformToGlobal(List<CloudPoint> cloudPoints, Pose pose) {
        double yawRad = Math.toRadians(pose.getYaw());
        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);

        List<CloudPoint> globalPoints = new ArrayList<>();

        for (CloudPoint point : cloudPoints) {
            double xGlobal = (cosYaw * point.getX()) - (sinYaw * point.getY()) + pose.getX();
            double yGlobal = (sinYaw * point.getX()) + (cosYaw * point.getY()) + pose.getY();
            globalPoints.add(new CloudPoint(xGlobal, yGlobal));
        }

        return globalPoints;
    }

    /**
     * Averages two sets of cloud points to create a refined representation.
     *
     * @param existingPoints A list of existing global {@link CloudPoint}.
     * @param newPoints      A list of new transformed {@link CloudPoint}.
     * @return A list of averaged {@link CloudPoint}, combining both input lists.
     * @pre existingPoints != null && newPoints != null
     * @post result.size() == Math.max(existingPoints.size(), newPoints.size())
     */
    public List<CloudPoint> averageCoordinates(List<CloudPoint> existingPoints, List<CloudPoint> newPoints) {
        List<CloudPoint> averagedPoints = new ArrayList<>();
        int minSize = Math.min(existingPoints.size(), newPoints.size());

        for (int i = 0; i < minSize; i++) {
            double avgX = (existingPoints.get(i).getX() + newPoints.get(i).getX()) / 2;
            double avgY = (existingPoints.get(i).getY() + newPoints.get(i).getY()) / 2;
            averagedPoints.add(new CloudPoint(avgX, avgY));
        }

        // Add any remaining points from the longer list
        for (int i = minSize; i < newPoints.size(); i++) {
            averagedPoints.add(newPoints.get(i));
        }
        for (int i = minSize; i < existingPoints.size(); i++) {
            averagedPoints.add(existingPoints.get(i));
        }

        return averagedPoints;
    }
}
