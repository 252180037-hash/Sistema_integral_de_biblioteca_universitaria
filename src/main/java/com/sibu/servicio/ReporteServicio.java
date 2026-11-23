package com.sibu.servicio;

import com.sibu.dao.ReporteDAO;
import com.sibu.modelo.Prestamo;
import com.sibu.modelo.ReporteDisponibilidadMateria;
import com.sibu.modelo.ReporteLibroRanking;
import com.sibu.modelo.Sancion;
import com.sibu.servicio.exception.ValidacionException;
import com.sibu.util.CsvExporter;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de lógica de negocio para la generación, análisis y exportación
 * de los reportes del sistema (RF5).
 * <p>
 * Transforma los datos analíticos en representaciones tabulares para la vista Swing
 * y permite la exportación a formato CSV con codificación UTF-8 compatible con Microsoft Excel.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class ReporteServicio {

    private final ReporteDAO reporteDAO;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReporteServicio() {
        this.reporteDAO = new ReporteDAO();
    }

    public ReporteServicio(ReporteDAO reporteDAO) {
        this.reporteDAO = reporteDAO;
    }

    // =========================================================================
    // 1. REPORTE: PRÉSTAMOS POR PERIODO
    // =========================================================================

    public String[] getEncabezadosPrestamosPeriodo() {
        return new String[]{"ID Préstamo", "Ejemplar", "Título del Libro", "Identificación Lector", "Nombre Lector", "Fecha Préstamo", "Fecha Límite", "Fecha Devolución", "Estado"};
    }

    public List<String[]> generarDatosPrestamosPeriodo(LocalDate inicio, LocalDate fin) throws ValidacionException, SQLException {
        if (inicio == null || fin == null) {
            throw new ValidacionException("Debe especificar la fecha de inicio y la fecha de fin.");
        }
        if (inicio.isAfter(fin)) {
            throw new ValidacionException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        List<Prestamo> lista = reporteDAO.obtenerPrestamosPorPeriodo(inicio, fin);
        List<String[]> filas = new ArrayList<>();

        for (Prestamo p : lista) {
            filas.add(new String[]{
                    String.valueOf(p.getIdPrestamo()),
                    p.getCodigoInventario(),
                    p.getTituloLibro(),
                    p.getIdentificacionLector(),
                    p.getNombreLector(),
                    p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(FORMATO_FECHA_HORA) : "",
                    p.getFechaLimite() != null ? p.getFechaLimite().format(FORMATO_FECHA) : "",
                    p.getFechaDevolucion() != null ? p.getFechaDevolucion().format(FORMATO_FECHA_HORA) : "Pendiente",
                    p.getEstado().getEtiqueta()
            });
        }
        return filas;
    }

    // =========================================================================
    // 2. REPORTE: PRÉSTAMOS VENCIDOS ACTUALMENTE
    // =========================================================================

    public String[] getEncabezadosPrestamosVencidos() {
        return new String[]{"ID Préstamo", "Ejemplar", "Título del Libro", "Identificación Lector", "Nombre Lector", "Email Lector", "Fecha Préstamo", "Fecha Límite", "Días Atraso", "Multa Estimada ($)"};
    }

    public List<String[]> generarDatosPrestamosVencidos(double multaDiaria) throws SQLException {
        List<Prestamo> lista = reporteDAO.obtenerPrestamosVencidos();
        List<String[]> filas = new ArrayList<>();

        for (Prestamo p : lista) {
            long diasAtraso = p.calcularDiasAtraso();
            double multaEstimada = diasAtraso * multaDiaria;

            filas.add(new String[]{
                    String.valueOf(p.getIdPrestamo()),
                    p.getCodigoInventario(),
                    p.getTituloLibro(),
                    p.getIdentificacionLector(),
                    p.getNombreLector(),
                    p.getEmailLector(),
                    p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(FORMATO_FECHA_HORA) : "",
                    p.getFechaLimite() != null ? p.getFechaLimite().format(FORMATO_FECHA) : "",
                    String.valueOf(diasAtraso),
                    String.format("%.2f", multaEstimada)
            });
        }
        return filas;
    }

    // =========================================================================
    // 3. REPORTE: RANKING DE LIBROS MÁS PRESTADOS
    // =========================================================================

    public String[] getEncabezadosRankingLibros() {
        return new String[]{"Posición", "ISBN", "Título de la Obra", "Autor", "Materia", "Total Préstamos", "Copias Físicas", "Disponibles"};
    }

    public List<String[]> generarDatosRankingLibros(int limite) throws SQLException {
        List<ReporteLibroRanking> lista = reporteDAO.obtenerRankingLibrosMasPrestados(limite);
        List<String[]> filas = new ArrayList<>();
        int posicion = 1;

        for (ReporteLibroRanking item : lista) {
            filas.add(new String[]{
                    "#" + (posicion++),
                    item.getIsbn(),
                    item.getTitulo(),
                    item.getAutor(),
                    item.getMateria(),
                    String.valueOf(item.getTotalPrestamos()),
                    String.valueOf(item.getEjemplaresTotales()),
                    String.valueOf(item.getEjemplaresDisponibles())
            });
        }
        return filas;
    }

    // =========================================================================
    // 4. REPORTE: USUARIOS SANCIONADOS (ACTIVOS E HISTÓRICOS)
    // =========================================================================

    public String[] getEncabezadosUsuariosSancionados() {
        return new String[]{"ID Sanción", "Identificación", "Nombre del Usuario", "Email", "Ejemplar", "Días Retraso", "Monto ($)", "Motivo", "Estado", "Fecha Generación", "Fecha Pago"};
    }

    public List<String[]> generarDatosUsuariosSancionados() throws SQLException {
        List<Sancion> lista = reporteDAO.obtenerUsuariosSancionados();
        List<String[]> filas = new ArrayList<>();

        for (Sancion s : lista) {
            filas.add(new String[]{
                    String.valueOf(s.getIdSancion()),
                    s.getIdentificacionUsuario(),
                    s.getNombreUsuario(),
                    s.getEmailUsuario(),
                    s.getCodigoInventario() != null ? s.getCodigoInventario() : "N/A",
                    String.valueOf(s.getDiasRetraso()),
                    String.format("%.2f", s.getMonto()),
                    s.getMotivo(),
                    s.getEstado().getEtiqueta(),
                    s.getFechaGeneracion() != null ? s.getFechaGeneracion().format(FORMATO_FECHA_HORA) : "",
                    s.getFechaPago() != null ? s.getFechaPago().format(FORMATO_FECHA_HORA) : "Sin liquidar"
            });
        }
        return filas;
    }

    // =========================================================================
    // 5. REPORTE: DISPONIBILIDAD DE EJEMPLARES POR MATERIA
    // =========================================================================

    public String[] getEncabezadosDisponibilidadMateria() {
        return new String[]{"Materia / Área", "Títulos Distintos", "Copias Totales", "Disponibles", "Prestados", "Dañados / Baja", "% Disponibilidad"};
    }

    public List<String[]> generarDatosDisponibilidadMateria() throws SQLException {
        List<ReporteDisponibilidadMateria> lista = reporteDAO.obtenerDisponibilidadPorMateria();
        List<String[]> filas = new ArrayList<>();

        for (ReporteDisponibilidadMateria r : lista) {
            filas.add(new String[]{
                    r.getMateria(),
                    String.valueOf(r.getTotalTitulos()),
                    String.valueOf(r.getTotalEjemplares()),
                    String.valueOf(r.getDisponibles()),
                    String.valueOf(r.getPrestados()),
                    String.valueOf(r.getNoDisponibles()),
                    String.format("%.1f %%", r.getPorcentajeDisponibilidad())
            });
        }
        return filas;
    }

    // =========================================================================
    // EXPORTACIÓN A ARCHIVO CSV
    // =========================================================================

    /**
     * Exporta cualquier conjunto de datos reportable directamente a un archivo CSV.
     * 
     * @param encabezados Títulos de las columnas.
     * @param datos Filas procesadas.
     * @param archivoDestino Archivo físico .csv de destino.
     * @return Total de filas exportadas.
     * @throws IOException Si ocurre un fallo en la escritura en disco.
     */
    public int exportarReporteCsv(String[] encabezados, List<String[]> datos, File archivoDestino) throws IOException {
        return CsvExporter.exportarDatos(encabezados, datos, archivoDestino);
    }
}
