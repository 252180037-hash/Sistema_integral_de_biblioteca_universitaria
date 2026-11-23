package com.sibu;

import com.sibu.util.ConexionDB;
import com.sibu.util.Configuracion;
import com.sibu.util.SeguridadUtil;
import com.sibu.util.TemaUtil;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal para el Sistema Integral de Biblioteca Universitaria (SIBU).
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Iniciando SIBU - Sistema Integral de Biblioteca Universitaria...");

        // 1. Inicialización de Look and Feel moderno
        TemaUtil.inicializarTema();

        // 2. Comprobación de configuración externa
        Configuracion config = Configuracion.getInstancia();
        LOGGER.info("Configuración cargada. Conectando a BD: " + config.getDbUrl());

        // 3. Verificación de prueba de conexión
        boolean conexionOk = ConexionDB.getInstancia().probarConexion();
        if (conexionOk) {
            LOGGER.info("Diagnóstico de conectividad: EXITOSO. La base de datos responde correctamente.");
        } else {
            LOGGER.warning("Diagnóstico de conectividad: No se pudo contactar con la BD. "
                    + "Por favor verifique que MySQL/MariaDB esté activo y ejecute database.sql.");
        }

        LOGGER.info("Fase 1 completada con éxito. Listo para la Fase 2.");
    }
}
