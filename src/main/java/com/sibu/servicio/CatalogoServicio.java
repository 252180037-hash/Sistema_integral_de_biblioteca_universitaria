package com.sibu.servicio;

import com.sibu.dao.BitacoraDAO;
import com.sibu.dao.EjemplarDAO;
import com.sibu.dao.LibroDAO;
import com.sibu.modelo.AccionBitacora;
import com.sibu.modelo.Bitacora;
import com.sibu.modelo.Ejemplar;
import com.sibu.modelo.EstadoEjemplar;
import com.sibu.modelo.Libro;
import com.sibu.servicio.exception.ValidacionException;
import com.sibu.util.ConexionDB;
import com.sibu.util.SeguridadUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de lógica de negocio para la gestión del catálogo bibliográfico y sus copias físicas.
 * <p>
 * Implementa <b>RF1 (Gestión de libros y ejemplares)</b> y <b>RF4 (Búsqueda de catálogo)</b>.
 * Asegura la consistencia del inventario mediante transacciones JDBC explícitas
 * y validaciones preventivas de ISBN y códigos de barra (ISO/IEC 25010:2023 - Fiabilidad).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class CatalogoServicio {

    private static final Logger LOGGER = Logger.getLogger(CatalogoServicio.class.getName());

    private final LibroDAO libroDAO;
    private final EjemplarDAO ejemplarDAO;
    private final BitacoraDAO bitacoraDAO;

    public CatalogoServicio() {
        this.libroDAO = new LibroDAO();
        this.ejemplarDAO = new EjemplarDAO();
        this.bitacoraDAO = new BitacoraDAO();
    }

    public CatalogoServicio(LibroDAO libroDAO, EjemplarDAO ejemplarDAO, BitacoraDAO bitacoraDAO) {
        this.libroDAO = libroDAO;
        this.ejemplarDAO = ejemplarDAO;
        this.bitacoraDAO = bitacoraDAO;
    }

    // =========================================================================
    // GESTIÓN DE LIBROS (TÍTULOS DEL CATÁLOGO)
    // =========================================================================

    /**
     * Registra un nuevo libro en el catálogo bibliográfico validando estructura de ISBN y unicidad.
     */
    public Libro registrarLibro(Libro libro, Integer idOperador) throws ValidacionException, SQLException {
        validarDatosLibro(libro);

        if (libroDAO.buscarPorIsbn(libro.getIsbn()) != null) {
            throw new ValidacionException("Ya existe un libro registrado con el ISBN: " + libro.getIsbn());
        }

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            int idGenerado = libroDAO.insertar(libro, con);
            libro.setIdLibro(idGenerado);

            Bitacora bit = new Bitacora(idOperador, "CATALOGO", AccionBitacora.INSERT,
                    "Alta de libro ID #" + idGenerado + ": \"" + libro.getTitulo() + "\" (ISBN: " + libro.getIsbn() + ")");
            bitacoraDAO.registrar(bit, con);

            con.commit();
            LOGGER.info("Libro registrado exitosamente con ID: " + idGenerado);
            return libro;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Fallo en rollback de registro de libro.", ex);
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Actualiza la ficha bibliográfica de un libro.
     */
    public boolean actualizarLibro(Libro libro, Integer idOperador) throws ValidacionException, SQLException {
        if (libro == null || libro.getIdLibro() == null) {
            throw new ValidacionException("El libro a actualizar no es válido.");
        }
        validarDatosLibro(libro);

        Libro existente = libroDAO.buscarPorIsbn(libro.getIsbn());
        if (existente != null && !existente.getIdLibro().equals(libro.getIdLibro())) {
            throw new ValidacionException("El ISBN " + libro.getIsbn() + " ya está asignado a otro libro.");
        }

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            boolean exito = libroDAO.actualizar(libro, con);

            Bitacora bit = new Bitacora(idOperador, "CATALOGO", AccionBitacora.UPDATE,
                    "Modificación de libro ID #" + libro.getIdLibro() + ": \"" + libro.getTitulo() + "\"");
            bitacoraDAO.registrar(bit, con);

            con.commit();
            return exito;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Fallo en rollback de actualización de libro.", ex);
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Elimina un libro del catálogo si no posee ejemplares físicos registrados.
     */
    public boolean eliminarLibro(int idLibro, Integer idOperador) throws ValidacionException, SQLException {
        List<Ejemplar> ejemplares = ejemplarDAO.listarPorLibro(idLibro);
        if (!ejemplares.isEmpty()) {
            throw new ValidacionException("No es posible eliminar el libro: tiene " + ejemplares.size() + " ejemplar(es) físico(s) en inventario. Debe dar de baja o eliminar primero sus copias.");
        }

        Libro libro = libroDAO.buscarPorId(idLibro);
        if (libro == null) {
            throw new ValidacionException("El libro no existe.");
        }

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            boolean exito = libroDAO.eliminar(idLibro, con);

            Bitacora bit = new Bitacora(idOperador, "CATALOGO", AccionBitacora.DELETE,
                    "Eliminación de libro ID #" + idLibro + ": \"" + libro.getTitulo() + "\"");
            bitacoraDAO.registrar(bit, con);

            con.commit();
            return exito;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Fallo en rollback de eliminación de libro.", ex);
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public List<Libro> listarTodosLosLibros() throws SQLException {
        return libroDAO.listarTodos();
    }

    public Libro buscarLibroPorId(int idLibro) throws SQLException {
        return libroDAO.buscarPorId(idLibro);
    }

    public List<Libro> consultarCatalogo(String titulo, String autor, String isbn, String materia) throws SQLException {
        return libroDAO.buscarCatalogo(titulo, autor, isbn, materia);
    }

    public List<String> obtenerMaterias() throws SQLException {
        return libroDAO.listarMateriasUnicas();
    }

    // =========================================================================
    // GESTIÓN DE EJEMPLARES FÍSICOS (COPIAS INDIVIDUALES)
    // =========================================================================

    /**
     * Registra un nuevo ejemplar físico asociado a un libro del catálogo.
     */
    public Ejemplar registrarEjemplar(Ejemplar ejemplar, Integer idOperador) throws ValidacionException, SQLException {
        validarDatosEjemplar(ejemplar);

        if (ejemplarDAO.buscarPorCodigo(ejemplar.getCodigoInventario()) != null) {
            throw new ValidacionException("Ya existe un ejemplar registrado con el código de inventario: " + ejemplar.getCodigoInventario());
        }

        Libro libro = libroDAO.buscarPorId(ejemplar.getIdLibro());
        if (libro == null) {
            throw new ValidacionException("El libro asociado no existe en el catálogo.");
        }

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            int idGenerado = ejemplarDAO.insertar(ejemplar, con);
            ejemplar.setIdEjemplar(idGenerado);

            Bitacora bit = new Bitacora(idOperador, "CATALOGO", AccionBitacora.INSERT,
                    "Ingreso de ejemplar #" + ejemplar.getCodigoInventario() + " para libro \"" + libro.getTitulo() + "\" en ubicación: " + ejemplar.getUbicacion());
            bitacoraDAO.registrar(bit, con);

            con.commit();
            LOGGER.info("Ejemplar registrado exitosamente con ID: " + idGenerado);
            return ejemplar;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Fallo en rollback de registro de ejemplar.", ex);
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Actualiza la ubicación, estado físico u observaciones de un ejemplar.
     */
    public boolean actualizarEjemplar(Ejemplar ejemplar, Integer idOperador) throws ValidacionException, SQLException {
        if (ejemplar == null || ejemplar.getIdEjemplar() == null) {
            throw new ValidacionException("El ejemplar a actualizar no es válido.");
        }
        validarDatosEjemplar(ejemplar);

        Ejemplar existente = ejemplarDAO.buscarPorCodigo(ejemplar.getCodigoInventario());
        if (existente != null && !existente.getIdEjemplar().equals(ejemplar.getIdEjemplar())) {
            throw new ValidacionException("El código " + ejemplar.getCodigoInventario() + " ya está asignado a otro ejemplar.");
        }

        Ejemplar actual = ejemplarDAO.buscarPorId(ejemplar.getIdEjemplar());
        if (actual != null && actual.getEstado() == EstadoEjemplar.PRESTADO && ejemplar.getEstado() == EstadoEjemplar.DISPONIBLE) {
            throw new ValidacionException("No puede cambiar el estado a DISPONIBLE manualmente: el ejemplar está actualmente prestado. Debe registrarse la devolución en el módulo de circulación.");
        }

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            boolean exito = ejemplarDAO.actualizar(ejemplar, con);

            Bitacora bit = new Bitacora(idOperador, "CATALOGO", AccionBitacora.UPDATE,
                    "Actualización de ejemplar #" + ejemplar.getCodigoInventario() + " (Estado: " + ejemplar.getEstado() + ")");
            bitacoraDAO.registrar(bit, con);

            con.commit();
            return exito;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Fallo en rollback de actualización de ejemplar.", ex);
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Da de baja o elimina un ejemplar físico siempre que no se encuentre prestado.
     */
    public boolean eliminarEjemplar(int idEjemplar, Integer idOperador) throws ValidacionException, SQLException {
        Ejemplar e = ejemplarDAO.buscarPorId(idEjemplar);
        if (e == null) {
            throw new ValidacionException("El ejemplar no existe.");
        }
        if (e.getEstado() == EstadoEjemplar.PRESTADO) {
            throw new ValidacionException("No es posible eliminar o dar de baja un ejemplar que se encuentra actualmente en calidad de PRESTADO.");
        }

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            boolean exito = ejemplarDAO.eliminar(idEjemplar, con);

            Bitacora bit = new Bitacora(idOperador, "CATALOGO", AccionBitacora.DELETE,
                    "Eliminación física de ejemplar #" + e.getCodigoInventario());
            bitacoraDAO.registrar(bit, con);

            con.commit();
            return exito;
        } catch (SQLException ex) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rEx) {
                    LOGGER.log(Level.SEVERE, "Fallo en rollback de eliminación de ejemplar.", rEx);
                }
            }
            throw ex;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public List<Ejemplar> listarEjemplaresPorLibro(int idLibro) throws SQLException {
        return ejemplarDAO.listarPorLibro(idLibro);
    }

    public Ejemplar buscarEjemplarPorCodigo(String codigo) throws SQLException {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        return ejemplarDAO.buscarPorCodigo(codigo);
    }

    // =========================================================================
    // VALIDACIONES PRIVADAS
    // =========================================================================

    private void validarDatosLibro(Libro l) throws ValidacionException {
        if (l == null) {
            throw new ValidacionException("Los datos del libro no pueden ser nulos.");
        }
        if (l.getTitulo() == null || l.getTitulo().trim().isEmpty()) {
            throw new ValidacionException("El título del libro es obligatorio.");
        }
        if (l.getAutor() == null || l.getAutor().trim().isEmpty()) {
            throw new ValidacionException("El autor del libro es obligatorio.");
        }
        if (l.getEditorial() == null || l.getEditorial().trim().isEmpty()) {
            throw new ValidacionException("La editorial es obligatoria.");
        }
        if (l.getMateria() == null || l.getMateria().trim().isEmpty()) {
            throw new ValidacionException("La materia o área del conocimiento es obligatoria.");
        }
        if (l.getAnioPublicacion() == null || l.getAnioPublicacion() < 1000 || l.getAnioPublicacion() > Year.now().getValue() + 1) {
            throw new ValidacionException("El año de publicación debe ser válido (entre 1000 y " + (Year.now().getValue() + 1) + ").");
        }
        if (!SeguridadUtil.esIsbnValido(l.getIsbn())) {
            throw new ValidacionException("El código ISBN ingresado no es válido (debe cumplir formato ISBN-10 o ISBN-13 estándar).");
        }

        l.setTitulo(SeguridadUtil.sanitizarTexto(l.getTitulo()));
        l.setAutor(SeguridadUtil.sanitizarTexto(l.getAutor()));
        l.setEditorial(SeguridadUtil.sanitizarTexto(l.getEditorial()));
        l.setMateria(SeguridadUtil.sanitizarTexto(l.getMateria()));
        l.setIsbn(l.getIsbn().replaceAll("[\\s-]", "").trim());
    }

    private void validarDatosEjemplar(Ejemplar e) throws ValidacionException {
        if (e == null) {
            throw new ValidacionException("Los datos del ejemplar no pueden ser nulos.");
        }
        if (e.getIdLibro() == null || e.getIdLibro() <= 0) {
            throw new ValidacionException("El ejemplar debe estar vinculado a un libro válido.");
        }
        if (e.getCodigoInventario() == null || e.getCodigoInventario().trim().isEmpty()) {
            throw new ValidacionException("El código de inventario o código de barras es obligatorio.");
        }
        if (e.getUbicacion() == null || e.getUbicacion().trim().isEmpty()) {
            throw new ValidacionException("La ubicación física (estante, nivel, anaquel) es obligatoria.");
        }
        if (e.getEstado() == null) {
            e.setEstado(EstadoEjemplar.DISPONIBLE);
        }

        e.setCodigoInventario(SeguridadUtil.sanitizarTexto(e.getCodigoInventario()).toUpperCase());
        e.setUbicacion(SeguridadUtil.sanitizarTexto(e.getUbicacion()));
        if (e.getObservaciones() != null) {
            e.setObservaciones(SeguridadUtil.sanitizarTexto(e.getObservaciones()));
        }
    }
}
