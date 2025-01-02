package bgu.spl.mics.application;

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

        if (args.length != 1) {
            System.err.println("Usage: java GurionRockRunner <configuration file path>");
            return;
        }

        String configFilePath = args[0];

        try {
            // Parse the configuration file using GSON
            JsonObject config = parseJsonConfig(configFilePath);


            // Extract simulation timing
            int tickDuration = config.get("TickTime").getAsInt();
            int duration = config.get("Duration").getAsInt();


            // Initialize TimeService
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

            // Start Threads
            for (CameraService cameraService : cameraServices) {
                Thread cameraThread = new Thread(cameraService);
                cameraThread.start();
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
            
            // Start Threads
            for (LiDarService lidarService : lidarServices) {
                Thread lidarThread = new Thread(lidarService);
                lidarThread.start();
            }


            // Initialize PoseService
            PoseService poseService = null;
            // Create the GPSIMU object
            String poseDataPath = config.get("poseJsonFile").getAsString();

            try(FileReader poseReader = new FileReader(poseDataPath)){
                Gson gson = new Gson();
                Type poseListType = new TypeToken<List<Pose>>(){}.getType();
                List<Pose> poseData = gson.fromJson(poseReader, poseListType);

                GPSIMU gpsimu = new GPSIMU();
                for (Pose pose : poseData) {
                    gpsimu.updateTick(pose.getX(), pose.getY(), pose.getYaw(), pose.getTime());
                }
            
            // Create and start the PoseService
                poseService = new PoseService(gpsimu);
                Thread poseServiceThread = new Thread(poseService);
                poseServiceThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }


            // Initialize FusionSlamService
                // Create the Fusion Slam object
                FusionSlam fusionSlam = FusionSlam.getInstance();

                // Initialize service
                FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);

                // Start Thread
                Thread fusionSlamThread = new Thread(fusionSlamService);
                fusionSlamThread.start();

                
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
        // Start TimeService
        Thread timeThread = new Thread(timeService);
        timeThread.start();

        // Start camera and LiDAR services
        for (CameraService camera : cameraServices) {
            new Thread(camera).start();
        }
        
        for (LiDarService lidar : lidarServices) {
            new Thread(lidar).start();
        }

        // Start PoseService and FusionSlamService
        new Thread(poseService).start();
        new Thread(fusionSlamService).start();

        // Wait for TimeService to complete
        timeThread.join();

        //Terminate all services
        for (CameraService camera : cameraServices) {
            camera.stopService();
        }
        for (LiDarService lidar : lidarServices) {
            lidar.stopService();
        }
        poseService.stopService();
        fusionSlamService.stopService();
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

