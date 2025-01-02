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
    private List<LandMark> landMarks; // Global map
    private List<Pose> poses; // Robot poses

    // Constructor
    private FusionSlam() {
        this.landMarks = new ArrayList<>();
        this.poses = new LinkedList<>();
    }

    // Methods 

    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    public synchronized void updateMap(List<LandMark> trackedObjects) { // Update the global map with new landmarks
        if (trackedObjects != null) {
            for (LandMark newLandmark : trackedObjects) {

                // Check if a landmark with the same ID already exists
                LandMark existingLandmark = landMarks.stream()
                                                   .filter(lm -> lm.getID().equals(newLandmark.getID()))
                                                   .findFirst()
                                                   .orElse(null);
    
                if (existingLandmark != null) {
                    // Update existing landmark coordinates by averaging
                    existingLandmark.updateCoordinates(newLandmark.getCoordinates()); 
                } else {
                    // Add the new landmark to the list
                    landMarks.add(newLandmark);
                    StatisticalFolder stats = StatisticalFolder.getInstance();
                    stats.incrementNumLandmarks();
                }
            }        
            System.out.println("FusionSlam updated with " + trackedObjects.size() + " tracked objects."); 
        }
    }

    public synchronized void updatePose(Pose pose) { // Update the global map with a new pose
        if (pose != null) {
            poses.add(pose);
            System.out.println("FusionSlam updated with new pose."); // Debug
        }
    }

    public synchronized List<LandMark> getLandMarks() {
        return new ArrayList<>(landMarks); // Return a copy to maintain encapsulation
    }

    public synchronized List<Pose> getPoses() {
        return new ArrayList<>(poses); // Return a copy to maintain encapsulation
    }

    public synchronized Pose getCurrentPose(){
        if (!poses.isEmpty())
            return poses.get(poses.size()-1);

        return new Pose(0, 0, 0, 0);
    }
}
