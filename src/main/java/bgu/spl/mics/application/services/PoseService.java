package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.ServiceInitLatch;

import java.util.List;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting {@link PoseEvent} at every tick.
 * <p>
 * It interacts with {@link GPSIMU} to fetch pose data and ensures smooth pose tracking
 * throughout the simulation runtime.
 */
public class PoseService extends MicroService {

    private GPSIMU gpsimu;
    private int currentPoseIndex;

    /**
     * Constructs a PoseService with a given GPSIMU instance.
     *
     * @param gpsimu The {@link GPSIMU} object that provides the robot's pose data. Must not be null.
     * @pre gpsimu != null
     * @post this.gpsimu == gpsimu && this.currentPoseIndex == 0
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
        this.currentPoseIndex = 0;
    }

    /**
     * Initializes the PoseService.
     * <ul>
     *   <li>Subscribes to {@link TerminatedBroadcast} for graceful shutdown.</li>
     *   <li>Subscribes to {@link CrashedBroadcast} for error handling.</li>
     *   <li>Subscribes to {@link TickBroadcast} to update and broadcast pose data at each tick.</li>
     * </ul>
     *
     * @pre gpsimu.getPoseList() != null
     * @post Service is subscribed to broadcasts and pose data is processed at each tick.
     */
    @Override
    protected void initialize() {
        // Handle Termination Broadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if (broadcast.getSender().equals("TimeService")) {
                System.out.println(getName() + " received TerminatedBroadcast. Shutting down.");
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
            if (this.gpsimu.getStatus() == STATUS.DOWN || this.gpsimu.getStatus() == STATUS.ERROR) {
                System.out.println(getName() + " has status " + this.gpsimu.getStatus());
                return;
            }

            int currentTick = broadcast.getTick();
            System.out.println(getName() + " received TickBroadcast at tick: " + currentTick);

            this.gpsimu.setCurrentTick(currentTick);

            List<Pose> poseList = gpsimu.getPoseList();
            if (poseList == null || poseList.isEmpty()) {
                System.out.println(getName() + ": Pose list is empty or not loaded.");
                return;
            }

            // Check if all poses are processed
            if (currentPoseIndex >= poseList.size()) {
                System.out.println(getName() + ": Pose list is fully processed.");
                this.gpsimu.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                return;
            }

            // Process poses based on tick time
            while (currentPoseIndex < poseList.size()) {
                Pose currentPose = poseList.get(currentPoseIndex);

                if (currentPose.getTime() == currentTick) {
                    sendEvent(new PoseEvent(currentTick, currentPose));
                    System.out.println(getName() + " sent PoseEvent: " + currentPose.getYaw());
                    currentPoseIndex++;
                    break;
                } else if (currentPose.getTime() > currentTick) {
                    // Future pose, wait for the next tick
                    break;
                } else {
                    // Past pose, skip to the next one
                    currentPoseIndex++;
                }
            }
        });
        ServiceInitLatch.latch.countDown(); // Signal readiness
    }
}
