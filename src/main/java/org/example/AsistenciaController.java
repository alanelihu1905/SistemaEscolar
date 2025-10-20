package org.example;

import com.jcraft.jsch.JSchException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.SQLException;

public class AsistenciaController {

    @FXML private TableView<Asistencia> tabla;
    @FXML private TableColumn<Asistencia, Number>  colIdAsistencia;
    @FXML private TableColumn<Asistencia, Number>  colIdInscripcion;
    @FXML private TableColumn<Asistencia, String>  colFecha;
    @FXML private TableColumn<Asistencia, String>  colCreado;      // tu FXML los define como String
    @FXML private TableColumn<Asistencia, String>  colActualizado; // idem
    @FXML private TextField idInscripcionTextField;
    @FXML private DatePicker datePicker;

    private final ObservableList<Asistencia> data = FXCollections.observableArrayList();
    private String date = "";

    @FXML
    private void initialize() {
        // Enlace directo a las properties de tu modelo
        colIdAsistencia.setCellValueFactory(c -> c.getValue().idAsistenciaProperty());
        colIdInscripcion.setCellValueFactory(c -> c.getValue().idInscripcionProperty());
        colFecha.setCellValueFactory(c -> c.getValue().fechaProperty());

        // Como tus columnas creado/actualizado son String en el FXML, los formateo aquí:
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        colCreado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().format(f) : "-"
                )
        );
        colActualizado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getUpdatedAt() != null ? c.getValue().getUpdatedAt().format(f) : "-"
                )
        );

        tabla.setItems(data);

        // Orden por ID ascendente
        colIdAsistencia.setSortType(TableColumn.SortType.ASCENDING);
        tabla.getSortOrder().add(colIdAsistencia);
    }

    // ============== HANDLERS QUE PIDE TU FXML =================

    @FXML
    void guardarButtonPressed(ActionEvent event) {
        obtenerFecha();
        if (idInscripcionTextField.getText().isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(null, "⚠️ Por favor, complete todos los campos.");
            return;
        }

        int id_inscripcion;
        try {
            id_inscripcion = Integer.parseInt(idInscripcionTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "⚠️ ID de inscripción inválido.");
            return;
        }

        // Ejecutar la operación en un hilo de fondo para no bloquear la UI
        new Thread(() -> {
            try {
                conexion.ejecutarConexion();

                // 🔍 Verificar si ya existe (evita lecturas compartidas; aún puede haber race, por eso usamos upsert más abajo)
                String countSql = String.format(
                    "SELECT COUNT(*) FROM asistencias WHERE id_inscripcion = %d AND fecha = '%s';",
                    id_inscripcion, date
                );

                int count = conexion.ejecutarScalarInt(countSql);

                if (count > 0) {
                    Platform.runLater(() ->
                        JOptionPane.showMessageDialog(null, "⚠️ Ya existe una asistencia para ese alumno en esa fecha.")
                    );
                    return; // Evita insertar duplicado
                }

                // ✅ Insertar nueva asistencia con ON DUPLICATE KEY UPDATE para evitar excepción si hay race
                String insertSql = String.format(
                    "INSERT INTO asistencias (id_inscripcion, fecha, created_at, updated_at) " +
                    "VALUES (%d, '%s', NOW(), NOW()) " +
                    "ON DUPLICATE KEY UPDATE updated_at = NOW();",
                    id_inscripcion, date
                );

                conexion.ejecutarComandoUpdate(insertSql);

                // Refrescar tabla (no desconectamos aquí)
                try {
                    refrescarTabla();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Mostrar mensaje y limpiar campos en hilo de UI
                Platform.runLater(() -> {
                    JOptionPane.showMessageDialog(null, "✅ Asistencia guardada correctamente.");
                    idInscripcionTextField.clear();
                    datePicker.setValue(null);
                });

            } catch (JSchException e) {
                Platform.runLater(() -> JOptionPane.showMessageDialog(null, "🚫 Error SSH: " + e.getMessage()));
            } catch (SQLException e) {
                Platform.runLater(() -> JOptionPane.showMessageDialog(null, "❌ Error SQL: " + e.getMessage()));
                e.printStackTrace();
            } catch (Exception e) {
                Platform.runLater(() -> JOptionPane.showMessageDialog(null, "⚠️ Error general: " + e.getMessage()));
                e.printStackTrace();
            }
            // NOTA: no desconectamos aquí para no cerrar el túnel mientras otras operaciones lo usan
        }).start();
    }

    @FXML
    void verButtonPressed(ActionEvent event) {
        // Cargar en segundo plano para no bloquear la UI
        new Thread(() -> {
            try {
                refrescarTabla();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> JOptionPane.showMessageDialog(null, "⚠️ Error al cargar asistencias."));
            }
        }).start();
    }

    @FXML
    void limpiarCampos(ActionEvent event) {
        idInscripcionTextField.clear();
        datePicker.setValue(null);
    }

    // ==================== AUXILIARES ==========================

    private void refrescarTabla() throws Exception {
        ObservableList<Asistencia> nuevas = null;
        // abrimos la conexión SSH si hace falta, pero NO la cerramos aquí
        conexion.ejecutarConexion();
        String query = "SELECT id_asistencia, id_inscripcion, fecha, created_at, updated_at " +
                "FROM asistencias ORDER BY id_asistencia ASC;";
        nuevas = conexion.ejecutarComandoSelect(query);

        // Actualizar UI en hilo de JavaFX
        final ObservableList<Asistencia> finalNuevas = (nuevas != null) ? nuevas : FXCollections.observableArrayList();
        Platform.runLater(() -> {
            data.setAll(finalNuevas);
            tabla.sort();
        });
    }

    private void obtenerFecha() {
        LocalDate sel = datePicker.getValue();
        date = (sel != null) ? sel.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
    }
}
