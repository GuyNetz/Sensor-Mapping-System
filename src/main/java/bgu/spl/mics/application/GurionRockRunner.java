package bgu.spl.mics.application;

import bgu.spl.mics.application.services.*;
import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
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
            TimeService timeService = new TimeService(config.get("TickTime").getAsInt(), config.get("Duration").getAsInt());
            
            // Initialize CameraServices
            JsonArray camerasConfigurations = config.getAsJsonArray("CamerasConfigurations");

            CameraService[] cameras = new CameraService[camerasConfigurations.size()];
            for (int i = 0; i < camerasConfigurations.size(); i++) {
                JsonObject cameraConfig = camerasConfigurations.get(i).getAsJsonObject();
                Camera camera = new Camera(
                    cameraConfig.get("id").getAsInt(),
                    cameraConfig.get("frequency").getAsInt()
                );
                cameras[i] = new CameraService(camera);
            }

            //TODO: FIX THIS
            // Initialize LiDarWorkerServices
            LiDarWorkerService[] lidarServices = initializeLiDarServices(config.getAsJsonArray("LiDars"));
            
            // Initialize PoseService
            PoseService poseService = new PoseService("PoseService", config.get("PoseJsonFile").getAsString());
            
            // Initialize FusionSlamService
            FusionSlamService fusionSlamService = new FusionSlamService("FusionSlamService");




            
            // Start the simulation
            startSimulation(timeService, cameraServices, lidarServices, poseService, fusionSlamService);

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
     * Initializes the camera services from the configuration.
     *
     * @param camerasConfig JSON array of camera configurations.
     * @return Array of initialized CameraService instances.
     */
    private static CameraService[] initializeCameraServices(JsonArray camerasConfig) {
        CameraService[] cameras = new CameraService[camerasConfig.size()];
        for (int i = 0; i < camerasConfig.size(); i++) {
            JsonObject cameraConfig = camerasConfig.get(i).getAsJsonObject();
            cameras[i] = new CameraService(
                "Camera-" + cameraConfig.get("id").getAsInt(),
                cameraConfig.get("camera_datas_path").getAsString(),
                cameraConfig.get("frequency").getAsInt()
            );
        }
        return cameras;
    }

    /**
     * Initializes the LiDAR services from the configuration.
     *
     * @param lidarsConfig JSON array of LiDAR configurations.
     * @return Array of initialized LiDarWorkerService instances.
     */
    private static LiDarWorkerService[] initializeLiDarServices(JsonArray lidarsConfig) {
        LiDarWorkerService[] lidars = new LiDarWorkerService[lidarsConfig.size()];
        for (int i = 0; i < lidarsConfig.size(); i++) {
            JsonObject lidarConfig = lidarsConfig.get(i).getAsJsonObject();
            lidars[i] = new LiDarWorkerService(
                "LiDAR-" + lidarConfig.get("id").getAsInt(),
                lidarConfig.get("lidars_data_path").getAsString(),
                lidarConfig.get("frequency").getAsInt()
            );
        }
        return lidars;
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
        LiDarWorkerService[] lidarServices,
        PoseService poseService,
        FusionSlamService fusionSlamService
    ) throws InterruptedException {
        Thread timeThread = new Thread(timeService);
        timeThread.start();

        // Start camera and LiDAR services
        for (CameraService camera : cameraServices) {
            new Thread(camera).start();
        }
        for (LiDarWorkerService lidar : lidarServices) {
            new Thread(lidar).start();
        }

        // Start PoseService and FusionSlamService
        new Thread(poseService).start();
        new Thread(fusionSlamService).start();

        // Wait for TimeService to complete
        timeThread.join();

        // Terminate all services
        for (CameraService camera : cameraServices) {
            camera.terminate();
        }
        for (LiDarWorkerService lidar : lidarServices) {
            lidar.terminate();
        }
        poseService.terminate();
        fusionSlamService.terminate();
    }
}
