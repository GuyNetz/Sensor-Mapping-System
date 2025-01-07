package bgu.spl.mics.application;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedList;

import bgu.spl.mics.application.services.*;
import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.List;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the
     *             path to the configuration file.
     */
    public static void main(String[] args) {
        System.out.println(args[0]);
        Map<String, String> idToDescription = new HashMap<>();
        System.out.println("Starting the GurionRock Pro Max Ultra Over 9000 simulation...");
        
        if (args.length != 1) {
        System.err.println("Usage: java GurionRockRunner <configuration file path>");
        return;
        }

        String relativePath = args[0].substring(0, args[0].lastIndexOf("/"));
        String configFilePath = args[0];

        try {
            System.out.println("Parsing configuration file...");
            // Parse the configuration file using GSON
            JsonObject config = parseJsonConfig(configFilePath);

            System.out.println("Initializing services...");
            /***********************************
             * Initialize TimeService
             ***********************************/
            int tickDuration = config.get("TickTime").getAsInt();
            int duration = config.get("Duration").getAsInt();
            TimeService timeService = new TimeService(tickDuration, duration);

            /***********************************
             * Initialize CameraServices
             ***********************************/
            // Getting the relevant JSON objects
            JsonObject cameras = config.getAsJsonObject("Cameras");
            JsonArray camerasConfigurations = cameras.getAsJsonArray("CamerasConfigurations");
            String camDataPath = relativePath + cameras.get("camera_datas_path").getAsString().substring(1);
            JsonObject camerasData = parseJsonConfig(camDataPath); // Try to change that

            // loop over all cameras
            CameraService[] cameraServices = new CameraService[camerasConfigurations.size()];
            for (int i = 0; i < camerasConfigurations.size(); i++) {

                // getting camera data from config
                JsonObject cameraConfig = camerasConfigurations.get(i).getAsJsonObject();
                int id = cameraConfig.get("id").getAsInt();
                int frequency = cameraConfig.get("frequency").getAsInt();
                String camera_key = cameraConfig.get("camera_key").getAsString();

                // creating a list of detected objects and a list of StampedDetectedObjects

                List<StampedDetectedObjects> StampedDetectedObjectsList = new ArrayList<>();

                // getting current camera data from camera file
                JsonArray currentCamera = camerasData.getAsJsonArray(camera_key);
                for (JsonElement element : currentCamera) {
                    List<DetectedObject> detectedObjectsList = new ArrayList<>();
                    JsonObject object = element.getAsJsonObject();
                    int time = object.get("time").getAsInt();
                    JsonArray currentCameraDetectedObjects = object.getAsJsonArray("detectedObjects");

                    // going over all detected objects and creating a list of StampedDetectedObjects
                    // with time
                    if (currentCameraDetectedObjects != null) {
                        for (int j = 0; (j < currentCameraDetectedObjects.size()); j++) {
                            JsonObject detectedObject = currentCameraDetectedObjects.get(j).getAsJsonObject();
                            String detectedObjectID = detectedObject.get("id").getAsString();
                            String detectedObjectDescription = detectedObject.get("description").getAsString();
                            idToDescription.put(detectedObjectID, detectedObjectDescription);
                            detectedObjectsList.add(new DetectedObject(detectedObjectID, detectedObjectDescription));
                        }
                        StampedDetectedObjectsList.add(new StampedDetectedObjects(time, detectedObjectsList));
                    }
                }

                // creating a camera and a camera service
                Camera camera = new Camera(id, frequency, StampedDetectedObjectsList);
                cameraServices[i] = new CameraService(camera);
            }

            /***********************************
             * Initialize LiDarServices
             ***********************************/
            JsonObject lidars = config.getAsJsonObject("LiDarWorkers");
            JsonArray lidarsConfigurations = lidars.getAsJsonArray("LidarConfigurations");
            String liDarDataPath = relativePath + lidars.get("lidars_data_path").getAsString().substring(1);
            LiDarDataBase lidarDataBase = LiDarDataBase.getInstance(liDarDataPath);

            // loop over all lidars
            LiDarService[] lidarServices = new LiDarService[lidarsConfigurations.size()];
            for (int i = 0; i < lidarsConfigurations.size(); i++) {

                // getting lidar data from config(id and frequency)
                JsonObject lidarConfig = lidarsConfigurations.get(i).getAsJsonObject();
                int id = lidarConfig.get("id").getAsInt();
                int frequency = lidarConfig.get("frequency").getAsInt();

                // creating a list of tracked objects for the lidar
                List<TrackedObject> trackedObjectsList = new ArrayList<>();

                // going over the lidarDataBase and adding the tracked objects to the trackedObjectsList
                List<StampedCloudPoints> stampedCloudPoints = lidarDataBase.getCloudPoints();
                for (StampedCloudPoints point : stampedCloudPoints) {
                    List<CloudPoint> cloudPointList = new ArrayList<>();
                    for (List<Double> pointList : point.getCloudPoints()) {
                        cloudPointList.add(new CloudPoint(pointList.get(0), pointList.get(1)));
                    }
                    trackedObjectsList.add(new TrackedObject(point.getID(), point.getTime(),
                            idToDescription.get(point.getID()), cloudPointList));
                }

                // creating a lidar and a lidar service
                LiDarWorkerTracker lidar = new LiDarWorkerTracker(id, frequency, trackedObjectsList);
                lidarServices[i] = new LiDarService(lidar);
            }

            /***********************************
             * Initialize PoseService
             ***********************************/
            String poseDataPath = relativePath + config.get("poseJsonFile").getAsString().substring(1);
            JsonArray poseData = parseJsonArrayConfig(poseDataPath); // Try to change that
            List<Pose> poseList = new LinkedList<>();

            // loop over all poses
            for (int i = 0; i < poseData.size(); i++) {

                // getting pose data from config
                JsonObject poseConfig = poseData.get(i).getAsJsonObject();
                int time = poseConfig.get("time").getAsInt();
                float x = poseConfig.get("x").getAsFloat();
                float y = poseConfig.get("y").getAsFloat();
                float yaw = poseConfig.get("yaw").getAsFloat();

                poseList.add(new Pose(x, y, yaw, time));
            }

            // Initialize GPSIMU with the pose data
            GPSIMU gpsimu = new GPSIMU();
            for (Pose pose : poseList) {
                gpsimu.updateTick(pose.getX(), pose.getY(), pose.getYaw(), pose.getTime());
            }

            // Create the PoseService
            PoseService poseService = new PoseService(gpsimu);

            /***********************************
             * Initialize FusionSlamService
             ***********************************/
            FusionSlam fusionSlam = FusionSlam.getInstance();
            FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);

            System.out.println("Starting simulation...");
            // Start the simulation
            startSimulation(timeService, cameraServices, lidarServices, poseService, fusionSlamService);
            System.out.println("Simulation completed.");

            System.out.println("Building output file...");
            // build the output file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            StatisticalFolder stats = StatisticalFolder.getInstance();

            List<LandMark> landMarks = fusionSlam.getLandMarks();
            //Get the error msg and the faulty sensor
            String error = null;
            String faultySensor = null;
            //loop that finds the first camera that crashed and saves the error msg
            for (CameraService cameraService : cameraServices){
                if(cameraService.getCamera().getStatus() == STATUS.ERROR){
                    Camera faultyCamera = cameraService.getCamera();
                    error = faultyCamera.getErrorObject().getDescription();
                    faultySensor = "Camera " + faultyCamera.getID();
                }        
            }

            //loop that finds the first lidar that crashed and saves the error msg
            for (LiDarService lidarService : lidarServices){
                if(lidarService.getLiDarWorkerTracker().getStatus() == STATUS.ERROR){
                    LiDarWorkerTracker faultyLiDar = lidarService.getLiDarWorkerTracker();
                    error = faultyLiDar.getErrorObject().getDescription();
                    faultySensor = "LiDar " + faultyLiDar.getID();
                }        
            }

            //getting the poses until the error
            List<Pose> poses = new LinkedList<>();
            if(error != null){
                for(int i = 0; i < stats.getSystemRuntime(); i++){
                    poses.add(poseList.get(i)); 
                }
            }

            //getting the last camera frames
            Map<String, LastFrameData> lastCameraFrames = new HashMap<>();
            for(CameraService cameraService : cameraServices){
                List<DetectedObject> detectedObjects = new ArrayList<>();
                StampedDetectedObjects lastFrame = cameraService.getLastFrame();
                if(lastFrame != null){
                    for(DetectedObject detectedObject : lastFrame.getDetectedObjects()){
                        detectedObjects.add(detectedObject);
                    }
                    lastCameraFrames.put("Camera " + cameraService.getCamera().getID(), new LastFrameData(lastFrame.getTime(), detectedObjects));
                }
            }

            // Getting the last lidar frames
            Map<String, List<TrackedObject>> lastLidarFrames = new HashMap<>();
            for (LiDarService lidarService : lidarServices) {
                List<TrackedObject> trackedObjects = new ArrayList<>(lidarService.getLiDarWorkerTracker().getTrackedObjectsList());
                lastLidarFrames.put("LiDar " + lidarService.getLiDarWorkerTracker().getID(), trackedObjects);
            }

            
            OutputData outputData = new OutputData(stats, landMarks);
            ErrorOutputData errorOutputData = new ErrorOutputData(error, faultySensor,lastCameraFrames ,lastLidarFrames, poses, outputData);

            
            if(error == null){
                try (FileWriter writer = new FileWriter("output_file.json")) {
                gson.toJson(outputData, writer);
            }
            System.out.println("Output file created: output_file.json");
            }else{
                try (FileWriter writer = new FileWriter("OutputError.json")) {
                gson.toJson(errorOutputData, writer);
                }
                System.out.println("Output file created: OutputError.json");
            }

        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred during the simulation.");
        }
    }

    /**
     * Parses the JSON configuration file and returns a JsonObject.
     *
     * @param filePath Path to the JSON configuration file.
     * @return JsonObject representing the configuration.
     * @throws IOException if the file cannot be read.
     */
    private static JsonObject parseJsonConfig(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static JsonArray parseJsonArrayConfig(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }

    /**
     * Starts the simulation by running all services.
     *
     * @param timeService       The TimeService instance.
     * @param cameraServices    Array of CameraService instances.
     * @param lidarServices     Array of LiDarWorkerService instances.
     * @param poseService       The PoseService instance.
     * @param fusionSlamService The FusionSlamService instance.
     */
    private static void startSimulation(
            TimeService timeService,
            CameraService[] cameraServices,
            LiDarService[] lidarServices,
            PoseService poseService,
            FusionSlamService fusionSlamService) throws InterruptedException {

        // Create a list of threads for all services
        List<Thread> threads = new ArrayList<>();

        // add camera and LiDAR services to the threads list
        for (CameraService camera : cameraServices) {
            threads.add(new Thread(camera));
        }

        for (LiDarService lidar : lidarServices) {
            threads.add(new Thread(lidar));
        }

        // add PoseService and FusionSlamService to the threads list
        threads.add(new Thread(poseService));
        threads.add(new Thread(fusionSlamService));

        // Start all threads except TimeService
        for (Thread thread : threads) {
            thread.start();
        }

        // Add TimeService to the threads list
        Thread timeThread = new Thread(timeService);
        threads.add(timeThread);

        // Start TimeService thread
        timeThread.start();

        // Wait for all threads to finish before returning
        for (Thread thread : threads) {
            thread.join();
        }
    }
}

@SuppressWarnings("unused")
class OutputData {
    private final int systemRuntime;
    private final int numDetectedObjects;
    private final int numTrackedObjects;
    private final int numLandmarks;
    private final List<LandMark> landMarks;

    public OutputData(StatisticalFolder stats, List<LandMark> landMarks) {
        this.systemRuntime = stats.getSystemRuntime();
        this.numDetectedObjects = stats.getNumDetectedObjects();
        this.numTrackedObjects = stats.getNumTrackedObjects();
        this.numLandmarks = stats.getNumLandmarks();
        this.landMarks = landMarks;
    }
}

@SuppressWarnings("unused")
class ErrorOutputData {
    private final String error;
    private final String faultySensor;
    private final Map<String, LastFrameData> lastCamerasFrames;
    private final Map<String, List<TrackedObject>> lastLiDarWorkerTrackersFrames;
    private final List<Pose> poses;
    private final OutputData statistics;
   

    public ErrorOutputData(String error, String faultySensor, Map<String, LastFrameData> lastCameraFrames, Map<String, List<TrackedObject>> lastLidarFrames, List<Pose> poses, OutputData statistics) {
        this.error = error;
        this.faultySensor = faultySensor;
        this.lastCamerasFrames = lastCameraFrames;
        this.lastLiDarWorkerTrackersFrames = lastLidarFrames;
        this.poses = poses;
        this.statistics = statistics;
    }
}

@SuppressWarnings("unused")
class LastFrameData {
    private final int time;
    private final List<DetectedObject> detectedObjects;

    public LastFrameData(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjects = detectedObjects;
    }
}
