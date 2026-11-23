package com.sibu.dao;

import com.sibu.modelo.EstadoPrestamo;
import com.sibu.modelo.Prestamo;
import com.sibu.util.ConexionDB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos (DAO) para el módulo de circulación {@link Prestamo}.
 * <p>
 * Implementa <b>RF3 (Módulo de circulación: Préstamo, Devolución y Renovación)</b>.
 * Requiere soporte transaccional explícito para operar en conjunto con {@link EjemplarDAO}
 * y {@link SancionDAO} (ISO/IEC 25010:2023 - Fiabilidad).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class PrestamoDAO {

    private static final String QUERY_BASE = 
            "SELECT p.*, "
          + "  u.identificacion AS lector_identificacion, "
          + "  CONCAT(u.nombre, ' ', u.apellido) AS lector_nombre, "
          + "  u.email AS lector_email, "
          + "  e.codigo_inventario AS ejemplar_codigo, "
          + "  l.titulo AS libro_titulo, "
          + "  l.isbn AS libro_isbn, "
          + "  CONCAT(b.nombre, ' ', b.apellido) AS bibliotecario_nombre "
          + "FROM prestamos p "
          + "INNER JOIN usuarios u ON p.id_usuario = u.id_usuario "
          + "INNER JOIN ejemplares e ON p.id_ejemplar = e.id_ejemplar "
          + "INNER JOIN libros l ON e.id_libro = l.id_libro "
          + "INNER JOIN usuarios b ON p.id_bibliotecario = b.id_usuario ";

    /**
     * Registra un nuevo préstamo dentro de una transacción activa.
     */
    public int insertar(Prestamo p, Connection con) throws SQLException {
        String sql = "INSERT INTO prestamos (id_usuario, id_ejemplar, id_bibliotecario, "
                   + "fecha_prestamo, fecha_limite, estado, renovaciones, observaciones) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getIdUsuario());
            ps.setInt(2, p.getIdEjemplar());
            ps.setInt(3, p.getIdBibliotecario());
            ps.setTimestamp(4, p.getFechaPrestamo() != null 
                    ? Timestamp.valueOf(p.getFechaPrestamo()) 
                    : new Timestamp(System.currentTimeMillis()));
            ps.setDate(5, Date.valueOf(p.getFechaLimite()));
            ps.setString(6, p.getEstado().name());
            ps.setInt(7, p.getRenovaciones());
            ps.setString(8, p.getObservaciones());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    p.setIdPrestamo(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Registra formalmente la devolución física de un ejemplar.
     * Actualiza la fecha real de retorno y el estado del préstamo.
     */
    public boolean registrarDevolucion(int idPrestamo, LocalDateTime fechaDevolucion, 
                                       EstadoPrestamo nuevoEstado, Connection con) throws SQLException {
        String sql = "UPDATE prestamos SET fecha_devolucion = ?, estado = ? WHERE id_prestamo = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fechaDevolucion));
            ps.setString(2, nuevoEstado.name());
            ps.setInt(3, idPrestamo);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Extiende la fecha límite de devolución e incrementa el contador de renovaciones.
     */
    public boolean renovarPrestamo(int idPrestamo, LocalDate nuevaFechaLimite, Connection con) throws SQLException {
        String sql = "UPDATE prestamos SET fecha_limite = ?, renovaciones = renovaciones + 1 WHERE id_prestamo = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(nuevaFechaLimite));
            ps.setInt(2, idPrestamo);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza el estado de vencimiento de préstamos que han rebasado la fecha límite.
     */
    public int actualizarPrestamosVencidos(Connection con) throws SQLException {
        String sql = "UPDATE prestamos SET estado = 'VENCIDO' "
                   + "WHERE estado = 'ACTIVO' AND fecha_limite < CURDATE()";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    /**
     * Busca un préstamo por su ID primario con todo su detalle de navegación.
     */
    public Prestamo buscarPorId(int idPrestamo) throws SQLException {
        String sql = QUERY_BASE + "WHERE p.id_prestamo = ?";
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPrestamo(rs);
                }
            }
        }
        return null;
    }

    /**
     * Busca el préstamo activo o vencido actualmente asociado a una copia física específica.
     */
    public Prestamo buscarPrestamoActivoPorEjemplar(int idEjemplar) throws SQLException {
        String sql = QUERY_BASE + "WHERE p.id_ejemplar = ? AND p.estado IN ('ACTIVO', 'VENCIDO')";
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEjemplar);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPrestamo(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lista todos los préstamos históricos y actuales ordenados por fecha descendente.
     */
    public List<Prestamo> listarTodos() throws SQLException {
        String sql = QUERY_BASE + "ORDER BY p.fecha_prestamo DESC";
        List<Prestamo> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearPrestamo(rs));
            }
        }
        return lista;
    }

    /**
     * Lista préstamos actualmente en curso (activos o vencidos pendientes de devolución).
     */
    public List<Prestamo> listarActivosYVencidos() throws SQLException {
        String sql = QUERY_BASE + "WHERE p.estado IN ('ACTIVO', 'VENCIDO') ORDER BY p.fecha_limite ASC";
        List<Prestamo> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearPrestamo(rs));
            }
        }
        return lista;
    }

    /**
     * Lista únicamente los préstamos que se encuentran vencidos actualmente (Reporte 2).
     */
    public List<Prestamo> listarVencidos() throws SQLException {
        String sql = QUERY_BASE + "WHERE (p.estado = 'VENCIDO' OR (p.estado = 'ACTIVO' AND p.fecha_limite < CURDATE())) "
                   + "ORDER BY p.fecha_limite ASC";
        List<Prestamo> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearPrestamo(rs));
            }
        }
        return lista;
    }

    /**
     * Lista el historial de préstamos perteneciente a un lector específico.
     */
    public List<Prestamo> listarPorUsuario(int idUsuario) throws SQLException {
        String sql = QUERY_BASE + "WHERE p.id_usuario = ? ORDER BY p.fecha_prestamo DESC";
        List<Prestamo> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPrestamo(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Consulta préstamos realizados dentro de un periodo de fechas determinado (Reporte 1).
     */
    public List<Prestamo> listarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = QUERY_BASE + "WHERE DATE(p.fecha_prestamo) BETWEEN ? AND ? ORDER BY p.fecha_prestamo DESC";
        List<Prestamo> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPrestamo(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Retorna la cantidad de libros que tiene actualmente en su poder un usuario.
     */
    public int contarPrestamosActivosDeUsuario(int idUsuario, Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_usuario = ? AND estado IN ('ACTIVO', 'VENCIDO')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setIdPrestamo(rs.getInt("id_prestamo"));
        p.setIdUsuario(rs.getInt("id_usuario"));
        p.setIdEjemplar(rs.getInt("id_ejemplar"));
        p.setIdBibliotecario(rs.getInt("id_bibliotecario"));

        Timestamp tsPrestamo = rs.getTimestamp("fecha_prestamo");
        if (tsPrestamo != null) {
            p.setFechaPrestamo(tsPrestamo.toLocalDateTime());
        }

        Date dLimite = rs.getDate("fecha_limite");
        if (dLimite != null) {
            p.setFechaLimite(dLimite.toLocalDate());
        }

        Timestamp tsDevolucion = rs.getTimestamp("fecha_devolucion");
        if (tsDevolucion != null) {
            p.setFechaDevolucion(tsDevolucion.toLocalDateTime());
        }

        p.setEstado(EstadoPrestamo.valueOf(rs.getString("estado")));
        p.setRenovaciones(rs.getInt("renovaciones"));
        p.setObservaciones(rs.getString("observaciones"));

        p.setIdentificacionLector(rs.getString("lector_identificacion"));
        p.setNombreLector(rs.getString("lector_nombre"));
        p.setEmailLector(rs.getString("lector_email"));
        p.setCodigoInventario(rs.getString("ejemplar_codigo"));
        p.setTituloLibro(rs.getString("libro_titulo"));
        p.setIsbnLibro(rs.getString("libro_isbn"));
        p.setNombreBibliotecario(rs.getString("bibliotecario_nombre"));

        return p;
    }
}
