package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.DetectObjectsEvent;

import java.util.LinkedList;
import java.util.List;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and
 * process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private LiDarWorkerTracker LiDarWorkerTracker;
    private int LiDarWorkerTrackerFreq;
    private int curTick;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service
     *                           will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LidarService" + LiDarWorkerTracker.getID());
        this.LiDarWorkerTracker = LiDarWorkerTracker;
        this.LiDarWorkerTrackerFreq = LiDarWorkerTracker.getFrequency();
        curTick = 0;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            curTick = tickBroadcast.getCurrentTick();
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            System.out.println("terminate Lidar");
            terminate();
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            LiDarWorkerTracker.setStatus(STATUS.DOWN);
            terminate();
        });

        // Subscribe to DetectObjectsEvent
subscribeEvent(DetectObjectsEvent.class, detectObjectsEvent -> {

    // Check if the LiDAR worker is operational
    if (LiDarWorkerTracker.getStatus() == STATUS.UP && !LiDarWorkerTracker.getTrackedObjectsList().isEmpty()) {

        // Process the data and send a TrackedObjectsEvent to the MessageBus
        List<TrackedObject> matchingObjects = new LinkedList<>();

        for (TrackedObject trackedObject : LiDarWorkerTracker.getTrackedObjectsList()) {
            // Ensure we only process objects not already tracked
            if (trackedObject.getTime() + LiDarWorkerTrackerFreq <= detectObjectsEvent.getDetectionTime()) {

                // Check for "ERROR" condition
                if ("ERROR".equals(trackedObject.getID())) {
                    System.out.println("LiDAR sensor error detected at tick " + detectObjectsEvent.getDetectionTime()
                            + ". Broadcasting CrashedBroadcast.");
                    sendBroadcast(new CrashedBroadcast("LiDar Service" + LiDarWorkerTracker.getID()));
                    LiDarWorkerTracker.setStatus(STATUS.DOWN);
                    return; // Stop further processing
                }

                // Add valid tracked objects to the list
                if (!matchingObjects.contains(trackedObject)) { // Prevent duplicates
                    matchingObjects.add(trackedObject);
                }
            }
        }

        // If there are valid matching objects, send an event and log them
        if (!matchingObjects.isEmpty()) {
            sendEvent(new TrackedObjectsEvent(matchingObjects));
            StatisticalFolder.getInstance().logTrackedObjects(LiDarWorkerTracker.getID(),
                    detectObjectsEvent.getDetectionTime(), matchingObjects);
        }

        // Clear the processed objects from the list to prevent re-processing
        LiDarWorkerTracker.getTrackedObjectsList().removeAll(matchingObjects);
    }
});

    }

    public void stopService() {
        terminate(); // This calls the protected method from MicroService
    }
}
