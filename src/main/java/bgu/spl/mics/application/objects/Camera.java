package bgu.spl.mics.application.objects;

import java.util.List;

import java.util.LinkedList;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private int id;
    private int frequency;
    private STATUS status; // "Up", "Down", "Error"
    private List<StampedDetectedObjects> detectedObjectsList;

    // Constructor
    public Camera (int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedObjectsList = new LinkedList<>();
    }

    // Methods
    public int getID() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public List<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjectsList;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

}
