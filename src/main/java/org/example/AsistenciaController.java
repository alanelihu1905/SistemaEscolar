package org.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AsistenciaController {

    @FXML private TableView<Asistencia> tabla;
    @FXML private TableColumn<Asistencia, Number>  colIdAsistencia;
    @FXML private TableColumn<Asistencia, Number>  colIdInscripcion;
    @FXML private TableColumn<Asistencia, String>  colFecha;
    @FXML private TableColumn<Asistencia, String>  colCreado;
    @FXML private TableColumn<Asistencia, String>  colActualizado;
    @FXML private TextField idInscripcionTextField;
    @FXML private DatePicker datePicker;
    @FXML private Button guardarButton;
    @FXML private Button verButton;

    private final ObservableList<Asistencia> data = FXCollections.observableArrayList();
    private final AsistenciaRepository repository = new AsistenciaRepository();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private void initialize() {
        colIdAsistencia.setCellValueFactory(c -> c.getValue().idAsistenciaProperty());
        colIdInscripcion.setCellValueFactory(c -> c.getValue().idInscripcionProperty());
        colFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
        colCreado.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().format(dateTimeFormatter) : "-"));
        colActualizado.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getUpdatedAt() != null ? c.getValue().getUpdatedAt().format(dateTimeFormatter) : "-"));

        tabla.setItems(data);
        tabla.setPlaceholder(new Label("No hay asistencias registradas"));
        colIdAsistencia.setSortType(TableColumn.SortType.ASCENDING);
        tabla.getSortOrder().add(colIdAsistencia);

        cargarAsistenciasAsync();
    }

    @FXML
    void guardarButtonPressed(ActionEvent event) {
        String idTexto = idInscripcionTextField.getText();
        LocalDate fechaSeleccionada = datePicker.getValue();

        if (idTexto == null || idTexto.isBlank() || fechaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Por favor, complete todos los campos.");
            return;
        }

        final int idInscripcion;
        try {
            idInscripcion = Integer.parseInt(idTexto.trim());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "El ID de inscripción debe ser numérico.");
            return;
        }

        registrarAsistenciaAsync(idInscripcion, fechaSeleccionada);
    }

    @FXML
    void verButtonPressed(ActionEvent event) {
        cargarAsistenciasAsync();
    }

    @FXML
    void limpiarCampos(ActionEvent event) {
        idInscripcionTextField.clear();
        datePicker.setValue(null);
    }

    private void cargarAsistenciasAsync() {
        Task<ObservableList<Asistencia>> task = new Task<>() {
            @Override
            protected ObservableList<Asistencia> call() throws Exception {
                return repository.obtenerAsistencias();
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<Asistencia> nuevas = task.getValue();
            data.setAll(nuevas);
            tabla.sort();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null) {
                ex.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "No fue posible cargar las asistencias: " + ex.getMessage());
            }
        });

        ejecutarTarea(task);
    }

    private void registrarAsistenciaAsync(int idInscripcion, LocalDate fecha) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                repository.registrarAsistencia(idInscripcion, fecha);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Asistencia guardada correctamente.");
            idInscripcionTextField.clear();
            datePicker.setValue(null);
            cargarAsistenciasAsync();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex instanceof AsistenciaDuplicadaException) {
                mostrarAlerta(Alert.AlertType.WARNING, ex.getMessage());
            } else if (ex != null) {
                ex.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar la asistencia: " + ex.getMessage());
            }
        });

        ejecutarTarea(task);
    }

    private void ejecutarTarea(Task<?> task) {
        task.setOnRunning(e -> bloquearControles(true));
        task.stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED || newState == Worker.State.FAILED || newState == Worker.State.CANCELLED) {
                bloquearControles(false);
            }
        });

        Thread thread = new Thread(task, "db-task-" + System.nanoTime());
        thread.setDaemon(true);
        thread.start();
    }

    private void bloquearControles(boolean bloquear) {
        Platform.runLater(() -> {
            if (guardarButton != null) {
                guardarButton.setDisable(bloquear);
            }
            if (verButton != null) {
                verButton.setDisable(bloquear);
            }
            if (tabla != null) {
                tabla.setDisable(bloquear);
            }
        });
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(tipo);
            alerta.setHeaderText(null);
            alerta.setContentText(mensaje);
            alerta.showAndWait();
        });
    }
}
