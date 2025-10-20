package org.example;

import com.jcraft.jsch.JSchException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AsistenciaController {

    @FXML private TableView<Asistencia> tabla;
    @FXML private TableColumn<Asistencia, Number> colIdAsistencia;
    @FXML private TableColumn<Asistencia, Number> colIdInscripcion;
    @FXML private TableColumn<Asistencia, String> colFecha;
    @FXML private TableColumn<Asistencia, String> colCreado;
    @FXML private TableColumn<Asistencia, String> colActualizado;

    @FXML private TextField idInscripcionTextField;
    @FXML private DatePicker datePicker;

    private final ObservableList<Asistencia> data = FXCollections.observableArrayList();
    private String date;

    @FXML
    private void initialize() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        colIdAsistencia.setCellValueFactory(c -> c.getValue().idAsistenciaProperty());
        colIdInscripcion.setCellValueFactory(c -> c.getValue().idInscripcionProperty());
        colFecha.setCellValueFactory(c -> c.getValue().fechaProperty());

        colCreado.setCellValueFactory(c -> {
            LocalDateTime f = c.getValue().getCreatedAt();
            return new javafx.beans.property.SimpleStringProperty(
                    f != null ? f.format(formato) : "-"
            );
        });

        colActualizado.setCellValueFactory(c -> {
            LocalDateTime f = c.getValue().getUpdatedAt();
            return new javafx.beans.property.SimpleStringProperty(
                    f != null ? f.format(formato) : "-"
            );
        });

        tabla.setItems(data);
    }

    // 🟢 GUARDAR ASISTENCIA (valida existencia y duplicados)
    @FXML
    void guardarButtonPressed(ActionEvent event) {
        obtenerFecha();

        if (idInscripcionTextField.getText().isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(null, "⚠️ Por favor, complete todos los campos.");
            return;
        }

        try {
            conexion.ejecutarConexion();
            int id_inscripcion = Integer.parseInt(idInscripcionTextField.getText());

            // 🧩 Verificar si existe la inscripción
            String sqlCheckInscripcion = String.format(
                    "SELECT COUNT(*) AS total FROM inscripciones WHERE id_inscripcion = %d;",
                    id_inscripcion
            );
            try (Statement st = conexion.obtenerConexion().createStatement();
                 ResultSet rs = st.executeQuery(sqlCheckInscripcion)) {

                if (rs.next() && rs.getInt("total") == 0) {
                    JOptionPane.showMessageDialog(null,
                            "⚠️ No existe una inscripción con ese ID. Verifique antes de guardar.");
                    conexion.desconectar();
                    return;
                }
            }

            // 🔍 Verificar si ya existe asistencia ese día
            String sqlVerificar = String.format(
                    "SELECT COUNT(*) AS total FROM asistencias WHERE id_inscripcion = %d AND fecha = '%s';",
                    id_inscripcion, date
            );
            try (Statement st = conexion.obtenerConexion().createStatement();
                 ResultSet rs = st.executeQuery(sqlVerificar)) {

                if (rs.next() && rs.getInt("total") > 0) {
                    JOptionPane.showMessageDialog(null,
                            "⚠️ Ya existe una asistencia registrada para ese estudiante en esa fecha.");
                    conexion.desconectar();
                    return;
                }
            }

            // ✅ Insertar si todo está correcto
            conexion.sql = String.format(
                    "INSERT INTO asistencias (id_inscripcion, fecha, created_at, updated_at) " +
                            "VALUES (%d, '%s', NOW(), NOW());",
                    id_inscripcion, date
            );

            conexion.ejecutarComandoUpdate();
            refrescarTabla();
            JOptionPane.showMessageDialog(null, "✅ Asistencia insertada correctamente.");
            limpiarCampos(null);

        } catch (JSchException e) {
            JOptionPane.showMessageDialog(null, "⚠️ Error de conexión SSH.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "⚠️ Error al insertar la asistencia.");
            e.printStackTrace();
        } finally {
            conexion.desconectar();
        }
    }

    // 🟣 VER ASISTENCIAS
    @FXML
    void verButtonPressed(ActionEvent event) {
        try {
            refrescarTabla();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "⚠️ Error al cargar la tabla.");
        }
    }

    // 🧹 LIMPIAR CAMPOS
    @FXML
    void limpiarCampos(ActionEvent event) {
        idInscripcionTextField.clear();
        datePicker.setValue(null);
    }

    // 🔁 REFRESCAR TABLA
    private void refrescarTabla() throws Exception {
        conexion.ejecutarConexion();
        conexion.sql = "SELECT * FROM asistencias ORDER BY id_asistencia ASC;";
        ObservableList<Asistencia> nuevas = conexion.ejecutarComandoSelect();
        data.setAll(nuevas);
        tabla.sort();
        conexion.desconectar();
    }

    // 📅 OBTENER FECHA
    private void obtenerFecha() {
        LocalDate fechaSeleccionada = datePicker.getValue();
        date = (fechaSeleccionada != null)
                ? fechaSeleccionada.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "";
    }
}
