package com.sibu.dao;

import com.sibu.modelo.Ejemplar;
import com.sibu.modelo.EstadoEjemplar;
import com.sibu.util.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos (DAO) para las copias físicas {@link Ejemplar}.
 * <p>
 * Implementa <b>RF1 (Gestión de ejemplares)</b> y <b>RF3 (Circulación)</b>.
 * Permite cambiar de estado atómicamente a DISPONIBLE, PRESTADO, DANADO o BAJA.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class EjemplarDAO {

    /**
     * Inserta un nuevo ejemplar físico en el inventario.
     */
    public int insertar(Ejemplar e, Connection con) throws SQLException {
        String sql = "INSERT INTO ejemplares (id_libro, codigo_inventario, ubicacion, estado, observaciones) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getIdLibro());
            ps.setString(2, e.getCodigoInventario().trim().toUpperCase());
            ps.setString(3, e.getUbicacion().trim());
            ps.setString(4, e.getEstado().name());
            ps.setString(5, e.getObservaciones());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    e.setIdEjemplar(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public int insertar(Ejemplar e) throws SQLException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        return insertar(e, con);
    }

    /**
     * Actualiza los datos de ubicación, estado y observaciones de un ejemplar.
     */
    public boolean actualizar(Ejemplar e, Connection con) throws SQLException {
        String sql = "UPDATE ejemplares SET id_libro = ?, codigo_inventario = ?, ubicacion = ?, "
                   + "estado = ?, observaciones = ? "
                   + "WHERE id_ejemplar = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, e.getIdLibro());
            ps.setString(2, e.getCodigoInventario().trim().toUpperCase());
            ps.setString(3, e.getUbicacion().trim());
            ps.setString(4, e.getEstado().name());
            ps.setString(5, e.getObservaciones());
            ps.setInt(6, e.getIdEjemplar());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Ejemplar e) throws SQLException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        return actualizar(e, con);
    }

    /**
     * Actualiza el estado de un ejemplar dentro de una transacción crítica (préstamo, devolución o baja).
     */
    public boolean actualizarEstado(int idEjemplar, EstadoEjemplar nuevoEstado, Connection con) throws SQLException {
        String sql = "UPDATE ejemplares SET estado = ? WHERE id_ejemplar = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idEjemplar);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarEstado(int idEjemplar, EstadoEjemplar nuevoEstado) throws SQLException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        return actualizarEstado(idEjemplar, nuevoEstado, con);
    }

    /**
     * Elimina físicamente un ejemplar si no está vinculado a préstamos históricos.
     */
    public boolean eliminar(int idEjemplar, Connection con) throws SQLException {
        String sql = "DELETE FROM ejemplares WHERE id_ejemplar = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEjemplar);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idEjemplar) throws SQLException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        return eliminar(idEjemplar, con);
    }

    /**
     * Busca un ejemplar por su clave primaria, incluyendo metadatos del libro.
     */
    public Ejemplar buscarPorId(int idEjemplar) throws SQLException {
        String sql = "SELECT e.*, l.titulo AS libro_titulo, l.isbn AS libro_isbn, l.autor AS libro_autor "
                   + "FROM ejemplares e "
                   + "INNER JOIN libros l ON e.id_libro = l.id_libro "
                   + "WHERE e.id_ejemplar = ?";

        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEjemplar);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEjemplar(rs);
                }
            }
        }
        return null;
    }

    /**
     * Busca un ejemplar por su código de barras o código de inventario único.
     */
    public Ejemplar buscarPorCodigo(String codigoInventario) throws SQLException {
        String sql = "SELECT e.*, l.titulo AS libro_titulo, l.isbn AS libro_isbn, l.autor AS libro_autor "
                   + "FROM ejemplares e "
                   + "INNER JOIN libros l ON e.id_libro = l.id_libro "
                   + "WHERE UPPER(e.codigo_inventario) = ?";

        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoInventario.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEjemplar(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lista todos los ejemplares asociados a un libro específico.
     */
    public List<Ejemplar> listarPorLibro(int idLibro) throws SQLException {
        String sql = "SELECT e.*, l.titulo AS libro_titulo, l.isbn AS libro_isbn, l.autor AS libro_autor "
                   + "FROM ejemplares e "
                   + "INNER JOIN libros l ON e.id_libro = l.id_libro "
                   + "WHERE e.id_libro = ? "
                   + "ORDER BY e.codigo_inventario ASC";

        List<Ejemplar> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEjemplar(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Lista todos los ejemplares físicos registrados en el inventario general.
     */
    public List<Ejemplar> listarTodos() throws SQLException {
        String sql = "SELECT e.*, l.titulo AS libro_titulo, l.isbn AS libro_isbn, l.autor AS libro_autor "
                   + "FROM ejemplares e "
                   + "INNER JOIN libros l ON e.id_libro = l.id_libro "
                   + "ORDER BY l.titulo ASC, e.codigo_inventario ASC";

        List<Ejemplar> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearEjemplar(rs));
            }
        }
        return lista;
    }

    /**
     * Cuenta cuántos ejemplares están disponibles para préstamo de un libro.
     */
    public int contarDisponiblesPorLibro(int idLibro) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ejemplares WHERE id_libro = ? AND estado = 'DISPONIBLE'";
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Ejemplar mapearEjemplar(ResultSet rs) throws SQLException {
        Ejemplar e = new Ejemplar();
        e.setIdEjemplar(rs.getInt("id_ejemplar"));
        e.setIdLibro(rs.getInt("id_libro"));
        e.setCodigoInventario(rs.getString("codigo_inventario"));
        e.setUbicacion(rs.getString("ubicacion"));
        e.setEstado(EstadoEjemplar.valueOf(rs.getString("estado")));
        e.setObservaciones(rs.getString("observaciones"));

        e.setTituloLibro(rs.getString("libro_titulo"));
        e.setIsbnLibro(rs.getString("libro_isbn"));
        e.setAutorLibro(rs.getString("libro_autor"));
        return e;
    }
}
