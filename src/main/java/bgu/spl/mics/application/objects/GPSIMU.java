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

    //Methods
    public synchronized void updateTick(float x, float y, float yaw, int tick) {
        this.currentTick = tick;
        Pose newPose = new Pose(x, y, yaw, tick);
        poseList.add(newPose);
    }

    public synchronized STATUS getStatus() {
        return status;
    }

    public synchronized void setStatus(STATUS status) {
        this.status = status;
    }

    //getter for currentTick
    public synchronized int getCurrentTick() {
        return currentTick;
    }
    
    

    public Pose getPose(int tick) {
        // Iterate through the poseList 
        for (Pose pose : poseList) {
            // Check if the pose's time matches the target tick
            if (pose.getTime() == tick) {
                // If a match is found, return the pose
                return pose; 
            }
        }

        // If no matching pose is found
        return null;
    }
}
