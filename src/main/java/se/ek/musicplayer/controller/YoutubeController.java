package se.ek.musicplayer.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.ek.musicplayer.YoutubeRepository;
import se.ek.musicplayer.model.Playlist;
import se.ek.musicplayer.model.YoutubeSong;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static se.ek.musicplayer.Util.*;

public class YoutubeController implements Initializable {

    @FXML
    private TableView<YoutubeSong> table;
    @FXML
    private TableColumn<YoutubeSong, String> title;
    @FXML
    private TableColumn<YoutubeSong, String> artist;
    @FXML
    private TableColumn<YoutubeSong, String> coverImage;
    @FXML
    private TableColumn<YoutubeSong, String> uri;
    @FXML
    private TableColumn<YoutubeSong, String> duration;
    @FXML
    private TableColumn<YoutubeSong, ImageView> status;

    private Log log = LogFactory.getLog(getClass());

    private YoutubeSong selectedSong;

    private MainController mainController;
    public ObservableList<YoutubeSong> youtubeSongs = FXCollections.observableArrayList();
    private final YoutubeRepository youtubeRepo = new YoutubeRepository();

    private ContextMenu tableContextMenu = new ContextMenu();
    private Menu addToPlaylist = new Menu("Add to playlist");

    public void updateContextMenusPlaylists(ObservableList<Playlist> playlistItems) {
        addToPlaylist.getItems().clear();
        for (Playlist playlist : playlistItems) {
            MenuItem submenuItem = new MenuItem(playlist.getName());
            submenuItem.setOnAction(actionEvent -> {
                ObservableList<YoutubeSong> downloadedSongs = FXCollections.observableArrayList();
                for (YoutubeSong selectedItem : table.getSelectionModel().getSelectedItems()) {
                    if (selectedItem.getPath() != null) downloadedSongs.add(selectedItem);
                }
                playlist.addSongs(downloadedSongs);
            });
            addToPlaylist.getItems().add(submenuItem);
        }
        tableContextMenu.getItems().remove(addToPlaylist);
        tableContextMenu.getItems().add(0, addToPlaylist);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.debug("Init youtubeController");

        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        artist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        coverImage.setCellValueFactory(new PropertyValueFactory<>("coverImage"));
        uri.setCellValueFactory(new PropertyValueFactory<>("uri"));
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        table.setItems(youtubeSongs);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        List<YoutubeSong> results = youtubeRepo.getSearchResults("https://www.youtube.com/results?search_query=music"); //TODO if already exist make it playable
        youtubeSongs.addAll(results);

        table.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (table.getSelectionModel().getSelectedItem() != null) {
                selectedSong = newValue;
                if (newValue != null) {
                    mainController.setNewSongInstance(true);
                }
            }
        });

        table.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                selectedSong = table.getSelectionModel().getSelectedItem();
                final YoutubeSong selectedSongLocal = table.getSelectionModel().getSelectedItem();

                if (mouseEvent.getClickCount() == 2) {
                    if (selectedSong.getPath() == null) {
                        CompletableFuture.supplyAsync(() -> {
                            ImageView image = new ImageView(resolveImage("loading.gif"));
                            image.setFitHeight(22);
                            image.setPreserveRatio(true);
                            log.info("Download youtube song " + selectedSongLocal.getUri());
                            Platform.runLater(() -> {
                                selectedSongLocal.setStatus(image);
                                table.refresh();
                            });
                            return youtubeRepo.downloadMp3FromVideo(selectedSongLocal, mainController.getLocalFilesPath() + "/youtube");
                        }).thenAccept(file -> {
                            log.info("Downloaded file " + file);
                            ImageView image = new ImageView(resolveImage("tick.png"));
                            image.setFitHeight(18);
                            image.setPreserveRatio(true);
                            log.info("Updated selected song, path: " + selectedSongLocal.getPath());

                            Platform.runLater(() -> {
                                selectedSongLocal.setStatus(image);
                                selectedSongLocal.setPath(file.getAbsolutePath());
                                table.refresh();
                            });

                        });
                    } else {
                        log.info("SelectedSong found playing it " + selectedSong.getPath());
                        mainController.setSelectedSong(selectedSong);
                        mainController.startWaveVisualization();
                        mainController.playSound();
                    }
                }
            }
        });
        table.setContextMenu(tableContextMenu);
    }

    public void search(String text) {
        List<YoutubeSong> resultsOnSearch = youtubeRepo.getSearchResults("https://www.youtube.com/results?search_query=" + text);
        youtubeSongs.clear();
        youtubeSongs.addAll(resultsOnSearch);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
