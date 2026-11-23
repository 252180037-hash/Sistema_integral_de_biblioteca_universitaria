package com.sibu.modelo;

/**
 * Estados posibles para una sanción o multa por entrega tardía.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public enum EstadoSancion {
    /**
     * Multa pendiente de pago por el lector.
     */
    PENDIENTE("Pendiente"),

    /**
     * Sanción liquidada en su totalidad en caja o tesorería.
     */
    PAGADA("Pagada"),

    /**
     * Sanción perdonada o condonada por un Administrador con causa justificada.
     */
    CONDONADA("Condonada");

    private final String etiqueta;

    EstadoSancion(String etiqueta) {
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
