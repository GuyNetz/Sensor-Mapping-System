package bgu.spl.mics.application.objects;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam instance = new FusionSlam();
    }

    // Fields
    private List<LandMark> landMarks;
    private List<Pose> poses;
    

    // Constructor
    private FusionSlam() {
        this.landMarks = new ArrayList<>();
        this.poses = new LinkedList<>();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }


}
