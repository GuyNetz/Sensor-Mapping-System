package bgu.spl.mics.application.services;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;



/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private Camera camera;
    private int cameraFrequency;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService" + camera.getID());
        this.camera = camera;
        this.cameraFrequency = camera.getFrequency();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcasts
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {

            // Check if the camera is operational
            if (camera.getStatus() == STATUS.UP) {

                // Check if the current tick is a multiple of the camera's frequency
                if (tickBroadcast.getCurrentTick() % cameraFrequency == 0) {

                    // Send a DetectObjectsEvent to the MessageBus
                    List<StampedDetectedObjects> matchingObjects = new LinkedList<>();
                    for (StampedDetectedObjects stampedObject : camera.getDetectedObjectsList()) {
                        if (stampedObject.getTime() == tickBroadcast.getCurrentTick()) {
                            matchingObjects.add(stampedObject);
                        }
                    }
                    if(!matchingObjects.isEmpty()){
                        sendEvent(new DetectObjectsEvent(tickBroadcast.getCurrentTick() , matchingObjects));

                        // Log the detected objects in the StatisticalFolder
                        StatisticalFolder.getInstance().logDetectedObjects(camera.getID(), tickBroadcast.getCurrentTick(), matchingObjects);
                    }
                }

                    
            }
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            terminate();
        });
    
        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            camera.setStatus(STATUS.DOWN);
        });
    
    }

    public void stopService() {
        terminate(); // This calls the protected method from MicroService
    }
}
