package com.sibu.modelo;

/**
 * Roles de usuario admitidos en el sistema SIBU con permisos diferenciados.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public enum RolUsuario {
    /**
     * Acceso total: Usuarios, Catálogo, Circulación, Reportes, Bitácora y Respaldos.
     */
    ADMINISTRADOR("Administrador"),

    /**
     * Acceso operativo: Catálogo bibliográfico, Circulación de préstamos y Reportes.
     */
    BIBLIOTECARIO("Bibliotecario"),

    /**
     * Acceso de consulta: Búsqueda en catálogo y visualización de su historial personal.
     */
    LECTOR("Lector");

    private final String etiqueta;

    RolUsuario(String etiqueta) {
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
