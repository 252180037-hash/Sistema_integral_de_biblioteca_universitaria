package com.sibu.modelo;

/**
 * Estados físicos y de inventario para una copia física (ejemplar) de un libro.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public enum EstadoEjemplar {
    /**
     * Disponible en estante para consulta o préstamo a domicilio.
     */
    DISPONIBLE("Disponible"),

    /**
     * Prestado activamente a un usuario lector.
     */
    PRESTADO("Prestado"),

    /**
     * Con deterioro físico; en proceso de encuadernación o restauración.
     */
    DANADO("Dañado"),

    /**
     * Desincorporado formalmente del patrimonio bibliográfico (extravío o destrucción).
     */
    BAJA("Baja");

    private final String etiqueta;

    EstadoEjemplar(String etiqueta) {
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
