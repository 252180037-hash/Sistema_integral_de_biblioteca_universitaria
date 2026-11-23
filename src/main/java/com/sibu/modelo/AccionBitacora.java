package com.sibu.modelo;

/**
 * Tipos de acciones registrables en la bitácora de auditoría del sistema SIBU.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public enum AccionBitacora {
    INSERT("Creación"),
    UPDATE("Modificación"),
    DELETE("Eliminación"),
    LOGIN("Inicio de Sesión"),
    LOGOUT("Cierre de Sesión"),
    PRESTAMO("Préstamo de Ejemplar"),
    DEVOLUCION("Devolución de Ejemplar"),
    RENOVACION("Renovación de Préstamo"),
    BACKUP("Copia de Seguridad"),
    RESTORE("Restauración de Base de Datos");

    private final String descripcion;

    AccionBitacora(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
