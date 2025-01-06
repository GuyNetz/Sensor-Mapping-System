package bgu.spl.mics.application.objects;
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

    // Constructor
    public LandMark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
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

    // Update the coordinates of the LandMark by averaging the new coordinates
    public void averageCoordinates(List<CloudPoint> newCoordinates) {
        if (newCoordinates != null && coordinates != null) {
            // Find the shorter length between the existing and new coordinates
            int minLength = Math.min(coordinates.size(), newCoordinates.size());
    
            // Update the existing coordinates with the average for the overlapping points
            for (int i = 0; i < minLength; i++) {
                double averagedX = 0.5 * (coordinates.get(i).getX() + newCoordinates.get(i).getX());
                double averagedY = 0.5 * (coordinates.get(i).getY() + newCoordinates.get(i).getY());
                coordinates.set(i, new CloudPoint(averagedX, averagedY));
            }
    
            // Add any remaining points from the new coordinates list to the existing list
            if (newCoordinates.size() > coordinates.size()) {
                for (int i = coordinates.size(); i < newCoordinates.size(); i++) {
                    coordinates.add(newCoordinates.get(i));
                }
            }
        }
    }

}
