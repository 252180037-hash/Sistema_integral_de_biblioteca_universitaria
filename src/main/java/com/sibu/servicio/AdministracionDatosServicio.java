package com.sibu.servicio;

import com.sibu.dao.BitacoraDAO;
import com.sibu.modelo.AccionBitacora;
import com.sibu.modelo.Bitacora;
import com.sibu.servicio.exception.ValidacionException;
import com.sibu.util.BackupUtil;
import com.sibu.util.ConexionDB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de administración de datos, respaldos, restauraciones y seeders (RF8).
 * <p>
 * Permite generar copias de seguridad (.sql), restaurar la base de datos y
 * repoblar el catálogo y usuarios de prueba (Seeders) en transacciones JDBC controladas.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class AdministracionDatosServicio {

    private static final Logger LOGGER = Logger.getLogger(AdministracionDatosServicio.class.getName());

    private final BitacoraDAO bitacoraDAO;

    public AdministracionDatosServicio() {
        this.bitacoraDAO = new BitacoraDAO();
    }

    public AdministracionDatosServicio(BitacoraDAO bitacoraDAO) {
        this.bitacoraDAO = bitacoraDAO;
    }

    /**
     * Genera una copia de seguridad en un archivo SQL y audita la acción.
     * 
     * @param archivoDestino Archivo físico .sql donde se almacenará el volcado.
     * @param idAdministrador Usuario que autorizó el respaldo.
     */
    public void generarRespaldo(File archivoDestino, Integer idAdministrador) throws Exception {
        if (archivoDestino == null) {
            throw new ValidacionException("Debe seleccionar una ruta de archivo válida para guardar el respaldo.");
        }

        BackupUtil.generarRespaldo(archivoDestino);

        Bitacora bit = new Bitacora(idAdministrador, "RESPALDO", AccionBitacora.BACKUP,
                "Generación de copia de seguridad en archivo: " + archivoDestino.getName() + " (" + archivoDestino.length() + " bytes)");
        bitacoraDAO.registrar(bit);
    }

    /**
     * Restaura la base de datos a partir de un archivo .sql externo.
     * 
     * @param archivoOrigen Archivo de respaldo a aplicar.
     * @param idAdministrador Usuario que ejecuta la restauración.
     */
    public void restaurarBaseDeDatos(File archivoOrigen, Integer idAdministrador) throws Exception {
        if (archivoOrigen == null || !archivoOrigen.exists()) {
            throw new ValidacionException("El archivo de respaldo seleccionado no existe o no es accesible.");
        }

        BackupUtil.restaurarRespaldo(archivoOrigen);

        Bitacora bit = new Bitacora(idAdministrador, "RESPALDO", AccionBitacora.RESTORE,
                "Restauración completa de la base de datos desde el archivo: " + archivoOrigen.getName());
        bitacoraDAO.registrar(bit);
    }

    /**
     * Ejecuta el script de Seeders para poblar o reiniciar los datos de prueba
     * (usuarios de los 3 roles, libros, copias, préstamos y sanciones).
     * 
     * @param idAdministrador Identificador del operador.
     */
    public void ejecutarSeeders(Integer idAdministrador) throws Exception {
        File archivoSql = new File("database.sql");
        if (!archivoSql.exists()) {
            archivoSql = new File("scripts/database.sql");
        }

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false); // Transacción estricta

            try (BufferedReader br = archivoSql.exists() 
                    ? new BufferedReader(new FileReader(archivoSql, StandardCharsets.UTF_8))
                    : new BufferedReader(new InputStreamReader(
                            getClass().getClassLoader().getResourceAsStream("database.sql"), StandardCharsets.UTF_8));
                 Statement stmt = con.createStatement()) {

                StringBuilder sentencia = new StringBuilder();
                String linea;

                while ((linea = br.readLine()) != null) {
                    String lineaTrim = linea.trim();
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

                Bitacora bit = new Bitacora(idAdministrador, "ADMIN_DATOS", AccionBitacora.INSERT,
                        "Ejecución exitosa de script Seeders (reinicio y poblado inicial de base de datos)");
                bitacoraDAO.registrar(bit, con);

                con.commit();
                LOGGER.info("Script de Seeders ejecutado y confirmado (commit) exitosamente.");
            } catch (Exception e) {
                con.rollback();
                LOGGER.log(Level.SEVERE, "Fallo al ejecutar Seeders. Rollback ejecutado.", e);
                throw e;
            }
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
