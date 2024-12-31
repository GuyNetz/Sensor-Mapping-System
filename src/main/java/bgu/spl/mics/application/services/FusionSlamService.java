package bgu.spl.mics.application.services;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private FusionSlam fusionSlam;
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast, TerminatedBroadcast, and CrashedBroadcast
        subscribeBroadcast(TickBroadcast.class, (tickBroadcast) -> {}); // No action needed for tick broadcasts
        subscribeBroadcast(TerminatedBroadcast.class, (terminatedBroadcast) -> terminate());
        subscribeBroadcast(CrashedBroadcast.class, (crashedBroadcast) -> terminate());

        // Subscribe to TrackedObjectsEvent and PoseEvent
        subscribeEvent(TrackedObjectsEvent.class, event -> {
            // Transform TrackedObjects into LandMarks
            List<LandMark> landMarks = event.getTrackedObjects().stream()
            .map(trackedObject -> new LandMark(trackedObject.getID(),trackedObject.getDescription() ,trackedObject.getCoordinates()))
            .collect(Collectors.toList());
            //updates landmarks in the global map
            fusionSlam.updateMap(landMarks);
        });

        // Subscribe to PoseEvent
        subscribeEvent(PoseEvent.class, event -> {
            Pose pose = event.getPose();
            fusionSlam.updatePose(pose);
        });

    }
}
