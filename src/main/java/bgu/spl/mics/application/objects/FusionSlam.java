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

    // Calculate the coordinates relative to the docking station
    public List<CloudPoint> getRelativeCoordinates(Pose robotPose, List<CloudPoint> coordinates) {
        List<CloudPoint> globalCoordinates = new ArrayList<>();
        double thetaRad = Math.toRadians(robotPose.getYaw()); // convert yaw to radians
        double cosTheta = Math.cos(thetaRad); // 2. calculate the cosine of the yaw angle
        double sinTheta = Math.sin(thetaRad); // 2. calculate the sine of the yaw angle

        for (CloudPoint localPoint : coordinates) {
            //calculate the global coordinates
            double xGlobal = cosTheta * localPoint.getX() - sinTheta * localPoint.getY() + robotPose.getX();
            double yGlobal = sinTheta * localPoint.getX() + cosTheta * localPoint.getY() + robotPose.getY();

            globalCoordinates.add(new CloudPoint(xGlobal, yGlobal));
        }
        return globalCoordinates;
    }


    // Check if a landmark with the given ID exists in the global map
    private boolean doesLandMarkExist(String id) {
        return landMarks.stream()
                        .anyMatch(landMark -> landMark.getID().equals(id));
    }

    // Find a landmark by its ID
    public LandMark findLandmarkById(String id) {
        for (LandMark landMark : landMarks) {
            if (landMark.getID().equals(id)) { // Assuming getId() is the method to retrieve the ID
                return landMark;
            }
        }
        return null; // Return null if no matching LandMark is found(cant happen)
    }

    //update function
    public synchronized void update(int currTick, String id, String description, List<CloudPoint> coordinates){
        //Get the current pose
        Pose currPose = new Pose(0,0,0,0);
        for(Pose pose : poses){
            if(pose.getTime() == currTick){
                currPose = pose;
            }
        }

        //Get the coordinates relative to the docking station
        List<CloudPoint> relativeCoordinates = getRelativeCoordinates(currPose, coordinates);

        //Check if the current object alraedy exists in map
        LandMark existingLandmark = findLandmarkById(id);
        if(doesLandMarkExist(id)){
            //Update the coordinates by averaging   
            existingLandmark.averageCoordinates(relativeCoordinates);
        }else{
            //add the new object to the map
            landMarks.add(new LandMark(id, description, relativeCoordinates));
            StatisticalFolder.getInstance().incrementNumLandmarks();
        }

    }
}
