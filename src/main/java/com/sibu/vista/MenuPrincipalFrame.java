package com.sibu.vista;

import com.sibu.modelo.RolUsuario;
import com.sibu.modelo.Usuario;
import com.sibu.util.TemaUtil;
import com.sibu.vista.componentes.UIHelper;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Ventana principal del sistema SIBU con arquitectura modular y control estricto de roles (RF6).
 * <p>
 * Diseñada bajo la norma <b>ISO/IEC 25010:2023 (Seguridad, Usabilidad y Estética de Interfaz)</b>.
 * Muestra u oculta módulos en función del rol del usuario autenticado:
 * <ul>
 *   <li><b>Lector:</b> Acceso exclusivo a consulta de catálogo y su propio historial de préstamos.</li>
 *   <li><b>Bibliotecario:</b> Gestión de catálogo, módulo de circulación y reportes institucionales.</li>
 *   <li><b>Administrador:</b> Control total del sistema, incluyendo usuarios, bitácora y respaldos.</li>
 * </ul>
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class MenuPrincipalFrame extends JFrame {

    private final Usuario usuarioActual;

    private JPanel panelLateral;
    private JPanel panelContenido;
    private CardLayout cardLayout;
    private JButton btnAlternarTema;
    private final List<JButton> botonesMenu = new ArrayList<>();

    // Identificadores de paneles (CardLayout)
    public static final String CARD_BUSQUEDA = "BUSQUEDA_CATALOGO";
    public static final String CARD_CATALOGO = "CATALOGO_LIBROS";
    public static final String CARD_CIRCULACION = "CIRCULACION";
    public static final String CARD_USUARIOS = "USUARIOS";
    public static final String CARD_REPORTES = "REPORTES";
    public static final String CARD_BITACORA = "BITACORA";
    public static final String CARD_ADMIN_DATOS = "ADMIN_DATOS";
    public static final String CARD_HISTORIAL_LECTOR = "HISTORIAL_LECTOR";

    public MenuPrincipalFrame(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        inicializarUI();
    }

    private void inicializarUI() {
        setTitle("SIBU - Sistema Integral de Biblioteca Universitaria [" + usuarioActual.getRol().getEtiqueta() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);

        JPanel panelContenedor = new JPanel(new BorderLayout());

        // 1. Barra Lateral de Navegación (Sidebar)
        panelLateral = construirSidebar();
        panelContenedor.add(panelLateral, BorderLayout.WEST);

        // 2. Área Central Dinámica (CardLayout)
        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);

        construirVistasSegunRol();

        panelContenedor.add(panelContenido, BorderLayout.CENTER);
        setContentPane(panelContenedor);
    }

    private JPanel construirSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, TemaUtil.COLOR_NEUTRO_BORDE),
                BorderFactory.createEmptyBorder(15, 12, 15, 12)
        ));

        // Logotipo / Encabezado
        JLabel lblLogo = new JLabel("SIBU", SwingConstants.LEFT);
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblLogo.setForeground(TemaUtil.COLOR_PRIMARIO);

        JLabel lblSub = new JLabel("Biblioteca Universitaria", SwingConstants.LEFT);
        lblSub.setFont(UIHelper.FUENTE_SUBTITULO);
        lblSub.setForeground(new Color(100, 116, 139));

        sidebar.add(lblLogo);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(lblSub);
        sidebar.add(Box.createVerticalStrut(15));

        // Tarjeta de perfil de usuario actual
        JPanel cardUser = UIHelper.crearTarjeta();
        cardUser.setLayout(new BoxLayout(cardUser, BoxLayout.Y_AXIS));

        JLabel lblNombre = new JLabel(usuarioActual.getNombreCompleto());
        lblNombre.setFont(UIHelper.FUENTE_LABEL);

        JLabel lblRol = new JLabel(usuarioActual.getRol().getEtiqueta().toUpperCase());
        lblRol.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblRol.setForeground(obtenerColorRol(usuarioActual.getRol()));

        JLabel lblIdent = new JLabel("ID: " + usuarioActual.getIdentificacion());
        lblIdent.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblIdent.setForeground(new Color(100, 116, 139));

        cardUser.add(lblNombre);
        cardUser.add(Box.createVerticalStrut(2));
        cardUser.add(lblRol);
        cardUser.add(Box.createVerticalStrut(2));
        cardUser.add(lblIdent);

        sidebar.add(cardUser);
        sidebar.add(Box.createVerticalStrut(20));

        // Menú de opciones según rol (RF6)
        RolUsuario rol = usuarioActual.getRol();

        // Todos tienen acceso a Búsqueda
        agregarBotonNavegacion(sidebar, "Búsqueda de Catálogo", CARD_BUSQUEDA, true);

        // Bibliotecario y Administrador
        if (rol == RolUsuario.ADMINISTRADOR || rol == RolUsuario.BIBLIOTECARIO) {
            agregarBotonNavegacion(sidebar, "Gestión de Catálogo", CARD_CATALOGO, false);
            agregarBotonNavegacion(sidebar, "Módulo Circulación", CARD_CIRCULACION, false);
            agregarBotonNavegacion(sidebar, "Reportes y Estadísticas", CARD_REPORTES, false);
        }

        // Solo Administrador
        if (rol == RolUsuario.ADMINISTRADOR) {
            agregarBotonNavegacion(sidebar, "Gestión de Usuarios", CARD_USUARIOS, false);
            agregarBotonNavegacion(sidebar, "Bitácora de Auditoría", CARD_BITACORA, false);
            agregarBotonNavegacion(sidebar, "Respaldos y Datos", CARD_ADMIN_DATOS, false);
        }

        // Solo Lector
        if (rol == RolUsuario.LECTOR) {
            agregarBotonNavegacion(sidebar, "Mis Préstamos", CARD_HISTORIAL_LECTOR, false);
        }

        // Espaciador flexible para empujar el pie de la barra lateral al fondo
        sidebar.add(Box.createVerticalGlue());

        // Selector de Tema (Modo Claro / Modo Oscuro)
        btnAlternarTema = UIHelper.crearBotonSecundario(TemaUtil.esModoOscuro() ? "Modo Claro" : "Modo Oscuro");
        btnAlternarTema.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnAlternarTema.setAlignmentX(LEFT_ALIGNMENT);
        btnAlternarTema.addActionListener(e -> alternarTema());
        sidebar.add(btnAlternarTema);
        sidebar.add(Box.createVerticalStrut(8));

        // Botón Cerrar Sesión
        JButton btnLogout = UIHelper.crearBotonPeligro("Cerrar Sesión");
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnLogout.setAlignmentX(LEFT_ALIGNMENT);
        btnLogout.addActionListener(e -> cerrarSesion());
        sidebar.add(btnLogout);

        return sidebar;
    }

    private void agregarBotonNavegacion(JPanel panel, String texto, String cardId, boolean seleccionadoInicial) {
        JButton btn = new JButton(texto);
        btn.setFont(UIHelper.FUENTE_REGULAR);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addActionListener(e -> {
            cardLayout.show(panelContenido, cardId);
            actualizarEstadoBotones(btn);
        });

        if (seleccionadoInicial) {
            btn.setFont(UIHelper.FUENTE_LABEL);
            btn.setForeground(TemaUtil.COLOR_PRIMARIO);
        }

        botonesMenu.add(btn);
        panel.add(btn);
        panel.add(Box.createVerticalStrut(5));
    }

    private void actualizarEstadoBotones(JButton botonActivo) {
        for (JButton b : botonesMenu) {
            if (b == botonActivo) {
                b.setFont(UIHelper.FUENTE_LABEL);
                b.setForeground(TemaUtil.COLOR_PRIMARIO);
            } else {
                b.setFont(UIHelper.FUENTE_REGULAR);
                b.setForeground(null);
            }
        }
    }

    private void construirVistasSegunRol() {
        // 1. Buscador de catálogo (disponible para todos)
        panelContenido.add(new BusquedaCatalogoPanel(), CARD_BUSQUEDA);

        RolUsuario rol = usuarioActual.getRol();

        // 2. Módulos operativos
        if (rol == RolUsuario.ADMINISTRADOR || rol == RolUsuario.BIBLIOTECARIO) {
            panelContenido.add(new CatalogoPanel(usuarioActual), CARD_CATALOGO);
            panelContenido.add(new CirculacionPanel(usuarioActual), CARD_CIRCULACION);
            panelContenido.add(crearPanelPlaceholder("Reportes y Estadísticas (RF5)", "Disponible en Fase 5 con exportación CSV."), CARD_REPORTES);
        }

        // 3. Módulos administrativos
        if (rol == RolUsuario.ADMINISTRADOR) {
            panelContenido.add(new UsuariosPanel(usuarioActual), CARD_USUARIOS);
            panelContenido.add(crearPanelPlaceholder("Bitácora de Auditoría (RF7)", "Disponible en Fase 5 con filtros avanzados."), CARD_BITACORA);
            panelContenido.add(crearPanelPlaceholder("Administración de Respaldos (RF8)", "Disponible en Fase 5 con motor dual .sql."), CARD_ADMIN_DATOS);
        }

        // 4. Panel de Lector
        if (rol == RolUsuario.LECTOR) {
            panelContenido.add(crearPanelHistorialLector(), CARD_HISTORIAL_LECTOR);
        }

        // Mostrar por defecto el catálogo
        cardLayout.show(panelContenido, CARD_BUSQUEDA);
    }

    private JPanel crearPanelPlaceholder(String titulo, String subtitulo) {
        JPanel p = new JPanel(new GridBagLayout());
        JPanel tarjeta = UIHelper.crearTarjeta();
        tarjeta.setPreferredSize(new Dimension(500, 200));
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));

        JLabel l1 = new JLabel(titulo, SwingConstants.CENTER);
        l1.setFont(UIHelper.FUENTE_TITULO);
        l1.setAlignmentX(CENTER_ALIGNMENT);

        JLabel l2 = new JLabel(subtitulo, SwingConstants.CENTER);
        l2.setFont(UIHelper.FUENTE_REGULAR);
        l2.setForeground(new Color(100, 116, 139));
        l2.setAlignmentX(CENTER_ALIGNMENT);

        tarjeta.add(Box.createVerticalGlue());
        tarjeta.add(l1);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(l2);
        tarjeta.add(Box.createVerticalGlue());

        p.add(tarjeta);
        return p;
    }

    private JPanel crearPanelHistorialLector() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(UIHelper.crearEncabezadoModulo(
                "Mi Historial de Préstamos y Sanciones",
                "Consulte el estado de los libros que tiene actualmente en préstamo y su historial."
        ), BorderLayout.NORTH);

        // Tabla con préstamos del usuario lector
        String[] cols = {"ID Préstamo", "Ejemplar", "Título de la Obra", "Fecha Préstamo", "Fecha Límite", "Estado"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        UIHelper.estilizarTabla(table);

        try {
            com.sibu.servicio.CirculacionServicio cs = new com.sibu.servicio.CirculacionServicio();
            List<com.sibu.modelo.Prestamo> prestamos = cs.listarHistorialLector(usuarioActual.getIdUsuario());
            for (com.sibu.modelo.Prestamo p : prestamos) {
                model.addRow(new Object[]{
                        p.getIdPrestamo(),
                        p.getCodigoInventario(),
                        p.getTituloLibro(),
                        p.getFechaPrestamo() != null ? p.getFechaPrestamo().toLocalDate().toString() : "",
                        p.getFechaLimite() != null ? p.getFechaLimite().toString() : "",
                        p.estaVencido() ? "VENCIDO" : p.getEstado().getEtiqueta()
                });
            }
        } catch (Exception e) {
            // Manejo silencioso en vista previa
        }

        panel.add(new javax.swing.JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private Color obtenerColorRol(RolUsuario rol) {
        return switch (rol) {
            case ADMINISTRADOR -> TemaUtil.COLOR_PRIMARIO;
            case BIBLIOTECARIO -> TemaUtil.COLOR_EXITO;
            case LECTOR -> new Color(100, 116, 139);
        };
    }

    private void alternarTema() {
        boolean oscuro = TemaUtil.alternarTema();
        btnAlternarTema.setText(oscuro ? "Modo Claro" : "Modo Oscuro");
    }

    private void cerrarSesion() {
        if (UIHelper.confirmarAccion(this, "¿Está seguro de cerrar la sesión actual?", "Cerrar Sesión")) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }
}
