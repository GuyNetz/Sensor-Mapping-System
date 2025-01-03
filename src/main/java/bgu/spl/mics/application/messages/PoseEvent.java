package bgu.spl.mics.application.messages;

import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.Event;

public class PoseEvent implements Event<Pose> {
    //Fields
    private final Pose pose;

    //Constructor
    public PoseEvent(Pose pose) {
        this.pose = pose;
    }

    //Getters
    public Pose getPose() {
        return pose;
    }

}
