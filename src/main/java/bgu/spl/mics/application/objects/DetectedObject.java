package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
public class DetectedObject {
    // Fields
    private String id;
    private String description;

    // Constructor
    public DetectedObject(String id, String description) {
        this.id = id;
        this.description = description;
    }
}
