package bgu.spl.mics.application.messages;

import java.util.List;

import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

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

    public DetectObjectsEvent(StampedDetectedObjects stampedDetectedObjects){
        this.detectionTime = stampedDetectedObjects.getTime();
        this.detectedObjects = stampedDetectedObjects.getDetectedObjects();
}

    // Getters
    public int getDetectionTime() {
        return detectionTime;
    }

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }
}
