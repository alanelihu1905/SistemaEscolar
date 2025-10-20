package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Controlador del menú principal (Menu Hazel)
 */
public class MenuController {

    @FXML
    void abrirAsistencias(ActionEvent event) {
        SceneNavigator.cambiarEscena(event, "/Asistencia.fxml");
    }

    @FXML
    void abrirPersonas(ActionEvent event) {
        SceneNavigator.cambiarEscena(event, "/Persona.fxml");
    }

    @FXML
    void abrirMaterias(ActionEvent event) {
        SceneNavigator.cambiarEscena(event, "/Materias.fxml");
    }

    @FXML
    void abrirInscripciones(ActionEvent event) {
        SceneNavigator.cambiarEscena(event, "/Inscripciones.fxml");
    }

    @FXML
    void cerrarAplicacion(ActionEvent event) {
        System.out.println("🚪 Cerrando aplicación...");
        // Asegurarse de cerrar el túnel SSH antes de salir
        conexion.desconectar();
        System.exit(0);
    }

}
