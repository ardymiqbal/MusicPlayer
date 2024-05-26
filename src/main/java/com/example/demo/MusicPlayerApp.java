package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class MusicPlayerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("iksan.fxml"));
        primaryStage.setTitle("Music Player");
        Image icon = new Image(getClass().getResource("/images/Logo.png").toExternalForm());
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(false); // Mengatur maksimal ke false
        primaryStage.setResizable(false); // Mengatur resizable ke false
        primaryStage.show();
    }

}
