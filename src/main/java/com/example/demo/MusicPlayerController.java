package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;

import java.io.File;
import java.util.*;

public class MusicPlayerController {


    @FXML private Label durationLabel;
    @FXML private Button playButton;
    @FXML private ListView<String> songList;
    @FXML private Slider volumeSlider;
    @FXML private ImageView albumCover;
    @FXML private Label songName;
    @FXML private Label ArtistName;
    @FXML private Slider timeSlider;
    @FXML
    private ImageView pauseImage;
    @FXML
    private ImageView playImage;




    private MediaPlayer mediaPlayer;
    private List<File> playlist = new ArrayList<>();
    private int currentSongIndex = -1;



double volume = 1.0;
    @FXML void initialize() {
        timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null && timeSlider.isValueChanging()) {
                mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(newValue.doubleValue() / 100.0));
            }
        });

        volumeSlider.valueChangingProperty().addListener((observable, oldValue, isChanging) -> {
            if (!isChanging) {
                double sliderValue = volumeSlider.getValue();
                 volume = sliderValue / 100.0; // Normalisasi nilai slider menjadi rentang 0.0 - 1.0

                if (mediaPlayer != null) {
                    if (sliderValue == 0) {
                        mediaPlayer.setVolume(0); // Jika slider di ujung kiri, atur volume menjadi 0
                    } else {
                        mediaPlayer.setVolume(volume); // Atur volume berdasarkan posisi slider
                    }
                }
            }
        });

        songList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2){
                int selectedIndex = songList.getSelectionModel().getSelectedIndex();
                if (selectedIndex != -1) {
                    currentSongIndex = selectedIndex;
                    playSelectedMusic(playlist.get(selectedIndex));
                }
            }
        });

        songList.setOnDragOver(event -> {
            if (event.getGestureSource() != songList && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        songList.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                List<File> files = dragboard.getFiles();
                files.forEach(this::addToPlaylist); // Tambahkan file yang di-drop ke dalam playlist
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
        songList.setCellFactory(list -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                int selectedIndex = cell.getIndex();
                if (selectedIndex >= 0) {
                    playlist.remove(selectedIndex);
                    songList.getItems().remove(selectedIndex);
                    if (selectedIndex < currentSongIndex) {
                        currentSongIndex--;
                    }
                }
                updateSongList();
            });

            contextMenu.getItems().add(deleteItem);

            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                cell.setContextMenu(isNowEmpty ? null : contextMenu);
            });
            cell.setStyle("-fx-background-color: rgba(0,0,0,0); -fx-text-fill: white;");
            return cell;

        });


    }

    @FXML void play() {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playButton.setText("Play");
                pauseImage.setVisible(false);
                playImage.setVisible(true);
            } else {
                mediaPlayer.play();
                playButton.setText("Pause");
                playImage.setVisible(false);
                pauseImage.setVisible(true);
            }
        }
    }

    @FXML void next() {
        if (!playlist.isEmpty() && currentSongIndex < playlist.size() - 1) {
            mediaPlayer.stop();
            currentSongIndex++;
            playSelectedMusic(playlist.get(currentSongIndex));
        }
        else {
            mediaPlayer.play();
        }
    }

    @FXML void previous() {
        if (!playlist.isEmpty() && currentSongIndex > 0) {
            mediaPlayer.stop();
            currentSongIndex--;
            playSelectedMusic(playlist.get(currentSongIndex));
        }
        else {
            mediaPlayer.play();
        }
    }

    @FXML void chooseMusic() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Music Files");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.ogg", "*.m4a")
        );
        Stage stage = (Stage) playButton.getScene().getWindow();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null) {
            selectedFiles.forEach(this::addToPlaylist);
        }
    }

    private void addToPlaylist(File file) {
        playlist.add(file);
        updateSongList();
        if (currentSongIndex == -1) {
            currentSongIndex = 0;
            playSelectedMusic(playlist.get(currentSongIndex));
        }
    }

    private void updateSongList() {
        songList.getItems().clear();
        int songNumber = 1;
        for (File song : playlist) {
            String songName = song.getName();
            songList.getItems().add(songNumber + ". " + songName);
            songNumber++;
        }
    }
    private String formatDuration(javafx.util.Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
    private void updateDurationLabel() {
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                javafx.util.Duration currentTime = mediaPlayer.getCurrentTime();
                javafx.util.Duration totalDuration = mediaPlayer.getTotalDuration();
                durationLabel.setText(formatDuration(currentTime) + " / " + formatDuration(totalDuration));
            }
        });

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null && !timeSlider.isValueChanging()) {
                double currentTimeMs = mediaPlayer.getCurrentTime().toMillis();
                double totalTimeMs = mediaPlayer.getTotalDuration().toMillis();
                double percentage = (currentTimeMs / totalTimeMs) * 100.0;
                timeSlider.setValue(percentage);
            }
        });
    }

    private void playSelectedMusic(File file) {
        Media media = new Media(file.toURI().toString());

        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            String title = null;
            String artist = null;
            Artwork artwork = null;

            if (tag != null) {
                title = tag.getFirst(FieldKey.TITLE);
                artist = tag.getFirst(FieldKey.ALBUM_ARTIST);
                artwork = tag.getFirstArtwork();
            }

            ArtistName.setText(artist != null ? artist : file.getName());
            songName.setText((title != null && !title.isEmpty()) ? title : file.getName());

            if (artwork != null) {
                byte[] imageData = artwork.getBinaryData();
                Image image = new Image(new ByteArrayInputStream(imageData));
                albumCover.setImage(image);
            } else {
                Image defaultImage = new Image(getClass().getResource("/images/image-not-found.jpg").toExternalForm());
                albumCover.setImage(defaultImage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
            playButton.setText("Pause");
            playImage.setVisible(false);
            pauseImage.setVisible(true);
            updateDurationLabel();
            mediaPlayer.setVolume(volume);
        });
        mediaPlayer.setOnEndOfMedia(this::next);


    }


}
