package bgu.spl.mics.application.messages;

import bgu.spl.mics.application.objects.Pose;

public class PoseEvent {
    //Fields
    private final Pose pose;
    private final int time;

    //Constructor
    public PoseEvent(Pose pose, int time) {
        this.pose = pose;
        this.time = time;
    }

    //Getters
    public Pose getPose() {
        return pose;
    }

}
