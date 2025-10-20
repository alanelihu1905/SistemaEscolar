package org.example;

public class InscripcionesController extends BaseTablaSimpleController {
    @Override
    protected String getConsultaSql() {
        return "SELECT * FROM inscripciones ORDER BY 1";
    }

    @Override
    protected String getTitulo() {
        return "Catálogo de Inscripciones";
    }
}
