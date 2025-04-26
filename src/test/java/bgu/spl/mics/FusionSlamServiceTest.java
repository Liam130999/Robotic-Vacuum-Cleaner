package bgu.spl.mics;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.FusionSlamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced Unit tests for {@link FusionSlamService}.
 * Tests `updateLandmarks` and `transformToGlobal` with deeper calculations and validation.
 */
class FusionSlamServiceTest {

    private FusionSlamService fusionSlamService;
    private StatisticalFolder folder;
    private FusionSlam fusionSlam;

    @BeforeEach
    void setUp() {
        folder = new StatisticalFolder();
        fusionSlamService = new FusionSlamService(folder);
        fusionSlam = FusionSlam.getInstance();

        // Clear FusionSlam state before each test
        fusionSlam.getLandmarks().clear();
        fusionSlam.getPoses().clear();
    }

    /**
     * Test {@link FusionSlamService#updateLandmarks(List)} with multiple tracked objects
     * and validate both additions and updates of landmarks.
     */
    @Test
    void testUpdateLandmarksWithMultipleTrackedObjects() {
        // Arrange
        List<TrackedObject> trackedObjects1 = new ArrayList<>();
        List<CloudPoint> points1 = new ArrayList<>();
        points1.add(new CloudPoint(1.0, 1.0));
        points1.add(new CloudPoint(2.0, 2.0));
        trackedObjects1.add(new TrackedObject("Landmark1", 1, "Description1", points1));

        List<TrackedObject> trackedObjects2 = new ArrayList<>();
        List<CloudPoint> points2 = new ArrayList<>();
        points2.add(new CloudPoint(3.0, 3.0));
        points2.add(new CloudPoint(4.0, 4.0));
        trackedObjects2.add(new TrackedObject("Landmark1", 1, "Description1", points2));

        List<TrackedObject> trackedObjects3 = new ArrayList<>();
        List<CloudPoint> points3 = new ArrayList<>();
        points3.add(new CloudPoint(5.0, 5.0));
        points3.add(new CloudPoint(6.0, 6.0));
        trackedObjects3.add(new TrackedObject("Landmark2", 1, "Description2", points3));

        // Ensure Pose exists for the tracked time
        Pose pose = new Pose(0, 0, 0, 1); // Time matches tracked objects
        fusionSlam.getPoses().add(pose);

        // Act
        fusionSlamService.updateLandmarks(trackedObjects1);
        fusionSlamService.updateLandmarks(trackedObjects2);
        fusionSlamService.updateLandmarks(trackedObjects3);

        // Assert
        List<LandMark> landmarks = fusionSlam.getLandmarks();
        assertEquals(2, landmarks.size(), "Two landmarks should exist after updates.");

        // Validate the first landmark's averaged coordinates
        LandMark landmark1 = landmarks.stream()
                .filter(l -> l.getId().equals("Landmark1"))
                .findFirst()
                .orElse(null);
        assertNotNull(landmark1, "Landmark1 should exist.");

        List<CloudPoint> averagedPoints1 = landmark1.getCoordinates();
        assertEquals(2.0, averagedPoints1.get(0).getX(), 0.01, "First point X should be averaged to 2.0");
        assertEquals(2.0, averagedPoints1.get(0).getY(), 0.01, "First point Y should be averaged to 2.0");
        assertEquals(3.0, averagedPoints1.get(1).getX(), 0.01, "Second point X should be averaged to 3.0");
        assertEquals(3.0, averagedPoints1.get(1).getY(), 0.01, "Second point Y should be averaged to 3.0");

        // Validate the second landmark's coordinates
        LandMark landmark2 = landmarks.stream()
                .filter(l -> l.getId().equals("Landmark2"))
                .findFirst()
                .orElse(null);
        assertNotNull(landmark2, "Landmark2 should exist.");

        List<CloudPoint> averagedPoints2 = landmark2.getCoordinates();
        assertEquals(5.0, averagedPoints2.get(0).getX(), 0.01, "First point X should match original value");
        assertEquals(5.0, averagedPoints2.get(0).getY(), 0.01, "First point Y should match original value");
    }

    /**
     * Test {@link FusionSlamService#transformToGlobal(List, Pose)} with various poses.
     */
    @Test
    void testTransformToGlobalWithMultiplePoses() {
        // Arrange
        List<CloudPoint> localPoints = new ArrayList<>();
        localPoints.add(new CloudPoint(1.0, 1.0));
        localPoints.add(new CloudPoint(2.0, 2.0));

        // Pose 1: No rotation, shift by (2,3)
        Pose pose1 = new Pose(2, 3, 0, 1);
        List<CloudPoint> transformedPoints1 = fusionSlamService.transformToGlobal(localPoints, pose1);

        assertEquals(3.0, transformedPoints1.get(0).getX(), 0.01, "First point X with pose1 should be 3.0");
        assertEquals(4.0, transformedPoints1.get(0).getY(), 0.01, "First point Y with pose1 should be 4.0");

        assertEquals(4.0, transformedPoints1.get(1).getX(), 0.01, "Second point X with pose1 should be 4.0");
        assertEquals(5.0, transformedPoints1.get(1).getY(), 0.01, "Second point Y with pose1 should be 5.0");

        // Pose 2: 90° rotation, shift by (0,0)
        Pose pose2 = new Pose(0, 0, 90, 1);
        List<CloudPoint> transformedPoints2 = fusionSlamService.transformToGlobal(localPoints, pose2);

        assertEquals(-1.0, transformedPoints2.get(0).getX(), 0.01, "First point X with pose2 should be -1.0");
        assertEquals(1.0, transformedPoints2.get(0).getY(), 0.01, "First point Y with pose2 should be 1.0");

        assertEquals(-2.0, transformedPoints2.get(1).getX(), 0.01, "Second point X with pose2 should be -2.0");
        assertEquals(2.0, transformedPoints2.get(1).getY(), 0.01, "Second point Y with pose2 should be 2.0");
    }
}
