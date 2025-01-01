package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.StatisticalFolder;



/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private LiDarWorkerTracker LiDarWorkerTracker;
    private int LiDarWorkerTrackerFreq;
    

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LidarService" + LiDarWorkerTracker.getID());
        this.LiDarWorkerTracker = LiDarWorkerTracker;
        this.LiDarWorkerTrackerFreq = LiDarWorkerTracker.getFrequency();
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

            // Check if the LiDAR worker is operational
            if (LiDarWorkerTracker.getStatus() == STATUS.UP) {

                // Check if the current tick is a multiple of the LiDAR worker's frequency
                if (tickBroadcast.getCurrentTick() % LiDarWorkerTrackerFreq == 0) {

                    // Process the data and send a TrackedObjectsEvent to the MessageBus
                    sendEvent(new TrackedObjectsEvent(LiDarWorkerTracker.getTrackedObjectsList()));

                    // Log the tracked objects in the StatisticalFolder
                    StatisticalFolder.getInstance().logTrackedObjects(LiDarWorkerTracker.getID(), tickBroadcast.getCurrentTick(), LiDarWorkerTracker.getTrackedObjectsList());
                }
            }
        });
    
        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            terminate();
        });
    
        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            LiDarWorkerTracker.setStatus(STATUS.DOWN);
        });

        // Subscribe to DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, detectObjectsEvent -> {
           
            // Check if the LiDAR worker is operational
            if (LiDarWorkerTracker.getStatus() == STATUS.UP) {

                // Process the data and send a TrackedObjectsEvent to the MessageBus
                sendEvent(new TrackedObjectsEvent(LiDarWorkerTracker.getTrackedObjectsList()));

                // Log the tracked objects in the StatisticalFolder
                StatisticalFolder.getInstance().logTrackedObjects(LiDarWorkerTracker.getID(), detectObjectsEvent.getDetectionTime(), LiDarWorkerTracker.getTrackedObjectsList());
            }
        });
    }

    public void stopService() {
        terminate(); // This calls the protected method from MicroService
    }
}
