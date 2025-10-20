package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Utilidad ligera para cambiar entre escenas FXML.
 */
public final class SceneNavigator {

    private SceneNavigator() {
        // utilidad
    }

    public static void cambiarEscena(ActionEvent event, String fxmlRuta) {
        try {
            Parent nuevaVista = FXMLLoader.load(SceneNavigator.class.getResource(fxmlRuta));
            Scene escena = new Scene(nuevaVista);
            Stage ventana = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ventana.setScene(escena);
            ventana.show();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo abrir la vista " + fxmlRuta, e);
        }
    }
}
