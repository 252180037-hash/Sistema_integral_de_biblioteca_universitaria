package com.sibu.vista;

import com.sibu.modelo.EstadoUsuario;
import com.sibu.modelo.RolUsuario;
import com.sibu.modelo.Usuario;
import com.sibu.servicio.UsuarioServicio;
import com.sibu.servicio.exception.ValidacionException;
import com.sibu.vista.componentes.UIHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Panel para la administración de usuarios y roles (RF2).
 * <p>
 * Permite gestionar perfiles (Administrador, Bibliotecario y Lector),
 * activar/inactivar cuentas y restablecer contraseñas de forma segura.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class UsuariosPanel extends JPanel {

    private final UsuarioServicio usuarioServicio;
    private final Usuario operadorActual;

    private JTable tablaUsuarios;
    private DefaultTableModel modeloUsuarios;
    private JTextField txtBuscar;

    // Campos formulario
    private JTextField txtIdUsuario;
    private JTextField txtIdentificacion;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtEmail;
    private JTextField txtTelefono;
    private JComboBox<RolUsuario> cmbRol;
    private JComboBox<EstadoUsuario> cmbEstado;
    private JPasswordField txtPassword;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnCambiarPass;
    private JButton btnEliminar;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public UsuariosPanel(Usuario operadorActual) {
        this.usuarioServicio = new UsuarioServicio();
        this.operadorActual = operadorActual;
        inicializarUI();
        cargarUsuarios();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Encabezado
        add(UIHelper.crearEncabezadoModulo(
                "Administración de Usuarios y Perfiles de Acceso",
                "Gestione los permisos diferenciados (RF2) para Administradores, Personal Bibliotecario y Lectores."
        ), BorderLayout.NORTH);

        // Barra superior de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusqueda.add(new JLabel("Buscar usuario:"));
        txtBuscar = UIHelper.crearCampoTexto(25);
        JButton btnBuscar = UIHelper.crearBotonSecundario("Buscar");
        btnBuscar.addActionListener(e -> buscarUsuarios());
        JButton btnRecargar = UIHelper.crearBotonSecundario("Recargar Todo");
        btnRecargar.addActionListener(e -> cargarUsuarios());

        panelBusqueda.add(txtBuscar);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnRecargar);

        // Tabla de usuarios
        String[] columnas = {"ID", "Identificación", "Nombre Completo", "Correo Electrónico", "Teléfono", "Rol", "Estado", "Registro"};
        modeloUsuarios = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaUsuarios = new JTable(modeloUsuarios);
        UIHelper.estilizarTabla(tablaUsuarios);
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() != -1) {
                seleccionarFilaUsuario();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaUsuarios);

        // Formulario lateral
        JPanel panelForm = UIHelper.crearTarjeta();
        panelForm.setLayout(new GridBagLayout());
        panelForm.setPreferredSize(new Dimension(380, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        int fila = 0;
        gbc.gridx = 0; gbc.gridy = fila++; gbc.gridwidth = 2;
        JLabel lblTituloForm = new JLabel("Ficha de Usuario");
        lblTituloForm.setFont(UIHelper.FUENTE_SECCION);
        panelForm.add(lblTituloForm, gbc);

        gbc.gridwidth = 1;
        txtIdUsuario = UIHelper.crearCampoTexto(5);
        txtIdUsuario.setEditable(false);
        txtIdUsuario.setEnabled(false);
        agregarFilaForm(panelForm, gbc, fila++, "ID:", txtIdUsuario);

        txtIdentificacion = UIHelper.crearCampoTexto(15);
        agregarFilaForm(panelForm, gbc, fila++, "Identificación / Matrícula:", txtIdentificacion);

        txtNombre = UIHelper.crearCampoTexto(15);
        agregarFilaForm(panelForm, gbc, fila++, "Nombre:", txtNombre);

        txtApellido = UIHelper.crearCampoTexto(15);
        agregarFilaForm(panelForm, gbc, fila++, "Apellido:", txtApellido);

        txtEmail = UIHelper.crearCampoTexto(15);
        agregarFilaForm(panelForm, gbc, fila++, "Correo Electrónico:", txtEmail);

        txtTelefono = UIHelper.crearCampoTexto(15);
        agregarFilaForm(panelForm, gbc, fila++, "Teléfono:", txtTelefono);

        cmbRol = new JComboBox<>(RolUsuario.values());
        cmbRol.setFont(UIHelper.FUENTE_REGULAR);
        agregarFilaForm(panelForm, gbc, fila++, "Rol / Perfil:", cmbRol);

        cmbEstado = new JComboBox<>(EstadoUsuario.values());
        cmbEstado.setFont(UIHelper.FUENTE_REGULAR);
        agregarFilaForm(panelForm, gbc, fila++, "Estado:", cmbEstado);

        txtPassword = new JPasswordField(15);
        txtPassword.setFont(UIHelper.FUENTE_REGULAR);
        txtPassword.setPreferredSize(new Dimension(txtPassword.getPreferredSize().width, 30));
        agregarFilaForm(panelForm, gbc, fila++, "Contraseña Inicial:", txtPassword);

        // Botones de acción
        gbc.gridx = 0; gbc.gridy = fila++; gbc.gridwidth = 2;
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));

        btnNuevo = UIHelper.crearBotonSecundario("Limpiar");
        btnNuevo.addActionListener(e -> limpiarFormulario());

        btnGuardar = UIHelper.crearBotonPrimario("Guardar");
        btnGuardar.addActionListener(e -> guardarUsuario());

        btnCambiarPass = UIHelper.crearBotonSecundario("Cambiar Clave");
        btnCambiarPass.addActionListener(e -> cambiarClaveUsuario());

        btnEliminar = UIHelper.crearBotonPeligro("Eliminar");
        btnEliminar.addActionListener(e -> eliminarUsuario());

        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCambiarPass);
        panelBotones.add(btnEliminar);

        panelForm.add(panelBotones, gbc);

        // Disposición dividida
        JPanel contenedorCentro = new JPanel(new BorderLayout());
        contenedorCentro.add(panelBusqueda, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTabla, panelForm);
        split.setResizeWeight(0.65);
        contenedorCentro.add(split, BorderLayout.CENTER);

        add(contenedorCentro, BorderLayout.CENTER);
    }

    private void agregarFilaForm(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, javax.swing.JComponent campo) {
        gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = 1; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(UIHelper.FUENTE_LABEL);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(campo, gbc);
    }

    // =========================================================================
    // ACCIONES Y LÓGICA
    // =========================================================================

    private void cargarUsuarios() {
        modeloUsuarios.setRowCount(0);
        try {
            List<Usuario> lista = usuarioServicio.listarTodos();
            for (Usuario u : lista) {
                modeloUsuarios.addRow(new Object[]{
                        u.getIdUsuario(),
                        u.getIdentificacion(),
                        u.getNombreCompleto(),
                        u.getEmail(),
                        u.getTelefono() != null ? u.getTelefono() : "",
                        u.getRol().getEtiqueta(),
                        u.getEstado().getEtiqueta(),
                        u.getFechaRegistro() != null ? u.getFechaRegistro().format(FORMATTER) : ""
                });
            }
        } catch (SQLException e) {
            UIHelper.mostrarError(this, "Error al consultar usuarios: " + e.getMessage());
        }
    }

    private void buscarUsuarios() {
        String texto = txtBuscar.getText().trim();
        modeloUsuarios.setRowCount(0);
        try {
            List<Usuario> lista = usuarioServicio.buscarPorCriterio(texto);
            for (Usuario u : lista) {
                modeloUsuarios.addRow(new Object[]{
                        u.getIdUsuario(),
                        u.getIdentificacion(),
                        u.getNombreCompleto(),
                        u.getEmail(),
                        u.getTelefono() != null ? u.getTelefono() : "",
                        u.getRol().getEtiqueta(),
                        u.getEstado().getEtiqueta(),
                        u.getFechaRegistro() != null ? u.getFechaRegistro().format(FORMATTER) : ""
                });
            }
        } catch (SQLException e) {
            UIHelper.mostrarError(this, "Error en búsqueda: " + e.getMessage());
        }
    }

    private void seleccionarFilaUsuario() {
        int r = tablaUsuarios.getSelectedRow();
        if (r >= 0) {
            int id = (int) modeloUsuarios.getValueAt(r, 0);
            try {
                Usuario u = usuarioServicio.buscarPorId(id);
                if (u != null) {
                    txtIdUsuario.setText(String.valueOf(u.getIdUsuario()));
                    txtIdentificacion.setText(u.getIdentificacion());
                    txtNombre.setText(u.getNombre());
                    txtApellido.setText(u.getApellido());
                    txtEmail.setText(u.getEmail());
                    txtTelefono.setText(u.getTelefono() != null ? u.getTelefono() : "");
                    cmbRol.setSelectedItem(u.getRol());
                    cmbEstado.setSelectedItem(u.getEstado());
                    txtPassword.setText("");
                    txtPassword.setEnabled(false); // La contraseña no se edita directamente en el form
                }
            } catch (SQLException e) {
                UIHelper.mostrarError(this, "Error al recuperar usuario: " + e.getMessage());
            }
        }
    }

    private void limpiarFormulario() {
        txtIdUsuario.setText("");
        txtIdentificacion.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");
        cmbRol.setSelectedItem(RolUsuario.LECTOR);
        cmbEstado.setSelectedItem(EstadoUsuario.ACTIVO);
        txtPassword.setText("");
        txtPassword.setEnabled(true);
        tablaUsuarios.clearSelection();
        txtIdentificacion.requestFocus();
    }

    private void guardarUsuario() {
        try {
            String ident = txtIdentificacion.getText().trim();
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String email = txtEmail.getText().trim();
            String tel = txtTelefono.getText().trim();
            RolUsuario rol = (RolUsuario) cmbRol.getSelectedItem();
            EstadoUsuario estado = (EstadoUsuario) cmbEstado.getSelectedItem();

            if (txtIdUsuario.getText().isEmpty()) {
                // Nuevo usuario
                String pass = new String(txtPassword.getPassword()).trim();
                Usuario nuevo = new Usuario(nombre, apellido, ident, email, tel, "", "", rol, estado);
                usuarioServicio.registrarUsuario(nuevo, pass, operadorActual.getIdUsuario());
                UIHelper.mostrarExito(this, "Usuario registrado exitosamente con contraseña segura.");
            } else {
                // Modificación de usuario existente
                int id = Integer.parseInt(txtIdUsuario.getText());
                Usuario existente = usuarioServicio.buscarPorId(id);
                existente.setIdentificacion(ident);
                existente.setNombre(nombre);
                existente.setApellido(apellido);
                existente.setEmail(email);
                existente.setTelefono(tel);
                existente.setRol(rol);
                existente.setEstado(estado);

                usuarioServicio.actualizarUsuario(existente, operadorActual.getIdUsuario());
                UIHelper.mostrarExito(this, "Datos de usuario actualizados correctamente.");
            }

            limpiarFormulario();
            cargarUsuarios();

        } catch (ValidacionException ve) {
            UIHelper.mostrarAdvertencia(this, ve.getMessage());
        } catch (Exception e) {
            UIHelper.mostrarError(this, "Fallo al guardar usuario: " + e.getMessage());
        }
    }

    private void cambiarClaveUsuario() {
        if (txtIdUsuario.getText().isEmpty()) {
            UIHelper.mostrarAdvertencia(this, "Seleccione un usuario para cambiar su contraseña.");
            return;
        }

        int id = Integer.parseInt(txtIdUsuario.getText());
        String nueva = JOptionPane.showInputDialog(this, "Ingrese la nueva contraseña (mínimo 6 caracteres):", "Restablecer Contraseña", JOptionPane.QUESTION_MESSAGE);
        if (nueva != null && !nueva.trim().isEmpty()) {
            try {
                usuarioServicio.cambiarPassword(id, null, nueva.trim(), operadorActual.getIdUsuario());
                UIHelper.mostrarExito(this, "Contraseña actualizada exitosamente con nuevo Salt criptográfico.");
            } catch (ValidacionException ve) {
                UIHelper.mostrarAdvertencia(this, ve.getMessage());
            } catch (Exception e) {
                UIHelper.mostrarError(this, "Fallo al actualizar contraseña: " + e.getMessage());
            }
        }
    }

    private void eliminarUsuario() {
        if (txtIdUsuario.getText().isEmpty()) {
            UIHelper.mostrarAdvertencia(this, "Seleccione un usuario de la tabla para eliminar.");
            return;
        }

        int id = Integer.parseInt(txtIdUsuario.getText());
        String nombre = txtNombre.getText() + " " + txtApellido.getText();

        if (id == operadorActual.getIdUsuario()) {
            UIHelper.mostrarAdvertencia(this, "No puede eliminar su propia cuenta de usuario en sesión.");
            return;
        }

        if (UIHelper.confirmarAccion(this, "¿Está seguro de eliminar definitivamente al usuario \"" + nombre + "\"?", "Confirmar Eliminación")) {
            try {
                usuarioServicio.eliminarUsuario(id, operadorActual.getIdUsuario());
                UIHelper.mostrarExito(this, "Usuario eliminado del sistema.");
                limpiarFormulario();
                cargarUsuarios();
            } catch (ValidacionException ve) {
                UIHelper.mostrarAdvertencia(this, ve.getMessage());
            } catch (Exception e) {
                UIHelper.mostrarError(this, "Error al eliminar usuario: " + e.getMessage());
            }
        }
    }
}
