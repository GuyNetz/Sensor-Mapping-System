
package bgu.spl.mics.application;
import java.util.Map;
import bgu.spl.mics.application.objects.LandMark;


public class JsonData{
    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    private Map<String, LandMark> landMarks;

    public JsonData(int systemRuntime, int numDetectedObjects, int numTrackedObjects, int numLandmarks, Map<String, LandMark> landMarks) {
        this.systemRuntime = systemRuntime;
        this.numDetectedObjects = numDetectedObjects;
        this.numTrackedObjects = numTrackedObjects;
        this.numLandmarks = numLandmarks;
        this.landMarks = landMarks;
    }
}