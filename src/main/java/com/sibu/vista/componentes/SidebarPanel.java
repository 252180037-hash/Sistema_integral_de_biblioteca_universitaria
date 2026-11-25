package com.sibu.vista.componentes;

import com.sibu.modelo.RolUsuario;
import com.sibu.modelo.Usuario;
import com.sibu.util.TemaUtil;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Barra lateral persistente y colapsable (Sidebar) de navegación institucional para SIBU.
 * <p>
 * Diseñada bajo la norma <b>ISO/IEC 25010:2023 - Usabilidad y Estética</b>:
 * <ul>
 *   <li>Estado Expandido: 260 px con iconos universales, etiquetas legibles y tarjeta de usuario.</li>
 *   <li>Estado Colapsado: 64 px sólo iconos con tooltips.</li>
 *   <li>Modo Claro Unificado con paleta botánica institucional.</li>
 * </ul>
 * </p>
 *
 * @author SIBU Arquitectura
 * @version 4.0.0
 */
public class SidebarPanel extends JPanel {

    public static final int ANCHO_EXPANDIDO = 260;
    public static final int ANCHO_COLAPSADO = 64;

    private static final int ALTO_ITEM_NAV   = 40;
    private static final int ALTO_BTN_ACCION = 34;

    // Glifos universales Java 8 Windows
    private static final String GLIFO_TOGGLE = "\u2261"; // ≡
    private static final String GLIFO_PUNTO  = "\u25AA"; // ▪
    private static final String GLIFO_ROMBO  = "\u25C6"; // ◆
    private static final String GLIFO_FLECHA = "\u25B6"; // ▶
    private static final String GLIFO_CIRCULO= "\u25CF"; // ●
    private static final String GLIFO_CERRAR = "\u2715"; // ✕

    private boolean expandido = true;
    private Consumer<Boolean> onToggleExpand;

    // Componentes visuales
    private final JButton btnToggle;
    private final JLabel  lblBrand;
    private final JLabel  lblSubtitulo;
    private final JPanel  cardUser;
    private final JLabel  lblNombreUsuario;
    private final JLabel  lblRolPill;
    private final JLabel  lblIdentificacion;
    private final JLabel  lblSeccion1;
    private final JLabel  lblSeccion2;
    private final JButton btnLogout;

    private final List<NavItem> navItems = new ArrayList<NavItem>();
    private final Map<JButton, String> mapBtnCard = new LinkedHashMap<JButton, String>();

    public SidebarPanel(Usuario usuario,
                        final NavCallback onNavegacion,
                        final Runnable onLogout) {
        this(
                usuario.getNombreCompleto(),
                usuario.getIdentificacion(),
                usuario.getRol().getEtiqueta(),
                usuario.getRol(),
                onNavegacion,
                onLogout
        );
    }

    public SidebarPanel(Usuario usuario,
                        final NavCallback onNavegacion,
                        final Runnable onToggleTemaIgnored,
                        final Runnable onLogout) {
        this(
                usuario.getNombreCompleto(),
                usuario.getIdentificacion(),
                usuario.getRol().getEtiqueta(),
                usuario.getRol(),
                onNavegacion,
                onLogout
        );
    }

    public SidebarPanel(String nombreCompleto, String identificacion,
                        String rolEtiqueta, RolUsuario rol,
                        final NavCallback onNavegacion,
                        final Runnable onToggleTemaIgnored,
                        final Runnable onLogout) {
        this(nombreCompleto, identificacion, rolEtiqueta, rol, onNavegacion, onLogout);
    }

    public SidebarPanel(String nombreCompleto, String identificacion,
                        String rolEtiqueta, RolUsuario rol,
                        final NavCallback onNavegacion,
                        final Runnable onLogout) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(ANCHO_EXPANDIDO, 0));
        actualizarColores();

        // 1. Botón Toggle
        btnToggle = crearBotonToggle();
        btnToggle.addActionListener(e -> alternar());

        // 2. Brand
        lblBrand = new JLabel("SIBU", SwingConstants.LEFT);
        lblBrand.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblBrand.setForeground(TemaUtil.getColorTextoPrincipal());
        lblBrand.setAlignmentX(LEFT_ALIGNMENT);

        lblSubtitulo = new JLabel("Biblioteca Universitaria", SwingConstants.LEFT);
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSubtitulo.setForeground(TemaUtil.getColorTextoSecundario());
        lblSubtitulo.setAlignmentX(LEFT_ALIGNMENT);

        // 3. Tarjeta de Usuario
        cardUser = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorSuperficie());
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        };
        cardUser.setOpaque(true);
        cardUser.setBackground(TemaUtil.getColorSuperficie());
        cardUser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        cardUser.setLayout(new BoxLayout(cardUser, BoxLayout.Y_AXIS));
        cardUser.setAlignmentX(LEFT_ALIGNMENT);
        cardUser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        lblNombreUsuario = new JLabel(nombreCompleto);
        lblNombreUsuario.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblNombreUsuario.setForeground(TemaUtil.getColorTextoPrincipal());
        lblNombreUsuario.setAlignmentX(LEFT_ALIGNMENT);

        lblRolPill = new JLabel("  " + rolEtiqueta.toUpperCase() + "  ");
        lblRolPill.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblRolPill.setOpaque(true);
        actualizarEstiloRolPill();
        lblRolPill.setAlignmentX(LEFT_ALIGNMENT);

        lblIdentificacion = new JLabel("ID: " + identificacion);
        lblIdentificacion.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblIdentificacion.setForeground(TemaUtil.getColorTextoSecundario());
        lblIdentificacion.setAlignmentX(LEFT_ALIGNMENT);

        cardUser.add(lblNombreUsuario);
        cardUser.add(Box.createVerticalStrut(3));
        cardUser.add(lblRolPill);
        cardUser.add(Box.createVerticalStrut(3));
        cardUser.add(lblIdentificacion);

        // 4. Secciones de navegación
        lblSeccion1 = crearEtiquetaSeccion("OPERACIÓN");
        lblSeccion2 = crearEtiquetaSeccion("SISTEMA");

        // Configuración de items con glifos limpios
        navItems.add(new NavItem("DASHBOARD",    GLIFO_PUNTO,   "Resumen General",        "DASHBOARD_INTEGRAL"));
        navItems.add(new NavItem("BUSQUEDA",     GLIFO_ROMBO,   "Búsqueda Catálogo",       "BUSQUEDA_CATALOGO"));

        if (rol == RolUsuario.ADMINISTRADOR || rol == RolUsuario.BIBLIOTECARIO) {
            navItems.add(new NavItem("CATALOGO",    GLIFO_PUNTO,  "Gestión Catálogo",        "CATALOGO_LIBROS"));
            navItems.add(new NavItem("CIRCULACION", GLIFO_FLECHA, "Circulación",             "CIRCULACION"));
            navItems.add(new NavItem("REPORTES",    GLIFO_PUNTO,  "Reportes CSV",            "REPORTES"));
        }

        if (rol == RolUsuario.ADMINISTRADOR) {
            navItems.add(new NavItem("USUARIOS",    GLIFO_CIRCULO,"Gestión Usuarios",        "USUARIOS"));
            navItems.add(new NavItem("BITACORA",    GLIFO_TOGGLE, "Bitácora Auditoría",      "BITACORA"));
            navItems.add(new NavItem("ADMIN_DATOS", GLIFO_PUNTO,  "Respaldos y Seeders",     "ADMIN_DATOS"));
        }

        if (rol == RolUsuario.LECTOR) {
            navItems.add(new NavItem("HISTORIAL",   GLIFO_ROMBO,  "Mis Préstamos",           "HISTORIAL_LECTOR"));
        }

        for (NavItem item : navItems) {
            JButton btn = crearBotonNav(item, false);
            btn.addActionListener(e -> {
                onNavegacion.navegar(item.cardId);
                marcarActivo(btn);
            });
            mapBtnCard.put(btn, item.cardId);
        }

        if (!mapBtnCard.isEmpty()) {
            marcarActivo(mapBtnCard.keySet().iterator().next());
        }

        // 5. Botón Inferior de Cerrar Sesión
        btnLogout = crearBotonAccion(GLIFO_CERRAR + "  Cerrar Sesión",
                TemaUtil.getColorPeligro(), Color.WHITE);
        btnLogout.addActionListener(e -> onLogout.run());

        construirLayout();
    }

    private void actualizarEstiloRolPill() {
        lblRolPill.setBackground(TemaUtil.COLOR_TEA_GREEN);
        lblRolPill.setForeground(TemaUtil.COLOR_EVERGREEN);
        lblRolPill.setBorder(BorderFactory.createLineBorder(TemaUtil.COLOR_MUTED_OLIVE, 1, true));
    }

    private void construirLayout() {
        removeAll();

        int padH = expandido ? 14 : 8;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, TemaUtil.getColorBorde()),
                BorderFactory.createEmptyBorder(14, padH, 14, padH)
        ));

        // Toggle
        add(btnToggle);
        add(Box.createVerticalStrut(10));

        // Brand
        if (expandido) {
            add(lblBrand);
            add(Box.createVerticalStrut(2));
            add(lblSubtitulo);
            add(Box.createVerticalStrut(14));
            add(cardUser);
            add(Box.createVerticalStrut(14));
            add(lblSeccion1);
            add(Box.createVerticalStrut(6));
        } else {
            JLabel ico = new JLabel(GLIFO_PUNTO, SwingConstants.CENTER);
            ico.setFont(new Font("SansSerif", Font.BOLD, 18));
            ico.setForeground(TemaUtil.getColorPrimario());
            ico.setAlignmentX(CENTER_ALIGNMENT);
            add(ico);
            add(Box.createVerticalStrut(14));
        }

        // Navegación
        boolean secAdminMostrada = false;
        for (JButton btn : mapBtnCard.keySet()) {
            String cardId = mapBtnCard.get(btn);
            if (!secAdminMostrada && expandido &&
                    (cardId.equals("USUARIOS") || cardId.equals("BITACORA") || cardId.equals("ADMIN_DATOS"))) {
                add(Box.createVerticalStrut(10));
                add(lblSeccion2);
                add(Box.createVerticalStrut(6));
                secAdminMostrada = true;
            }

            for (NavItem ni : navItems) {
                if (ni.cardId.equals(cardId)) {
                    btn.setText(expandido ? (ni.icono + "  " + ni.etiqueta) : ni.icono);
                    break;
                }
            }
            add(btn);
            add(Box.createVerticalStrut(3));
        }

        add(Box.createVerticalGlue());

        // Botón inferior: Cerrar Sesión
        configurarBotonInferior(btnLogout);
        add(btnLogout);

        revalidate();
        repaint();
    }

    private void alternar() {
        expandido = !expandido;
        int nuevoAncho = expandido ? ANCHO_EXPANDIDO : ANCHO_COLAPSADO;
        setPreferredSize(new Dimension(nuevoAncho, 0));
        btnToggle.setText(expandido ? (GLIFO_TOGGLE + "  Contraer") : GLIFO_TOGGLE);
        btnToggle.setHorizontalAlignment(expandido ? SwingConstants.LEFT : SwingConstants.CENTER);
        construirLayout();
        if (onToggleExpand != null) {
            onToggleExpand.accept(expandido);
        }
    }

    private JButton crearBotonToggle() {
        JButton btn = new JButton(GLIFO_TOGGLE + "  Contraer");
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setForeground(TemaUtil.getColorTextoSecundario());
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return btn;
    }

    private JButton crearBotonNav(NavItem item, boolean activo) {
        JButton btn = new JButton(expandido ? (item.icono + "  " + item.etiqueta) : item.icono);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTO_ITEM_NAV));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(expandido ? SwingConstants.LEFT : SwingConstants.CENTER);
        btn.setToolTipText(expandido ? null : item.etiqueta);

        if (activo) {
            estilizarNavActivo(btn);
        } else {
            estilizarNavInactivo(btn);
        }
        return btn;
    }

    private JButton crearBotonAccion(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTO_BTN_ACCION));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return btn;
    }

    private JLabel crearEtiquetaSeccion(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TemaUtil.getColorTextoSecundario());
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    public void marcarActivo(JButton activo) {
        for (JButton b : mapBtnCard.keySet()) {
            if (b == activo) {
                estilizarNavActivo(b);
            } else {
                estilizarNavInactivo(b);
            }
        }
    }

    public void seleccionarPorCardId(String cardId) {
        if (cardId == null) {
            return;
        }
        for (Map.Entry<JButton, String> entry : mapBtnCard.entrySet()) {
            if (cardId.equals(entry.getValue())) {
                marcarActivo(entry.getKey());
                break;
            }
        }
    }

    private void estilizarNavActivo(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setOpaque(true);
        btn.setBackground(TemaUtil.getColorSuperficieContenedor());
        btn.setForeground(TemaUtil.getColorTextoPrincipal());
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUtil.getColorPrimario(), 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private void estilizarNavInactivo(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setOpaque(false);
        btn.setBackground(null);
        btn.setForeground(TemaUtil.getColorTextoSecundario());
        btn.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    private void configurarBotonInferior(JButton btn) {
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTO_BTN_ACCION));
        if (!expandido) {
            String txt = btn.getText();
            if (txt.contains("Cerrar")) {
                btn.setText(GLIFO_CERRAR);
            }
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setToolTipText(txt);
        } else {
            btn.setHorizontalAlignment(SwingConstants.LEFT);
        }
    }

    public void refrescarTema() {
        actualizarColores();

        lblBrand.setForeground(TemaUtil.getColorTextoPrincipal());
        lblSubtitulo.setForeground(TemaUtil.getColorTextoSecundario());
        btnToggle.setForeground(TemaUtil.getColorTextoSecundario());
        btnToggle.setText(expandido ? (GLIFO_TOGGLE + "  Contraer") : GLIFO_TOGGLE);

        lblNombreUsuario.setForeground(TemaUtil.getColorTextoPrincipal());
        lblIdentificacion.setForeground(TemaUtil.getColorTextoSecundario());
        lblSeccion1.setForeground(TemaUtil.getColorTextoSecundario());
        lblSeccion2.setForeground(TemaUtil.getColorTextoSecundario());

        cardUser.setBackground(TemaUtil.getColorSuperficie());
        cardUser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        actualizarEstiloRolPill();

        for (JButton b : mapBtnCard.keySet()) {
            if (b.getFont().isBold()) {
                estilizarNavActivo(b);
            } else {
                estilizarNavInactivo(b);
            }
        }

        btnLogout.setBackground(TemaUtil.getColorPeligro());
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setText(expandido ? (GLIFO_CERRAR + "  Cerrar Sesión") : GLIFO_CERRAR);

        revalidate();
        repaint();
    }

    private void actualizarColores() {
        setBackground(TemaUtil.getColorSidebar());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, TemaUtil.getColorBorde()),
                BorderFactory.createEmptyBorder(14, expandido ? 14 : 8, 14, expandido ? 14 : 8)
        ));
    }

    public boolean isExpandido() {
        return expandido;
    }

    public void setOnToggleExpand(Consumer<Boolean> onToggleExpand) {
        this.onToggleExpand = onToggleExpand;
    }

    public interface NavCallback {
        void navegar(String cardId);
    }

    private static final class NavItem {
        final String clave;
        final String icono;
        final String etiqueta;
        final String cardId;

        NavItem(String clave, String icono, String etiqueta, String cardId) {
            this.clave    = clave;
            this.icono    = icono;
            this.etiqueta = etiqueta;
            this.cardId   = cardId;
        }
    }
}
