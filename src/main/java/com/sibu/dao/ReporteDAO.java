package com.sibu.dao;

import com.sibu.modelo.Prestamo;
import com.sibu.modelo.ReporteDisponibilidadMateria;
import com.sibu.modelo.ReporteLibroRanking;
import com.sibu.modelo.Sancion;
import com.sibu.util.ConexionDB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos especializado en la emisión analítica de los 5 reportes
 * del requisito funcional <b>RF5 (Generación de Reportes)</b>.
 * <p>
 * Optimizado con agregaciones SQL y agrupamientos nativos que aprovechan los índices
 * de base de datos para responder en menos de 2 segundos (ISO/IEC 25010:2023 - Eficiencia).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class ReporteDAO {

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final SancionDAO sancionDAO = new SancionDAO();

    /**
     * Reporte 1: Préstamos por periodo (rango de fechas de préstamo).
     */
    public List<Prestamo> obtenerPrestamosPorPeriodo(LocalDate inicio, LocalDate fin) throws SQLException {
        return prestamoDAO.listarPorRangoFechas(inicio, fin);
    }

    /**
     * Reporte 2: Préstamos vencidos actualmente sin devolución física.
     */
    public List<Prestamo> obtenerPrestamosVencidos() throws SQLException {
        return prestamoDAO.listarVencidos();
    }

    /**
     * Reporte 3: Ranking de libros más prestados en el sistema.
     * 
     * @param limite Cantidad máxima de títulos a incluir en el ranking.
     * @return Lista de libros ordenada descendentemente por cantidad total de préstamos.
     */
    public List<ReporteLibroRanking> obtenerRankingLibrosMasPrestados(int limite) throws SQLException {
        String sql = "SELECT l.id_libro, l.isbn, l.titulo, l.autor, l.materia, "
                   + "  COUNT(p.id_prestamo) AS total_prestamos, "
                   + "  COUNT(DISTINCT e.id_ejemplar) AS ejemplares_totales, "
                   + "  SUM(CASE WHEN e.estado = 'DISPONIBLE' THEN 1 ELSE 0 END) AS ejemplares_disponibles "
                   + "FROM libros l "
                   + "LEFT JOIN ejemplares e ON l.id_libro = e.id_libro "
                   + "LEFT JOIN prestamos p ON e.id_ejemplar = p.id_ejemplar "
                   + "GROUP BY l.id_libro "
                   + "ORDER BY total_prestamos DESC, l.titulo ASC "
                   + "LIMIT ?";

        List<ReporteLibroRanking> ranking = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite > 0 ? limite : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReporteLibroRanking item = new ReporteLibroRanking();
                    item.setIdLibro(rs.getInt("id_libro"));
                    item.setIsbn(rs.getString("isbn"));
                    item.setTitulo(rs.getString("titulo"));
                    item.setAutor(rs.getString("autor"));
                    item.setMateria(rs.getString("materia"));
                    item.setTotalPrestamos(rs.getInt("total_prestamos"));
                    item.setEjemplaresTotales(rs.getInt("ejemplares_totales"));
                    item.setEjemplaresDisponibles(rs.getInt("ejemplares_disponibles"));
                    ranking.add(item);
                }
            }
        }
        return ranking;
    }

    /**
     * Reporte 4: Usuarios sancionados (activos e históricos).
     */
    public List<Sancion> obtenerUsuariosSancionados() throws SQLException {
        return sancionDAO.listarTodas();
    }

    /**
     * Reporte 5: Disponibilidad de ejemplares agrupada por materia o disciplina.
     */
    public List<ReporteDisponibilidadMateria> obtenerDisponibilidadPorMateria() throws SQLException {
        String sql = "SELECT "
                   + "  l.materia, "
                   + "  COUNT(DISTINCT l.id_libro) AS total_titulos, "
                   + "  COUNT(e.id_ejemplar) AS total_ejemplares, "
                   + "  SUM(CASE WHEN e.estado = 'DISPONIBLE' THEN 1 ELSE 0 END) AS disponibles, "
                   + "  SUM(CASE WHEN e.estado = 'PRESTADO' THEN 1 ELSE 0 END) AS prestados, "
                   + "  SUM(CASE WHEN e.estado IN ('DANADO', 'BAJA') THEN 1 ELSE 0 END) AS no_disponibles "
                   + "FROM libros l "
                   + "LEFT JOIN ejemplares e ON l.id_libro = e.id_libro "
                   + "WHERE l.materia IS NOT NULL AND l.materia != '' "
                   + "GROUP BY l.materia "
                   + "ORDER BY total_ejemplares DESC, l.materia ASC";

        List<ReporteDisponibilidadMateria> reporte = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String materia = rs.getString("materia");
                int titulos = rs.getInt("total_titulos");
                int ejemplares = rs.getInt("total_ejemplares");
                int disponibles = rs.getInt("disponibles");
                int prestados = rs.getInt("prestados");
                int noDisponibles = rs.getInt("no_disponibles");

                reporte.add(new ReporteDisponibilidadMateria(
                        materia, titulos, ejemplares, disponibles, prestados, noDisponibles
                ));
            }
        }
        return reporte;
    }
}
