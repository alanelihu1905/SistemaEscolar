package org.example;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Asistencia {
    private final IntegerProperty idAsistencia = new SimpleIntegerProperty();
    private final IntegerProperty idInscripcion = new SimpleIntegerProperty();
    private final StringProperty  fecha = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    public Asistencia(int idA, int idI, String f, LocalDateTime cAt, LocalDateTime uAt){
        setIdAsistencia(idA);
        setIdInscripcion(idI);
        setFecha(f);
        setCreatedAt(cAt);
        setUpdatedAt(uAt);
    }

    public int getIdAsistencia(){ return idAsistencia.get(); }
    public void setIdAsistencia(int v){ idAsistencia.set(v); }
    public IntegerProperty idAsistenciaProperty(){ return idAsistencia; }

    public int getIdInscripcion(){ return idInscripcion.get(); }
    public void setIdInscripcion(int v){ idInscripcion.set(v); }
    public IntegerProperty idInscripcionProperty(){ return idInscripcion; }

    public String getFecha(){ return fecha.get(); }
    public void setFecha(String v){ fecha.set(v); }
    public StringProperty fechaProperty(){ return fecha; }

    public LocalDateTime getCreatedAt(){ return createdAt.get(); }
    public void setCreatedAt(LocalDateTime v){ createdAt.set(v); }
    public ObjectProperty<LocalDateTime> createdAtProperty(){ return createdAt; }

    public LocalDateTime getUpdatedAt(){ return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime v){ updatedAt.set(v); }
    public ObjectProperty<LocalDateTime> updatedAtProperty(){ return updatedAt; }
}