package com.example.demo1;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Charger le fichier FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));

        // Créer la scène avec le contenu FXML
        Scene scene = new Scene(fxmlLoader.load());

        // Définir la scène de la fenêtre
        stage.setScene(scene);

        // Fixer les tailles minimale et maximale de la fenêtre
        stage.setMinWidth(500); // Largeur minimale de la fenêtre
        stage.setMinHeight(750); // Hauteur minimale de la fenêtre
        stage.setMaxHeight(750); // Hauteur maximale de la fenêtre

        // Définir le titre de la fenêtre
        stage.setTitle("Image Viewer");

        // Afficher la fenêtre
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
