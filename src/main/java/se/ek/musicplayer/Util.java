package se.ek.musicplayer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;

public final class Util {

    public static String getDurationFormatted(int duration) {
        int seconds = duration % 60;
        int minutes = (duration / 60) % 60;
        String sec = (seconds < 10 ? "0" : "") + seconds;
        String min = Integer.toString(minutes);
        return min + ":" + sec;
    }

    public static Parent loadFXMLFile(String location, Object controllerInstance) {
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(Util.class.getResource(location));
            if (controllerInstance != null) loader.setController(controllerInstance);
            root = loader.load();
            return root;
        } catch (Exception e) {
            LogFactory.getLog(Util.class).error("Failed to load fxml error: " + e.getMessage());
        }
        return null;
    }

    public static String getFileNameFromURL(URL location) {
        return location.getFile().substring(location.getFile().lastIndexOf('/') + 1);
    }

    public static void saveObject(String filename, Serializable data) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filename);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(data);
        } catch (IOException e) {
            LogFactory.getLog(Util.class).error("Failed to save data: " + e.getMessage());
        }
    }

    public static Object loadObject(String filename) {
        if (!new File(filename).exists()) return null;
        try (FileInputStream fileInputStream = new FileInputStream(filename);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LogFactory.getLog(Util.class).error("Failed to load persisted data: " + e.getMessage());
        }
        return null;
    }

    public static Image resolveImage(String imageFile) {
        return new Image(Util.class.getResourceAsStream("/images/" + imageFile));
    }
}
