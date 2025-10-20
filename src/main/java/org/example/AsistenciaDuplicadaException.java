package org.example;

/**
 * Excepción checked que indica que se intentó registrar una asistencia repetida.
 */
public class AsistenciaDuplicadaException extends Exception {

    public AsistenciaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
