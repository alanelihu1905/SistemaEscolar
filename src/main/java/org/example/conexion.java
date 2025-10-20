package org.example;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.sql.*;

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

}
