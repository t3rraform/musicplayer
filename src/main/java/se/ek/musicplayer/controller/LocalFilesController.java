package se.ek.musicplayer.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import se.ek.musicplayer.model.Song;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LocalFilesController implements Initializable {

    private MainController mainController;

    @FXML
    private AnchorPane dragAndDropContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        dragAndDropContainer.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });

        dragAndDropContainer.setOnDragDropped(event -> {
            List<Song> songs = mainController.readSongsFromFileSystem(event.getDragboard().getFiles().get(0).toString());
            mainController.addSongsToSelectedPlaylist(songs);
        });
    }

    public void selectSongFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedFolder = directoryChooser.showDialog(null);
        if (selectedFolder != null) {
            List<Song> songs = mainController.readSongsFromFileSystem(selectedFolder.toString());
            mainController.setLocalFilesPath(selectedFolder.toString());
            mainController.addSongsToSelectedPlaylist(songs);
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

}
