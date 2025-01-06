package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

class FusionSlamTest {

    // Tolerance for floating-point comparisons to account for precision errors
    private static final double TOLERANCE = 1e-6;

    @Test
    void testGetRelativeCoordinatesZeroRotation() {
        // Arrange: Set up a robot pose with no rotation and local coordinates
        Pose robotPose = new Pose(5.0f, 5.0f, 0.0f, 0); // Robot at (5,5) with 0 degrees yaw
        List<CloudPoint> localCoordinates = Arrays.asList(
            new CloudPoint(1.0, 1.0), // Local coordinate (1,1)
            new CloudPoint(-2.0, 3.0) // Local coordinate (-2,3)
        );
        FusionSlam fusionSlam = FusionSlam.getInstance(); // Get the singleton instance

        // Act: Calculate global coordinates based on the robot pose
        List<CloudPoint> globalCoordinates = fusionSlam.getRelativeCoordinates(robotPose, localCoordinates);

        // Assert: Verify the global coordinates are as expected
        assertEquals(2, globalCoordinates.size()); // Ensure the size matches
        assertEquals(6.0, globalCoordinates.get(0).getX(), TOLERANCE); // First point's X
        assertEquals(6.0, globalCoordinates.get(0).getY(), TOLERANCE); // First point's Y
        assertEquals(3.0, globalCoordinates.get(1).getX(), TOLERANCE); // Second point's X
        assertEquals(8.0, globalCoordinates.get(1).getY(), TOLERANCE); // Second point's Y
    }

    @Test
    void testGetRelativeCoordinatesWithRotation() {
        // Arrange: Set up a robot pose with 90 degrees rotation and local coordinates
        Pose robotPose = new Pose(5.0f, 5.0f, 90.0f, 0); // Robot at (5,5) with 90 degrees yaw
        List<CloudPoint> localCoordinates = Arrays.asList(
            new CloudPoint(1.0, 0.0), // Local coordinate (1,0)
            new CloudPoint(0.0, -2.0) // Local coordinate (0,-2)
        );
        FusionSlam fusionSlam = FusionSlam.getInstance(); // Get the singleton instance

        // Act: Calculate global coordinates based on the robot pose
        List<CloudPoint> globalCoordinates = fusionSlam.getRelativeCoordinates(robotPose, localCoordinates);

        // Assert: Verify the global coordinates are as expected
        assertEquals(2, globalCoordinates.size()); // Ensure the size matches
        assertEquals(5.0, globalCoordinates.get(0).getX(), TOLERANCE); // First point's X after rotation
        assertEquals(6.0, globalCoordinates.get(0).getY(), TOLERANCE); // First point's Y after rotation
        assertEquals(7.0, globalCoordinates.get(1).getX(), TOLERANCE); // Second point's X after rotation
        assertEquals(5.0, globalCoordinates.get(1).getY(), TOLERANCE); // Second point's Y after rotation
    }
}
