package com.sibu.servicio.exception;

/**
 * Excepción base verificada para la lógica de negocio del sistema SIBU.
 * <p>
 * Facilita el manejo centralizado de errores y provee mensajes comprensibles
 * para el usuario final (ISO/IEC 25010:2023 - Usabilidad y Fiabilidad).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class SibuException extends Exception {

    private static final long serialVersionUID = 1L;

    public SibuException(String mensaje) {
        super(mensaje);
    }

    public SibuException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
