package com.example.demo1;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import javafx.scene.control.*;

import com.example.demo1.Filters.*;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.util.Stack;

import javafx.scene.image.WritableImage;


import java.io.File;

import static com.example.demo1.Filters.DerbyManager.getConnection;

public class HelloController {


    @FXML
    private TextField tagTextField;
    private static final String IMAGE_DIRECTORY = "src/main/resources/images/";

    @FXML
    public Button filtererButton;
    @FXML
    private BorderPane mainPane;

    @FXML
    private ImageView imageView;

    @FXML
    private Button loadButton, rotateButton, zoomInButton, zoomOutButton, addTagButton,searchButton,redoButton,resetButton;
    @FXML
    private Button grayscaleButton, sepiaButton, swapRGBButton, sobelButton, undoButton, mirrorButton, verticalMirrorButton, symetrie, zoom;
    private final Stack<ImageState> history = new Stack<>();
    private final Stack<ImageState> redoHistory = new Stack<>();
    private double zoomFactor = 1.0;
    private Image originalImage;
    private boolean loadFiltre = false;
    private boolean loadSymetrie = false;
    private boolean loadZoom = false;
    private final double MAX_ZOOM = 1.5;
    private final double MIN_ZOOM = 0.5;
    private File loadedFile;
    private final List<String> transforms = new ArrayList<>();
    private final List<String> transformsredo = new ArrayList<>();
    private final Stack<String> redoTransforms = new Stack<>();
    Connection conn;
    public HelloController() throws SQLException {
        // Création de la table si elle n'existe pas (avec la colonne transformations)
        DerbyManager.initializeDatabase();
        conn = DriverManager.getConnection("jdbc:derby:monDB;create=true", "user", "password");
    }
    @FXML
    public void initialize() {
        hideAllButtonsExceptLoad();

        loadButton.setOnAction(event -> loadImage());
        rotateButton.setOnAction(event -> rotateImage());
        zoomInButton.setOnAction(event -> zoomIn());
        zoomOutButton.setOnAction(event -> zoomOut());

        filtererButton.setOnAction(event -> filtres());
        symetrie.setOnAction(event -> symetries());
        zoom.setOnAction(event ->zooms());
        grayscaleButton.setOnAction(e -> applyFilter(new GrayscaleManualFilter(), "grayscale"));
        sepiaButton.setOnAction(e -> applyFilter(new SepiaFilterManual(), "sepia"));
        swapRGBButton.setOnAction(e -> applyFilter(new SwapRGBFilter(), "swapRGB"));
        sobelButton.setOnAction(e -> applyFilter(new SobelFilter(), "sobel"));
        mirrorButton.setOnAction(e -> applyFilter(new MirrorFilter(), "flipH"));
        verticalMirrorButton.setOnAction(e -> applyFilter(new VerticalMirrorFilter(), "flipV"));


        undoButton.setOnAction(e -> undo());
        redoButton.setOnAction(event -> redo());


        addTagButton.setOnAction(event -> saveImageWithTag());
        searchButton.setOnAction(event -> searchImageByTag());

        resetButton.setOnAction(event -> resetTagTextField() );


    }

    @FXML public void saveImageWithTag() {
        String tag = tagTextField.getText().trim();
        if (tag.isEmpty()) {
            showAlert(Alert.AlertType.ERROR,"Erreur", "Veuillez entrer un tag.");
            return;
        }
        if (loadedFile == null) {
            showAlert(Alert.AlertType.ERROR,"Erreur", "Aucune image chargée.");
            return;
        }
        String name = loadedFile.getName();
        String url  = loadedFile.toURI().toString();
        String txCsv = String.join(",", transforms);

        // Vérifier si le tag existe déjà dans la base de données
        if (tagExists(tag)) {
            showAlert(Alert.AlertType.ERROR,"Erreur", "Ce tag existe déjà pour une autre image.");
            return;  // Empêche l'enregistrement du tag s'il existe déjà
        }


        // enregistrement
        String sql = "INSERT INTO APP.images_tags(image_name,image_url,tag,transformations) VALUES(?,?,?,?)";
        try (Connection c = getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, name);
            st.setString(2, url);
            st.setString(3, tag);
            st.setString(4, txCsv);
            st.executeUpdate();
            showAlert(Alert.AlertType.CONFIRMATION ,"Succès", "Image et transformations enregistrées.");
            // verrouiller le tag
            tagTextField.setDisable(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Erreur", "Impossible d’enregistrer : " + ex.getMessage());
        }
    }

    // Fonction pour vérifier si un tag existe déjà dans la base de données
    private boolean tagExists(String tag) {
        boolean exists = false;
        String checkTagQuery = "SELECT COUNT(*) FROM APP.images_tags WHERE tag = ?";

        try (PreparedStatement stmt = conn.prepareStatement(checkTagQuery)) {
            stmt.setString(1, tag);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;  // Le tag existe déjà
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Erreur", "Une erreur est survenue lors de la vérification du tag.");
        }
        return exists;
    }





    @FXML public void searchImageByTag() {

        // reset historique + transformations
        history.clear();
        imageView.setRotate(0);
        resetZoom();

        transforms.clear();

        String tag = tagTextField.getText().trim();
        if (tag.isEmpty()) {
            showAlert(Alert.AlertType.ERROR,"Erreur", "Veuillez entrer un tag pour la recherche.");
            return;
        }

        String sql = "SELECT image_url, transformations FROM APP.images_tags WHERE tag=?";
        try (Connection c = getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, tag);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    showAlert(Alert.AlertType.ERROR,"Recherche", "Aucune image pour le tag '" + tag + "'.");
                    return;
                }
                String url = rs.getString("image_url");
                String tx = rs.getString("transformations");
                // charger l'image originale
                Image img = new Image(url);
                imageView.setImage(img);

                // ✅ Mettre à jour currentImageFile
                try {
                    loadedFile = new File(new URI(url)); // ou new File(url) si c'est un chemin classique
                } catch (Exception e) {
                    e.printStackTrace();
                    loadedFile = null;
                }

                // appliquer les transformations dans l'ordre
                applyStoredTransforms(tx);
                history.push(new ImageState(imageView.getImage(), imageView.getRotate()));
                // rendre visibles les contrôles
                showAllButtons();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Erreur", "Impossible de récupérer l'image.");
        }
    }

    // applique la liste de transformations stockée ("rotate,sepia,flipH")
    private void applyStoredTransforms(String txCsv) {
        if (txCsv == null || txCsv.isBlank()) return;
        for (String op : txCsv.split(",")) {
            switch (op) {
                case "rotate": rotateImage();  break;
                case "flipH":  applyFilter(new MirrorFilter(),"flipH"); break;
                case "flipV":  applyFilter(new VerticalMirrorFilter(),"flipV");   break;
                case "sepia":  applyFilter(new SepiaFilterManual(),"sepia");  break;
                case "grayscale": applyFilter(new GrayscaleManualFilter(),"grayscale");       break;
                case "swapRGB": applyFilter(new SwapRGBFilter(),"swapRGB");           break;
                case "sobel":  applyFilter(new SobelFilter(),"sobel");              break;
            }
        }
    }


    // Méthode pour afficher une alerte (remplace showAlert de la version précédente)
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);  // Pas de texte d'entête
        alert.setContentText(message);
        alert.showAndWait(); // Afficher l'alerte et attendre la réponse
    }


    private void filtres() {
        loadFiltre = !loadFiltre;

        // Actualiser la visibilité des boutons
        grayscaleButton.setVisible(loadFiltre);
        sepiaButton.setVisible(loadFiltre);
        swapRGBButton.setVisible(loadFiltre);
        sobelButton.setVisible(loadFiltre);
    }

    private void symetries() {
        loadSymetrie = !loadSymetrie;

        mirrorButton.setVisible(loadSymetrie);
        verticalMirrorButton.setVisible(loadSymetrie);

    }

    private void zooms() {
        loadZoom = !loadZoom;

        zoomInButton.setVisible(loadZoom);
        zoomOutButton.setVisible(loadZoom);

    }





    private void loadImage() {
        FileChooser fileChooser = new FileChooser();
        File initialDir = new File("src/main/resources/images");
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(loadButton.getScene().getWindow());

        if (file != null) {
            // Charger la nouvelle image
            originalImage = new Image(file.toURI().toString());
            imageView.setImage(originalImage);
            loadedFile = file;


            // Réinitialiser tout
            resetZoom();
            imageView.setRotate(0);
            history.clear();
            hideAllButtonsExceptLoad() ;

            showAllButtons();
        }
    }


    private void applyFilter(Filter filter,String transformationName) {
            history.push(new ImageState(imageView.getImage(), imageView.getRotate())); // sauvegarde avant transformation
            imageView.setImage(filter.apply(imageView.getImage()));
            transforms.add(transformationName);

    }

    private void rotateImage() {
        history.push(new ImageState(imageView.getImage(), imageView.getRotate()));  // push l’état actuel
        imageView.setRotate(imageView.getRotate() + 30);  // applique rotation
        transforms.add("rotate");
    }


    private void zoomIn() {
        if (zoomFactor < MAX_ZOOM) {
            zoomFactor += 0.1;
            applyZoom();
        }
    }

    private void zoomOut() {
        if (zoomFactor > MIN_ZOOM) {
            zoomFactor -= 0.1;
            applyZoom();
        }
    }

    private void applyZoom() {
        imageView.setFitWidth(400 * zoomFactor);
        imageView.setFitHeight(400 * zoomFactor);
    }

    private void resetZoom() {
        zoomFactor = 1.0;
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
    }

    private void undo() {
        if (!history.isEmpty()) {
            // Sauvegarde l'état actuel dans redo
            redoHistory.push(new ImageState(imageView.getImage(), imageView.getRotate()));
            // Supprime la dernière transformation si elle existe
            if (!transforms.isEmpty()) {
                transformsredo.add(transforms.get(transforms.size()-1));
                transforms.remove(transforms.size() - 1);
            }
            // Restaure depuis l’historique
            ImageState prevState = history.pop();
            imageView.setImage(prevState.image);
            imageView.setRotate(prevState.rotation);
            updateRedoButtonVisibility();
        }
    }



    private void redo() {
        if (!redoHistory.isEmpty()) {
            history.push(new ImageState(imageView.getImage(), imageView.getRotate()));
            if (!transforms.isEmpty()) {
                transforms.add(transformsredo.get(transformsredo.size()-1));
                transformsredo.remove(transformsredo.size() - 1);
            }
            ImageState redoState = redoHistory.pop();
            imageView.setImage(redoState.image);
            imageView.setRotate(redoState.rotation);
            updateRedoButtonVisibility();
        }
    }



    // Mise à jour de la visibilité du bouton "Rétablir"
    private void updateRedoButtonVisibility() {
        redoButton.setVisible(!redoHistory.isEmpty());
    }

    @FXML
    /*private void resetImage() {
        if (!history.isEmpty()) {
            // Revenir à l'image précédente dans l'historique
            imageView.setImage(history.peek());
        }
    }*/

    private void updateTagTextFieldAfterSearch() {
        tagTextField.setDisable(true);
        tagTextField.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white;");  // Fond sombre et texte clair
    }

    private void resetTagTextField() {
        // Réinitialiser le champ de texte
        tagTextField.clear();
        tagTextField.setDisable(false);
        tagTextField.setStyle("-fx-background-color: white; -fx-text-fill: black;");  // Réinitialiser le style
    }

    private void hideAllButtonsExceptLoad() {
        rotateButton.setVisible(false);
        zoomInButton.setVisible(false);
        zoomOutButton.setVisible(false);
        grayscaleButton.setVisible(false);
        sepiaButton.setVisible(false);
        swapRGBButton.setVisible(false);
        sobelButton.setVisible(false);
        mirrorButton.setVisible(false);
        undoButton.setVisible(false);
        verticalMirrorButton.setVisible(false);
        filtererButton.setVisible(false);
        symetrie.setVisible(false);
        zoom.setVisible(false);
    }

    private void showAllButtons() {
        rotateButton.setVisible(true);
        undoButton.setVisible(true);
        filtererButton.setVisible(true);
        symetrie.setVisible(true);
        zoom.setVisible(true);
    }
}
