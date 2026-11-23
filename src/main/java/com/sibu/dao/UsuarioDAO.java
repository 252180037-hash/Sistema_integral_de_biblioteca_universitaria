package com.sibu.dao;

import com.sibu.modelo.EstadoUsuario;
import com.sibu.modelo.RolUsuario;
import com.sibu.modelo.Usuario;
import com.sibu.util.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos (DAO) para la entidad {@link Usuario}.
 * <p>
 * Implementa el requisito funcional <b>RF2 (Gestión de usuarios y roles)</b> y <b>RF6 (Seguridad de acceso)</b>.
 * Cumple estrictamente con el uso exclusivo de {@link PreparedStatement} parametrizados para mitigar
 * riesgos de inyección SQL (ISO/IEC 25010:2023 - Seguridad).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class UsuarioDAO {

    /**
     * Inserta un nuevo usuario en la base de datos dentro de una transacción activa.
     * 
     * @param u Objeto {@link Usuario} con los datos a persistir.
     * @param con Conexión JDBC activa bajo control transaccional.
     * @return El identificador autogenerado asignado al usuario.
     * @throws SQLException Si ocurre un error de integridad o comunicación.
     */
    public int insertar(Usuario u, Connection con) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, apellido, identificacion, email, telefono, "
                   + "password_hash, salt, rol, estado, fecha_registro) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getIdentificacion());
            ps.setString(4, u.getEmail().toLowerCase().trim());
            ps.setString(5, u.getTelefono());
            ps.setString(6, u.getPasswordHash());
            ps.setString(7, u.getSalt());
            ps.setString(8, u.getRol().name());
            ps.setString(9, u.getEstado().name());
            ps.setTimestamp(10, u.getFechaRegistro() != null 
                    ? Timestamp.valueOf(u.getFechaRegistro()) 
                    : new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    u.setIdUsuario(idGenerado);
                    return idGenerado;
                }
            }
        }
        return -1;
    }

    /**
     * Inserta un nuevo usuario utilizando la conexión gestionada por defecto.
     */
    public int insertar(Usuario u) throws SQLException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        return insertar(u, con);
    }

    /**
     * Actualiza la información personal, rol y estado de un usuario.
     * No modifica la contraseña ni el salt.
     * 
     * @param u Objeto {@link Usuario} con las modificaciones.
     * @param con Conexión JDBC activa.
     * @return {@code true} si se actualizó exactamente un registro.
     * @throws SQLException Si ocurre un error en la ejecución.
     */
    public boolean actualizar(Usuario u, Connection con) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, identificacion = ?, "
                   + "email = ?, telefono = ?, rol = ?, estado = ? "
                   + "WHERE id_usuario = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getIdentificacion());
            ps.setString(4, u.getEmail().toLowerCase().trim());
            ps.setString(5, u.getTelefono());
            ps.setString(6, u.getRol().name());
            ps.setString(7, u.getEstado().name());
            ps.setInt(8, u.getIdUsuario());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Usuario u) throws SQLException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        return actualizar(u, con);
    }

    /**
     * Actualiza de forma exclusiva el estado operativo del usuario (ACTIVO, INACTIVO, SANCIONADO).
     */
    public boolean actualizarEstado(int idUsuario, EstadoUsuario nuevoEstado, Connection con) throws SQLException {
        String sql = "UPDATE usuarios SET estado = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza la contraseña encriptada y su correspondiente salt.
     */
    public boolean actualizarPassword(int idUsuario, String nuevoHash, String nuevoSalt, Connection con) throws SQLException {
        String sql = "UPDATE usuarios SET password_hash = ?, salt = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoHash);
            ps.setString(2, nuevoSalt);
            ps.setInt(3, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina físicamente un usuario por su identificador.
     */
    public boolean eliminar(int idUsuario, Connection con) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Busca un usuario por su identificador primario.
     */
    public Usuario buscarPorId(int idUsuario) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    /**
     * Busca un usuario por su dirección de correo electrónico (utilizado en Login).
     */
    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE LOWER(email) = ?";
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    /**
     * Busca un usuario por su identificación oficial (cédula / matrícula).
     */
    public Usuario buscarPorIdentificacion(String identificacion) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE identificacion = ?";
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, identificacion.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lista la totalidad de los usuarios registrados ordenados alfabéticamente.
     */
    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT * FROM usuarios ORDER BY apellido, nombre";
        List<Usuario> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        }
        return lista;
    }

    /**
     * Filtra usuarios por coincidencia en nombre, apellido, identificación o email.
     */
    public List<Usuario> buscarPorCriterio(String criterio) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE LOWER(nombre) LIKE ? "
                   + "OR LOWER(apellido) LIKE ? OR identificacion LIKE ? OR LOWER(email) LIKE ? "
                   + "ORDER BY apellido, nombre";
        List<Usuario> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String parametro = "%" + criterio.trim().toLowerCase() + "%";
            ps.setString(1, parametro);
            ps.setString(2, parametro);
            ps.setString(3, parametro);
            ps.setString(4, parametro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearUsuario(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Cuenta la cantidad de préstamos activos (no devueltos) que posee un usuario actualmente.
     */
    public int contarPrestamosActivos(int idUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_usuario = ? AND estado IN ('ACTIVO', 'VENCIDO')";
        Connection con = ConexionDB.getInstancia().obtenerConexion();
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

    /**
     * Mapea un registro de {@link ResultSet} a una instancia de {@link Usuario}.
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setIdentificacion(rs.getString("identificacion"));
        u.setEmail(rs.getString("email"));
        u.setTelefono(rs.getString("telefono"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setRol(RolUsuario.valueOf(rs.getString("rol")));
        u.setEstado(EstadoUsuario.valueOf(rs.getString("estado")));

        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            u.setFechaRegistro(ts.toLocalDateTime());
        }
        return u;
    }
}
