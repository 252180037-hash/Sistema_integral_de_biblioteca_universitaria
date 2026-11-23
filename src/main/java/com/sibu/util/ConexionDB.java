package com.sibu.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestor centralizado de conexiones a la base de datos MySQL mediante JDBC puro.
 * <p>
 * Implementa el patrón Singleton para reutilización eficiente de conexiones y
 * prevención de fugas de recursos, alineado con el criterio de <b>Fiabilidad</b>
 * y <b>Eficiencia de desempeño</b> de la norma <b>ISO/IEC 25010:2023</b>.
 * No utiliza frameworks ORM para otorgar control explícito total sobre las transacciones.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public final class ConexionDB {

    private static final Logger LOGGER = Logger.getLogger(ConexionDB.class.getName());
    private static ConexionDB instancia;
    private Connection conexion;

    static {
        // Carga preventiva del driver JDBC de MySQL
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("Driver com.mysql.cj.jdbc.Driver registrado exitosamente.");
        } catch (ClassNotFoundException e) {
            // Intentar driver alternativo (compatibilidad MariaDB)
            try {
                Class.forName("org.mariadb.jdbc.Driver");
                LOGGER.info("Driver alternativo org.mariadb.jdbc.Driver registrado exitosamente.");
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "No se encontró ningún driver JDBC compatible para MySQL/MariaDB.", e);
            }
        }
    }

    /**
     * Constructor privado para prevenir instanciación externa.
     */
    private ConexionDB() {
    }

    /**
     * Obtiene la instancia única del gestor de conexiones.
     * 
     * @return Instancia única de {@link ConexionDB}.
     */
    public static synchronized ConexionDB getInstancia() {
        if (instancia == null) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    /**
     * Obtiene una conexión activa a la base de datos.
     * Si no existe o se encuentra cerrada o inválida, se establece una nueva.
     * 
     * @return Objeto {@link Connection} listo para ejecutar consultas.
     * @throws SQLException Si ocurre un error de comunicación o credenciales con el motor MySQL.
     */
    public synchronized Connection obtenerConexion() throws SQLException {
        if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
            establecerNuevaConexion();
        }
        return conexion;
    }

    /**
     * Establece una conexión física nueva utilizando los parámetros externos de {@link Configuracion}.
     * 
     * @throws SQLException Si la conexión no puede ser establecida.
     */
    private void establecerNuevaConexion() throws SQLException {
        Configuracion config = Configuracion.getInstancia();
        String url = config.getDbUrl();
        String user = config.getDbUser();
        String password = config.getDbPassword();

        try {
            conexion = DriverManager.getConnection(url, user, password);
            LOGGER.info("Conexión JDBC establecida exitosamente con el catálogo: " + conexion.getCatalog());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fallo al conectar con la base de datos MySQL en URL: " + url, e);
            throw e;
        }
    }

    /**
     * Crea y retorna una conexión independiente (útil para tareas que requieran
     * un ciclo de vida o transacción aislada de la conexión principal).
     * 
     * @return Nueva instancia de {@link Connection}.
     * @throws SQLException Si falla la conexión.
     */
    public Connection crearConexionAislada() throws SQLException {
        Configuracion config = Configuracion.getInstancia();
        return DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPassword());
    }

    /**
     * Verifica la disponibilidad del servidor de base de datos ejecutando una prueba de ping.
     * 
     * @return {@code true} si la conexión es funcional y responde; {@code false} en caso contrario.
     */
    public synchronized boolean probarConexion() {
        try {
            Connection con = obtenerConexion();
            return con != null && !con.isClosed() && con.isValid(3);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Prueba de conexión JDBC fallida: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cierra de forma ordenada y segura la conexión persistente principal si está activa.
     */
    public synchronized void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    LOGGER.info("Conexión JDBC cerrada ordenadamente.");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error al cerrar la conexión JDBC.", e);
            } finally {
                conexion = null;
            }
        }
    }

    /**
     * Utilidad nula-segura para cerrar recursos JDBC (ResultSet, Statement, PreparedStatement, Connection)
     * sin propagar excepciones ruidosas.
     * 
     * @param recursos Uno o más objetos {@link AutoCloseable} a cerrar.
     */
    public static void cerrarRecursos(AutoCloseable... recursos) {
        if (recursos == null) {
            return;
        }
        for (AutoCloseable rec : recursos) {
            if (rec != null) {
                try {
                    rec.close();
                } catch (Exception e) {
                    LOGGER.log(Level.FINE, "Error no crítico al cerrar recurso JDBC.", e);
                }
            }
        }
    }
}
