package bgu.spl.mics;

import java.util.List;

import bgu.spl.mics.application.objects.DetectedObject;

public class DetectObjectsEvent {
    
    // The time at which the objects were detected.
    private final int detectionTime;

    // A list of detected objects, each containing an ID and a description.
    private final List<DetectedObject> detectedObjects;

   // Constructor
   public DetectObjectsEvent(int detectionTime, List<DetectedObject> detectedObjects) {
        this.detectionTime = detectionTime;
        this.detectedObjects = detectedObjects;
    } 

    // Getters
    public int getDetectionTime() {
        return detectionTime;
    }

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }
}
