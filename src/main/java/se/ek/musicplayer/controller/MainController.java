package se.ek.musicplayer.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import application.WaveFormService;
import application.WaveVisualization;
import com.mpatric.mp3agic.Mp3File;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mp4parser.IsoFile;
import se.ek.musicplayer.PersistedData;
import se.ek.musicplayer.model.Playlist;
import se.ek.musicplayer.model.Song;

import static se.ek.musicplayer.Util.*;

public final class MainController implements Initializable {

    @FXML
    private TableView<Song> table;
    @FXML
    private TableColumn<Song, String> title;
    @FXML
    private TableColumn<Song, String> artist;
    @FXML
    private TableColumn<Song, String> album;
    @FXML
    private TableColumn<Song, String> duration;
    @FXML
    private TableColumn<Song, String> path;
    @FXML
    private Label durationLabel;
    @FXML
    private ImageView play_btn;
    @FXML
    private Label time;
    @FXML
    private Slider volume;
    @FXML
    private Slider timeSlider;
    @FXML
    private ImageView mute_img;
    @FXML
    private ImageView shuffleButton;
    @FXML
    private ImageView replayButton;
    @FXML
    private Label trackLabel;
    @FXML
    private Label artistLabel;
    @FXML
    private ImageView songImage;
    @FXML
    private BorderPane borderPane;
    @FXML
    private AnchorPane bottomPane;
    @FXML
    private AnchorPane timeSliderContainer;
    @FXML
    private AnchorPane playlist;
    @FXML
    private AnchorPane localFiles;
    @FXML
    private AnchorPane youtube;
    @FXML
    private AnchorPane titleBar;
    @FXML
    private Pane close;
    @FXML
    private Pane resize;
    @FXML
    private Pane minimize;
    @FXML
    private AnchorPane dragPane;
    @FXML
    private AnchorPane dragPaneLeft;
    @FXML
    private ListView<Playlist> playlists;
    @FXML
    private YoutubeController youtubeController;
    @FXML
    private LocalFilesController localFilesController;
    @FXML
    private TextField search;
    @FXML
    private Label localDriveLabel;
    @FXML
    private Label youtubeLabel;

    private Log log = LogFactory.getLog(getClass());

    private enum CurrentView {PLAYLIST, YOUTUBE, LOCAL_DRIVE}

    private CurrentView currentView = CurrentView.PLAYLIST;

    private double xOffset = 0;
    private double yOffset = 0;

    private Stage stage;
    private boolean maximized = false;
    private boolean mute = false;
    private boolean shuffle = false;
    private boolean repeat = false;
    private boolean playing = false;
    private MediaPlayer mp;
    private boolean newSongInstance = false;
    private Image playImage = resolveImage("play.png");
    private Image pauseImage = resolveImage("pause.png");
    private Image volumeImage = resolveImage("volume.png");
    private Image volumeMutedImage = resolveImage("volumeMuted.png");
    private Image shuffleImage = resolveImage("shuffle.png");
    private Image shuffleSelectedImage = resolveImage("shuffleSelected.png");
    private Image replayImage = resolveImage("replay.png");
    private Image replaySelectedImage = resolveImage("replaySelected.png");
    private Image defaultSongImage = resolveImage("albumCoverPlaceHolder.png");

    private int selectedSongIndex = 0;
    private Song selectedSong;
    private Duration durationValue;
    private double lastVolume;
    private int selectedPlaylistIndex;
    private Playlist selectedPlaylist;

    private List<Integer> previousSelectedSongs = new ArrayList<>();
    private int previousSelectedSongsIndex = 0;

    private InvalidationListener listen;
    public ObservableList<Song> tableItems = FXCollections.observableArrayList();
    public ObservableList<Playlist> playlistItems = FXCollections.observableArrayList();

    WaveVisualization waveVisualization = new WaveVisualization(400, 50);

    private ContextMenu playListContextMenu = new ContextMenu();
    private MenuItem editPlaylist = new MenuItem("Edit playlist");
    private MenuItem removePlaylist = new MenuItem("Remove playlist");

    private ContextMenu tableContextMenu = new ContextMenu();
    private Menu addToPlaylist = new Menu("Add to playlist");
    private MenuItem removeSong = new MenuItem("Remove from playlist");
    private String localFilesPath;
    private Parent titleBarObj;

    public void loadTitleBar() {
        titleBarObj = loadFXMLFile("/TitleBar.fxml", this);

        close.setOnMousePressed(event -> {
            stage.close();
        });
        resize.setOnMousePressed(event -> {
            stage.setMaximized(maximized = !maximized);
        });
        minimize.setOnMousePressed(event -> {
            stage.setIconified(true);
        });

        dragPane.setOnMousePressed(this::titleBarMousePressed);
        dragPane.setOnMouseClicked(this::titleBarMouseClicked);
        dragPane.setOnMouseDragged(this::titleBarMouseDragged);

        dragPaneLeft.setOnMousePressed(this::titleBarMousePressed);
        dragPaneLeft.setOnMouseClicked(this::titleBarMouseClicked);
        dragPaneLeft.setOnMouseDragged(this::titleBarMouseDragged);
    }

    public void titleBarMousePressed(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY) && !maximized) {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        }
    }

    public void titleBarMouseClicked(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (!maximized) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
            if (event.getClickCount() == 2) stage.setMaximized(maximized = !maximized);
        }
    }

    public void titleBarMouseDragged(MouseEvent event) {
        stage.setMaximized(maximized = false);
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    public void loadPlayList() {
        Parent root = loadFXMLFile("/Playlist.fxml", this);
        search.setPromptText("Search in playlist");
        tableItems.clear();
        tableItems.addAll(selectedPlaylist.getSongs());
        ((Pane) root).getChildren().add(titleBarObj);
        borderPane.setCenter(root);
        currentView = CurrentView.PLAYLIST;
        youtubeLabel.setStyle("-fx-text-fill: #bbbbbb");
        localDriveLabel.setStyle("-fx-text-fill: #bbbbbb");
    }

    public void loadLocalFiles() {
        localFiles.getChildren().add(titleBarObj);
        borderPane.setCenter(localFiles);
        currentView = CurrentView.LOCAL_DRIVE;
        localDriveLabel.setStyle("-fx-text-fill: white");
        youtubeLabel.setStyle("-fx-text-fill: #bbbbbb");
    }

    public void loadYoutubeRepository() {
        youtube.getChildren().add(titleBarObj);
        borderPane.setCenter(youtube);
        playlists.getSelectionModel().clearSelection();
        updateContextMenusPlaylists();
        search.setPromptText("Search on youtube");
        currentView = CurrentView.YOUTUBE;
        youtubeLabel.setStyle("-fx-text-fill: white");
        localDriveLabel.setStyle("-fx-text-fill: #bbbbbb");
    }

    public void createPlaylist() {
        playlistItems.add(0, new Playlist("New playlist"));
        updateContextMenusPlaylists();
    }

    public void updateContextMenusPlaylists() {
        addToPlaylist.getItems().clear();
        for (Playlist playlist : playlistItems) {
            MenuItem submenuItem = new MenuItem(playlist.getName());
            submenuItem.setOnAction(actionEvent -> playlist.addSongs(table.getSelectionModel().getSelectedItems()));
            addToPlaylist.getItems().add(submenuItem);
        }
        tableContextMenu.getItems().remove(addToPlaylist);
        tableContextMenu.getItems().add(0, addToPlaylist);
        youtubeController.updateContextMenusPlaylists(playlistItems);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        youtubeController.setMainController(this);
        localFilesController.setMainController(this);

        if (getFileNameFromURL(location).equals("Main.fxml")) {
            log.debug("Main init");
            BorderPane waveVisualizationContainer = new BorderPane();
            waveVisualizationContainer.setPrefSize(240, 50);
            waveVisualizationContainer.setCenter(waveVisualization);

            timeSliderContainer.getChildren().add(0, waveVisualizationContainer);

            timeSliderContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
                waveVisualization.setWidth(newVal.doubleValue());
                waveVisualizationContainer.setPrefWidth(newVal.doubleValue());
            });

            timeSliderContainer.boundsInLocalProperty().addListener(l -> {
                waveVisualization.setWidth(timeSliderContainer.getWidth());
                waveVisualization.setHeight(timeSliderContainer.getHeight());
            });

            playlists.setItems(playlistItems);
            playlists.setCellFactory(lv -> {
                TextFieldListCell<Playlist> cell = new TextFieldListCell<>();
                cell.setConverter(new StringConverter<Playlist>() {
                    @Override
                    public String toString(Playlist playlist) {
                        return playlist.getName();
                    }

                    @Override
                    public Playlist fromString(String string) {
                        Playlist playlist = cell.getItem();
                        playlist.setName(string);
                        return playlist;
                    }
                });
                return cell;
            });
            playlists.setOnEditCommit(row -> {
                playlists.getItems().set(row.getIndex(), row.getNewValue());
                if (row.getNewValue().toString().equals("")) playlistItems.remove(row.getIndex());
                playlists.setEditable(false);
                updateContextMenusPlaylists();
            });

            editPlaylist.setOnAction(actionEvent -> {
                playlists.setEditable(true);
                playlists.edit(selectedPlaylistIndex);
            });

            removePlaylist.setOnAction(actionEvent -> {
                playlistItems.remove(selectedPlaylist);
                tableItems.clear();
                updateContextMenusPlaylists();
            });

            playListContextMenu.getItems().addAll(removePlaylist, editPlaylist);
            playlists.setContextMenu(playListContextMenu);
            playlists.setOnMouseClicked(mouseEvent -> {
                selectedPlaylist = playlists.getSelectionModel().getSelectedItem();
                selectedPlaylistIndex = playlists.getSelectionModel().getSelectedIndex();
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    loadPlayList();
                    if (mouseEvent.getClickCount() == 2) {
                        nextSong();
                    }
                }
                if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                    playListContextMenu.show(playlists, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
            });

            search.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    String text = search.getText();
                    if (currentView.equals(CurrentView.YOUTUBE)) {
                        youtubeController.search(text);
                    }
                    if (currentView.equals(CurrentView.PLAYLIST)) {
                        if (text.equals("")) {
                            loadPlayList();
                            reselectSelectedSong();
                            return;
                        }
                        ObservableList<Song> searchResult = selectedPlaylist.getSongs().stream()
                                .filter(song -> searchFilter(song, text))
                                .collect(Collectors.toCollection(FXCollections::observableArrayList));
                        tableItems.clear();
                        tableItems.addAll(searchResult);
                        reselectSelectedSong();
                    }
                }
            });

            loadTitleBar();
            loadData();
            loadPlayList();
        }

        if (getFileNameFromURL(location).equals("Playlist.fxml")) {
            log.debug("Playlist init");
            title.setCellValueFactory(new PropertyValueFactory<>("title"));
            artist.setCellValueFactory(new PropertyValueFactory<>("artist"));
            album.setCellValueFactory(new PropertyValueFactory<>("album"));
            duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
            path.setCellValueFactory(new PropertyValueFactory<>("path"));
            table.setItems(tableItems);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            volume.valueProperty().addListener(ov -> {
                if (volume.isValueChanging()) {
                    try {
                        mp.setVolume(volume.getValue() / 100.0);
                        lastVolume = volume.getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            table.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
                if (table.getSelectionModel().getSelectedItem() != null) {
                    selectedSong = newValue;
                    selectedSongIndex = table.getSelectionModel().getSelectedIndex();
                    if (newValue != null) newSongInstance = true;
                    startWaveVisualization();
                }
            });
            table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            updateContextMenusPlaylists();

            removeSong.setOnAction(actionEvent -> {
                for (Song song : table.getSelectionModel().getSelectedItems()) {
                    selectedPlaylist.getSongs().removeIf(songInPlaylist -> songInPlaylist.equals(song));
                }
                tableItems.removeAll(table.getSelectionModel().getSelectedItems());
            });
            if (!tableContextMenu.getItems().contains(removeSong)) tableContextMenu.getItems().add(removeSong);

            table.setContextMenu(tableContextMenu);
            table.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    selectedSong = table.getSelectionModel().getSelectedItem();
                    selectedSongIndex = table.getSelectionModel().getSelectedIndex();
                    if (mouseEvent.getClickCount() == 2) {
                        previousSelectedSongs.clear();
                        previousSelectedSongs.add(selectedSongIndex);
                        previousSelectedSongsIndex = 0;
                        playSound();
                    }
                }
                if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                    tableContextMenu.show(table, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
            });

            timeSlider.setMin(0);
            timeSlider.setMax(1);
            timeSlider.valueProperty().addListener(ov -> {
                if (timeSlider.isValueChanging()) {
                    durationValue = mp.getMedia().getDuration();
                    mp.seek(durationValue.multiply(timeSlider.getValue()));
                }
            });
            timeSlider.setOnMouseClicked(e -> {
                timeSlider.setValue(e.getX() / timeSlider.getWidth() * timeSlider.getMax());
                mp.seek(durationValue.multiply(e.getX() / timeSlider.getWidth() * timeSlider.getMax()));
            });
        }
    }

    public boolean searchFilter(Song song, String text) {
        return song.getTitle().toLowerCase().contains(text)
                || (song.getArtist() != null && song.getArtist().toLowerCase().contains(text))
                || (song.getAlbum() != null && song.getAlbum().toLowerCase().contains(text));
    }

    public void reselectSelectedSong() {
        for (int i = 0; i < tableItems.size(); i++) {
            if (tableItems.get(i).equals(selectedSong)) {
                log.debug("Found song , reselect index: " + i);
                table.requestFocus();
                table.getSelectionModel().select(i);
                table.getFocusModel().focus(0);
                break;
            }
        }
    }

    public void startWaveVisualization() {
        waveVisualization.getWaveService().reset(); //TODO fix illegalstate exception
        waveVisualization.getWaveService().startService(selectedSong.getPath(), WaveFormService.WaveFormJob.AMPLITUDES_AND_WAVEFORM);
    }

    public List<Song> readSongsFromFileSystem(String pathToFolder) {
        List<Song> songs = new ArrayList<>();
        File musicFolder = new File(pathToFolder);

        for (File file : Objects.requireNonNull(musicFolder.listFiles())) {
            String fileName = file.getName();
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            String title = "", artist = "", album = "";
            byte[] imageData = null;
            if (extension.equals("mp3") || extension.equals("wav")) {
                try {
                    Mp3File mp3 = new Mp3File(file);
                    if (mp3.getId3v1Tag() != null) {
                        title = mp3.getId3v1Tag().getTitle();
                        artist = mp3.getId3v1Tag().getArtist();
                        album = mp3.getId3v1Tag().getAlbum();
                        imageData = mp3.getId3v2Tag().getAlbumImage();
                    } else if (mp3.getId3v2Tag() != null) {
                        title = mp3.getId3v2Tag().getTitle();
                        artist = mp3.getId3v2Tag().getArtist();
                        album = mp3.getId3v2Tag().getAlbum();
                        imageData = mp3.getId3v2Tag().getAlbumImage();
                    } else {
                        if (file.getName().contains("-")) {
                            title = fileName.split("-")[1].substring(0, fileName.split("-")[1].length() - 4);
                            artist = fileName.split("-")[0];
                        } else {
                            title = fileName.substring(0, fileName.length() - 4);
                        }
                    }
                    int duration = (int) mp3.getLengthInSeconds();
                    if (title == null || title.equals("")) continue;
                    songs.add(new Song(title, artist, album, duration, imageData, file.getAbsolutePath()));
                } catch (Exception e) {
                    log.warn("Error at reading mp3 or wav file: " + file.getName() + ", " + e.getMessage());
                }
            }
            if (extension.equals("mp4")) {
                if (file.getName().contains("-")) {
                    title = fileName.split("-")[1];
                    artist = file.getName().split("-")[0];
                } else {
                    title = file.getName();
                }
                try {
                    IsoFile isoFile = new IsoFile(file);
                    int duration = (int) (isoFile.getMovieBox().getMovieHeaderBox().getDuration() / isoFile.getMovieBox().getMovieHeaderBox().getTimescale());
                    songs.add(new Song(title, artist, album, duration, null, file.getAbsolutePath()));
                } catch (IOException e) {
                    log.warn("Error at reading mp4 file: " + file.getName() + ", " + e.getMessage());
                }
            }
        }
        return songs;
    }

    public void mute() {
        if (!mute) {
            mute_img.setImage(volumeMutedImage);
            mute = true;
            mp.setMute(true);
        } else {
            mute_img.setImage(volumeImage);
            mute = false;
            mp.setMute(false);
        }
    }

    public void shuffle() {
        if (!shuffle) {
            shuffle = true;
            shuffleButton.setImage(shuffleSelectedImage);
        } else {
            shuffle = false;
            shuffleButton.setImage(shuffleImage);
        }
    }

    public void repeat() {
        if (!repeat) {
            repeat = true;
            replayButton.setImage(replaySelectedImage);
        } else {
            repeat = false;
            replayButton.setImage(replayImage);
        }
    }

    public void nextSong() {
        if (shuffle) {
            Random rng = new Random();
            selectedSongIndex = rng.nextInt(tableItems.size());
            //If next song is same as last previous call nextSong again
            if (previousSelectedSongs.get(previousSelectedSongs.size() - 1) == selectedSongIndex && tableItems.size() > 1) {
                nextSong();
                return;
            }
        } else {
            selectedSongIndex = table.getSelectionModel().getSelectedIndex();
            selectedSongIndex++;
            if (selectedSongIndex > tableItems.size() - 1) selectedSongIndex = 0;
        }

        if (previousSelectedSongsIndex == previousSelectedSongs.size() - 1) {
            previousSelectedSongs.add(selectedSongIndex);
        }
        if (previousSelectedSongsIndex < previousSelectedSongs.size() - 1) previousSelectedSongsIndex++;

        selectedSongIndex = previousSelectedSongs.get(previousSelectedSongsIndex);

        table.getSelectionModel().clearSelection();
        table.getSelectionModel().select(selectedSongIndex);
        selectedSong = table.getSelectionModel().getSelectedItem();
        playSound();
    }

    public void previousSong() {
        if (previousSelectedSongsIndex > 0) previousSelectedSongsIndex--;
        selectedSongIndex = previousSelectedSongs.get(previousSelectedSongsIndex);

        table.getSelectionModel().clearSelection();
        table.getSelectionModel().select(selectedSongIndex);
        selectedSong = table.getSelectionModel().getSelectedItem();
        playSound();
    }

    public void displayTime() {
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5), (ActionEvent actionEvent) -> {
            time.setText(getDurationFormatted((int) mp.getCurrentTime().toSeconds()));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        durationLabel.setText(selectedSong.getDuration());
    }

    public void timeSlider() {
        Platform.runLater(() -> {
            double currentTime = mp.getCurrentTime().toMillis();
            try {
                timeSlider.setValue(currentTime / mp.getMedia().getDuration().toMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void playSound() {
        try {
            if (newSongInstance) {
                if (playing) {
                    mp.stop();
                    play_btn.setImage(playImage);
                    playing = false;
                }
                Media hit = new Media(Paths.get(selectedSong.getPath()).toUri().toString());
                mp = new MediaPlayer(hit);
                mp.setVolume(volume.getValue() / 100.0);
                mp.play();

                trackLabel.setText(selectedSong.getTitle());
                artistLabel.setText(selectedSong.getArtist());
                if (selectedSong.getImageData() != null) {
                    Image image = new Image(new ByteArrayInputStream(selectedSong.getImageData()));
                    songImage.setImage(image);
                } else {
                    songImage.setImage(defaultSongImage);
                }
                songImage.setFitWidth(115);
                songImage.setFitHeight(90);
                songImage.setSmooth(true);
                songImage.setPreserveRatio(false);

                listen = ov -> {
                    timeSlider();
                    mp.setOnEndOfMedia(() -> {
                        mp.dispose();
                        if (repeat) {
                            newSongInstance = true;
                            playSound();
                        } else {
                            nextSong();
                        }
                    });
                };
                mp.currentTimeProperty().addListener(listen);
                displayTime();
                play_btn.setImage(pauseImage);
                playing = true;
                newSongInstance = false;
            } else if (playing) {
                mp.pause();
                play_btn.setImage(playImage);
                playing = false;
            } else if (mp != null) {
                mp.play();
                play_btn.setImage(pauseImage);
                playing = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addSongsToSelectedPlaylist(List<Song> songs) {
        log.info("Add songs to selected playlist, size " + songs.size());
        for (Song song : songs) {
            if (!selectedPlaylist.getSongs().contains(song)) selectedPlaylist.getSongs().add(song);
        }
    }

    public void setLocalFilesPath(String path) {
        this.localFilesPath = path;
    }

    public String getLocalFilesPath() {
        return localFilesPath;
    }

    public void loadData() {
        log.info("Load persisted data");
        PersistedData persistedData = (PersistedData) loadObject("Musicplayer.ser");
        if (persistedData == null) {
            log.warn("Persisted data null");
            return;
        }
        if (persistedData.isEmpty()) {
            Playlist defaultPlaylist = new Playlist("Default playlist");
            selectedPlaylist = defaultPlaylist;
            playlistItems.add(defaultPlaylist);
            playlistItems.add(new Playlist("Youtube songs"));
            volume.setValue(25);
            return;
        }

        playlistItems.addAll(persistedData.getPlaylistItems());
        selectedPlaylist = persistedData.getSelectedPlaylist();
        selectedSong = persistedData.getSelectedSong();
        selectedSongIndex = persistedData.getSelectedSongIndex();
        previousSelectedSongs.clear();
        previousSelectedSongs.add(selectedSongIndex);
        previousSelectedSongsIndex = 0;
        localFilesPath = persistedData.getLocalFilesPath();

        playlists.getSelectionModel().select(persistedData.getSelectedPlaylist());

        Platform.runLater(() -> {
            table.requestFocus();
            table.getSelectionModel().select(selectedSongIndex);
            table.getFocusModel().focus(selectedSongIndex);
            table.scrollTo(selectedSongIndex);

            lastVolume = persistedData.getLastVolume();
            volume.setValue(lastVolume);

            if (selectedSong != null) {
                newSongInstance = true;
                playSound();
                timeSlider.setValue(persistedData.getTimeSliderValue());
                mp.setOnReady(() -> {
                    mp.seek(Duration.seconds(selectedSong.getDurationValue() * persistedData.getTimeSliderValue()));
                    playSound();
                });
                waveVisualization.getWaveService().startService(selectedSong.getPath(), WaveFormService.WaveFormJob.AMPLITUDES_AND_WAVEFORM);
            }
            if (persistedData.isShuffle()) shuffle();
            if (persistedData.isRepeat()) repeat();
            if (persistedData.isMute()) mute();
        });
    }

    public void saveData() {
        log.info("Persist data");
        PersistedData persistedData = new PersistedData(new ArrayList<>(playlistItems), selectedPlaylist, selectedPlaylistIndex, selectedSong, selectedSongIndex, timeSlider.getValue(), lastVolume, shuffle, repeat, mute, localFilesPath);
        saveObject("Musicplayer.ser", persistedData);
    }

    public void setSelectedSong(Song selectedSong) {
        this.selectedSong = selectedSong;
    }

    public void setNewSongInstance(boolean newSongInstance) {
        this.newSongInstance = newSongInstance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
