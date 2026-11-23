package com.sibu.modelo;

/**
 * Estados operativos posibles para una cuenta de usuario en el sistema SIBU.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public enum EstadoUsuario {
    /**
     * Usuario habilitado para iniciar sesión y solicitar préstamos.
     */
    ACTIVO("Activo"),

    /**
     * Cuenta deshabilitada manualmente por un Administrador.
     */
    INACTIVO("Inactivo"),

    /**
     * Cuenta bloqueada para préstamos debido a sanciones o préstamos vencidos.
     */
    SANCIONADO("Sancionado");

    private final String etiqueta;

    EstadoUsuario(String etiqueta) {
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
