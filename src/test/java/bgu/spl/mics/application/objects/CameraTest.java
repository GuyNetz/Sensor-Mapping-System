package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test class for the Camera class, focusing on the functionality of the
 * getNextObjectToProcess method.
 */
class CameraTest {

    /**
     * Tests the getNextObjectToProcess method with valid data.
     * Ensures that an object matching the conditions is returned and removed from the list.
     */
    @Test
    void testGetNextObjectToProcessWithValidData() {
        // Arrange: Initialize a detected objects list with valid data
        List<StampedDetectedObjects> detectedObjectsList = new ArrayList<>();
        detectedObjectsList.add(new StampedDetectedObjects(1, Arrays.asList(new DetectedObject("Object1", "Description1"))));
        detectedObjectsList.add(new StampedDetectedObjects(2, Arrays.asList(new DetectedObject("Object2", "Description2"))));
        
        // Create a Camera instance with the detected objects list
        Camera camera = new Camera(1, 1, detectedObjectsList);
        
        // Act: Call the getNextObjectToProcess method with a valid tick and frequency
        StampedDetectedObjects result = camera.getNextObjectToProcess(3, 1);

        // Assert: Verify the result and the state of the detectedObjectsList
        assertNotNull(result, "The result should not be null");
        assertEquals(1, result.getTime(), "The time of the processed object should be 1");
        assertEquals(1, camera.getDetectedObjectsList().size(), "The detectedObjectsList should now have 1 item");
        assertEquals("Object2", camera.getDetectedObjectsList().get(0).getDetectedObjects().get(0).getId(), "Remaining object should have ID 'Object2'");
    }

    /**
     * Tests the getNextObjectToProcess method with no valid data.
     * Ensures that null is returned and the list remains unchanged when no objects match the condition.
     */
    @Test
    void testGetNextObjectToProcessWithNoValidData() {
        // Arrange: Initialize a detected objects list with no matching data
        List<StampedDetectedObjects> detectedObjectsList = new ArrayList<>();
        detectedObjectsList.add(new StampedDetectedObjects(5, Arrays.asList(new DetectedObject("Object1", "Description1"))));
        detectedObjectsList.add(new StampedDetectedObjects(6, Arrays.asList(new DetectedObject("Object2", "Description2"))));

        // Create a Camera instance with the detected objects list
        Camera camera = new Camera(1, 1, detectedObjectsList);

        // Act: Call the getNextObjectToProcess method with a tick and frequency that don't match any objects
        StampedDetectedObjects result = camera.getNextObjectToProcess(3, 1);

        // Assert: Verify that the result is null and the list remains unchanged
        assertNull(result, "The result should be null when no objects match the condition");
        assertEquals(2, camera.getDetectedObjectsList().size(), "The detectedObjectsList should remain unchanged");
    }
}
