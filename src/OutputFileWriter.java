public class OutputFileWriter {
    static class OutputData{
        private int systemRuntime;
        private int numDetectedObjects;
        private int numTrackedObjects;
        private int numLandmarks;
        private Map<String, LandMark> landMarks;
    
        public OutputData(int systemRuntime, int numDetectedObjects, int numTrackedObjects, int numLandmarks, Map<String, LandMark> landMarks) {
            this.systemRuntime = systemRuntime;
            this.numDetectedObjects = numDetectedObjects;
            this.numTrackedObjects = numTrackedObjects;
            this.numLandmarks = numLandmarks;
            this.landMarks = landMarks;
        }
    }   
}
