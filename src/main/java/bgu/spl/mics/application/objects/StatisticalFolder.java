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

    // Getters
    public int getSystemRuntime() {
        return systemRuntime;
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects;
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects;
    }

    public int getNumLandmarks() {
        return numLandmarks;
    }

    // Static holder for the singleton instance
    private static final StatisticalFolder instance = new StatisticalFolder();

    // Public method to get the singleton instance
    public static StatisticalFolder getInstance() {
        return instance;
    }

    // Other methods
    public synchronized void logDetectedObjects(int cameraID, int tick, List<StampedDetectedObjects> objects) { // Logs detected objects and updates the count.
        System.out.println("Camera " + cameraID + " detected " + objects.size() + " objects at tick " + tick);
        numDetectedObjects += objects.size();
    }

    public synchronized void logTrackedObjects(int LiDarID, int tick, List<TrackedObject> objects) { // Logs tracked objects and updates the count.
        System.out.println("LiDar " + LiDarID + " tracked " + objects.size() + " objects at tick " + tick);
        numTrackedObjects += objects.size();
    }

    public void incrementNumLandmarks() {
        this.numLandmarks++;
    }

    public void incrementSystemRuntime() {
        this.systemRuntime++;
    }
}