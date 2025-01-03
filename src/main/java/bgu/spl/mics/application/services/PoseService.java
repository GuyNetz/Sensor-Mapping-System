package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            // Get the current pose from GPSIMU
            int currentTime = tickBroadcast.getCurrentTick();
            Pose currentPose = gpsimu.getPose(currentTime);
            

            // Send a PoseEvent with the current pose and time
            PoseEvent poseEvent = new PoseEvent(currentPose);
            sendEvent(poseEvent);
        });
        
        // Subscribe to TerminatedBroadcast and CrashedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, (terminatedBroadcast) -> terminate());
        subscribeBroadcast(CrashedBroadcast.class, (crashedBroadcast) -> terminate());
    }
    
    public void stopService() {
        terminate(); // This calls the protected method from MicroService
    }
}
