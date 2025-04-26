package bgu.spl.mics.application.objects;

/**
 * Represents the robot's pose (position and orientation) in the environment.
 * Includes x, y coordinates and the yaw angle relative to a global coordinate system.
 */
public class Pose {
    private float x;
    private float y;
    private float yaw;
    private int time;

    /**
     * Constructs a new Pose instance with specified position, orientation, and time.
     *
     * @param x    The x-coordinate of the robot's position.
     * @param y    The y-coordinate of the robot's position.
     * @param yaw  The yaw angle (orientation) of the robot in degrees.
     * @param time The timestamp of the pose in simulation ticks. Must not be negative.
     * @pre time >= 0
     * @post this.x == x && this.y == y && this.yaw == yaw && this.time == time
     * @throws IllegalArgumentException if time is negative.
     */
    public Pose(float x, float y, float yaw, int time) {
        if (time < 0) {
            throw new IllegalArgumentException("Time cannot be negative");
        }
        this.x = x;
        this.y = y;
        this.yaw = yaw;
        this.time = time;
    }

    /**
     * Retrieves the x-coordinate of the robot's position.
     *
     * @return The x-coordinate.
     * @pre none
     * @post result == this.x
     */
    public float getX() {
        return x;
    }

    /**
     * Retrieves the y-coordinate of the robot's position.
     *
     * @return The y-coordinate.
     * @pre none
     * @post result == this.y
     */
    public float getY() {
        return y;
    }

    /**
     * Retrieves the yaw (orientation) angle of the robot.
     *
     * @return The yaw angle in degrees.
     * @pre none
     * @post result == this.yaw
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Retrieves the timestamp of when this pose was recorded.
     *
     * @return The time in simulation ticks.
     * @pre none
     * @post result == this.time
     */
    public int getTime() {
        return time;
    }

    /**
     * Provides a JSON-like string representation of the pose.
     * Includes time, x, y coordinates, and yaw angle with specific formatting.
     *
     * @return A JSON-formatted string representing the pose.
     * @pre none
     * @post result != null
     */
    @Override
    public String toString() {
        return "{\"time\":" + time +
                ",\"x\":" + String.format("%.4f", x) +
                ",\"y\":" + String.format("%.4f", y) +
                ",\"yaw\":" + String.format("%.2f", yaw) + "}";
    }
}
