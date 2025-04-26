package bgu.spl.mics.application.objects;

/**
 * Represents a specific point in a 3D space as detected by the LiDAR.
 * These points are used to generate a point cloud representing objects in the environment.
 */
public class CloudPoint {
    private final double x;
    private final double y;

    /**
     * Constructs a new CloudPoint with specified coordinates.
     *
     * @param x The x-coordinate of the point.
     * @param y The y-coordinate of the point.
     * @pre none
     * @post this.x == x && this.y == y
     */
    public CloudPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Retrieves the x-coordinate of this point.
     *
     * @return The x-coordinate.
     * @pre none
     * @post result == this.x
     */
    public double getX() {
        return x;
    }

    /**
     * Retrieves the y-coordinate of this point.
     *
     * @return The y-coordinate.
     * @pre none
     * @post result == this.y
     */
    public double getY() {
        return y;
    }

    /**
     * Provides a string representation of the CloudPoint in JSON format.
     *
     * @return A JSON-like string representing the CloudPoint.
     * @pre none
     * @post result != null
     */
    @Override
    public String toString() {
        return "{\"x\":" + x + ",\"y\":" + y + "}";
    }
}
