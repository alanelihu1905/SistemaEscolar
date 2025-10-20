package org.example;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class conexion {

    private static final String hostname = "fi.jcaguilar.dev";
    private static final String sshUser = "patito";
    private static final String sshPass = "cuack";
    private static final String dbUser = "becario";
    private static final String dbPass = "FdI-its-5a";

    private static Session sesion;
    private static int port;
    public static String sql;

    public static void ejecutarConexion() throws JSchException {
        if (sesion != null && sesion.isConnected()) return;
        JSch jsch = new JSch();
        sesion = jsch.getSession(sshUser, hostname);
        sesion.setPassword(sshPass);
        sesion.setConfig("StrictHostKeyChecking", "no");
        sesion.connect();
        port = sesion.setPortForwardingL(0, "localhost", 3306);
        System.out.println("✅ Conexión SSH establecida. Puerto: " + port);
    }

    public static void desconectar() {
        if (sesion != null && sesion.isConnected()) {
            sesion.disconnect();
            System.out.println("🔒 Sesión SSH cerrada correctamente.");
        }
    }

    public static Connection obtenerConexion() throws SQLException {
        String conString = "jdbc:mariadb://localhost:" + port + "/its5a";
        return DriverManager.getConnection(conString, dbUser, dbPass);
    }

    public static void ejecutarComandoUpdate() {
        try (Connection con = obtenerConexion();
             Statement sentencia = con.createStatement()) {
            sentencia.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("❌ Error al ejecutar comando UPDATE:");
            e.printStackTrace();
        }
    }

    public static ObservableList<Asistencia> ejecutarComandoSelect() {
        ObservableList<Asistencia> list = FXCollections.observableArrayList();
        try (Connection con = obtenerConexion();
             Statement sentencia = con.createStatement();
             ResultSet resultado = sentencia.executeQuery(sql)) {

            while (resultado.next()) {
                int id_asistencia = resultado.getInt("id_asistencia");
                int id_inscripcion = resultado.getInt("id_inscripcion");
                String fecha = resultado.getString("fecha");

                Timestamp created_at = resultado.getTimestamp("created_at");
                Timestamp updated_at = resultado.getTimestamp("updated_at");
                LocalDateTime createdAtTime = created_at != null ? created_at.toLocalDateTime() : null;
                LocalDateTime updatedAtTime = updated_at != null ? updated_at.toLocalDateTime() : null;

                list.add(new Asistencia(id_asistencia, id_inscripcion, fecha, createdAtTime, updatedAtTime));
            }

        } catch (Exception e) {
            System.err.println("❌ Error al ejecutar SELECT:");
            e.printStackTrace();
        }
        return list;
    }
}
