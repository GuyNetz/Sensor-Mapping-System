package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    //Fields
    private static LiDarDataBase instance = null;
    private List<StampedCloudPoints> cloudPoints;

    //Constructor
    private LiDarDataBase(String filePath) {
        loadData(filePath);
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static synchronized LiDarDataBase getInstance(String filePath) {
        if (instance == null) {
            instance = new LiDarDataBase(filePath);
        }
        return instance;
    }

    private void loadData(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            // Create a new GSON instance for parsing.
            Gson gson = new Gson();

            // Define the type of the data structure to be loaded.
            Type listType = new TypeToken<List<StampedCloudPoints>>() {}.getType();

            // Parse the JSON file into the cloudPoints list.
            cloudPoints = gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load LiDAR data from file: " + filePath);
        }
    }

    /**
     * Retrieves the cloud points data.
     *
     * @return The list of StampedCloudPoints objects.
     */
    public List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }
}