# 📸 Gestion de Bibliothèque d’Images

Cette application permet de gérer une collection d’images avec des fonctionnalités avancées de traitement, de marquage, de chiffrement et de persistance des données.

## 🧠 Objectif

Créer une application Java respectant l’architecture MVC avec une interface JavaFX (FXML) permettant :

- L’ouverture et l’affichage d’images.
- Des transformations d’image (rotation, symétrie).
- L’application de filtres (Noir & Blanc, Sépia, Échange RGB, Sobel).
- L’ajout de tags personnalisés.
- La sauvegarde des métadonnées (tags, transformations).
- Le chiffrement et déchiffrement d’images avec mot de passe.
- La persistance des données dans une base Derby.

---

## 🖼️ Aperçu

![aperçu](captures/accueil.gif)

> GIF illustrant les principales fonctionnalités de l'application (chargement d’image, filtres, tags...).

---

## ⚙️ Fonctionnalités principales

| Fonction                          | Description |
|----------------------------------|-------------|
| ✅ Lecture et affichage d’images | Via `FileChooser` et `ImageView` |
| 🔁 Transformations de base       | Rotation, symétrie |
| 🎨 Filtres                        | RGB, NB, Sépia, Sobel |
| 🏷️ Gestion de tags               | Ajout, suppression, recherche |
| 🔒 Chiffrement                   | Basé sur SecureRandom et SHA-256 |
| 💾 Sauvegarde des métadonnées    | Intégration de Derby via JDBC |

---

## 🧱 Architecture

Le projet suit le paradigme **MVC** :

- `model/` → Classes métier, traitement des images, tags, filtres.
- `view/` → Fichiers `.fxml` (interface utilisateur).
- `controller/` → Contrôleurs JavaFX.
- `resources/` → Images et fichiers nécessaires.

---

## 🛠️ Installation locale

1. **Télécharger le projet** :
   - Allez sur la page du dépôt GitHub.
   - Cliquez sur le bouton vert **Code** → **Download ZIP**.
   - Extrayez le fichier ZIP sur votre machine.
2. **Ouvrir dans un IDE**:
    - Utilisez IntelliJ IDEA, Eclipse ou NetBeans.
    - Ouvrez le dossier extrait ou importez-le comme projet Java.
3. **Prérequis** :
    - Java 11 ou supérieur installé.
    - JavaFX configuré dans votre environnement.
    - Derby (JavaDB) installé ou intégré
4. **Lancer l'application**:
   - Exécutez la classe (HelloApplication) -- \src\main\java\com\example\demo1\HelloApplication.java
