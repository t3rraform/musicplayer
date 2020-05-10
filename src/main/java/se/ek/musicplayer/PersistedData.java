package se.ek.musicplayer;

import se.ek.musicplayer.model.Playlist;
import se.ek.musicplayer.model.Song;

import java.io.*;
import java.util.List;

public final class PersistedData implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Playlist> playlistItems;
    private Playlist selectedPlaylist;
    private int selectedPlaylistIndex;
    private Song selectedSong;
    private int selectedSongIndex;
    private double timeSliderValue;
    private double lastVolume;
    private boolean isEmpty;
    private boolean shuffle;
    private boolean repeat;
    private boolean mute;
    private String localFilesPath;

    public PersistedData(List<Playlist> playlistItems, Playlist selectedPlaylist, int selectedPlaylistIndex, Song selectedSong, int selectedSongIndex,
                         double timeSliderValue, double lastVolume, boolean shuffle, boolean repeat, boolean mute, String localFilesPath) {
        this.playlistItems = playlistItems;
        this.selectedPlaylist = selectedPlaylist;
        this.selectedPlaylistIndex = selectedPlaylistIndex;
        this.selectedSong = selectedSong;
        this.selectedSongIndex = selectedSongIndex;
        this.timeSliderValue = timeSliderValue;
        this.lastVolume = lastVolume;
        this.shuffle = shuffle;
        this.repeat = repeat;
        this.mute = mute;
        this.localFilesPath = localFilesPath;
    }

    public PersistedData() {
        this.isEmpty = true;
    }

    public List<Playlist> getPlaylistItems() {
        return playlistItems;
    }

    public void setPlaylistItems(List<Playlist> playlistItems) {
        this.playlistItems.addAll(playlistItems);
    }

    public Playlist getSelectedPlaylist() {
        return selectedPlaylist;
    }

    public void setSelectedPlaylist(Playlist selectedPlaylist) {
        this.selectedPlaylist = selectedPlaylist;
    }

    public int getSelectedPlaylistIndex() {
        return selectedPlaylistIndex;
    }

    public void setSelectedPlaylistIndex(int selectedPlaylistIndex) {
        this.selectedPlaylistIndex = selectedPlaylistIndex;
    }

    public Song getSelectedSong() {
        return selectedSong;
    }

    public void setSelectedSong(Song selectedSong) {
        this.selectedSong = selectedSong;
    }

    public int getSelectedSongIndex() {
        return selectedSongIndex;
    }

    public void setSelectedSongIndex(int selectedSongIndex) {
        this.selectedSongIndex = selectedSongIndex;
    }

    public double getTimeSliderValue() {
        return timeSliderValue;
    }

    public void setTimeSliderValue(double timeSliderValue) {
        this.timeSliderValue = timeSliderValue;
    }

    public double getLastVolume() {
        return lastVolume;
    }

    public void setLastVolume(double lastVolume) {
        this.lastVolume = lastVolume;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public String getLocalFilesPath() {
        return localFilesPath;
    }

    public void setLocalFilesPath(String localFilesPath) {
        this.localFilesPath = localFilesPath;
    }

    @Override
    public String toString() {
        return "PersistedData{" +
                "playlistItems=" + playlistItems +
                ", selectedPlaylist=" + selectedPlaylist +
                ", selectedSong=" + selectedSong +
                ", selectedSongIndex=" + selectedSongIndex +
                ", timeSliderValue=" + timeSliderValue +
                ", lastVolume=" + lastVolume +
                '}';
    }
}
