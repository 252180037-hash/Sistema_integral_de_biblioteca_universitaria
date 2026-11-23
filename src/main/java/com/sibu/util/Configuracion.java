package com.sibu.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestor centralizado de configuración externa para el sistema SIBU.
 * <p>
 * Implementa el patrón Singleton para garantizar una única fuente de verdad.
 * Cumple con el criterio de <b>Flexibilidad</b> de la norma <b>ISO/IEC 25010:2023</b>,
 * permitiendo modificar los parámetros del sistema (conexión JDBC, sanciones, rutas)
 * sin requerir recompilación de código ni alteración del empaquetado JAR.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public final class Configuracion {

    private static final Logger LOGGER = Logger.getLogger(Configuracion.class.getName());
    private static final String ARCHIVO_CONFIG = "config.properties";
    private static Configuracion instancia;

    private final Properties propiedades;
    private File archivoExterno;

    /**
     * Constructor privado para prevenir instanciación externa.
     * Carga las propiedades intentando primero el archivo local externo y,
     * en caso de no existir, recurre al recurso empaquetado en el classpath.
     */
    private Configuracion() {
        propiedades = new Properties();
        cargarPropiedades();
    }

    /**
     * Obtiene la instancia única del gestor de configuración (Patrón Singleton).
     * 
     * @return Instancia única de {@link Configuracion}.
     */
    public static synchronized Configuracion getInstancia() {
        if (instancia == null) {
            instancia = new Configuracion();
        }
        return instancia;
    }

    /**
     * Carga las propiedades desde el sistema de archivos externo o el classpath.
     */
    private void cargarPropiedades() {
        archivoExterno = new File(ARCHIVO_CONFIG);

        // 1. Intentar cargar desde el archivo físico externo en el directorio de trabajo
        if (archivoExterno.exists() && archivoExterno.isFile()) {
            try (InputStream input = new FileInputStream(archivoExterno)) {
                propiedades.load(input);
                LOGGER.info("Configuración cargada exitosamente desde archivo externo: " + archivoExterno.getAbsolutePath());
                return;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "No fue posible leer el archivo externo config.properties. Se usará el recurso interno.", e);
            }
        }

        // 2. Fallback: Cargar desde el classpath (src/main/resources/config.properties)
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(ARCHIVO_CONFIG)) {
            if (input != null) {
                propiedades.load(input);
                LOGGER.info("Configuración cargada exitosamente desde recurso interno del Classpath.");
            } else {
                LOGGER.severe("No se encontró config.properties ni en disco ni en el classpath. Se aplicarán valores por defecto.");
                establecerValoresPorDefecto();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error crítico al leer config.properties desde el classpath.", e);
            establecerValoresPorDefecto();
        }
    }

    /**
     * Establece valores por defecto seguros en memoria si no se encuentra archivo alguno.
     */
    private void establecerValoresPorDefecto() {
        propiedades.setProperty("db.host", "localhost");
        propiedades.setProperty("db.port", "3306");
        propiedades.setProperty("db.name", "sibu_db");
        propiedades.setProperty("db.url", "jdbc:mysql://localhost:3306/sibu_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
        propiedades.setProperty("db.user", "root");
        propiedades.setProperty("db.password", "");
        propiedades.setProperty("backup.dir", "backups");
        propiedades.setProperty("mysqldump.path", "mysqldump");
        propiedades.setProperty("mysql.path", "mysql");
        propiedades.setProperty("app.multa.diaria", "5.00");
        propiedades.setProperty("app.prestamo.dias_limite", "7");
        propiedades.setProperty("app.prestamo.max_libros", "3");
        propiedades.setProperty("app.prestamo.renovaciones_max", "1");
        propiedades.setProperty("app.tema.default", "LIGHT");
    }

    /**
     * Guarda las modificaciones de configuración en el archivo externo {@code config.properties}.
     * 
     * @return {@code true} si la operación fue exitosa, {@code false} en caso de error.
     */
    public synchronized boolean guardar() {
        try (FileOutputStream output = new FileOutputStream(archivoExterno)) {
            propiedades.store(output, "SIBU - Configuración actualizada por el usuario");
            LOGGER.info("Archivo de configuración actualizado en: " + archivoExterno.getAbsolutePath());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al persistir la configuración en disco.", e);
            return false;
        }
    }

    /**
     * Obtiene el valor de una clave textual.
     * 
     * @param clave Nombre de la propiedad requerida.
     * @return Valor textual o {@code null} si no existe.
     */
    public String get(String clave) {
        return propiedades.getProperty(clave);
    }

    /**
     * Obtiene el valor de una clave textual con un valor por defecto.
     * 
     * @param clave Nombre de la propiedad requerida.
     * @param valorPorDefecto Valor a retornar si la clave no existe o está vacía.
     * @return Valor textual.
     */
    public String get(String clave, String valorPorDefecto) {
        return propiedades.getProperty(clave, valorPorDefecto);
    }

    /**
     * Obtiene el valor de una clave convertido a entero.
     * 
     * @param clave Nombre de la propiedad.
     * @param valorPorDefecto Valor retornado si no existe o falla la conversión.
     * @return Valor entero.
     */
    public int getInt(String clave, int valorPorDefecto) {
        String val = get(clave);
        if (val == null || val.trim().isEmpty()) {
            return valorPorDefecto;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning("Formato numérico inválido para propiedad: " + clave + ". Se usará: " + valorPorDefecto);
            return valorPorDefecto;
        }
    }

    /**
     * Obtiene el valor de una clave convertido a decimal de punto flotante.
     * 
     * @param clave Nombre de la propiedad.
     * @param valorPorDefecto Valor retornado si no existe o falla la conversión.
     * @return Valor double.
     */
    public double getDouble(String clave, double valorPorDefecto) {
        String val = get(clave);
        if (val == null || val.trim().isEmpty()) {
            return valorPorDefecto;
        }
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning("Formato decimal inválido para propiedad: " + clave + ". Se usará: " + valorPorDefecto);
            return valorPorDefecto;
        }
    }

    /**
     * Establece o actualiza una propiedad en memoria.
     * 
     * @param clave Nombre de la propiedad.
     * @param valor Nuevo valor.
     */
    public void set(String clave, String valor) {
        propiedades.setProperty(clave, valor);
    }

    // =========================================================================
    // Métodos tipados de acceso a parámetros del sistema
    // =========================================================================

    public String getDbUrl() {
        return get("db.url", "jdbc:mysql://localhost:3306/sibu_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
    }

    public String getDbUser() {
        return get("db.user", "root");
    }

    public String getDbPassword() {
        return get("db.password", "");
    }

    public String getDbHost() {
        return get("db.host", "localhost");
    }

    public int getDbPort() {
        return getInt("db.port", 3306);
    }

    public String getDbName() {
        return get("db.name", "sibu_db");
    }

    public String getBackupDir() {
        return get("backup.dir", "backups");
    }

    public String getMysqldumpPath() {
        return get("mysqldump.path", "mysqldump");
    }

    public String getMysqlPath() {
        return get("mysql.path", "mysql");
    }

    public double getMultaDiaria() {
        return getDouble("app.multa.diaria", 5.00);
    }

    public int getDiasLimitePrestamo() {
        return getInt("app.prestamo.dias_limite", 7);
    }

    public int getMaxLibrosPrestamo() {
        return getInt("app.prestamo.max_libros", 3);
    }

    public int getMaxRenovaciones() {
        return getInt("app.prestamo.renovaciones_max", 1);
    }

    public String getTemaDefault() {
        return get("app.tema.default", "LIGHT");
    }
}
