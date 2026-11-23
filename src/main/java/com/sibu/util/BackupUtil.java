package com.sibu.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestor robusto de copias de seguridad (Backup) y restauración (Restore) de la base de datos MySQL.
 * <p>
 * Diseñado bajo el requisito funcional <b>RF8</b> y el criterio de <b>Fiabilidad</b>
 * de la norma <b>ISO/IEC 25010:2023</b>. Implementa una arquitectura resiliente de dos niveles:
 * <ol>
 *   <li><b>Nivel Primario (Nativo CLI):</b> Intenta utilizar los binarios nativos {@code mysqldump} y {@code mysql}
 *       para máxima velocidad y fidelidad binaria.</li>
 *   <li><b>Nivel Secundario (Fallback JDBC Puro):</b> Si los ejecutables no están instalados o accesibles en el PATH,
 *       extrae la estructura DDL y los registros INSERT a través de JDBC puro de forma completamente autónoma,
 *       garantizando que la funcionalidad nunca falle por ausencia de dependencias externas.</li>
 * </ol>
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public final class BackupUtil {

    private static final Logger LOGGER = Logger.getLogger(BackupUtil.class.getName());

    private BackupUtil() {
    }

    /**
     * Genera un archivo de respaldo SQL con nombre fechado automáticamente en el directorio de backups.
     * 
     * @return Objeto {@link File} del respaldo generado con éxito.
     * @throws Exception Si ambos métodos (CLI y Fallback JDBC) fallan.
     */
    public static File generarRespaldoAutomatico() throws Exception {
        Configuracion config = Configuracion.getInstancia();
        File carpetaBackups = new File(config.getBackupDir());
        if (!carpetaBackups.exists()) {
            carpetaBackups.mkdirs();
        }

        String estampadoTiempo = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreArchivo = "sibu_backup_" + estampadoTiempo + ".sql";
        File archivoSalida = new File(carpetaBackups, nombreArchivo);

        generarRespaldo(archivoSalida);
        return archivoSalida;
    }

    /**
     * Genera un respaldo de la base de datos en el archivo destino especificado.
     * 
     * @param archivoDestino Archivo físico .sql donde se almacenará el volcado.
     * @throws Exception Si no es posible generar el respaldo.
     */
    public static void generarRespaldo(File archivoDestino) throws Exception {
        if (archivoDestino == null) {
            throw new IllegalArgumentException("El archivo destino de respaldo no puede ser nulo.");
        }

        Configuracion config = Configuracion.getInstancia();
        String mysqldumpPath = resolverRutaEjecutable(config.getMysqldumpPath(), "mysqldump.exe");

        // Intentar método 1: Ejecutable nativo mysqldump
        if (mysqldumpPath != null) {
            try {
                LOGGER.info("Iniciando respaldo nativo con mysqldump: " + mysqldumpPath);
                ejecutarMysqldumpNativo(mysqldumpPath, archivoDestino, config);
                LOGGER.info("Respaldo nativo completado con éxito en: " + archivoDestino.getAbsolutePath());
                return;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Fallo en mysqldump nativo. Activando motor de respaldo JDBC Fallback...", e);
            }
        }

        // Método 2: Fallback autónomo JDBC Puro
        LOGGER.info("Ejecutando respaldo mediante motor JDBC puro...");
        generarRespaldoJdbc(archivoDestino);
        LOGGER.info("Respaldo JDBC completado con éxito en: " + archivoDestino.getAbsolutePath());
    }

    /**
     * Restaura la base de datos a partir de un archivo de respaldo .sql.
     * 
     * @param archivoRespaldo Archivo SQL con las instrucciones a restaurar.
     * @throws Exception Si ocurre un fallo irrecuperable durante la restauración.
     */
    public static void restaurarRespaldo(File archivoRespaldo) throws Exception {
        if (archivoRespaldo == null || !archivoRespaldo.exists()) {
            throw new IllegalArgumentException("El archivo de respaldo especificado no existe o es nulo.");
        }

        Configuracion config = Configuracion.getInstancia();
        String mysqlPath = resolverRutaEjecutable(config.getMysqlPath(), "mysql.exe");

        // Intentar método 1: Ejecutable nativo mysql CLI
        if (mysqlPath != null) {
            try {
                LOGGER.info("Iniciando restauración nativa con cliente mysql: " + mysqlPath);
                ejecutarMysqlRestoreNativo(mysqlPath, archivoRespaldo, config);
                LOGGER.info("Restauración nativa completada con éxito.");
                return;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Fallo en restauración nativa CLI. Activando restaurador JDBC Fallback...", e);
            }
        }

        // Método 2: Fallback autónomo JDBC transaccional
        LOGGER.info("Ejecutando restauración transaccional mediante motor JDBC puro...");
        restaurarRespaldoJdbc(archivoRespaldo);
        LOGGER.info("Restauración JDBC completada con éxito.");
    }

    // =========================================================================
    // IMPLEMENTACIÓN NATIVA (PROCESS BUILDER)
    // =========================================================================

    private static void ejecutarMysqldumpNativo(String binario, File destino, Configuracion config) throws Exception {
        List<String> comando = new ArrayList<>();
        comando.add(binario);
        comando.add("-h" + config.getDbHost());
        comando.add("-P" + config.getDbPort());
        comando.add("-u" + config.getDbUser());

        String pass = config.getDbPassword();
        if (pass != null && !pass.isEmpty()) {
            comando.add("-p" + pass);
        }

        comando.add("--add-drop-table");
        comando.add("--complete-insert");
        comando.add("--comments");
        comando.add(config.getDbName());

        ProcessBuilder pb = new ProcessBuilder(comando);
        pb.redirectErrorStream(false);
        Process proceso = pb.start();

        try (InputStream is = proceso.getInputStream();
             FileOutputStream fos = new FileOutputStream(destino)) {
            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = is.read(buffer)) != -1) {
                fos.write(buffer, 0, leidos);
            }
        }

        // Capturar errores del proceso si los hubo
        String salidaError;
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(proceso.getErrorStream()))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = errorReader.readLine()) != null) {
                sb.append(linea).append("\n");
            }
            salidaError = sb.toString();
        }

        int exitCode = proceso.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("mysqldump retornó código de salida (" + exitCode + "): " + salidaError);
        }
    }

    private static void ejecutarMysqlRestoreNativo(String binario, File origen, Configuracion config) throws Exception {
        List<String> comando = new ArrayList<>();
        comando.add(binario);
        comando.add("-h" + config.getDbHost());
        comando.add("-P" + config.getDbPort());
        comando.add("-u" + config.getDbUser());

        String pass = config.getDbPassword();
        if (pass != null && !pass.isEmpty()) {
            comando.add("-p" + pass);
        }
        comando.add(config.getDbName());

        ProcessBuilder pb = new ProcessBuilder(comando);
        Process proceso = pb.start();

        try (FileInputStream fis = new FileInputStream(origen);
             OutputStream os = proceso.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = fis.read(buffer)) != -1) {
                os.write(buffer, 0, leidos);
            }
            os.flush();
        }

        int exitCode = proceso.waitFor();
        if (exitCode != 0) {
            String errorMsg;
            try (BufferedReader er = new BufferedReader(new InputStreamReader(proceso.getErrorStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = er.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                errorMsg = sb.toString();
            }
            throw new RuntimeException("mysql cliente retornó código de error (" + exitCode + "): " + errorMsg);
        }
    }

    // =========================================================================
    // MOTOR DE RESPALDO Y RESTAURACIÓN JDBC PURO (FALLBACK AUTÓNOMO)
    // =========================================================================

    private static void generarRespaldoJdbc(File destino) throws SQLException, IOException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        DatabaseMetaData metaData = con.getMetaData();
        String catalogo = con.getCatalog();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(destino, StandardCharsets.UTF_8))) {
            bw.write("-- ========================================================\n");
            bw.write("-- SIBU - Respaldo de Base de Datos (Motor JDBC Puro)\n");
            bw.write("-- Catálogo: " + catalogo + "\n");
            bw.write("-- Fecha: " + new Date() + "\n");
            bw.write("-- ========================================================\n\n");
            bw.write("SET FOREIGN_KEY_CHECKS=0;\n\n");

            // Obtener tablas en orden
            List<String> tablas = new ArrayList<>();
            try (ResultSet rsTablas = metaData.getTables(catalogo, null, "%", new String[]{"TABLE"})) {
                while (rsTablas.next()) {
                    tablas.add(rsTablas.getString("TABLE_NAME"));
                }
            }

            try (Statement stmt = con.createStatement()) {
                for (String tabla : tablas) {
                    bw.write("--\n-- Estructura de tabla: `" + tabla + "`\n--\n");
                    bw.write("DROP TABLE IF EXISTS `" + tabla + "`;\n");

                    // Extraer DDL original
                    try (ResultSet rsCreate = stmt.executeQuery("SHOW CREATE TABLE `" + tabla + "`")) {
                        if (rsCreate.next()) {
                            String ddl = rsCreate.getString(2);
                            bw.write(ddl + ";\n\n");
                        }
                    }

                    // Extraer Registros
                    bw.write("--\n-- Volcado de datos para: `" + tabla + "`\n--\n");
                    try (ResultSet rsDatos = stmt.executeQuery("SELECT * FROM `" + tabla + "`")) {
                        ResultSetMetaData rsMeta = rsDatos.getMetaData();
                        int columnas = rsMeta.getColumnCount();

                        while (rsDatos.next()) {
                            StringBuilder insert = new StringBuilder("INSERT INTO `").append(tabla).append("` VALUES (");
                            for (int i = 1; i <= columnas; i++) {
                                Object valor = rsDatos.getObject(i);
                                if (valor == null) {
                                    insert.append("NULL");
                                } else if (valor instanceof Number) {
                                    insert.append(valor.toString());
                                } else {
                                    String texto = valor.toString()
                                            .replace("\\", "\\\\")
                                            .replace("'", "\\'")
                                            .replace("\r", "\\r")
                                            .replace("\n", "\\n");
                                    insert.append("'").append(texto).append("'");
                                }
                                if (i < columnas) {
                                    insert.append(", ");
                                }
                            }
                            insert.append(");\n");
                            bw.write(insert.toString());
                        }
                    }
                    bw.write("\n");
                }
            }

            bw.write("SET FOREIGN_KEY_CHECKS=1;\n");
            bw.flush();
        }
    }

    private static void restaurarRespaldoJdbc(File origen) throws SQLException, IOException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        boolean autoCommitOriginal = con.getAutoCommit();

        try {
            con.setAutoCommit(false); // Control estricto de transacciones ISO 25010

            try (BufferedReader br = new BufferedReader(new FileReader(origen, StandardCharsets.UTF_8));
                 Statement stmt = con.createStatement()) {

                StringBuilder sentencia = new StringBuilder();
                String linea;

                while ((linea = br.readLine()) != null) {
                    String lineaTrim = linea.trim();
                    // Omitir comentarios de línea
                    if (lineaTrim.startsWith("--") || lineaTrim.startsWith("/*") || lineaTrim.isEmpty()) {
                        continue;
                    }

                    sentencia.append(linea).append("\n");

                    if (lineaTrim.endsWith(";")) {
                        String sql = sentencia.toString().trim();
                        if (sql.endsWith(";")) {
                            sql = sql.substring(0, sql.length() - 1);
                        }
                        if (!sql.isEmpty()) {
                            stmt.execute(sql);
                        }
                        sentencia.setLength(0);
                    }
                }

                con.commit();
                LOGGER.info("Transacción de restauración JDBC completada y confirmada (commit).");
            } catch (Exception e) {
                con.rollback();
                LOGGER.log(Level.SEVERE, "Fallo durante restauración JDBC. Transacción revertida (rollback).", e);
                throw e;
            }
        } finally {
            con.setAutoCommit(autoCommitOriginal);
        }
    }

    // =========================================================================
    // UTILIDADES DE DETECCIÓN DE RUTAS
    // =========================================================================

    private static String resolverRutaEjecutable(String rutaConfigurada, String nombreBinario) {
        if (rutaConfigurada != null && !rutaConfigurada.trim().isEmpty()) {
            File f = new File(rutaConfigurada.trim());
            if (f.exists() && f.canExecute()) {
                return f.getAbsolutePath();
            }
        }

        // Rutas habituales conocidas en Windows (MySQL y MariaDB)
        String[] candidatosWindows = new String[]{
                "C:/Program Files/MariaDB 13.0/bin/" + nombreBinario,
                "C:/Program Files/MariaDB 12.0/bin/" + nombreBinario,
                "C:/Program Files/MariaDB 11.0/bin/" + nombreBinario,
                "C:/Program Files/MySQL/MySQL Server 8.0/bin/" + nombreBinario,
                "C:/Program Files/MySQL/MySQL Server 8.3/bin/" + nombreBinario,
                "C:/xampp/mysql/bin/" + nombreBinario,
                "C:/wamp64/bin/mysql/mysql8.0.31/bin/" + nombreBinario
        };

        for (String cand : candidatosWindows) {
            File f = new File(cand);
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }

        // Probar si responde directamente en PATH
        try {
            Process p = new ProcessBuilder(nombreBinario, "--version").start();
            int code = p.waitFor();
            if (code == 0) {
                return nombreBinario;
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
