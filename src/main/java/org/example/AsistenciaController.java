package org.example;

import com.jcraft.jsch.JSchException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AsistenciaController {

    @FXML private TableView<Asistencia> tabla;
    @FXML private TableColumn<Asistencia, Number> colIdAsistencia;
    @FXML private TableColumn<Asistencia, Number> colIdInscripcion;
    @FXML private TableColumn<Asistencia, String> colEstudiante;
    @FXML private TableColumn<Asistencia, String> colMateria;
    @FXML private TableColumn<Asistencia, String> colFecha;
    @FXML private TableColumn<Asistencia, java.time.LocalDateTime> colCreado;
    @FXML private TableColumn<Asistencia, java.time.LocalDateTime> colActualizado;

    @FXML private TextField idInscripcionTextField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboEstudiante;
    @FXML private ComboBox<String> comboMateria;

    private final ObservableList<Asistencia> data = FXCollections.observableArrayList();
    private String date;

    @FXML
    private void initialize() {
        colIdAsistencia.setCellValueFactory(c -> c.getValue().idAsistenciaProperty());
        colIdInscripcion.setCellValueFactory(c -> c.getValue().idInscripcionProperty());
        colEstudiante.setCellValueFactory(c -> c.getValue().estudianteProperty());
        colMateria.setCellValueFactory(c -> c.getValue().materiaProperty());
        colFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
        colCreado.setCellValueFactory(c -> c.getValue().createdAtProperty());
        colActualizado.setCellValueFactory(c -> c.getValue().updatedAtProperty());
        tabla.setItems(data);

        comboEstudiante.setItems(FXCollections.observableArrayList(
                "Juan Pérez", "María López", "Carlos Ruiz", "Ana Torres"
        ));
        comboMateria.setItems(FXCollections.observableArrayList(
                "Matemáticas", "Programación", "Física", "Bases de Datos"
        ));
    }

    @FXML
    void guardarButtonPressed(ActionEvent event) {
        guardarAsistencia();
    }

    @FXML
    void verButtonPressed(ActionEvent event) {
        try {
            refrescarTabla();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "⚠️ Error al cargar la tabla.");
        }
    }

    private void guardarAsistencia() {
        obtenerFecha();

        if (idInscripcionTextField.getText().isEmpty() || date.isEmpty() ||
                comboEstudiante.getValue() == null || comboMateria.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Por favor, complete todos los campos.");
            return;
        }

        try {
            conexion.ejecutarConexion();

            int id_inscripcion = Integer.parseInt(idInscripcionTextField.getText());
            String fecha = date;
            String estudiante = comboEstudiante.getValue();
            String materia = comboMateria.getValue();

            conexion.sql = String.format(
                    "INSERT INTO asistencias(id_inscripcion, estudiante, materia, fecha) VALUES (%d, '%s', '%s', '%s');",
                    id_inscripcion, estudiante, materia, fecha
            );

            System.out.println("📝 Ejecutando: " + conexion.sql);
            conexion.ejecutarComandoUpdate();

            try {
                refrescarTabla();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "⚠️ Error al refrescar la tabla.");
            }

            JOptionPane.showMessageDialog(null, "✅ Asistencia guardada correctamente.");

        } catch (JSchException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "⚠️ Error al guardar la asistencia.");
        } finally {
            conexion.desconectar();
        }
    }

    private void refrescarTabla() throws Exception {
        conexion.ejecutarConexion();
        conexion.sql = "SELECT id_asistencia, id_inscripcion, estudiante, materia, fecha, created_at, updated_at FROM asistencias ORDER BY id_asistencia ASC;";
        ObservableList<Asistencia> nuevas = conexion.ejecutarComandoSelect();
        data.setAll(nuevas);
        tabla.sort();
        conexion.desconectar();
    }

    private void obtenerFecha() {
        LocalDate fechaSeleccionada = datePicker.getValue();
        if (fechaSeleccionada != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            date = fechaSeleccionada.format(formatter);
        } else {
            date = "";
        }
    }
}
