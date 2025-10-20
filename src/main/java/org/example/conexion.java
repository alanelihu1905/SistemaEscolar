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
    private static final String sshUser  = "patito";
    private static final String sshPass  = "cuack";
    private static final String dbUser   = "becario";
    private static final String dbPass   = "FdI-its-5a";
    private static final String dbName   = "its5a";

    private static Session sesion;
    private static int port;

    // Abre SSH (como en tu MainSSH.ejecutarConexion)
    public static synchronized void ejecutarConexion() throws JSchException {
        if (sesion != null && sesion.isConnected()) {
            System.out.println("🔁 SSH ya activa en puerto " + port);
            return;
        }
        JSch jsch = new JSch();
        sesion = jsch.getSession(sshUser, hostname);
        sesion.setPassword(sshPass);
        sesion.setConfig("StrictHostKeyChecking", "no");
        sesion.connect(10000); // 10s
        port = sesion.setPortForwardingL(0, "localhost", 3306);
        System.out.println("✅ SSH conectada en puerto local: " + port);
    }

    // Cierra SSH (como tu MainSSH.desconectar)
    public static void desconectar() {
        if (sesion != null && sesion.isConnected()) {
            sesion.disconnect();
            System.out.println("🔒 SSH desconectada.");
        }
    }

    // Conexión JDBC (como tu MainSSH.obtenerConexion)
    public static Connection obtenerConexion() throws SQLException {
        String conString = "jdbc:mariadb://localhost:" + port + "/" + dbName;
        return DriverManager.getConnection(conString, dbUser, dbPass);
    }

    // UPDATE/INSERT/DELETE: ahora acepta la consulta como parámetro para evitar la variable estática
    public static void ejecutarComandoUpdate(String query) throws SQLException {
        try (Connection con = obtenerConexion();
             Statement st = con.createStatement()) {
            st.executeUpdate(query);
        }
    }

    // SELECT → ObservableList<Asistencia>: acepta la consulta como parámetro
    public static ObservableList<Asistencia> ejecutarComandoSelect(String query) {
        ObservableList<Asistencia> list = FXCollections.observableArrayList();
        try (Connection con = obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                int id_asistencia = rs.getInt("id_asistencia");
                int id_inscripcion = rs.getInt("id_inscripcion");
                String fecha = rs.getString("fecha");

                Timestamp created_at = rs.getTimestamp("created_at");
                Timestamp updated_at = rs.getTimestamp("updated_at");
                LocalDateTime cAt = created_at != null ? created_at.toLocalDateTime() : null;
                LocalDateTime uAt = updated_at != null ? updated_at.toLocalDateTime() : null;

                list.add(new Asistencia(id_asistencia, id_inscripcion, fecha, cAt, uAt));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Nuevo: ejecutar una consulta que devuelve un entero (por ejemplo COUNT(*))
    public static int ejecutarScalarInt(String query) throws SQLException {
        try (Connection con = obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        }
    }
}
