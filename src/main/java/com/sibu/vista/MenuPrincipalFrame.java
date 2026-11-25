package com.sibu.vista;

import com.sibu.modelo.RolUsuario;
import com.sibu.modelo.Usuario;
import com.sibu.util.TemaUtil;
import com.sibu.vista.componentes.SidebarPanel;
import com.sibu.vista.componentes.UIHelper;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana principal del sistema SIBU.
 * <p>
 * Incorpora la barra lateral colapsable ({@link SidebarPanel}), CardLayout para
 * la navegación entre módulos y control de acceso basado en roles (RBAC).
 * Compatible estrictamente con Java 8 / Swing.
 * </p>
 *
 * @author SIBU Arquitectura
 * @version 3.0.0
 */
public class MenuPrincipalFrame extends JFrame {

    // CardLayout IDs (públicos para referencias externas)
    public static final String CARD_DASHBOARD        = "DASHBOARD_INTEGRAL";
    public static final String CARD_BUSQUEDA         = "BUSQUEDA_CATALOGO";
    public static final String CARD_CATALOGO         = "CATALOGO_LIBROS";
    public static final String CARD_CIRCULACION      = "CIRCULACION";
    public static final String CARD_USUARIOS         = "USUARIOS";
    public static final String CARD_REPORTES         = "REPORTES";
    public static final String CARD_BITACORA         = "BITACORA";
    public static final String CARD_ADMIN_DATOS      = "ADMIN_DATOS";
    public static final String CARD_HISTORIAL_LECTOR = "HISTORIAL_LECTOR";

    private final Usuario     usuarioActual;
    private SidebarPanel      sidebar;
    private JPanel            panelContenido;
    private CardLayout        cardLayout;
    private JPanel            panelHistorialLector;
    private JTable            tablaHistorialLector;

    public MenuPrincipalFrame(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        inicializarUI();
    }

    // =========================================================================
    // Inicialización
    // =========================================================================

    private void inicializarUI() {
        setTitle("SIBU — Sistema Integrado de Bibliotecas  [" + usuarioActual.getRol().getEtiqueta() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(480, 540));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(TemaUtil.getColorFondo());

        // --- Sidebar colapsable ---
        sidebar = new SidebarPanel(
            usuarioActual,
            this::mostrarPanel,
            this::cerrarSesion
        );

        JScrollPane scrollSidebar = new JScrollPane(sidebar);
        scrollSidebar.setBorder(null);
        scrollSidebar.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollSidebar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollSidebar.getVerticalScrollBar().setUnitIncrement(14);
        scrollSidebar.setPreferredSize(new Dimension(SidebarPanel.ANCHO_EXPANDIDO, 0));

        sidebar.setOnToggleExpand(expandido -> {
            int nuevoAncho = expandido
                    ? SidebarPanel.ANCHO_EXPANDIDO
                    : SidebarPanel.ANCHO_COLAPSADO;
            scrollSidebar.setPreferredSize(new Dimension(nuevoAncho, 0));
            scrollSidebar.revalidate();
        });

        root.add(scrollSidebar, BorderLayout.WEST);

        // --- Área central (CardLayout) ---
        cardLayout    = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(TemaUtil.getColorFondo());

        construirVistasSegunRol();

        root.add(panelContenido, BorderLayout.CENTER);
        setContentPane(root);
    }

    // =========================================================================
    // Paneles según rol
    // =========================================================================

    private void construirVistasSegunRol() {
        panelContenido.add(new DashboardIntegralPanel(usuarioActual, this::mostrarPanel), CARD_DASHBOARD);
        panelContenido.add(new BusquedaCatalogoPanel(usuarioActual), CARD_BUSQUEDA);

        RolUsuario rol = usuarioActual.getRol();

        if (rol == RolUsuario.ADMINISTRADOR || rol == RolUsuario.BIBLIOTECARIO) {
            panelContenido.add(new CatalogoPanel(usuarioActual),    CARD_CATALOGO);
            panelContenido.add(new CirculacionPanel(usuarioActual), CARD_CIRCULACION);
            panelContenido.add(new ReportesPanel(),                 CARD_REPORTES);
        }

        if (rol == RolUsuario.ADMINISTRADOR) {
            panelContenido.add(new UsuariosPanel(usuarioActual),    CARD_USUARIOS);
            panelContenido.add(new BitacoraPanel(),                 CARD_BITACORA);
            panelContenido.add(new AdminDatosPanel(usuarioActual),  CARD_ADMIN_DATOS);
        }

        if (rol == RolUsuario.LECTOR) {
            panelContenido.add(crearPanelHistorialLector(),         CARD_HISTORIAL_LECTOR);
        }

        cardLayout.show(panelContenido, CARD_DASHBOARD);
    }

    // =========================================================================
    // Navegación
    // =========================================================================

    public void mostrarPanel(String cardId) {
        cardLayout.show(panelContenido, cardId);
        if (sidebar != null) {
            sidebar.seleccionarPorCardId(cardId);
        }
    }

    // =========================================================================
    // Panel historial del lector
    // =========================================================================

    private JPanel crearPanelHistorialLector() {
        panelHistorialLector = new JPanel(new BorderLayout(12, 12)) {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorFondo());
            }
        };
        panelHistorialLector.setBackground(TemaUtil.getColorFondo());
        panelHistorialLector.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 18, 15, 18));

        panelHistorialLector.add(UIHelper.crearEncabezadoModulo(
                "Mi Historial de Préstamos",
                "Consulte el estado de sus libros prestados, plazos y trámite de devoluciones."
        ), BorderLayout.NORTH);

        String[] cols = {"ID", "Cód. Ejemplar", "Título", "Fecha Préstamo", "Fecha Límite", "Estado"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaHistorialLector = new JTable(model) {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorSuperficie());
                setForeground(TemaUtil.getColorTextoPrincipal());
                setGridColor(TemaUtil.getColorBorde());
                if (getTableHeader() != null) {
                    getTableHeader().setBackground(TemaUtil.getColorSuperficieContenedor());
                    getTableHeader().setForeground(TemaUtil.getColorTextoPrincipal());
                }
            }
        };
        UIHelper.estilizarTabla(tablaHistorialLector);

        Runnable cargar = () -> {
            model.setRowCount(0);
            try {
                com.sibu.servicio.CirculacionServicio cs = new com.sibu.servicio.CirculacionServicio();
                List<com.sibu.modelo.Prestamo> lista = cs.listarHistorialLector(usuarioActual.getIdUsuario());
                for (com.sibu.modelo.Prestamo p : lista) {
                    model.addRow(new Object[]{
                            p.getIdPrestamo(),
                            p.getCodigoInventario(),
                            p.getTituloLibro(),
                            p.getFechaPrestamo() != null ? p.getFechaPrestamo().toLocalDate().toString() : "",
                            p.getFechaLimite() != null ? p.getFechaLimite().toString() : "",
                            p.estaVencido() ? "VENCIDO" : p.getEstado().getEtiqueta()
                    });
                }
            } catch (Exception ignored) {}
        };

        cargar.run();
        JScrollPane scrollPane = new JScrollPane(tablaHistorialLector);
        scrollPane.setBackground(TemaUtil.getColorFondo());
        scrollPane.getViewport().setBackground(TemaUtil.getColorSuperficie());
        panelHistorialLector.add(scrollPane, BorderLayout.CENTER);

        // Botones
        JPanel barra = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 6));
        barra.setOpaque(false);

        javax.swing.JButton btnActualizar = UIHelper.crearBotonSecundario("Actualizar");
        btnActualizar.addActionListener(e -> cargar.run());

        javax.swing.JButton btnRenovar = UIHelper.crearBotonSecundario("Renovar (+7 días)");
        btnRenovar.addActionListener(e -> {
            int row = tablaHistorialLector.getSelectedRow();
            if (row < 0) { UIHelper.mostrarAdvertencia(panelHistorialLector, "Seleccione un préstamo."); return; }
            int idPrestamo = (int) model.getValueAt(row, 0);
            try {
                new com.sibu.servicio.CirculacionServicio().realizarRenovacion(idPrestamo, usuarioActual.getIdUsuario());
                UIHelper.mostrarExito(panelHistorialLector, "Préstamo renovado exitosamente.");
                cargar.run();
            } catch (Exception ex) {
                UIHelper.mostrarAdvertencia(panelHistorialLector, ex.getMessage());
            }
        });

        javax.swing.JButton btnDevolver = UIHelper.crearBotonExito("Devolver Libro");
        btnDevolver.addActionListener(e -> {
            int row = tablaHistorialLector.getSelectedRow();
            if (row < 0) { UIHelper.mostrarAdvertencia(panelHistorialLector, "Seleccione un préstamo."); return; }
            int idPrestamo = (int) model.getValueAt(row, 0);
            String titulo  = (String) model.getValueAt(row, 2);
            String estado  = (String) model.getValueAt(row, 5);
            if ("DEVUELTO".equalsIgnoreCase(estado)) {
                UIHelper.mostrarAdvertencia(panelHistorialLector, "Este libro ya fue devuelto."); return;
            }
            int ok = JOptionPane.showConfirmDialog(panelHistorialLector,
                    "¿Devolver \"" + titulo + "\"?", "Devolución", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                try {
                    com.sibu.modelo.Sancion s =
                            new com.sibu.servicio.CirculacionServicio()
                                .registrarDevolucion(idPrestamo, usuarioActual.getIdUsuario(), "Portal Lector");
                    if (s != null) {
                        UIHelper.mostrarAdvertencia(panelHistorialLector,
                                "Devolución con retraso: " + s.getDiasRetraso() + " días. Multa: $" + s.getMonto());
                    } else {
                        UIHelper.mostrarExito(panelHistorialLector, "Devolución registrada. ¡Gracias!");
                    }
                    cargar.run();
                } catch (Exception ex) {
                    UIHelper.mostrarAdvertencia(panelHistorialLector, ex.getMessage());
                }
            }
        });

        barra.add(btnActualizar);
        barra.add(btnRenovar);
        barra.add(btnDevolver);
        panelHistorialLector.add(barra, BorderLayout.SOUTH);
        return panelHistorialLector;
    }

    // =========================================================================
    // Refresco de interfaz
    // =========================================================================

    public void refrescarTemaUI() {
        if (sidebar != null) {
            sidebar.refrescarTema();
        }
        if (panelContenido != null) {
            panelContenido.setBackground(TemaUtil.getColorFondo());
            for (java.awt.Component c : panelContenido.getComponents()) {
                if (c instanceof DashboardIntegralPanel) {
                    ((DashboardIntegralPanel) c).refrescarTema();
                } else if (c instanceof JPanel) {
                    c.setBackground(TemaUtil.getColorFondo());
                }
            }
        }
        if (panelHistorialLector != null) {
            panelHistorialLector.setBackground(TemaUtil.getColorFondo());
        }
        if (tablaHistorialLector != null) {
            tablaHistorialLector.setBackground(TemaUtil.getColorSuperficie());
            tablaHistorialLector.setForeground(TemaUtil.getColorTextoPrincipal());
            tablaHistorialLector.setGridColor(TemaUtil.getColorBorde());
            if (tablaHistorialLector.getTableHeader() != null) {
                tablaHistorialLector.getTableHeader().setBackground(TemaUtil.getColorSuperficieContenedor());
                tablaHistorialLector.getTableHeader().setForeground(TemaUtil.getColorTextoPrincipal());
            }
            UIHelper.estilizarTabla(tablaHistorialLector);
        }
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
    }

    // =========================================================================
    // Sesión
    // =========================================================================

    private void cerrarSesion() {
        if (UIHelper.confirmarAccion(this, "¿Cerrar la sesión actual?", "Cerrar Sesión")) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
