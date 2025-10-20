package org.example;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * Controlador base para catálogos simples renderizados en una tabla dinámica.
 */
public abstract class BaseTablaSimpleController {

    @FXML private TableView<ObservableList<String>> tabla;
    @FXML private Label tituloLabel;
    @FXML private Label estadoLabel;

    /** Consulta SQL que alimenta la tabla. */
    protected abstract String getConsultaSql();

    /** Título mostrado en la vista. */
    protected abstract String getTitulo();

    @FXML
    private void initialize() {
        if (tituloLabel != null) {
            tituloLabel.setText(getTitulo());
        }
        if (tabla != null) {
            tabla.setPlaceholder(new Label("Sin registros"));
        }
        recargarDatos(null);
    }

    @FXML
    protected void recargarDatos(ActionEvent event) {
        if (tabla == null) {
            return;
        }
        tabla.getColumns().clear();
        tabla.getItems().clear();

        try {
            conexion.ejecutarConexion();
            try (Connection conn = conexion.obtenerConexion();
                 PreparedStatement stmt = conn.prepareStatement(getConsultaSql());
                 ResultSet rs = stmt.executeQuery()) {

                ResultSetMetaData meta = rs.getMetaData();
                int columnas = meta.getColumnCount();

                for (int i = 0; i < columnas; i++) {
                    final int index = i;
                    TableColumn<ObservableList<String>, String> columna = new TableColumn<>(meta.getColumnLabel(i + 1));
                    columna.setCellValueFactory(data -> {
                        ObservableList<String> fila = data.getValue();
                        String valor = index < fila.size() ? fila.get(index) : "";
                        return new ReadOnlyStringWrapper(valor);
                    });
                    columna.setPrefWidth(160);
                    tabla.getColumns().add(columna);
                }

                ObservableList<ObservableList<String>> filas = FXCollections.observableArrayList();
                while (rs.next()) {
                    ObservableList<String> fila = FXCollections.observableArrayList();
                    for (int i = 1; i <= columnas; i++) {
                        Object valor = rs.getObject(i);
                        fila.add(valor != null ? valor.toString() : "");
                    }
                    filas.add(fila);
                }

                tabla.setItems(filas);
                actualizarEstado("Registros cargados: " + filas.size());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            actualizarEstado("Error al cargar datos: " + ex.getMessage());
        } finally {
            conexion.desconectar();
        }
    }

    @FXML
    protected void volverMenu(ActionEvent event) {
        SceneNavigator.cambiarEscena(event, "/Menu.fxml");
    }

    private void actualizarEstado(String mensaje) {
        if (estadoLabel != null) {
            estadoLabel.setText(mensaje);
        }
    }
}
