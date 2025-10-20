package org.example;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Asistencia {
    private final IntegerProperty idAsistencia;
    private final IntegerProperty idInscripcion;
    private final StringProperty estudiante;
    private final StringProperty materia;
    private final StringProperty fecha;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;

    public Asistencia(int idAsistencia, int idInscripcion, String estudiante, String materia,
                      String fecha, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.idAsistencia = new SimpleIntegerProperty(idAsistencia);
        this.idInscripcion = new SimpleIntegerProperty(idInscripcion);
        this.estudiante = new SimpleStringProperty(estudiante);
        this.materia = new SimpleStringProperty(materia);
        this.fecha = new SimpleStringProperty(fecha);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
        this.updatedAt = new SimpleObjectProperty<>(updatedAt);
    }

    public IntegerProperty idAsistenciaProperty() { return idAsistencia; }
    public IntegerProperty idInscripcionProperty() { return idInscripcion; }
    public StringProperty estudianteProperty() { return estudiante; }
    public StringProperty materiaProperty() { return materia; }
    public StringProperty fechaProperty() { return fecha; }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
}
