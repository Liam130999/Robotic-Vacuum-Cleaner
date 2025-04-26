package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

/**
 * Represents an event that carries pose data at a specific time tick.
 * This event is used to communicate the robot's position and orientation
 * within the simulation framework.
 *
 * @param <Boolean> The expected result type when processing this event.
 */
public class PoseEvent implements Event<Boolean> {
    private final int time;
    private final Pose pose;

    /**
     * Constructs a new PoseEvent with the given time and pose.
     *
     * @param time The time tick at which the pose was recorded. Must be non-negative.
     * @param pose The Pose object containing position and orientation details. Must not be null.
     * @pre time >= 0 && pose != null
     * @post this.time == time && this.pose == pose
     * @throws IllegalArgumentException if time is negative or pose is null.
     */
    public PoseEvent(int time, Pose pose) {
        if (time < 0) throw new IllegalArgumentException("time must be greater than zero");
        if (pose == null) throw new IllegalArgumentException("pose must not be null");

        this.time = time;
        this.pose = pose;
    }

    /**
     * Retrieves the time tick associated with this PoseEvent.
     *
     * @return The time tick of the pose.
     * @pre none
     * @post result == this.time
     */
    public int getTime() {
        return time;
    }

    /**
     * Retrieves the Pose object associated with this event.
     *
     * @return The Pose containing position and orientation data.
     * @pre none
     * @post result == this.pose
     */
    public Pose getPose() {
        return pose;
    }
}
