package org._brown_tech;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class Main extends Application {

    public static final File RESOURCE_PATH = new File(System.getenv("JAVAFX_DEV_APP_HOME").concat("\\_brown_tech\\"));
    private double xOffset;
    private double yOffset;
    public static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/_brown_tech/_fxml/mainUI.fxml"));
        Scene scene = new Scene(root);
        scene.setOnMousePressed(event2 -> {
            xOffset = event2.getSceneX();
            yOffset = event2.getSceneY();
        });
        scene.setOnMouseDragged(event1 -> {
            primaryStage.setX(event1.getScreenX() - xOffset);
            primaryStage.setY(event1.getScreenY() - yOffset);
        });
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.getIcons().add(new Image(getClass().getResource("/org/_brown_tech/_images/_pictures/icon.png").toExternalForm()));
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setResizable(false);
        primaryStage.show();
        stage = primaryStage;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
