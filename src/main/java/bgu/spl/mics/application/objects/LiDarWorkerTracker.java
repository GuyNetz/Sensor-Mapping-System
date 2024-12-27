package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.LinkedList;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    //Fields
    private int id;
    private int frequency;
    private STATUS status; // "Up", "Down", "Error"
    private List<TrackedObject> trackedObjectsList;

    //Constructor
    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.trackedObjectsList = new LinkedList<>();
    }

}
