package bgu.spl.mics.application;

import java.util.ArrayList;
import bgu.spl.mics.application.services.*;
import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
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
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        System.out.println("Starting the GurionRock Pro Max Ultra Over 9000 simulation...");

        // if (args.length != 1) {
        //     System.err.println("Usage: java GurionRockRunner <configuration file path>");
        //     return;
        // }

        String configFilePath = "/workspaces/spl assignment 2/example input/configuration_file.json";

        try {
            System.out.println("Debug 1");
            // Parse the configuration file using GSON
            JsonObject config = parseJsonConfig(configFilePath);

            System.out.println("Debug 2");
            // Initialize TimeService
            int tickDuration = config.get("TickTime").getAsInt();
            int duration = config.get("Duration").getAsInt();
            TimeService timeService = new TimeService(tickDuration, duration);
            
            // Initialize CameraServices
            JsonObject cameras = config.getAsJsonObject("Cameras");
            JsonArray camerasConfigurations = cameras.getAsJsonArray("CamerasConfigurations");

            CameraService[] cameraServices = new CameraService[camerasConfigurations.size()];
            for (int i = 0; i < camerasConfigurations.size(); i++) {
                JsonObject cameraConfig = camerasConfigurations.get(i).getAsJsonObject();

                Camera camera = new Camera(
                    cameraConfig.get("id").getAsInt(),
                    cameraConfig.get("frequency").getAsInt()
                );
                cameraServices[i] = new CameraService(camera);
            }

            // Initialize LiDarServices
            JsonObject lidarWorkers = config.getAsJsonObject("LidarWorkers");
            JsonArray lidarConfigurations = lidarWorkers.getAsJsonArray("LidarConfigurations");

            LiDarService[] lidarServices = new LiDarService[lidarConfigurations.size()];
            for (int i = 0; i < lidarConfigurations.size(); i++) {
                JsonObject lidarConfig = lidarConfigurations.get(i).getAsJsonObject();

                LiDarWorkerTracker worker = new LiDarWorkerTracker(
                    lidarConfig.get("id").getAsInt(), 
                    lidarConfig.get("frequency").getAsInt()
                );

                lidarServices[i] = (new LiDarService(worker));
            }
        
            System.out.println("Debug 3");

            // Initialize PoseService
            PoseService poseService = null;
            String poseDataPath = config.get("poseJsonFile").getAsString();

            try(FileReader poseReader = new FileReader(poseDataPath)){
                Gson gson = new Gson();
                Type poseListType = new TypeToken<List<Pose>>(){}.getType();
                List<Pose> poseData = gson.fromJson(poseReader, poseListType);

                GPSIMU gpsimu = new GPSIMU();
                for (Pose pose : poseData) {
                    gpsimu.updateTick(pose.getX(), pose.getY(), pose.getYaw(), pose.getTime());
                }            
                poseService = new PoseService(gpsimu);
            }

            // Initialize FusionSlamService
            FusionSlam fusionSlam = FusionSlam.getInstance();
            FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);
                
            // Start the simulation
            startSimulation(timeService, cameraServices, lidarServices, poseService, fusionSlamService);

            //build the output file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            StatisticalFolder stats = StatisticalFolder.getInstance();
            List<LandMark> landMarks = fusionSlam.getLandMarks();

            OutputData outputData = new OutputData(stats, landMarks);

            try (FileWriter writer = new FileWriter("output.json")) {
                gson.toJson(outputData, writer); 
            }
            System.out.println("Output file created successfully.");
            System.out.println("Simulation completed successfully.");

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


    /**
     * Starts the simulation by running all services.
     *
     * @param timeService      The TimeService instance.
     * @param cameraServices   Array of CameraService instances.
     * @param lidarServices    Array of LiDarWorkerService instances.
     * @param poseService      The PoseService instance.
     * @param fusionSlamService The FusionSlamService instance.
     */
    private static void startSimulation(
        TimeService timeService,
        CameraService[] cameraServices,
        LiDarService[] lidarServices,
        PoseService poseService,
        FusionSlamService fusionSlamService
    ) throws InterruptedException {
        
        List<Thread> threads = new ArrayList<>();

        // add camera and LiDAR services
        for (CameraService camera : cameraServices) {
            threads.add(new Thread(camera));
        }
        
        for (LiDarService lidar : lidarServices) {
            threads.add(new Thread(lidar));
        }

        // add PoseService and FusionSlamService
        threads.add(new Thread(poseService));
        threads.add( new Thread(fusionSlamService));

        // Start all threads
        // for (Thread thread : threads) {
        //     thread.start();
        // }

        Thread timeThread = new Thread(timeService);
        threads.add(timeThread);
        
        // Start TimeService 
        timeThread.start();

        System.out.println("before join");
        // for(Thread thread : threads){
        //     thread.join();
        // }
        System.out.println("after join");
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

