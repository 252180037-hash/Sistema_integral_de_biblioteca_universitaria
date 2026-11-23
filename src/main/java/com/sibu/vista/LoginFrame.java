package com.sibu.vista;

import com.sibu.modelo.Usuario;
import com.sibu.servicio.UsuarioServicio;
import com.sibu.servicio.exception.AutenticacionException;
import com.sibu.servicio.exception.ValidacionException;
import com.sibu.util.TemaUtil;
import com.sibu.vista.componentes.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Pantalla de inicio de sesión y autenticación de usuarios para SIBU (RF6).
 * <p>
 * Diseñada bajo la norma <b>ISO/IEC 25010:2023 (Seguridad y Usabilidad)</b>.
 * Dispone de controles limpios, soporte de Modo Claro y Oscuro, atajos de teclado (Enter)
 * y botones de acceso rápido con las credenciales sembradas de los 3 perfiles (Admin, Bibliotecario, Lector).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class LoginFrame extends JFrame {

    private final UsuarioServicio usuarioServicio;

    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnAlternarTema;
    private JLabel lblMensajeError;

    public LoginFrame() {
        this.usuarioServicio = new UsuarioServicio();
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("SIBU - Inicio de Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 560);
        setMinimumSize(new Dimension(420, 520));
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // ---------------------------------------------------------------------
        // Barra Superior: Botón de cambio de Tema (Modo Claro / Modo Oscuro)
        // ---------------------------------------------------------------------
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnAlternarTema = new JButton(TemaUtil.esModoOscuro() ? "Modo Claro" : "Modo Oscuro");
        btnAlternarTema.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnAlternarTema.setFocusPainted(false);
        btnAlternarTema.addActionListener(e -> alternarTema());
        panelTop.add(btnAlternarTema);
        panelPrincipal.add(panelTop, BorderLayout.NORTH);

        // ---------------------------------------------------------------------
        // Centro: Tarjeta de Login
        // ---------------------------------------------------------------------
        JPanel cardLogin = UIHelper.crearTarjeta();
        cardLogin.setLayout(new BoxLayout(cardLogin, BoxLayout.Y_AXIS));

        // Título del sistema
        JLabel lblLogo = new JLabel("SIBU", SwingConstants.CENTER);
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblLogo.setForeground(TemaUtil.COLOR_PRIMARIO);
        lblLogo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Sistema Integral de Biblioteca Universitaria", SwingConstants.CENTER);
        lblSubtitulo.setFont(UIHelper.FUENTE_SUBTITULO);
        lblSubtitulo.setForeground(TemaUtil.getColorTextoSecundario());
        lblSubtitulo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblInstruccion = new JLabel("Ingrese sus credenciales institucionales", SwingConstants.CENTER);
        lblInstruccion.setFont(UIHelper.FUENTE_LABEL);
        lblInstruccion.setAlignmentX(CENTER_ALIGNMENT);

        cardLogin.add(lblLogo);
        cardLogin.add(Box.createVerticalStrut(4));
        cardLogin.add(lblSubtitulo);
        cardLogin.add(Box.createVerticalStrut(20));
        cardLogin.add(lblInstruccion);
        cardLogin.add(Box.createVerticalStrut(15));

        // Formulario de Credenciales
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Correo
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        JLabel lblEmail = new JLabel("Correo Electrónico:");
        lblEmail.setFont(UIHelper.FUENTE_LABEL);
        formPanel.add(lblEmail, gbc);

        gbc.gridy = 1;
        txtEmail = UIHelper.crearCampoTexto(20);
        txtEmail.setText("admin@sibu.edu");
        formPanel.add(txtEmail, gbc);

        // Contraseña
        gbc.gridy = 2;
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(UIHelper.FUENTE_LABEL);
        formPanel.add(lblPass, gbc);

        gbc.gridy = 3;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(UIHelper.FUENTE_REGULAR);
        txtPassword.setText("Admin123*");
        txtPassword.setPreferredSize(new Dimension(txtPassword.getPreferredSize().width, 30));
        formPanel.add(txtPassword, gbc);

        // Etiqueta de error
        gbc.gridy = 4;
        lblMensajeError = new JLabel(" ");
        lblMensajeError.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblMensajeError.setForeground(TemaUtil.COLOR_PELIGRO);
        formPanel.add(lblMensajeError, gbc);

        // Botón Ingresar
        gbc.gridy = 5;
        btnLogin = UIHelper.crearBotonPrimario("Iniciar Sesión");
        btnLogin.setPreferredSize(new Dimension(btnLogin.getPreferredSize().width, 36));
        btnLogin.addActionListener(e -> ejecutarLogin());
        formPanel.add(btnLogin, gbc);

        cardLogin.add(formPanel);
        panelPrincipal.add(cardLogin, BorderLayout.CENTER);

        // ---------------------------------------------------------------------
        // Pie de Página: Accesos Rápidos de Prueba (Seeders)
        // ---------------------------------------------------------------------
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));

        JLabel lblPrueba = new JLabel("Cuentas de prueba sembradas:");
        lblPrueba.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblPrueba.setAlignmentX(CENTER_ALIGNMENT);

        JPanel panelPills = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));

        JButton btnTestAdmin = new JButton("Admin");
        btnTestAdmin.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnTestAdmin.addActionListener(e -> rellenarCredenciales("admin@sibu.edu", "Admin123*"));

        JButton btnTestBiblio = new JButton("Bibliotecario");
        btnTestBiblio.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnTestBiblio.addActionListener(e -> rellenarCredenciales("bibliotecario@sibu.edu", "Biblio123*"));

        JButton btnTestLector = new JButton("Lector");
        btnTestLector.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnTestLector.addActionListener(e -> rellenarCredenciales("lector1@sibu.edu", "Lector123*"));

        panelPills.add(btnTestAdmin);
        panelPills.add(btnTestBiblio);
        panelPills.add(btnTestLector);

        panelInferior.add(lblPrueba);
        panelInferior.add(panelPills);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        // Soporte de tecla ENTER para enviar login
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ejecutarLogin();
                }
            }
        };
        txtEmail.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        setContentPane(panelPrincipal);
    }

    private void rellenarCredenciales(String email, String pass) {
        txtEmail.setText(email);
        txtPassword.setText(pass);
        lblMensajeError.setText(" ");
        txtPassword.requestFocus();
    }

    private void ejecutarLogin() {
        lblMensajeError.setText(" ");
        String email = txtEmail.getText().trim();
        char[] password = txtPassword.getPassword();

        btnLogin.setEnabled(false);
        btnLogin.setText("Validando...");

        SwingUtilities.invokeLater(() -> {
            try {
                Usuario usuario = usuarioServicio.autenticar(email, password);
                abrirMenuPrincipal(usuario);
            } catch (ValidacionException | AutenticacionException e) {
                lblMensajeError.setText(e.getMessage());
                txtPassword.setText("");
                txtPassword.requestFocus();
            } catch (Exception e) {
                lblMensajeError.setText("Error del sistema: " + e.getMessage());
            } finally {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar Sesión");
            }
        });
    }

    private void abrirMenuPrincipal(Usuario usuario) {
        this.dispose(); // Cierra pantalla de login
        SwingUtilities.invokeLater(() -> {
            MenuPrincipalFrame menu = new MenuPrincipalFrame(usuario);
            menu.setVisible(true);
        });
    }

    private void alternarTema() {
        boolean oscuro = TemaUtil.alternarTema();
        btnAlternarTema.setText(oscuro ? "Modo Claro" : "Modo Oscuro");
    }
}
