package com.sibu.modelo;

/**
 * Estados del ciclo de vida de un préstamo en el módulo de circulación.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public enum EstadoPrestamo {
    /**
     * En curso dentro del plazo estipulado.
     */
    ACTIVO("Activo"),

    /**
     * Ejemplar retornado e inspeccionado por la biblioteca.
     */
    DEVUELTO("Devuelto"),

    /**
     * Fecha límite superada sin devolución del ejemplar.
     */
    VENCIDO("Vencido");

    private final String etiqueta;

    EstadoPrestamo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
