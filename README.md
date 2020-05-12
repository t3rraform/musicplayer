# Music player
This is a project from my school's UX course with JavaFX revisited and completely overhauled.
I wanted to see how good I could make it this time compared to before. So I started with a design in Figma, the vision 
I had with it was that I wanted to do something a bit different but it should be useful and clean looking.

<h4>Features</h4>

* Responsive user interface with custom title bar.
* Loading mp3/mp4 files from local drive and play the songs with standard media controls.
* Playlist data and the application state is persisted and stored with a .ser file.
* Playlist management, create playlists and add songs to them from other playlists or local drive.
* Audio visualization timeline is displayed when selecting a song.
* Youtube integration, download mp4 songs asynchronously and add them to playlists.

<h4>How to run and use</h4>
Download or clone this project, if you are on windows you can run the .exe file(It's unsigned so it can cause false positive virus alert)
or run the jar file using this command: 

 ```java --module-path "<Path to JavaFX>\lib" --add-modules javafx.controls,javafx.fxml,javafx.media --add-exports javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED -jar Musicplayer.jar ```
 
 Java 8+ is required to run it and JavaFX 2 if run from the jar.

<br>

![alt tag](https://github.com/t3rraform/musicplayer/blob/master/Thumbnail.png)

<details>
  <summary>A glimpse of the first music player</summary>
 
 <br>
 
 ![alt tag](https://github.com/t3rraform/musicplayer/blob/master/OldMusicplayer.jpg)
 
 </details>
 
