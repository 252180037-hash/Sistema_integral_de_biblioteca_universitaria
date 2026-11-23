package com.sibu.servicio.exception;

/**
 * Excepción lanzada cuando una operación de préstamo, devolución o renovación
 * viola las políticas de circulación bibliotecaria.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class CirculacionException extends SibuException {

    private static final long serialVersionUID = 1L;

    public CirculacionException(String mensaje) {
        super(mensaje);
    }
}
