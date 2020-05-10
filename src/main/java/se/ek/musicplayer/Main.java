package se.ek.musicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.StageStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.ek.musicplayer.controller.MainController;

public class Main extends Application {

    private Log log = LogFactory.getLog(getClass());

    private MainController mainController;

    public void start(Stage primaryStage) {
        log.info("Starting application");
        primaryStage.initStyle(StageStyle.UNDECORATED); //Removes default titlebar
        if (Util.loadObject("Musicplayer.ser") == null) {
            Util.saveObject("Musicplayer.ser", new PersistedData());
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
            Parent root = loader.load();

            mainController = loader.getController();
            mainController.setStage(primaryStage);

            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.show();
            ResizeHelper.addResizeListener(primaryStage, 800, 650, Double.MAX_VALUE, Double.MAX_VALUE);
        } catch (Exception e) {
            log.fatal("Failed to start application: " + e.getMessage());
        }
    }

    public void stop() {
        log.info("Stopped application");
        mainController.saveData();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
