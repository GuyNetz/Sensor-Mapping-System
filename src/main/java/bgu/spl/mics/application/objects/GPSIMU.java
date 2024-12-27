package bgu.spl.mics.application.objects;
import java.util.List;
import java.util.LinkedList;    

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    // Fields
    private int currentTick;
    private STATUS status;
    private List<Pose> poseList;

    //constructor
    public GPSIMU() {
        this.currentTick = 0;
        this.status = STATUS.UP;
        this.poseList = new LinkedList<>();
    }
}
