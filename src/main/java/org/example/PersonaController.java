package org.example;

public class PersonaController extends BaseTablaSimpleController {
    @Override
    protected String getConsultaSql() {
        return "SELECT * FROM persona_escuela ORDER BY 1";
    }

    @Override
    protected String getTitulo() {
        return "Catálogo de Personas";
    }
}
