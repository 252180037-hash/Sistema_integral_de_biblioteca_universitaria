package com.sibu.dao;

import com.sibu.modelo.EstadoSancion;
import com.sibu.modelo.Sancion;
import com.sibu.util.ConexionDB;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos (DAO) para las sanciones y multas {@link Sancion}.
 * <p>
 * Implementa las reglas de sanción del <b>RF3</b> y la emisión del <b>Reporte 4 (RF5)</b>.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class SancionDAO {

    private static final String QUERY_BASE = 
            "SELECT s.*, "
          + "  u.identificacion AS usuario_identificacion, "
          + "  CONCAT(u.nombre, ' ', u.apellido) AS usuario_nombre, "
          + "  u.email AS usuario_email, "
          + "  e.codigo_inventario AS ejemplar_codigo, "
          + "  l.titulo AS libro_titulo "
          + "FROM sanciones s "
          + "INNER JOIN usuarios u ON s.id_usuario = u.id_usuario "
          + "INNER JOIN prestamos p ON s.id_prestamo = p.id_prestamo "
          + "INNER JOIN ejemplares e ON p.id_ejemplar = e.id_ejemplar "
          + "INNER JOIN libros l ON e.id_libro = l.id_libro ";

    /**
     * Inserta una nueva sanción económica calculada dentro de una transacción activa.
     */
    public int insertar(Sancion s, Connection con) throws SQLException {
        String sql = "INSERT INTO sanciones (id_prestamo, id_usuario, dias_retraso, "
                   + "monto, motivo, estado, fecha_generacion) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getIdPrestamo());
            ps.setInt(2, s.getIdUsuario());
            ps.setInt(3, s.getDiasRetraso());
            ps.setBigDecimal(4, s.getMonto());
            ps.setString(5, s.getMotivo());
            ps.setString(6, s.getEstado().name());
            ps.setTimestamp(7, s.getFechaGeneracion() != null 
                    ? Timestamp.valueOf(s.getFechaGeneracion()) 
                    : new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    s.setIdSancion(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Registra el pago en ventanilla de una sanción pendiente.
     */
    public boolean registrarPago(int idSancion, LocalDateTime fechaPago, Connection con) throws SQLException {
        String sql = "UPDATE sanciones SET estado = 'PAGADA', fecha_pago = ? WHERE id_sancion = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fechaPago));
            ps.setInt(2, idSancion);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Condonación formal de una sanción con registro justificado.
     */
    public boolean condonar(int idSancion, Connection con) throws SQLException {
        String sql = "UPDATE sanciones SET estado = 'CONDONADA', fecha_pago = NOW() WHERE id_sancion = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSancion);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Verifica de forma eficiente si un usuario tiene alguna sanción pendiente de pago.
     * Utiliza el índice idx_sanciones_usuario_estado para respuesta sub-milisegundo.
     */
    public boolean tieneSancionesPendientes(int idUsuario, Connection con) throws SQLException {
        String sql = "SELECT 1 FROM sanciones WHERE id_usuario = ? AND estado = 'PENDIENTE' LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean tieneSancionesPendientes(int idUsuario) throws SQLException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        return tieneSancionesPendientes(idUsuario, con);
    }

    /**
     * Calcula la sumatoria monetaria de multas impagas que adeuda un lector.
     */
    public BigDecimal calcularTotalMultasPendientes(int idUsuario) throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto), 0) FROM sanciones WHERE id_usuario = ? AND estado = 'PENDIENTE'";
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Busca una sanción por su identificador primario.
     */
    public Sancion buscarPorId(int idSancion) throws SQLException {
        String sql = QUERY_BASE + "WHERE s.id_sancion = ?";
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSancion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearSancion(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lista todas las sanciones registradas (activas e históricas) para el Reporte 4 (RF5).
     */
    public List<Sancion> listarTodas() throws SQLException {
        String sql = QUERY_BASE + "ORDER BY s.fecha_generacion DESC";
        List<Sancion> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearSancion(rs));
            }
        }
        return lista;
    }

    /**
     * Lista las sanciones vigentes pendientes de cobro.
     */
    public List<Sancion> listarPendientes() throws SQLException {
        String sql = QUERY_BASE + "WHERE s.estado = 'PENDIENTE' ORDER BY s.fecha_generacion DESC";
        List<Sancion> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearSancion(rs));
            }
        }
        return lista;
    }

    /**
     * Lista el historial de sanciones de un usuario en particular.
     */
    public List<Sancion> listarPorUsuario(int idUsuario) throws SQLException {
        String sql = QUERY_BASE + "WHERE s.id_usuario = ? ORDER BY s.fecha_generacion DESC";
        List<Sancion> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSancion(rs));
                }
            }
        }
        return lista;
    }

    private Sancion mapearSancion(ResultSet rs) throws SQLException {
        Sancion s = new Sancion();
        s.setIdSancion(rs.getInt("id_sancion"));
        s.setIdPrestamo(rs.getInt("id_prestamo"));
        s.setIdUsuario(rs.getInt("id_usuario"));
        s.setDiasRetraso(rs.getInt("dias_retraso"));
        s.setMonto(rs.getBigDecimal("monto"));
        s.setMotivo(rs.getString("motivo"));
        s.setEstado(EstadoSancion.valueOf(rs.getString("estado")));

        Timestamp tsGen = rs.getTimestamp("fecha_generacion");
        if (tsGen != null) {
            s.setFechaGeneracion(tsGen.toLocalDateTime());
        }

        Timestamp tsPago = rs.getTimestamp("fecha_pago");
        if (tsPago != null) {
            s.setFechaPago(tsPago.toLocalDateTime());
        }

        s.setIdentificacionUsuario(rs.getString("usuario_identificacion"));
        s.setNombreUsuario(rs.getString("usuario_nombre"));
        s.setEmailUsuario(rs.getString("usuario_email"));
        s.setCodigoInventario(rs.getString("ejemplar_codigo"));
        s.setTituloLibro(rs.getString("libro_titulo"));

        return s;
    }
}
