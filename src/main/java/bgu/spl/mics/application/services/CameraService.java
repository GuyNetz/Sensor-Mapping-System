package bgu.spl.mics.application.services;

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
    // private int prevTick;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService" + camera.getID());
        this.camera = camera;
        this.cameraFrequency = camera.getFrequency();
        // prevTick = 0;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for
     * sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcasts
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            if (camera.getStatus() == STATUS.UP) {
                StampedDetectedObjects stampedObject = camera.getNextObjectToProcess(tickBroadcast.getCurrentTick(), cameraFrequency);
        
                if (stampedObject == null) {
                    if (camera.getStatus() == STATUS.ERROR) {
                        sendBroadcast(new CrashedBroadcast("CameraService" + camera.getID()));
                        terminate();
                    }
                } else {
                    sendEvent(new DetectObjectsEvent(stampedObject.getTime(), stampedObject));
        
                    // Log the detected objects in the StatisticalFolder
                    StatisticalFolder.getInstance().logDetectedObjects(camera.getID(), tickBroadcast.getCurrentTick(), stampedObject);
                }
            }
        });
        

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            System.out.println("terminate camera");
            terminate();
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            camera.setStatus(STATUS.DOWN);
            terminate();
        });

    }

    public void stopService() {
        terminate(); // This calls the protected method from MicroService
    }

    public Camera getCamera() {
        return camera;
    }
}
