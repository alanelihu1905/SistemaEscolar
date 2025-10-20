package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AsistenciaController {

    @FXML private TableView<Asistencia> tabla;
    @FXML private TableColumn<Asistencia, Number> colIdAsistencia;
    @FXML private TableColumn<Asistencia, Number> colIdInscripcion;
    @FXML private TableColumn<Asistencia, String> colFecha;
    @FXML private TableColumn<Asistencia, LocalDateTime> colCreado;
    @FXML private TableColumn<Asistencia, LocalDateTime> colActualizado;
    @FXML private TextField idInscripcionTextField;
    @FXML private DatePicker datePicker;

    private final ObservableList<Asistencia> data = FXCollections.observableArrayList();
    private final DateTimeFormatter fechaDbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter fechaVistaFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private void initialize() {
        colIdAsistencia.setCellValueFactory(new PropertyValueFactory<>("idAsistencia"));
        colIdInscripcion.setCellValueFactory(new PropertyValueFactory<>("idInscripcion"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colCreado.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colActualizado.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

        colCreado.setCellFactory(column -> crearCeldaFecha());
        colActualizado.setCellFactory(column -> crearCeldaFecha());

        tabla.setItems(data);
        tabla.setPlaceholder(new Label("No hay registros"));
        colIdAsistencia.setSortType(TableColumn.SortType.ASCENDING);
        tabla.getSortOrder().add(colIdAsistencia);

        recargarTabla();
    }

    @FXML
    void guardarButtonPressed(ActionEvent event) {
        String idTexto = idInscripcionTextField.getText();
        LocalDate fechaSeleccionada = datePicker.getValue();

        if (idTexto == null || idTexto.isBlank() || fechaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Por favor, completa todos los datos.");
            return;
        }

        int idInscripcion;
        try {
            idInscripcion = Integer.parseInt(idTexto.trim());
        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "El ID de inscripción debe ser numérico.");
            return;
        }

        try {
            conexion.ejecutarConexion();
            try (Connection conn = conexion.obtenerConexion();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO asistencias (id_inscripcion, fecha, created_at, updated_at) VALUES (?, ?, NOW(), NOW())")) {

                stmt.setInt(1, idInscripcion);
                stmt.setString(2, fechaSeleccionada.format(fechaDbFormatter));
                stmt.executeUpdate();
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Asistencia guardada.");
            limpiarCampos(null);
            recargarTabla();
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo guardar la asistencia: " + ex.getMessage());
        } finally {
            conexion.desconectar();
        }
    }

    @FXML
    void verButtonPressed(ActionEvent event) {
        recargarTabla();
    }

    @FXML
    void limpiarCampos(ActionEvent event) {
        idInscripcionTextField.clear();
        datePicker.setValue(null);
    }

    private void recargarTabla() {
        data.clear();
        try {
            conexion.ejecutarConexion();
            try (Connection conn = conexion.obtenerConexion();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT id_asistencia, id_inscripcion, fecha, created_at, updated_at FROM asistencias ORDER BY id_asistencia ASC");
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int idAsistencia = rs.getInt("id_asistencia");
                    int idInscripcion = rs.getInt("id_inscripcion");
                    String fecha = rs.getString("fecha");
                    Timestamp creado = rs.getTimestamp("created_at");
                    Timestamp actualizado = rs.getTimestamp("updated_at");

                    LocalDateTime creadoTime = creado != null ? creado.toLocalDateTime() : null;
                    LocalDateTime actualizadoTime = actualizado != null ? actualizado.toLocalDateTime() : null;

                    data.add(new Asistencia(idAsistencia, idInscripcion, fecha, creadoTime, actualizadoTime));
                }
            }

            tabla.sort();
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudieron cargar las asistencias: " + ex.getMessage());
        } finally {
            conexion.desconectar();
        }
    }

    private TableCell<Asistencia, LocalDateTime> crearCeldaFecha() {
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText("");
                } else {
                    setText(value.format(fechaVistaFormatter));
                }
            }
        };
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    void volverMenu(ActionEvent event) {
        SceneNavigator.cambiarEscena(event, "/Menu.fxml");
    }
}
