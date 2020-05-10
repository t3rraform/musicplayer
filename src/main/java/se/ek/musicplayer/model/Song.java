package se.ek.musicplayer.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.ek.musicplayer.Util;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Objects;

public class Song implements Serializable {

    protected String title;
    protected String artist;
    protected String album;
    protected String duration;
    protected byte[] imageData;
    protected int durationValue;
    protected String path; //Path on local drive
    protected transient ImageView coverImage; //From image fetched from youtube
    private transient Log log = LogFactory.getLog(getClass());

    public Song(String title, String artist, String album, int duration, byte[] imageData, String path) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = Util.getDurationFormatted(duration);
        this.durationValue = duration;
        this.imageData = imageData;
        this.path = path;
    }

    public Song(String title, String artist, String thumbnailUrl, String duration) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.coverImage = new ImageView(new Image(thumbnailUrl));
        coverImage.setFitHeight(55);
        coverImage.setPreserveRatio(true);
        try {
            imageData = IOUtils.toByteArray(new URL(thumbnailUrl));
        } catch (IOException e) {
            log.error("Failed to get byte array from IOUtils with url: " + thumbnailUrl);
        }
    }

    public String getDuration() {
        return duration;
    }

    public int getDurationValue() {
        return durationValue;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ImageView getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(ImageView coverImage) {
        this.coverImage = coverImage;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public byte[] getImageData() {
        return imageData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(title, song.title) &&
                Objects.equals(artist, song.artist) &&
                Objects.equals(album, song.album) &&
                Objects.equals(duration, song.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, artist, album, duration);
    }

    @Override
    public String toString() {
        return "Song{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", duration='" + duration + '\'' +
                ", durationValue=" + durationValue +
                ", path='" + path + '\'' +
                ", coverImage=" + coverImage +
                '}';
    }
}
