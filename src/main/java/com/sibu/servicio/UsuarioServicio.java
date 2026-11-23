package com.sibu.servicio;

import com.sibu.dao.BitacoraDAO;
import com.sibu.dao.UsuarioDAO;
import com.sibu.modelo.AccionBitacora;
import com.sibu.modelo.Bitacora;
import com.sibu.modelo.EstadoUsuario;
import com.sibu.modelo.RolUsuario;
import com.sibu.modelo.Usuario;
import com.sibu.servicio.exception.AutenticacionException;
import com.sibu.servicio.exception.ValidacionException;
import com.sibu.util.ConexionDB;
import com.sibu.util.SeguridadUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de lógica de negocio para la gestión de usuarios, roles y autenticación de acceso.
 * <p>
 * Implementa <b>RF2 (Gestión de usuarios y roles)</b> y <b>RF6 (Seguridad de acceso)</b>.
 * Cumple con los criterios de <b>Seguridad</b> y <b>Fiabilidad</b> de la norma <b>ISO/IEC 25010:2023</b>
 * mediante transacciones JDBC controladas, verificación criptográfica y auditoría en bitácora.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class UsuarioServicio {

    private static final Logger LOGGER = Logger.getLogger(UsuarioServicio.class.getName());

    private final UsuarioDAO usuarioDAO;
    private final BitacoraDAO bitacoraDAO;

    public UsuarioServicio() {
        this.usuarioDAO = new UsuarioDAO();
        this.bitacoraDAO = new BitacoraDAO();
    }

    public UsuarioServicio(UsuarioDAO usuarioDAO, BitacoraDAO bitacoraDAO) {
        this.usuarioDAO = usuarioDAO;
        this.bitacoraDAO = bitacoraDAO;
    }

    /**
     * Autentica a un usuario verificando credenciales, estado de la cuenta y registrando el evento de auditoría.
     * 
     * @param email Correo electrónico ingresado.
     * @param password Contraseña en texto plano.
     * @return Objeto {@link Usuario} autenticado si las credenciales son válidas.
     * @throws AutenticacionException Si las credenciales no coinciden o la cuenta está inhabilitada.
     * @throws ValidacionException Si los datos de entrada son nulos o vacíos.
     */
    public Usuario autenticar(String email, char[] password) throws AutenticacionException, ValidacionException {
        if (email == null || email.trim().isEmpty() || password == null || password.length == 0) {
            throw new ValidacionException("Debe ingresar su correo electrónico y contraseña.");
        }

        Usuario usuario;
        try {
            usuario = usuarioDAO.buscarPorEmail(email.trim());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fallo de conexión al buscar usuario durante el inicio de sesión.", e);
            throw new AutenticacionException("Error de comunicación con la base de datos al autenticar.");
        }

        if (usuario == null) {
            // Mensaje genérico para prevenir enumeración de usuarios
            throw new AutenticacionException("Credenciales de acceso incorrectas.");
        }

        if (usuario.getEstado() == EstadoUsuario.INACTIVO) {
            throw new AutenticacionException("Su cuenta se encuentra inactiva. Contacte al Administrador.");
        }

        boolean passwordValida = SeguridadUtil.verificarPassword(password, usuario.getSalt(), usuario.getPasswordHash());
        if (!passwordValida) {
            throw new AutenticacionException("Credenciales de acceso incorrectas.");
        }

        // Registrar inicio de sesión exitoso en la bitácora
        Bitacora log = new Bitacora(usuario.getIdUsuario(), "SEGURIDAD", AccionBitacora.LOGIN,
                "Inicio de sesión exitoso del usuario: " + usuario.getEmail() + " con rol: " + usuario.getRol().name());
        bitacoraDAO.registrar(log);

        return usuario;
    }

    /**
     * Registra un nuevo usuario en el sistema garantizando unicidad de email e identificación,
     * y generando su Salt criptográfico único dentro de una transacción JDBC.
     * 
     * @param nuevo Objeto {@link Usuario} con los datos a registrar.
     * @param passwordPlana Contraseña en texto plano a ser encriptada.
     * @param idOperador Identificador del administrador que ejecuta la operación.
     * @return El usuario registrado con su ID generado.
     */
    public Usuario registrarUsuario(Usuario nuevo, String passwordPlana, Integer idOperador) throws ValidacionException, SQLException {
        validarDatosUsuario(nuevo);

        if (passwordPlana == null || passwordPlana.trim().length() < 6) {
            throw new ValidacionException("La contraseña debe contener al menos 6 caracteres.");
        }

        // Validar unicidad
        if (usuarioDAO.buscarPorEmail(nuevo.getEmail()) != null) {
            throw new ValidacionException("Ya existe un usuario registrado con el correo: " + nuevo.getEmail());
        }
        if (usuarioDAO.buscarPorIdentificacion(nuevo.getIdentificacion()) != null) {
            throw new ValidacionException("Ya existe un usuario con la identificación: " + nuevo.getIdentificacion());
        }

        // Generar Salt único y Hash SHA-256
        String salt = SeguridadUtil.generarSalt();
        String hash = SeguridadUtil.hashPassword(passwordPlana, salt);
        nuevo.setSalt(salt);
        nuevo.setPasswordHash(hash);

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false); // Transacción estricta ISO 25010

            int idGenerado = usuarioDAO.insertar(nuevo, con);
            nuevo.setIdUsuario(idGenerado);

            // Registro en bitácora
            Bitacora bit = new Bitacora(idOperador, "USUARIOS", AccionBitacora.INSERT,
                    "Registro de usuario ID #" + idGenerado + ": " + nuevo.getNombreCompleto() + " (" + nuevo.getRol() + ")");
            bitacoraDAO.registrar(bit, con);

            con.commit();
            LOGGER.info("Usuario registrado exitosamente con ID: " + idGenerado);
            return nuevo;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Fallo al revertir transacción de registro de usuario.", ex);
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
     * Modifica los datos personales y perfil de un usuario existente.
     */
    public boolean actualizarUsuario(Usuario usuario, Integer idOperador) throws ValidacionException, SQLException {
        if (usuario == null || usuario.getIdUsuario() == null) {
            throw new ValidacionException("El usuario a actualizar no es válido.");
        }
        validarDatosUsuario(usuario);

        Usuario existenteEmail = usuarioDAO.buscarPorEmail(usuario.getEmail());
        if (existenteEmail != null && !existenteEmail.getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new ValidacionException("El correo " + usuario.getEmail() + " ya pertenece a otro usuario.");
        }

        Usuario existenteId = usuarioDAO.buscarPorIdentificacion(usuario.getIdentificacion());
        if (existenteId != null && !existenteId.getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new ValidacionException("La identificación " + usuario.getIdentificacion() + " ya pertenece a otro usuario.");
        }

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            boolean exito = usuarioDAO.actualizar(usuario, con);

            Bitacora bit = new Bitacora(idOperador, "USUARIOS", AccionBitacora.UPDATE,
                    "Modificación de usuario ID #" + usuario.getIdUsuario() + ": " + usuario.getNombreCompleto());
            bitacoraDAO.registrar(bit, con);

            con.commit();
            return exito;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error en rollback al actualizar usuario.", ex);
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
     * Cambia la contraseña de un usuario validando su clave actual y cifrando con nuevo Salt.
     */
    public boolean cambiarPassword(int idUsuario, char[] passwordActual, String nuevaPasswordPlana, Integer idOperador)
            throws ValidacionException, AutenticacionException, SQLException {
        if (nuevaPasswordPlana == null || nuevaPasswordPlana.trim().length() < 6) {
            throw new ValidacionException("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        Usuario u = usuarioDAO.buscarPorId(idUsuario);
        if (u == null) {
            throw new ValidacionException("Usuario no encontrado.");
        }

        // Si se proporciona la contraseña actual, validarla
        if (passwordActual != null && passwordActual.length > 0) {
            boolean actualValida = SeguridadUtil.verificarPassword(passwordActual, u.getSalt(), u.getPasswordHash());
            if (!actualValida) {
                throw new AutenticacionException("La contraseña actual proporcionada es incorrecta.");
            }
        }

        String nuevoSalt = SeguridadUtil.generarSalt();
        String nuevoHash = SeguridadUtil.hashPassword(nuevaPasswordPlana, nuevoSalt);

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            boolean exito = usuarioDAO.actualizarPassword(idUsuario, nuevoHash, nuevoSalt, con);

            Bitacora bit = new Bitacora(idOperador, "SEGURIDAD", AccionBitacora.UPDATE,
                    "Cambio de contraseña para usuario ID #" + idUsuario + " (" + u.getEmail() + ")");
            bitacoraDAO.registrar(bit, con);

            con.commit();
            return exito;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Fallo en rollback de cambio de contraseña.", ex);
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
     * Modifica el estado de un usuario (ACTIVO, INACTIVO, SANCIONADO).
     */
    public boolean cambiarEstadoUsuario(int idUsuario, EstadoUsuario nuevoEstado, Integer idOperador) throws SQLException {
        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            boolean exito = usuarioDAO.actualizarEstado(idUsuario, nuevoEstado, con);

            Bitacora bit = new Bitacora(idOperador, "USUARIOS", AccionBitacora.UPDATE,
                    "Cambio de estado a " + nuevoEstado + " para usuario ID #" + idUsuario);
            bitacoraDAO.registrar(bit, con);

            con.commit();
            return exito;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Fallo en rollback de cambio de estado de usuario.", ex);
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
     * Elimina a un usuario siempre que no posea préstamos activos ni responsabilidades pendientes.
     */
    public boolean eliminarUsuario(int idUsuario, Integer idOperador) throws ValidacionException, SQLException {
        int prestamosActivos = usuarioDAO.contarPrestamosActivos(idUsuario);
        if (prestamosActivos > 0) {
            throw new ValidacionException("No es posible eliminar el usuario: tiene " + prestamosActivos + " préstamo(s) activo(s) o vencido(s).");
        }

        Usuario u = usuarioDAO.buscarPorId(idUsuario);
        if (u == null) {
            throw new ValidacionException("El usuario no existe.");
        }

        Connection con = null;
        try {
            con = ConexionDB.getInstancia().crearConexionAislada();
            con.setAutoCommit(false);

            boolean exito = usuarioDAO.eliminar(idUsuario, con);

            Bitacora bit = new Bitacora(idOperador, "USUARIOS", AccionBitacora.DELETE,
                    "Eliminación física del usuario ID #" + idUsuario + ": " + u.getNombreCompleto());
            bitacoraDAO.registrar(bit, con);

            con.commit();
            return exito;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error en rollback de eliminación de usuario.", ex);
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

    public List<Usuario> listarTodos() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    public List<Usuario> buscarPorCriterio(String texto) throws SQLException {
        if (texto == null || texto.trim().isEmpty()) {
            return usuarioDAO.listarTodos();
        }
        return usuarioDAO.buscarPorCriterio(texto);
    }

    public Usuario buscarPorId(int idUsuario) throws SQLException {
        return usuarioDAO.buscarPorId(idUsuario);
    }

    private void validarDatosUsuario(Usuario u) throws ValidacionException {
        if (u == null) {
            throw new ValidacionException("Los datos del usuario no pueden ser nulos.");
        }
        if (u.getNombre() == null || u.getNombre().trim().isEmpty()) {
            throw new ValidacionException("El nombre del usuario es obligatorio.");
        }
        if (u.getApellido() == null || u.getApellido().trim().isEmpty()) {
            throw new ValidacionException("El apellido del usuario es obligatorio.");
        }
        if (u.getIdentificacion() == null || u.getIdentificacion().trim().isEmpty()) {
            throw new ValidacionException("La identificación oficial es obligatoria.");
        }
        if (!SeguridadUtil.esEmailValido(u.getEmail())) {
            throw new ValidacionException("El correo electrónico ingresado no tiene un formato válido (ejemplo: usuario@sibu.edu).");
        }
        if (u.getRol() == null) {
            throw new ValidacionException("Debe asignar un rol válido al usuario.");
        }
        // Sanitizar campos de texto
        u.setNombre(SeguridadUtil.sanitizarTexto(u.getNombre()));
        u.setApellido(SeguridadUtil.sanitizarTexto(u.getApellido()));
        u.setIdentificacion(SeguridadUtil.sanitizarTexto(u.getIdentificacion()));
        u.setEmail(SeguridadUtil.sanitizarTexto(u.getEmail()));
    }
}
