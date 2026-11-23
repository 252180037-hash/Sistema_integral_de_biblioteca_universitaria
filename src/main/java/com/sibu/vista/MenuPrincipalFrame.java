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
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana principal del sistema SIBU con arquitectura Shell Parity con Stitch y control RBAC (RF6).
 * <p>
 * Diseñada bajo la norma <b>ISO/IEC 25010:2023 (Seguridad, Usabilidad y Estética de Interfaz)</b>.
 * Incorpora la barra lateral persistente (260px) de la plantilla Stitch, el selector de perfiles (RF2/RF6),
 * el Dashboard Integral con telemetría de software (RF1–RF8) y soporte reactivo bimodal (Claro/Oscuro).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 2.1.0
 */
public class MenuPrincipalFrame extends JFrame {

    private final Usuario usuarioActual;

    private JPanel panelLateral;
    private JPanel panelContenido;
    private CardLayout cardLayout;
    private JButton btnAlternarTema;
    private JLabel lblRoleSummary;
    private final List<JButton> botonesMenu = new ArrayList<>();
    private final Map<JButton, String> mapaBotonesCard = new HashMap<>();

    // Identificadores de paneles (CardLayout)
    public static final String CARD_DASHBOARD = "DASHBOARD_INTEGRAL";
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
        setTitle("SIBU — Sistema Integrado de Bibliotecas [" + usuarioActual.getRol().getEtiqueta() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);

        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setBackground(TemaUtil.getColorFondo());

        // 1. Barra Lateral de Navegación Persistente (260px)
        panelLateral = construirSidebar();
        panelContenedor.add(panelLateral, BorderLayout.WEST);

        // 2. Área Central Dinámica (CardLayout)
        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(TemaUtil.getColorFondo());

        construirVistasSegunRol();

        panelContenedor.add(panelContenido, BorderLayout.CENTER);
        setContentPane(panelContenedor);
    }

    private JPanel construirSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorSidebar());
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, TemaUtil.getColorBorde()),
                        BorderFactory.createEmptyBorder(18, 14, 18, 14)
                ));
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(TemaUtil.getColorSidebar());
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, TemaUtil.getColorBorde()),
                BorderFactory.createEmptyBorder(18, 14, 18, 14)
        ));

        // ---------------------------------------------------------------------
        // 1. Brand Header (Plantilla Stitch)
        // ---------------------------------------------------------------------
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);
        brandPanel.setAlignmentX(LEFT_ALIGNMENT);

        // Icono biblioteca en caja redondeada
        JLabel brandIcon = new JLabel("🏛", SwingConstants.CENTER) {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorPrimario());
                setForeground(TemaUtil.getColorPrimarioTexto());
                setBorder(BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true));
            }
        };
        brandIcon.setFont(new Font("SansSerif", Font.PLAIN, 18));
        brandIcon.setOpaque(true);
        brandIcon.setBackground(TemaUtil.getColorPrimario());
        brandIcon.setForeground(TemaUtil.getColorPrimarioTexto());
        brandIcon.setPreferredSize(new Dimension(38, 38));
        brandIcon.setBorder(BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true));

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel lblLogo = new JLabel("SIBU", SwingConstants.LEFT);
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblLogo.setForeground(TemaUtil.getColorTextoPrincipal());

        JLabel lblSub = new JLabel("Java · 5 Capas", SwingConstants.LEFT);
        lblSub.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblSub.setForeground(TemaUtil.getColorTextoSecundario());

        brandText.add(lblLogo);
        brandText.add(Box.createVerticalStrut(1));
        brandText.add(lblSub);

        brandPanel.add(brandIcon);
        brandPanel.add(brandText);

        sidebar.add(brandPanel);
        sidebar.add(Box.createVerticalStrut(16));

        // ---------------------------------------------------------------------
        // 2. Selector de Perfiles (RF2 / RF6 Tab Pills)
        // ---------------------------------------------------------------------
        JPanel rolePillsCard = new JPanel();
        rolePillsCard.setOpaque(false);
        rolePillsCard.setLayout(new BoxLayout(rolePillsCard, BoxLayout.Y_AXIS));
        rolePillsCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblRoleTitle = new JLabel("Perfil activo (RF2 / RF6)");
        lblRoleTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblRoleTitle.setForeground(TemaUtil.getColorTextoSecundario());
        rolePillsCard.add(lblRoleTitle);
        rolePillsCard.add(Box.createVerticalStrut(6));

        JPanel pillsPanel = new JPanel(new GridLayout(1, 3, 4, 0));
        pillsPanel.setOpaque(false);
        pillsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        RolUsuario rol = usuarioActual.getRol();
        JButton tabAdmin = crearRoleTab("Admin", rol == RolUsuario.ADMINISTRADOR);
        JButton tabBiblio = crearRoleTab("Biblio", rol == RolUsuario.BIBLIOTECARIO);
        JButton tabLector = crearRoleTab("Lector", rol == RolUsuario.LECTOR);

        pillsPanel.add(tabAdmin);
        pillsPanel.add(tabBiblio);
        pillsPanel.add(tabLector);
        rolePillsCard.add(pillsPanel);
        rolePillsCard.add(Box.createVerticalStrut(6));

        lblRoleSummary = new JLabel(obtenerResumenRol(rol));
        lblRoleSummary.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblRoleSummary.setForeground(TemaUtil.getColorTextoSecundario());
        rolePillsCard.add(lblRoleSummary);

        sidebar.add(rolePillsCard);
        sidebar.add(Box.createVerticalStrut(14));

        // ---------------------------------------------------------------------
        // 3. Tarjeta de Usuario Actual
        // ---------------------------------------------------------------------
        JPanel cardUser = UIHelper.crearTarjeta();
        cardUser.setLayout(new BoxLayout(cardUser, BoxLayout.Y_AXIS));
        cardUser.setAlignmentX(LEFT_ALIGNMENT);
        cardUser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JLabel lblNombre = new JLabel(usuarioActual.getNombreCompleto());
        lblNombre.setFont(UIHelper.FUENTE_LABEL);
        lblNombre.setForeground(TemaUtil.getColorTextoPrincipal());

        JLabel lblRolPill = new JLabel("  " + usuarioActual.getRol().getEtiqueta().toUpperCase() + "  ");
        lblRolPill.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblRolPill.setOpaque(true);
        lblRolPill.setBackground(TemaUtil.COLOR_TEA_GREEN);
        lblRolPill.setForeground(TemaUtil.COLOR_EVERGREEN);
        lblRolPill.setBorder(BorderFactory.createLineBorder(TemaUtil.COLOR_MUTED_OLIVE, 1, true));

        JLabel lblIdent = new JLabel("ID: " + usuarioActual.getIdentificacion());
        lblIdent.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblIdent.setForeground(TemaUtil.getColorTextoSecundario());

        cardUser.add(lblNombre);
        cardUser.add(Box.createVerticalStrut(3));
        cardUser.add(lblRolPill);
        cardUser.add(Box.createVerticalStrut(3));
        cardUser.add(lblIdent);

        sidebar.add(cardUser);
        sidebar.add(Box.createVerticalStrut(16));

        // ---------------------------------------------------------------------
        // 4. Navegación Principal Modular con Iconos y Códigos RF
        // ---------------------------------------------------------------------
        JLabel lblSecOperacion = new JLabel("OPERACIÓN SIBU");
        lblSecOperacion.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblSecOperacion.setForeground(TemaUtil.getColorTextoSecundario());
        lblSecOperacion.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(lblSecOperacion);
        sidebar.add(Box.createVerticalStrut(6));

        // 1. Resumen General (Dashboard Integral)
        agregarBotonNavegacion(sidebar, "  📊  Resumen General", CARD_DASHBOARD, true);

        // 2. Consulta y Búsqueda de Catálogo (RF4)
        agregarBotonNavegacion(sidebar, "  🔍  Búsqueda Catálogo (RF4)", CARD_BUSQUEDA, false);

        // 3. Módulos Operativos (Admin / Bibliotecario)
        if (rol == RolUsuario.ADMINISTRADOR || rol == RolUsuario.BIBLIOTECARIO) {
            agregarBotonNavegacion(sidebar, "  📚  Gestión Catálogo (RF1)", CARD_CATALOGO, false);
            agregarBotonNavegacion(sidebar, "  🔄  Circulación (RF3)", CARD_CIRCULACION, false);
            agregarBotonNavegacion(sidebar, "  📈  Reportes CSV (RF5)", CARD_REPORTES, false);
        }

        // 4. Módulos de Calidad y Sistema (Admin)
        if (rol == RolUsuario.ADMINISTRADOR) {
            sidebar.add(Box.createVerticalStrut(8));
            JLabel lblSecAdmin = new JLabel("CALIDAD & SISTEMA");
            lblSecAdmin.setFont(new Font("SansSerif", Font.BOLD, 10));
            lblSecAdmin.setForeground(TemaUtil.getColorTextoSecundario());
            lblSecAdmin.setAlignmentX(LEFT_ALIGNMENT);
            sidebar.add(lblSecAdmin);
            sidebar.add(Box.createVerticalStrut(6));

            agregarBotonNavegacion(sidebar, "  👥  Gestión Usuarios (RF2)", CARD_USUARIOS, false);
            agregarBotonNavegacion(sidebar, "  📜  Bitácora Auditoría (RF7)", CARD_BITACORA, false);
            agregarBotonNavegacion(sidebar, "  💾  Respaldos & Seeders (RF8)", CARD_ADMIN_DATOS, false);
        }

        // 5. Historial para Lector
        if (rol == RolUsuario.LECTOR) {
            agregarBotonNavegacion(sidebar, "  📖  Mis Préstamos Activos", CARD_HISTORIAL_LECTOR, false);
        }

        sidebar.add(Box.createVerticalGlue());

        // ---------------------------------------------------------------------
        // 5. Pie de la Barra Lateral: Pill de Arquitectura y Acciones
        // ---------------------------------------------------------------------
        JPanel cardArch = new JPanel();
        cardArch.setOpaque(true);
        cardArch.setBackground(TemaUtil.getColorSuperficie());
        cardArch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        cardArch.setLayout(new BoxLayout(cardArch, BoxLayout.Y_AXIS));
        cardArch.setAlignmentX(LEFT_ALIGNMENT);
        cardArch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel rowState = new JPanel(new BorderLayout(5, 0));
        rowState.setOpaque(false);
        JLabel lblDbState = new JLabel("MySQL Red Local");
        lblDbState.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblDbState.setForeground(TemaUtil.getColorTextoPrincipal());

        JLabel badgeState = new JLabel(" Activo ");
        badgeState.setFont(new Font("SansSerif", Font.BOLD, 10));
        badgeState.setOpaque(true);
        badgeState.setBackground(TemaUtil.COLOR_TEA_GREEN);
        badgeState.setForeground(TemaUtil.COLOR_EXITO);
        badgeState.setBorder(BorderFactory.createLineBorder(TemaUtil.COLOR_MUTED_OLIVE, 1, true));

        rowState.add(lblDbState, BorderLayout.WEST);
        rowState.add(badgeState, BorderLayout.EAST);

        JLabel lblJdbc = new JLabel("PreparedStatement · JDBC");
        lblJdbc.setFont(new Font("Monospaced", Font.PLAIN, 10));
        lblJdbc.setForeground(TemaUtil.getColorTextoSecundario());

        cardArch.add(rowState);
        cardArch.add(Box.createVerticalStrut(2));
        cardArch.add(lblJdbc);

        sidebar.add(cardArch);
        sidebar.add(Box.createVerticalStrut(10));

        // Selector de Tema (Modo Claro / Modo Oscuro)
        btnAlternarTema = UIHelper.crearBotonSecundario(TemaUtil.esModoOscuro() ? "  ☀️  Modo Claro" : "  🌙  Modo Oscuro");
        btnAlternarTema.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnAlternarTema.setAlignmentX(LEFT_ALIGNMENT);
        btnAlternarTema.addActionListener(e -> alternarTema());
        sidebar.add(btnAlternarTema);
        sidebar.add(Box.createVerticalStrut(6));

        // Botón Cerrar Sesión
        JButton btnLogout = UIHelper.crearBotonPeligro("Cerrar Sesión");
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnLogout.setAlignmentX(LEFT_ALIGNMENT);
        btnLogout.addActionListener(e -> cerrarSesion());
        sidebar.add(btnLogout);

        return sidebar;
    }

    private JButton crearRoleTab(String texto, boolean activo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", activo ? Font.BOLD : Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(2, 4, 2, 4));

        if (activo) {
            btn.setBackground(TemaUtil.getColorSuperficie());
            btn.setForeground(TemaUtil.getColorPrimario());
            btn.setBorder(BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true));
        } else {
            btn.setBackground(TemaUtil.getColorSuperficieContenedor());
            btn.setForeground(TemaUtil.getColorTextoSecundario());
            btn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        }

        btn.addActionListener(e -> {
            lblRoleSummary.setText(obtenerResumenRolPorNombre(texto));
        });

        return btn;
    }

    private String obtenerResumenRol(RolUsuario rol) {
        return switch (rol) {
            case ADMINISTRADOR -> "Acceso total: catálogo, usuarios, bitácora y respaldos.";
            case BIBLIOTECARIO -> "Operación: catálogo bibliográfico, circulación y reportes.";
            case LECTOR -> "Consulta en tiempo real del catálogo y préstamos personales.";
        };
    }

    private String obtenerResumenRolPorNombre(String rolNombre) {
        return switch (rolNombre.toLowerCase()) {
            case "admin" -> "Acceso total: catálogo, usuarios, bitácora y respaldos.";
            case "biblio" -> "Operación: catálogo bibliográfico, circulación y reportes.";
            default -> "Consulta en tiempo real del catálogo y préstamos personales.";
        };
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

        botonesMenu.add(btn);
        mapaBotonesCard.put(btn, cardId);

        if (seleccionadoInicial) {
            estilizarBotonActivo(btn);
        } else {
            estilizarBotonInactivo(btn);
        }

        panel.add(btn);
        panel.add(Box.createVerticalStrut(4));
    }

    private void estilizarBotonActivo(JButton btn) {
        btn.setFont(UIHelper.FUENTE_LABEL);
        btn.setOpaque(true);
        if (TemaUtil.esModoOscuro()) {
            btn.setBackground(new Color(44, 79, 39));
            btn.setForeground(TemaUtil.COLOR_TEA_GREEN);
            btn.setBorder(BorderFactory.createLineBorder(TemaUtil.COLOR_SAGE_GREEN, 1, true));
        } else {
            btn.setBackground(TemaUtil.COLOR_TEA_GREEN);
            btn.setForeground(TemaUtil.COLOR_EVERGREEN);
            btn.setBorder(BorderFactory.createLineBorder(TemaUtil.COLOR_MUTED_OLIVE, 1, true));
        }
    }

    private void estilizarBotonInactivo(JButton btn) {
        btn.setFont(UIHelper.FUENTE_REGULAR);
        btn.setOpaque(false);
        btn.setBackground(null);
        btn.setForeground(TemaUtil.getColorTextoPrincipal());
        btn.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    private void actualizarEstadoBotones(JButton botonActivo) {
        for (JButton b : botonesMenu) {
            if (b == botonActivo) {
                estilizarBotonActivo(b);
            } else {
                estilizarBotonInactivo(b);
            }
        }
    }

    private void construirVistasSegunRol() {
        // 1. Dashboard Integral Telemetría (Shell Parity con Stitch) - Vista principal
        panelContenido.add(new DashboardIntegralPanel(usuarioActual), CARD_DASHBOARD);

        // 2. Buscador de catálogo (disponible para todos los roles con préstamo interactivo)
        panelContenido.add(new BusquedaCatalogoPanel(usuarioActual), CARD_BUSQUEDA);

        RolUsuario rol = usuarioActual.getRol();

        // 3. Módulos operativos
        if (rol == RolUsuario.ADMINISTRADOR || rol == RolUsuario.BIBLIOTECARIO) {
            panelContenido.add(new CatalogoPanel(usuarioActual), CARD_CATALOGO);
            panelContenido.add(new CirculacionPanel(usuarioActual), CARD_CIRCULACION);
            panelContenido.add(new ReportesPanel(), CARD_REPORTES);
        }

        // 4. Módulos administrativos
        if (rol == RolUsuario.ADMINISTRADOR) {
            panelContenido.add(new UsuariosPanel(usuarioActual), CARD_USUARIOS);
            panelContenido.add(new BitacoraPanel(), CARD_BITACORA);
            panelContenido.add(new AdminDatosPanel(usuarioActual), CARD_ADMIN_DATOS);
        }

        // 5. Panel de Lector
        if (rol == RolUsuario.LECTOR) {
            panelContenido.add(crearPanelHistorialLector(), CARD_HISTORIAL_LECTOR);
        }

        // Mostrar por defecto el Dashboard Integral
        cardLayout.show(panelContenido, CARD_DASHBOARD);
    }

    private JPanel crearPanelHistorialLector() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(TemaUtil.getColorFondo());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        panel.add(UIHelper.crearEncabezadoModulo(
                "Mi Historial de Préstamos y Sanciones (Portal Lector)",
                "Consulte en tiempo real el estado de sus libros prestados, plazos reglamentarios y tramite devoluciones."
        ), BorderLayout.NORTH);

        String[] cols = {"ID Préstamo", "Cód. Ejemplar", "Título de la Obra", "Fecha Préstamo", "Fecha Límite", "Estado de Circulación"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        UIHelper.estilizarTabla(table);

        Runnable recargarPrestamos = () -> {
            model.setRowCount(0);
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
            } catch (Exception ignored) {
            }
        };

        recargarPrestamos.run();

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Barra inferior de acciones del lector
        JPanel panelBotonesLector = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        panelBotonesLector.setOpaque(false);

        JButton btnActualizar = UIHelper.crearBotonSecundario("Actualizar");
        btnActualizar.addActionListener(e -> recargarPrestamos.run());

        JButton btnRenovar = UIHelper.crearBotonSecundario("Renovar Préstamo (+7 Días)");
        btnRenovar.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                UIHelper.mostrarAdvertencia(panel, "Seleccione un préstamo de la lista para renovar.");
                return;
            }
            int idPrestamo = (int) model.getValueAt(r, 0);
            try {
                com.sibu.servicio.CirculacionServicio cs = new com.sibu.servicio.CirculacionServicio();
                cs.realizarRenovacion(idPrestamo, 2);
                UIHelper.mostrarExito(panel, "Préstamo renovado exitosamente. Se ha extendido la fecha límite.");
                recargarPrestamos.run();
            } catch (Exception ex) {
                UIHelper.mostrarAdvertencia(panel, ex.getMessage());
            }
        });

        JButton btnDevolver = UIHelper.crearBotonExito("Devolver Libro Prestado");
        btnDevolver.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                UIHelper.mostrarAdvertencia(panel, "Seleccione el préstamo que desea devolver.");
                return;
            }
            int idPrestamo = (int) model.getValueAt(r, 0);
            String titulo = (String) model.getValueAt(r, 2);
            String estado = (String) model.getValueAt(r, 5);

            if ("DEVUELTO".equalsIgnoreCase(estado)) {
                UIHelper.mostrarAdvertencia(panel, "Este libro ya figura como DEVUELTO.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    panel,
                    "¿Desea tramitar la devolución de la obra:\n\"" + titulo + "\"?",
                    "Devolución de Ejemplar",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    com.sibu.servicio.CirculacionServicio cs = new com.sibu.servicio.CirculacionServicio();
                    com.sibu.modelo.Sancion s = cs.registrarDevolucion(idPrestamo, 2, "Devolución tramitada desde portal Lector");
                    if (s != null) {
                        UIHelper.mostrarAdvertencia(panel, "Devolución registrada con retraso (" + s.getDiasRetraso() + " días). Multa generada: $" + s.getMonto());
                    } else {
                        UIHelper.mostrarExito(panel, "Devolución registrada con éxito. ¡Gracias por entregar a tiempo!");
                    }
                    recargarPrestamos.run();
                } catch (Exception ex) {
                    UIHelper.mostrarAdvertencia(panel, ex.getMessage());
                }
            }
        });

        panelBotonesLector.add(btnActualizar);
        panelBotonesLector.add(btnRenovar);
        panelBotonesLector.add(btnDevolver);
        panel.add(panelBotonesLector, BorderLayout.SOUTH);

        return panel;
    }

    public void refrescarTemaUI() {
        if (btnAlternarTema != null) {
            btnAlternarTema.setText(TemaUtil.esModoOscuro() ? "  ☀️  Modo Claro" : "  🌙  Modo Oscuro");
        }
        if (panelLateral != null) {
            panelLateral.setBackground(TemaUtil.getColorSidebar());
        }
        if (panelContenido != null) {
            panelContenido.setBackground(TemaUtil.getColorFondo());
        }
        for (JButton b : botonesMenu) {
            if (b.getFont().isBold()) {
                estilizarBotonActivo(b);
            } else {
                estilizarBotonInactivo(b);
            }
        }
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
    }

    private void alternarTema() {
        TemaUtil.alternarTema();
        refrescarTemaUI();
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
