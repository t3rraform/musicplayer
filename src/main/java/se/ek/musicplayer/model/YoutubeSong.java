package se.ek.musicplayer.model;

import javafx.scene.image.ImageView;

import static se.ek.musicplayer.Util.*;

public final class YoutubeSong extends Song {

    private String uri; //Youtube link
    private String videoId;
    private transient ImageView status;

    public YoutubeSong(String title, String artist, String uri, String thumbnailUrl, String duration) {
        super(title, artist, thumbnailUrl, duration);
        this.uri = uri;
        this.videoId = uri.substring(uri.indexOf("=") + 1);
        this.status = new ImageView(resolveImage("download.png"));
        status.setFitHeight(18);
        status.setPreserveRatio(true);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public ImageView getStatus() {
        return status;
    }

    public void setStatus(ImageView status) {
        this.status = status;
    }
}
