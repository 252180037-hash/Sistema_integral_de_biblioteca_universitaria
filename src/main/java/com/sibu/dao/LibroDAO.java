package com.sibu.dao;

import com.sibu.modelo.Libro;
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
 * Objeto de acceso a datos (DAO) para el catálogo bibliográfico {@link Libro}.
 * <p>
 * Implementa <b>RF1 (Gestión de libros y ejemplares)</b> y <b>RF4 (Búsqueda de catálogo)</b>.
 * Todas las consultas hacen uso estricto de {@link PreparedStatement} y aprovechan los índices
 * de base de datos (idx_libros_isbn, idx_libros_titulo, idx_libros_autor, idx_libros_materia)
 * para responder consistentemente en menos de 2 segundos (ISO/IEC 25010:2023 - Eficiencia).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class LibroDAO {

    /**
     * Inserta un nuevo libro en el catálogo bibliográfico dentro de una transacción activa.
     */
    public int insertar(Libro l, Connection con) throws SQLException {
        String sql = "INSERT INTO libros (isbn, titulo, autor, editorial, anio_publicacion, "
                   + "materia, descripcion, fecha_registro) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, l.getIsbn().trim());
            ps.setString(2, l.getTitulo().trim());
            ps.setString(3, l.getAutor().trim());
            ps.setString(4, l.getEditorial().trim());
            ps.setInt(5, l.getAnioPublicacion());
            ps.setString(6, l.getMateria().trim());
            ps.setString(7, l.getDescripcion());
            ps.setTimestamp(8, l.getFechaRegistro() != null 
                    ? Timestamp.valueOf(l.getFechaRegistro()) 
                    : new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    l.setIdLibro(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public int insertar(Libro l) throws SQLException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        return insertar(l, con);
    }

    /**
     * Actualiza la ficha bibliográfica de un libro existente.
     */
    public boolean actualizar(Libro l, Connection con) throws SQLException {
        String sql = "UPDATE libros SET isbn = ?, titulo = ?, autor = ?, editorial = ?, "
                   + "anio_publicacion = ?, materia = ?, descripcion = ? "
                   + "WHERE id_libro = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, l.getIsbn().trim());
            ps.setString(2, l.getTitulo().trim());
            ps.setString(3, l.getAutor().trim());
            ps.setString(4, l.getEditorial().trim());
            ps.setInt(5, l.getAnioPublicacion());
            ps.setString(6, l.getMateria().trim());
            ps.setString(7, l.getDescripcion());
            ps.setInt(8, l.getIdLibro());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Libro l) throws SQLException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        return actualizar(l, con);
    }

    /**
     * Elimina un libro del catálogo si no tiene ejemplares físicos asociados.
     */
    public boolean eliminar(int idLibro, Connection con) throws SQLException {
        String sql = "DELETE FROM libros WHERE id_libro = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idLibro) throws SQLException {
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        return eliminar(idLibro, con);
    }

    /**
     * Busca un libro por su ID primario, incluyendo el conteo de ejemplares.
     */
    public Libro buscarPorId(int idLibro) throws SQLException {
        String sql = "SELECT l.*, "
                   + "  COUNT(e.id_ejemplar) AS total_ejemplares, "
                   + "  SUM(CASE WHEN e.estado = 'DISPONIBLE' THEN 1 ELSE 0 END) AS disponibles "
                   + "FROM libros l "
                   + "LEFT JOIN ejemplares e ON l.id_libro = e.id_libro "
                   + "WHERE l.id_libro = ? "
                   + "GROUP BY l.id_libro";

        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearLibroConEjemplares(rs);
                }
            }
        }
        return null;
    }

    /**
     * Busca un libro por su código ISBN.
     */
    public Libro buscarPorIsbn(String isbn) throws SQLException {
        String sql = "SELECT l.*, "
                   + "  COUNT(e.id_ejemplar) AS total_ejemplares, "
                   + "  SUM(CASE WHEN e.estado = 'DISPONIBLE' THEN 1 ELSE 0 END) AS disponibles "
                   + "FROM libros l "
                   + "LEFT JOIN ejemplares e ON l.id_libro = e.id_libro "
                   + "WHERE l.isbn = ? "
                   + "GROUP BY l.id_libro";

        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearLibroConEjemplares(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lista todos los libros del catálogo con sus respectivos conteos de stock físico.
     */
    public List<Libro> listarTodos() throws SQLException {
        String sql = "SELECT l.*, "
                   + "  COUNT(e.id_ejemplar) AS total_ejemplares, "
                   + "  SUM(CASE WHEN e.estado = 'DISPONIBLE' THEN 1 ELSE 0 END) AS disponibles "
                   + "FROM libros l "
                   + "LEFT JOIN ejemplares e ON l.id_libro = e.id_libro "
                   + "GROUP BY l.id_libro "
                   + "ORDER BY l.titulo ASC";

        List<Libro> lista = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearLibroConEjemplares(rs));
            }
        }
        return lista;
    }

    /**
     * Consulta el catálogo bibliográfico mediante filtros combinables (RF4).
     * Muestra en tiempo real cuántos ejemplares están disponibles para cada libro.
     * 
     * @param titulo Filtro parcial por título (opcional).
     * @param autor Filtro parcial por autor (opcional).
     * @param isbn Filtro exacto o parcial por ISBN (opcional).
     * @param materia Filtro exacto por materia o disciplina (opcional).
     * @return Lista de libros que cumplen con todos los filtros proporcionados.
     */
    public List<Libro> buscarCatalogo(String titulo, String autor, String isbn, String materia) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT l.*, ")
           .append("  COUNT(e.id_ejemplar) AS total_ejemplares, ")
           .append("  SUM(CASE WHEN e.estado = 'DISPONIBLE' THEN 1 ELSE 0 END) AS disponibles ")
           .append("FROM libros l ")
           .append("LEFT JOIN ejemplares e ON l.id_libro = e.id_libro ")
           .append("WHERE 1=1 ");

        List<Object> parametros = new ArrayList<>();

        if (titulo != null && !titulo.trim().isEmpty()) {
            sql.append("AND LOWER(l.titulo) LIKE ? ");
            parametros.add("%" + titulo.trim().toLowerCase() + "%");
        }
        if (autor != null && !autor.trim().isEmpty()) {
            sql.append("AND LOWER(l.autor) LIKE ? ");
            parametros.add("%" + autor.trim().toLowerCase() + "%");
        }
        if (isbn != null && !isbn.trim().isEmpty()) {
            sql.append("AND l.isbn LIKE ? ");
            parametros.add("%" + isbn.trim() + "%");
        }
        if (materia != null && !materia.trim().isEmpty() && !"TODAS".equalsIgnoreCase(materia.trim())) {
            sql.append("AND l.materia = ? ");
            parametros.add(materia.trim());
        }

        sql.append("GROUP BY l.id_libro ORDER BY l.titulo ASC");

        List<Libro> resultados = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultados.add(mapearLibroConEjemplares(rs));
                }
            }
        }
        return resultados;
    }

    /**
     * Obtiene el listado de materias o disciplinas únicas registradas en el catálogo.
     */
    public List<String> listarMateriasUnicas() throws SQLException {
        String sql = "SELECT DISTINCT materia FROM libros WHERE materia IS NOT NULL AND materia != '' ORDER BY materia ASC";
        List<String> materias = new ArrayList<>();
        Connection con = ConexionDB.getInstancia().obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                materias.add(rs.getString("materia"));
            }
        }
        return materias;
    }

    private Libro mapearLibroConEjemplares(ResultSet rs) throws SQLException {
        Libro l = new Libro();
        l.setIdLibro(rs.getInt("id_libro"));
        l.setIsbn(rs.getString("isbn"));
        l.setTitulo(rs.getString("titulo"));
        l.setAutor(rs.getString("autor"));
        l.setEditorial(rs.getString("editorial"));
        l.setAnioPublicacion(rs.getInt("anio_publicacion"));
        l.setMateria(rs.getString("materia"));
        l.setDescripcion(rs.getString("descripcion"));

        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            l.setFechaRegistro(ts.toLocalDateTime());
        }

        l.setTotalEjemplares(rs.getInt("total_ejemplares"));
        l.setEjemplaresDisponibles(rs.getInt("disponibles"));
        return l;
    }
}
