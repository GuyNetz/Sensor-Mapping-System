package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    // Fields
    private String id;
    private String description;
    private List<CloudPoint> coordinates;
    private int numScans = 0; // Number of scans used to calculate the current coordinates

    // Constructor
    public LandMark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
        this.numScans = 1;
    }

    // Methods
    public synchronized String getID() {
        return id;
    }

    public synchronized String getDescription() {
        return description;
    }

    public synchronized List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    public synchronized void updateCoordinates(List<CloudPoint> newCoordinates) { // Calculate the average coordinates for existing LandMark
        if (newCoordinates != null && coordinates != null && newCoordinates.size() == coordinates.size()) {
            List<CloudPoint> averagedCoordinates = new ArrayList<>();

            for (int i = 0; i < coordinates.size(); i++) {
                double averagedX = (coordinates.get(i).getX() * numScans + newCoordinates.get(i).getX()) / (numScans + 1);
                double averagedY = (coordinates.get(i).getY() * numScans + newCoordinates.get(i).getY()) / (numScans + 1);
                averagedCoordinates.add(new CloudPoint(averagedX, averagedY));
            }

            coordinates = averagedCoordinates;
            numScans++; // Increment the number of scans 
        }
    }
}
