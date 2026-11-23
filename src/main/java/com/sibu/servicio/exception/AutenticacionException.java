package com.sibu.servicio.exception;

/**
 * Excepción lanzada cuando fallan las credenciales de acceso o la cuenta está inhabilitada.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class AutenticacionException extends SibuException {

    private static final long serialVersionUID = 1L;

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
