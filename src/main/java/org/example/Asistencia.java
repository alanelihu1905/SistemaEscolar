package org.example;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Asistencia {
    private final IntegerProperty idAsistencia;
    private final IntegerProperty idInscripcion;
    private final StringProperty fecha;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;

    public Asistencia(int idAsistencia, int idInscripcion, String fecha,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.idAsistencia = new SimpleIntegerProperty(idAsistencia);
        this.idInscripcion = new SimpleIntegerProperty(idInscripcion);
        this.fecha = new SimpleStringProperty(fecha);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
        this.updatedAt = new SimpleObjectProperty<>(updatedAt);
    }

    public int getIdAsistencia() { return idAsistencia.get(); }
    public IntegerProperty idAsistenciaProperty() { return idAsistencia; }

    public int getIdInscripcion() { return idInscripcion.get(); }
    public IntegerProperty idInscripcionProperty() { return idInscripcion; }

    public String getFecha() { return fecha.get(); }
    public StringProperty fechaProperty() { return fecha; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
}
