package bgu.spl.mics.application.messages;

import java.util.List;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

public class TrackedObjectsEvent implements Event<List<TrackedObjectsEvent>>{
    
    //Fields
    // private int time; // indicates the time when the Lidar worker created the event. not sure if needed
    private final List<TrackedObject> trackedObjects;

    //Constructor
    public TrackedObjectsEvent(List<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

    // Methods
    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }
    
}
