package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    // Fields
    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;

    // Constructor
    public StatisticalFolder() {
        this.systemRuntime = 0;
        this.numDetectedObjects = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;
    }

    // Methods
    public synchronized void logDetectedObjects(int cameraID, int tick, List<StampedDetectedObjects> objects) { // Logs detected objects and updates the count.
        System.out.println("Camera " + cameraID + " detected " + objects.size() + " objects at tick " + tick);
        numDetectedObjects += objects.size();
    }
}
