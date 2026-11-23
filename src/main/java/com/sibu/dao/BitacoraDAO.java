package com.sibu.dao;

import com.sibu.modelo.AccionBitacora;
import com.sibu.modelo.Bitacora;
import com.sibu.util.ConexionDB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Objeto de acceso a datos (DAO) para la bitácora de auditoría y trazabilidad {@link Bitacora}.
 * <p>
 * Implementa <b>RF7 (Bitácora de operaciones)</b>. Registra de forma inmutable quién, cuándo,
 * desde qué módulo y qué acción se ejecutó en el sistema.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class BitacoraDAO {

    private static final Logger LOGGER = Logger.getLogger(BitacoraDAO.class.getName());

    private static final String QUERY_BASE = 
            "SELECT b.*, "
          + "  u.identificacion AS usuario_identificacion, "
          + "  CONCAT(u.nombre, ' ', u.apellido) AS usuario_nombre, "
          + "  u.email AS usuario_email, "
          + "  u.rol AS usuario_rol "
          + "FROM bitacora b "
          + "LEFT JOIN usuarios u ON b.id_usuario = u.id_usuario ";

    /**
     * Registra un nuevo evento de auditoría dentro de una transacción activa.
     */
    public int registrar(Bitacora b, Connection con) throws SQLException {
        String sql = "INSERT INTO bitacora (id_usuario, modulo, accion, detalle, ip_host, fecha) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (b.getIdUsuario() != null) {
                ps.setInt(1, b.getIdUsuario());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, b.getModulo());
            ps.setString(3, b.getAccion().name());
            ps.setString(4, b.getDetalle());
            ps.setString(5, (b.getIpHost() != null && !b.getIpHost().isEmpty()) ? b.getIpHost() : "LOCALHOST");
            ps.setTimestamp(6, b.getFecha() != null 
                    ? Timestamp.valueOf(b.getFecha()) 
                    : new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    b.setIdBitacora(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Registra un evento de auditoría utilizando una conexión independiente o gestionada
     * para no alterar transacciones en curso.
     */
    public void registrar(Bitacora b) {
        try {
            Connection con = ConexionDB.getInstancia().obtenerConexion();
            registrar(b, con);
        } catch (SQLException e) {
            // El fallo de bitácora no debe tumbar el flujo funcional principal, pero sí registrarse
            LOGGER.log(Level.SEVERE, "Fallo al registrar entrada en bitácora de auditoría.", e);
        }
    }

    /**
     * Lista los registros más recientes de la bitácora con un límite especificado.
     */
    public List<Bitacora> listarRecientes(int limite) throws SQLException {
        String sql = QUERY_BASE + "ORDER BY b.fecha DESC LIMIT ?";
        List<Bitacora> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearBitacora(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Filtra la bitácora de operaciones por módulo, acción y rango de fechas.
     */
    public List<Bitacora> filtrar(String modulo, AccionBitacora accion, LocalDate inicio, LocalDate fin) throws SQLException {
        StringBuilder sql = new StringBuilder(QUERY_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (modulo != null && !modulo.trim().isEmpty() && !"TODOS".equalsIgnoreCase(modulo.trim())) {
            sql.append("AND b.modulo = ? ");
            params.add(modulo.trim().toUpperCase());
        }
        if (accion != null) {
            sql.append("AND b.accion = ? ");
            params.add(accion.name());
        }
        if (inicio != null) {
            sql.append("AND DATE(b.fecha) >= ? ");
            params.add(Date.valueOf(inicio));
        }
        if (fin != null) {
            sql.append("AND DATE(b.fecha) <= ? ");
            params.add(Date.valueOf(fin));
        }

        sql.append("ORDER BY b.fecha DESC");

        List<Bitacora> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearBitacora(rs));
                }
            }
        }
        return lista;
    }

    private Bitacora mapearBitacora(ResultSet rs) throws SQLException {
        Bitacora b = new Bitacora();
        b.setIdBitacora(rs.getInt("id_bitacora"));

        int idUser = rs.getInt("id_usuario");
        if (!rs.wasNull()) {
            b.setIdUsuario(idUser);
        }

        b.setModulo(rs.getString("modulo"));
        b.setAccion(AccionBitacora.valueOf(rs.getString("accion")));
        b.setDetalle(rs.getString("detalle"));
        b.setIpHost(rs.getString("ip_host"));

        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) {
            b.setFecha(ts.toLocalDateTime());
        }

        b.setIdentificacionUsuario(rs.getString("usuario_identificacion"));
        b.setNombreUsuario(rs.getString("usuario_nombre"));
        b.setEmailUsuario(rs.getString("usuario_email"));
        b.setRolUsuario(rs.getString("usuario_rol"));

        return b;
    }
}
