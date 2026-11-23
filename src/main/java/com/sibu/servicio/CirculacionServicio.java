package com.sibu.servicio;

import com.sibu.dao.BitacoraDAO;
import com.sibu.dao.EjemplarDAO;
import com.sibu.dao.PrestamoDAO;
import com.sibu.dao.SancionDAO;
import com.sibu.dao.UsuarioDAO;
import com.sibu.modelo.AccionBitacora;
import com.sibu.modelo.Bitacora;
import com.sibu.modelo.Ejemplar;
import com.sibu.modelo.EstadoEjemplar;
import com.sibu.modelo.EstadoPrestamo;
import com.sibu.modelo.EstadoSancion;
import com.sibu.modelo.EstadoUsuario;
import com.sibu.modelo.Prestamo;
import com.sibu.modelo.Sancion;
import com.sibu.modelo.Usuario;
import com.sibu.servicio.exception.CirculacionException;
import com.sibu.servicio.exception.ValidacionException;
import com.sibu.util.ConexionDB;
import com.sibu.util.Configuracion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de lógica de negocio transaccional para el módulo de circulación.
 * <p>
 * Implementa <b>RF3 (Módulo de circulación: Préstamo, Devolución y Renovación)</b>.
 * Cumple con el criterio de <b>Fiabilidad</b> y <b>Consistencia ACID</b> de la norma
 * <b>ISO/IEC 25010:2023</b> utilizando bloques try-catch con control estricto de
 * transacciones JDBC ({@code setAutoCommit(false)}, {@code commit()} y {@code rollback()}).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class CirculacionServicio {

    private static final Logger LOGGER = Logger.getLogger(CirculacionServicio.class.getName());

    private final PrestamoDAO prestamoDAO;
    private final EjemplarDAO ejemplarDAO;
    private final UsuarioDAO usuarioDAO;
    private final SancionDAO sancionDAO;
    private final BitacoraDAO bitacoraDAO;

    public CirculacionServicio() {
        this.prestamoDAO = new PrestamoDAO();
        this.ejemplarDAO = new EjemplarDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.sancionDAO = new SancionDAO();
        this.bitacoraDAO = new BitacoraDAO();
    }

    public CirculacionServicio(PrestamoDAO prestamoDAO, EjemplarDAO ejemplarDAO,
                               UsuarioDAO usuarioDAO, SancionDAO sancionDAO, BitacoraDAO bitacoraDAO) {
        this.prestamoDAO = prestamoDAO;
        this.ejemplarDAO = ejemplarDAO;
        this.usuarioDAO = usuarioDAO;
        this.sancionDAO = sancionDAO;
        this.bitacoraDAO = bitacoraDAO;
    }

    // =========================================================================
    // 1. REGISTRO DE PRÉSTAMO TRANSACCIONAL
    // =========================================================================

    /**
     * Registra un nuevo préstamo bibliotecario en una transacción atómica.
     * <p>
     * Reglas de validación previas:
     * <ul>
     *   <li>El lector debe existir y estar en estado ACTIVO.</li>
     *   <li>El lector no debe poseer multas o sanciones económicas pendientes.</li>
     *   <li>El lector no debe rebasar el límite permitido de préstamos simultáneos.</li>
     *   <li>El ejemplar físico debe existir y encontrarse en estado DISPONIBLE.</li>
     * </ul>
     * </p>
     * 
     * @param idLector Identificador del usuario lector que recibe el libro.
     * @param idEjemplar Identificador de la copia física a prestar.
     * @param idBibliotecario Identificador del operador de biblioteca que procesa el préstamo.
     * @param observaciones Notas o condiciones del ejemplar al salir.
     * @return El objeto {@link Prestamo} registrado con su ID generado.
     */
    public Prestamo registrarPrestamo(int idLector, int idEjemplar, int idBibliotecario, String observaciones)
            throws CirculacionException, ValidacionException, SQLException {

        Configuracion config = Configuracion.getInstancia();
        int maxLibrosPermitidos = config.getMaxLibrosPrestamo();
        int diasPrestamo = config.getDiasLimitePrestamo();

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false); // INICIO DE TRANSACCIÓN ATÓMICA

            // 1. Validar lector
            Usuario lector = usuarioDAO.buscarPorId(idLector);
            if (lector == null) {
                throw new ValidacionException("El lector indicado no existe en el sistema.");
            }
            if (lector.getEstado() == EstadoUsuario.INACTIVO) {
                throw new CirculacionException("El lector se encuentra INACTIVO y no puede recibir préstamos.");
            }
            if (lector.getEstado() == EstadoUsuario.SANCIONADO) {
                throw new CirculacionException("El lector se encuentra SANCIONADO. Debe saldar sus multas pendientes antes de solicitar nuevos libros.");
            }

            // 2. Validar que no tenga sanciones pendientes en BD
            if (sancionDAO.tieneSancionesPendientes(idLector, con)) {
                throw new CirculacionException("El lector registra sanciones pendientes de pago en el sistema.");
            }

            // 3. Validar límite de préstamos simultáneos
            int prestamosActivos = prestamoDAO.contarPrestamosActivosDeUsuario(idLector, con);
            if (prestamosActivos >= maxLibrosPermitidos) {
                throw new CirculacionException("El lector ya alcanzó el límite máximo de " 
                        + maxLibrosPermitidos + " libros prestados simultáneamente (actuales: " + prestamosActivos + ").");
            }

            // 4. Validar ejemplar
            Ejemplar ejemplar = ejemplarDAO.buscarPorId(idEjemplar);
            if (ejemplar == null) {
                throw new ValidacionException("El ejemplar especificado no existe.");
            }
            if (ejemplar.getEstado() != EstadoEjemplar.DISPONIBLE) {
                throw new CirculacionException("El ejemplar #" + ejemplar.getCodigoInventario() 
                        + " no está disponible para préstamo (Estado actual: " + ejemplar.getEstado().getEtiqueta() + ").");
            }

            // 5. Modificar estado del ejemplar a PRESTADO
            boolean ejemplarActualizado = ejemplarDAO.actualizarEstado(idEjemplar, EstadoEjemplar.PRESTADO, con);
            if (!ejemplarActualizado) {
                throw new SQLException("No fue posible actualizar el estado del ejemplar a PRESTADO.");
            }

            // 6. Crear el registro del préstamo
            LocalDate fechaLimite = LocalDate.now().plusDays(diasPrestamo);
            Prestamo prestamo = new Prestamo(idLector, idEjemplar, idBibliotecario, fechaLimite, observaciones);
            int idPrestamo = prestamoDAO.insertar(prestamo, con);
            prestamo.setIdPrestamo(idPrestamo);

            // 7. Registrar auditoría en bitácora
            Bitacora bit = new Bitacora(idBibliotecario, "CIRCULACION", AccionBitacora.PRESTAMO,
                    "Préstamo #" + idPrestamo + " registrado. Ejemplar [" + ejemplar.getCodigoInventario() 
                    + "] otorgado a " + lector.getNombreCompleto() + " (" + lector.getIdentificacion() 
                    + "). Vence el: " + fechaLimite);
            bitacoraDAO.registrar(bit, con);

            // CONFIRMACIÓN DE LA TRANSACCIÓN
            con.commit();
            LOGGER.info("Préstamo #" + idPrestamo + " procesado exitosamente.");
            return prestamo;

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                    LOGGER.warning("Transacción de préstamo revertida (rollback) debido a error: " + e.getMessage());
                } catch (SQLException rEx) {
                    LOGGER.log(Level.SEVERE, "Fallo al ejecutar rollback de préstamo.", rEx);
                }
            }
            if (e instanceof CirculacionException ce) throw ce;
            if (e instanceof ValidacionException ve) throw ve;
            if (e instanceof SQLException se) throw se;
            throw new SQLException("Error imprevisto durante el préstamo: " + e.getMessage(), e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // =========================================================================
    // 2. REGISTRO DE DEVOLUCIÓN Y CÁLCULO DE SANCIONES
    // =========================================================================

    /**
     * Registra la devolución de un libro en una transacción atómica.
     * <p>
     * Si la fecha real de devolución supera la fecha límite, calcula automáticamente
     * la sanción monetaria en función de los días de retraso, genera el registro de multa
     * y marca al lector en estado SANCIONADO.
     * </p>
     * 
     * @param idPrestamo Identificador del préstamo a liquidar.
     * @param idBibliotecario Operador que recibe el ejemplar.
     * @param observaciones Notas de recepción (si se reporta daño, el ejemplar se marca DANADO).
     * @return Objeto {@link Sancion} generado si existió retraso; {@code null} si entregó a tiempo.
     */
    public Sancion registrarDevolucion(int idPrestamo, int idBibliotecario, String observaciones)
            throws CirculacionException, ValidacionException, SQLException {

        Configuracion config = Configuracion.getInstancia();
        double multaPorDia = config.getMultaDiaria();

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false); // INICIO DE TRANSACCIÓN ATÓMICA

            // 1. Validar existencia y vigencia del préstamo
            Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);
            if (prestamo == null) {
                throw new ValidacionException("El préstamo indicado no existe.");
            }
            if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
                throw new CirculacionException("Este préstamo ya fue devuelto con anterioridad.");
            }

            LocalDateTime fechaActual = LocalDateTime.now();
            LocalDate fechaHoy = fechaActual.toLocalDate();
            LocalDate fechaLimite = prestamo.getFechaLimite();

            // 2. Determinar estado físico final del ejemplar (DISPONIBLE o DANADO)
            EstadoEjemplar estadoNuevoEjemplar = EstadoEjemplar.DISPONIBLE;
            if (observaciones != null && observaciones.toLowerCase().contains("dañ")) {
                estadoNuevoEjemplar = EstadoEjemplar.DANADO;
            }

            // 3. Liberar ejemplar
            ejemplarDAO.actualizarEstado(prestamo.getIdEjemplar(), estadoNuevoEjemplar, con);

            // 4. Actualizar préstamo a DEVUELTO
            prestamoDAO.registrarDevolucion(idPrestamo, fechaActual, EstadoPrestamo.DEVUELTO, con);

            Sancion sancionGenerada = null;

            // 5. Evaluar si existe retraso en la entrega
            if (fechaHoy.isAfter(fechaLimite)) {
                long diasRetraso = ChronoUnit.DAYS.between(fechaLimite, fechaHoy);
                BigDecimal montoMulta = BigDecimal.valueOf(diasRetraso * multaPorDia);

                String motivo = "Retraso de " + diasRetraso + " día(s) en devolución de ejemplar [" 
                        + prestamo.getCodigoInventario() + "] ($" + multaPorDia + "/día)";

                sancionGenerada = new Sancion(idPrestamo, prestamo.getIdUsuario(), (int) diasRetraso, montoMulta, motivo);
                int idSancion = sancionDAO.insertar(sancionGenerada, con);
                sancionGenerada.setIdSancion(idSancion);

                // Marcar al lector como SANCIONADO
                usuarioDAO.actualizarEstado(prestamo.getIdUsuario(), EstadoUsuario.SANCIONADO, con);

                LOGGER.info("Sanción #" + idSancion + " generada automáticamente por $" + montoMulta);
            }

            // 6. Registrar en bitácora
            String detalleLog = "Devolución registrada para préstamo #" + idPrestamo + ". Ejemplar [" 
                    + prestamo.getCodigoInventario() + "] devuelto por " + prestamo.getNombreLector();
            if (sancionGenerada != null) {
                detalleLog += ". Se generó sanción por retraso de $" + sancionGenerada.getMonto();
            }

            Bitacora bit = new Bitacora(idBibliotecario, "CIRCULACION", AccionBitacora.DEVOLUCION, detalleLog);
            bitacoraDAO.registrar(bit, con);

            // CONFIRMACIÓN DE LA TRANSACCIÓN
            con.commit();
            LOGGER.info("Devolución de préstamo #" + idPrestamo + " confirmada con éxito.");
            return sancionGenerada;

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                    LOGGER.warning("Transacción de devolución revertida (rollback) debido a: " + e.getMessage());
                } catch (SQLException rEx) {
                    LOGGER.log(Level.SEVERE, "Fallo al ejecutar rollback de devolución.", rEx);
                }
            }
            if (e instanceof CirculacionException ce) throw ce;
            if (e instanceof ValidacionException ve) throw ve;
            if (e instanceof SQLException se) throw se;
            throw new SQLException("Error imprevisto durante la devolución: " + e.getMessage(), e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // =========================================================================
    // 3. RENOVACIÓN DE PRÉSTAMO
    // =========================================================================

    /**
     * Renueva un préstamo activo extendiendo su fecha de entrega.
     * <p>
     * Restricciones de negocio:
     * <ul>
     *   <li>El préstamo no debe encontrarse vencido.</li>
     *   <li>El número de renovaciones no debe rebasar el límite permitido.</li>
     *   <li>El lector no debe registrar sanciones pendientes.</li>
     * </ul>
     * </p>
     */
    public boolean realizarRenovacion(int idPrestamo, int idBibliotecario)
            throws CirculacionException, ValidacionException, SQLException {

        Configuracion config = Configuracion.getInstancia();
        int maxRenovaciones = config.getMaxRenovaciones();
        int diasExtension = config.getDiasLimitePrestamo();

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);
            if (prestamo == null) {
                throw new ValidacionException("El préstamo no existe.");
            }
            if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
                throw new CirculacionException("Solo es posible renovar préstamos con estado ACTIVO.");
            }
            if (prestamo.estaVencido()) {
                throw new CirculacionException("El préstamo ya rebasó su fecha límite de devolución. No puede renovarse; debe devolverse y saldar la multa correspondiente.");
            }
            if (prestamo.getRenovaciones() >= maxRenovaciones) {
                throw new CirculacionException("El préstamo ya alcanzó el límite máximo de " + maxRenovaciones + " renovación(es) permitida(s).");
            }
            if (sancionDAO.tieneSancionesPendientes(prestamo.getIdUsuario(), con)) {
                throw new CirculacionException("El lector registra sanciones pendientes; no se permite renovar.");
            }

            LocalDate nuevaFechaLimite = prestamo.getFechaLimite().plusDays(diasExtension);
            boolean exito = prestamoDAO.renovarPrestamo(idPrestamo, nuevaFechaLimite, con);

            Bitacora bit = new Bitacora(idBibliotecario, "CIRCULACION", AccionBitacora.RENOVACION,
                    "Renovación #" + (prestamo.getRenovaciones() + 1) + " del préstamo #" + idPrestamo 
                    + ". Nueva fecha límite: " + nuevaFechaLimite);
            bitacoraDAO.registrar(bit, con);

            con.commit();
            LOGGER.info("Préstamo #" + idPrestamo + " renovado con éxito hasta " + nuevaFechaLimite);
            return exito;

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rEx) {
                    LOGGER.log(Level.SEVERE, "Fallo en rollback de renovación.", rEx);
                }
            }
            if (e instanceof CirculacionException ce) throw ce;
            if (e instanceof ValidacionException ve) throw ve;
            if (e instanceof SQLException se) throw se;
            throw new SQLException("Error en renovación: " + e.getMessage(), e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // =========================================================================
    // 4. GESTIÓN Y COBRO DE SANCIONES
    // =========================================================================

    /**
     * Registra el pago en ventanilla de una sanción pendiente y reactiva al usuario
     * si no posee más deudas.
     */
    public boolean liquidarSancion(int idSancion, int idBibliotecario) throws ValidacionException, SQLException {
        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            Sancion sancion = sancionDAO.buscarPorId(idSancion);
            if (sancion == null) {
                throw new ValidacionException("La sanción indicada no existe.");
            }
            if (sancion.getEstado() != EstadoSancion.PENDIENTE) {
                throw new ValidacionException("La sanción ya se encuentra en estado: " + sancion.getEstado());
            }

            // Registrar pago
            sancionDAO.registrarPago(idSancion, LocalDateTime.now(), con);

            // Verificar si el lector ya no tiene otras sanciones pendientes
            boolean quedanPendientes = sancionDAO.tieneSancionesPendientes(sancion.getIdUsuario(), con);
            if (!quedanPendientes) {
                usuarioDAO.actualizarEstado(sancion.getIdUsuario(), EstadoUsuario.ACTIVO, con);
                LOGGER.info("Usuario #" + sancion.getIdUsuario() + " reactivado a estado ACTIVO al quedar libre de deudas.");
            }

            Bitacora bit = new Bitacora(idBibliotecario, "CIRCULACION", AccionBitacora.UPDATE,
                    "Liquidación y cobro de sanción #" + idSancion + " por $" + sancion.getMonto() 
                    + " a usuario " + sancion.getNombreUsuario());
            bitacoraDAO.registrar(bit, con);

            con.commit();
            return true;

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rEx) {
                    LOGGER.log(Level.SEVERE, "Fallo en rollback de pago de sanción.", rEx);
                }
            }
            if (e instanceof ValidacionException ve) throw ve;
            if (e instanceof SQLException se) throw se;
            throw new SQLException("Error al procesar pago: " + e.getMessage(), e);
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
     * Condonación administrativa de una multa con registro de motivo.
     */
    public boolean condonarSancion(int idSancion, int idAdmin, String motivo) throws ValidacionException, SQLException {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new ValidacionException("Debe ingresar el motivo o justificativo oficial para la condonación.");
        }

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            Sancion sancion = sancionDAO.buscarPorId(idSancion);
            if (sancion == null) {
                throw new ValidacionException("La sanción no existe.");
            }
            if (sancion.getEstado() != EstadoSancion.PENDIENTE) {
                throw new ValidacionException("La sanción ya está " + sancion.getEstado());
            }

            sancionDAO.condonar(idSancion, con);

            boolean quedanPendientes = sancionDAO.tieneSancionesPendientes(sancion.getIdUsuario(), con);
            if (!quedanPendientes) {
                usuarioDAO.actualizarEstado(sancion.getIdUsuario(), EstadoUsuario.ACTIVO, con);
            }

            Bitacora bit = new Bitacora(idAdmin, "CIRCULACION", AccionBitacora.UPDATE,
                    "Condonación de sanción #" + idSancion + " ($" + sancion.getMonto() + "). Justificación: " + motivo);
            bitacoraDAO.registrar(bit, con);

            con.commit();
            return true;

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rEx) {
                    LOGGER.log(Level.SEVERE, "Fallo en rollback de condonación.", rEx);
                }
            }
            if (e instanceof ValidacionException ve) throw ve;
            if (e instanceof SQLException se) throw se;
            throw new SQLException("Error al condonar sanción: " + e.getMessage(), e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // =========================================================================
    // CONSULTAS DE CIRCULACIÓN
    // =========================================================================

    public List<Prestamo> listarPrestamosEnCurso() throws SQLException {
        return prestamoDAO.listarActivosYVencidos();
    }

    /**
     * Localiza el préstamo activo o vencido actualmente asignado a un ejemplar físico.
     */
    public Prestamo buscarPrestamoActivoPorEjemplar(int idEjemplar) throws SQLException {
        return prestamoDAO.buscarPrestamoActivoPorEjemplar(idEjemplar);
    }

    public List<Prestamo> listarPrestamosVencidos() throws SQLException {
        return prestamoDAO.listarVencidos();
    }

    public List<Prestamo> listarHistorialLector(int idLector) throws SQLException {
        return prestamoDAO.listarPorUsuario(idLector);
    }

    public List<Sancion> listarSancionesPendientes() throws SQLException {
        return sancionDAO.listarPendientes();
    }

    public List<Sancion> listarTodasLasSanciones() throws SQLException {
        return sancionDAO.listarTodas();
    }

    public List<Sancion> listarSancionesDeUsuario(int idUsuario) throws SQLException {
        return sancionDAO.listarPorUsuario(idUsuario);
    }
}
