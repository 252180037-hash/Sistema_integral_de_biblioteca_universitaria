package com.sibu.servicio.exception;

/**
 * Excepción lanzada cuando los datos de entrada no cumplen con las reglas de negocio
 * o formatos requeridos (campos vacíos, longitud, formato de email o ISBN).
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class ValidacionException extends SibuException {

    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
