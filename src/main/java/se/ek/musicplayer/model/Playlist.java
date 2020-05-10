package se.ek.musicplayer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Playlist implements Serializable {

    private final UUID id;
    private String name;
    private List<Song> songs = new ArrayList<>();

    public Playlist(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public Playlist(String name, List<Song> songs) {
        this(name);
        this.songs = songs;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void addSongs(List<? extends Song> songs) {
        this.songs.addAll(songs);
    }

    @Override
    public String toString() {
        return name;
    }
}
