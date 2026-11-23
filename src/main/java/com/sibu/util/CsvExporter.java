package com.sibu.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Utilidad especializada para la exportación de conjuntos de datos y tablas a archivos en formato CSV.
 * <p>
 * Diseñada para satisfacer el requisito funcional <b>RF5 (Generación de Reportes)</b> y el criterio
 * de <b>Usabilidad</b> de la norma <b>ISO/IEC 25010:2023</b>.
 * Incorpora el Byte Order Mark (BOM) UTF-8 para garantizar compatibilidad nativa inmediata con
 * Microsoft Excel en Windows (evitando la corrupción de tildes, 'ñ' y caracteres especiales en español).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public final class CsvExporter {

    private static final String SEPARADOR_DEFECTO = ";"; // Recomendado para entornos hispanohablantes (Excel)
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private CsvExporter() {
    }

    /**
     * Exporta el contenido visual y estructurado de un {@link JTable} a un archivo CSV.
     * 
     * @param tabla Componente de tabla visual Swing con los datos.
     * @param archivoDestino Archivo físico donde se guardará el reporte.
     * @return Cantidad de filas de datos exportadas exitosamente.
     * @throws IOException Si ocurre un error de escritura en el sistema de archivos.
     */
    public static int exportarTabla(JTable tabla, File archivoDestino) throws IOException {
        if (tabla == null || archivoDestino == null) {
            throw new IllegalArgumentException("La tabla y el archivo destino no pueden ser nulos.");
        }
        TableModel modelo = tabla.getModel();
        int columnas = modelo.getColumnCount();
        int filas = modelo.getRowCount();

        String[] encabezados = new String[columnas];
        for (int c = 0; c < columnas; c++) {
            encabezados[c] = tabla.getColumnName(c);
        }

        try (FileOutputStream fos = new FileOutputStream(archivoDestino)) {
            // Escribir BOM UTF-8 para reconocimiento automático en Excel
            fos.write(UTF8_BOM);

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
                // Escribir encabezados
                escribirLineaCsv(writer, encabezados, SEPARADOR_DEFECTO);

                // Escribir registros
                for (int f = 0; f < filas; f++) {
                    String[] filaDatos = new String[columnas];
                    for (int c = 0; c < columnas; c++) {
                        Object valor = modelo.getValueAt(f, c);
                        filaDatos[c] = (valor != null) ? valor.toString() : "";
                    }
                    escribirLineaCsv(writer, filaDatos, SEPARADOR_DEFECTO);
                }
                writer.flush();
            }
        }
        return filas;
    }

    /**
     * Exporta una lista arbitraria de arreglos de cadenas con sus respectivos encabezados.
     * 
     * @param encabezados Arreglo de títulos de columnas.
     * @param filasDeDatos Lista de arreglos que representan cada registro a exportar.
     * @param archivoDestino Archivo físico de salida.
     * @return Cantidad de filas escritas.
     * @throws IOException En caso de fallas de entrada/salida.
     */
    public static int exportarDatos(String[] encabezados, List<String[]> filasDeDatos, File archivoDestino) throws IOException {
        if (archivoDestino == null) {
            throw new IllegalArgumentException("El archivo destino no puede ser nulo.");
        }

        try (FileOutputStream fos = new FileOutputStream(archivoDestino)) {
            fos.write(UTF8_BOM);

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
                if (encabezados != null && encabezados.length > 0) {
                    escribirLineaCsv(writer, encabezados, SEPARADOR_DEFECTO);
                }

                int totalFilas = 0;
                if (filasDeDatos != null) {
                    for (String[] fila : filasDeDatos) {
                        escribirLineaCsv(writer, fila, SEPARADOR_DEFECTO);
                        totalFilas++;
                    }
                }
                writer.flush();
                return totalFilas;
            }
        }
    }

    /**
     * Exporta el resultado de una consulta SQL {@link ResultSet} directamente a un archivo CSV.
     * 
     * @param rs Conjunto de resultados JDBC abierto.
     * @param archivoDestino Archivo físico de salida.
     * @return Total de registros exportados.
     * @throws IOException Si falla la escritura en disco.
     * @throws SQLException Si ocurre un error al iterar el ResultSet.
     */
    public static int exportarResultSet(ResultSet rs, File archivoDestino) throws IOException, SQLException {
        if (rs == null || archivoDestino == null) {
            throw new IllegalArgumentException("El ResultSet y el archivo no pueden ser nulos.");
        }

        ResultSetMetaData meta = rs.getMetaData();
        int columnas = meta.getColumnCount();
        String[] encabezados = new String[columnas];
        for (int c = 1; c <= columnas; c++) {
            encabezados[c - 1] = meta.getColumnLabel(c);
        }

        try (FileOutputStream fos = new FileOutputStream(archivoDestino)) {
            fos.write(UTF8_BOM);

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
                escribirLineaCsv(writer, encabezados, SEPARADOR_DEFECTO);

                int totalFilas = 0;
                while (rs.next()) {
                    String[] fila = new String[columnas];
                    for (int c = 1; c <= columnas; c++) {
                        Object obj = rs.getObject(c);
                        fila[c - 1] = (obj != null) ? obj.toString() : "";
                    }
                    escribirLineaCsv(writer, fila, SEPARADOR_DEFECTO);
                    totalFilas++;
                }
                writer.flush();
                return totalFilas;
            }
        }
    }

    /**
     * Escribe una línea formateada según el estándar RFC 4180.
     * Escapa comillas dobles y encierra entre comillas los campos con separadores o saltos de línea.
     */
    private static void escribirLineaCsv(BufferedWriter writer, String[] valores, String separador) throws IOException {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            String campo = (valores[i] != null) ? valores[i] : "";
            // Si contiene separador, comillas o salto de línea, debe entrecomillarse y escapar comillas
            boolean requiereComillas = campo.contains(separador) || campo.contains("\"") || campo.contains("\n") || campo.contains("\r");

            if (campo.contains("\"")) {
                campo = campo.replace("\"", "\"\"");
            }

            if (requiereComillas) {
                linea.append("\"").append(campo).append("\"");
            } else {
                linea.append(campo);
            }

            if (i < valores.length - 1) {
                linea.append(separador);
            }
        }
        writer.write(linea.toString());
        writer.newLine();
    }
}
