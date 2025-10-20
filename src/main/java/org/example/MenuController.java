package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controlador del menú principal (Menu Hazel)
 */
public class MenuController {

    @FXML
    void abrirAsistencias(ActionEvent event) {
        cambiarVentana(event, "/Asistencia.fxml");
    }

    @FXML
    void abrirEstudiantes(ActionEvent event) {
        // Puedes crear otra vista para estudiantes
        System.out.println("👩‍🎓 Módulo Estudiantes (en desarrollo)");
    }

    @FXML
    void abrirMaterias(ActionEvent event) {
        // Puedes crear otra vista para materias
        System.out.println("📘 Módulo Materias (en desarrollo)");
    }

    @FXML
    void cerrarAplicacion(ActionEvent event) {
        System.out.println("🚪 Cerrando aplicación...");
        System.exit(0);
    }

    // Reutilizable para cambiar escenas
    private void cambiarVentana(ActionEvent event, String vistaFXML) {
        try {
            Parent nuevaVista = FXMLLoader.load(getClass().getResource(vistaFXML));
            Scene nuevaEscena = new Scene(nuevaVista);
            Stage ventana = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ventana.setScene(nuevaEscena);
            ventana.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
