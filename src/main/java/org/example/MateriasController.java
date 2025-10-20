package org.example;

public class MateriasController extends BaseTablaSimpleController {
    @Override
    protected String getConsultaSql() {
        return "SELECT * FROM materias ORDER BY 1";
    }

    @Override
    protected String getTitulo() {
        return "Catálogo de Materias";
    }
}
