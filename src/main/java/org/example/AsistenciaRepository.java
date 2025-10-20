package org.example;

import com.jcraft.jsch.JSchException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Capa de acceso a datos dedicada al módulo de asistencias.
 * Se encarga de comunicarse con la base de datos a través del túnel SSH definido en {@link conexion}.
 */
public class AsistenciaRepository {

    private static final DateTimeFormatter DB_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Obtiene todas las asistencias registradas ordenadas por su identificador.
     */
    public ObservableList<Asistencia> obtenerAsistencias() throws SQLException, JSchException {
        conexion.ejecutarConexion();
        ObservableList<Asistencia> asistencias = FXCollections.observableArrayList();

        final String query = "SELECT id_asistencia, id_inscripcion, fecha, created_at, updated_at " +
                "FROM asistencias ORDER BY id_asistencia ASC";

        try (Connection connection = conexion.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                int idAsistencia = result.getInt("id_asistencia");
                int idInscripcion = result.getInt("id_inscripcion");
                String fecha = result.getString("fecha");

                Timestamp createdAt = result.getTimestamp("created_at");
                Timestamp updatedAt = result.getTimestamp("updated_at");
                LocalDateTime created = createdAt != null ? createdAt.toLocalDateTime() : null;
                LocalDateTime updated = updatedAt != null ? updatedAt.toLocalDateTime() : null;

                asistencias.add(new Asistencia(idAsistencia, idInscripcion, fecha, created, updated));
            }
        }

        return asistencias;
    }

    /**
     * Registra una asistencia si no existe previamente una para el mismo alumno y fecha.
     */
    public void registrarAsistencia(int idInscripcion, LocalDate fecha)
            throws SQLException, JSchException, AsistenciaDuplicadaException {

        conexion.ejecutarConexion();

        final String fechaNormalizada = fecha.format(DB_DATE_FORMAT);
        final String existeSql = "SELECT 1 FROM asistencias WHERE id_inscripcion = ? AND fecha = ? LIMIT 1";
        final String insertarSql = "INSERT INTO asistencias (id_inscripcion, fecha, created_at, updated_at) " +
                "VALUES (?, ?, NOW(), NOW())";

        try (Connection connection = conexion.obtenerConexion();
             PreparedStatement existeStmt = connection.prepareStatement(existeSql);
             PreparedStatement insertStmt = connection.prepareStatement(insertarSql)) {

            existeStmt.setInt(1, idInscripcion);
            existeStmt.setString(2, fechaNormalizada);

            try (ResultSet rs = existeStmt.executeQuery()) {
                if (rs.next()) {
                    throw new AsistenciaDuplicadaException("Ya existe una asistencia para esa fecha e inscripción.");
                }
            }

            insertStmt.setInt(1, idInscripcion);
            insertStmt.setString(2, fechaNormalizada);
            insertStmt.executeUpdate();
        }
    }
}
