package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Asistencia.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Gestión de Asistencias");
        stage.setScene(scene);
        stage.show();
        System.out.println("🚀 Aplicación iniciada correctamente.");
    }

    public static void main(String[] args) {
        launch();
    }
}
