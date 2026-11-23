package com.sibu;

import com.sibu.util.ConexionDB;
import com.sibu.util.Configuracion;
import com.sibu.util.TemaUtil;
import com.sibu.vista.LoginFrame;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal para el Sistema Integral de Biblioteca Universitaria (SIBU).
 * <p>
 * Inicializa el Look & Feel moderno (FlatLaf), verifica los parámetros de configuración
 * y despliega la ventana de autenticación (LoginFrame) en el hilo de despacho de eventos de Swing (EDT).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Iniciando SIBU - Sistema Integral de Biblioteca Universitaria...");

        // 1. Inicialización de Look & Feel moderno (Modo Claro / Modo Oscuro)
        TemaUtil.inicializarTema();

        // 2. Comprobación preventiva de configuración
        Configuracion config = Configuracion.getInstancia();
        LOGGER.info("Configuración cargada. Servidor BD: " + config.getDbHost() + ":" + config.getDbPort());

        // 3. Verificación de conectividad JDBC
        boolean conexionOk = ConexionDB.getInstancia().probarConexion();
        if (conexionOk) {
            LOGGER.info("Diagnóstico de conectividad: Base de datos MySQL conectada y lista.");
        } else {
            LOGGER.warning("Diagnóstico de conectividad: No se pudo contactar con la BD en este momento. "
                    + "Por favor verifique que su servidor MySQL/MariaDB esté activo.");
        }

        // 4. Lanzar Interfaz Gráfica en el Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
            LOGGER.info("Pantalla de inicio de sesión desplegada correctamente.");
        });
    }
}
