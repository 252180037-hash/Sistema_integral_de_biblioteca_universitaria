package com.sibu.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.sibu.vista.LoginFrame;
import com.sibu.vista.MenuPrincipalFrame;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Gestor de presentación visual y temas (Modo Claro / Modo Oscuro) del sistema SIBU.
 * <p>
 * Diseñado bajo el criterio de <b>Usabilidad</b> de la norma <b>ISO/IEC 25010:2023</b>.
 * Emplea FlatLaf configurado rigurosamente con la paleta botánica institucional:
 * <ul>
 *   <li><b>Tea Green:</b> #C9F2C7 (rgba(201, 242, 199, 1))</li>
 *   <li><b>Light Green:</b> #ACECA1 (rgba(172, 236, 161, 1))</li>
 *   <li><b>Muted Olive:</b> #96BE8C (rgba(150, 190, 140, 1))</li>
 *   <li><b>Sage Green:</b> #629460 (rgba(98, 148, 96, 1))</li>
 *   <li><b>Evergreen:</b> #243119 (rgba(36, 49, 25, 1))</li>
 * </ul>
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 2.2.0
 */
public final class TemaUtil {

    private static final Logger LOGGER = Logger.getLogger(TemaUtil.class.getName());

    // --- Paleta Botánica Institucional SIBU ---
    public static final Color COLOR_TEA_GREEN = new Color(201, 242, 199);   // #C9F2C7
    public static final Color COLOR_LIGHT_GREEN = new Color(172, 236, 161); // #ACECA1
    public static final Color COLOR_MUTED_OLIVE = new Color(150, 190, 140); // #96BE8C
    public static final Color COLOR_SAGE_GREEN = new Color(98, 148, 96);    // #629460
    public static final Color COLOR_EVERGREEN = new Color(36, 49, 25);       // #243119

    // --- Colores Semánticos Principales ---
    public static final Color COLOR_EXITO = new Color(20, 108, 46);          // Verde Éxito (#146C2E)
    public static final Color COLOR_PELIGRO = new Color(186, 26, 26);        // Carmesí / Error (#BA1A1A)
    public static final Color COLOR_ADVERTENCIA = new Color(140, 80, 0);     // Ámbar Advertencia (#8C5000)

    // --- Tokens Modo Claro ---
    public static final Color CLARO_BG = new Color(244, 251, 242);          // #F4FBF2
    public static final Color CLARO_SURFACE = Color.WHITE;                   // #FFFFFF
    public static final Color CLARO_SURFACE_LOW = new Color(235, 245, 232);  // #EBF5E8
    public static final Color CLARO_SURFACE_HIGH = new Color(226, 240, 222); // #E2F0DE
    public static final Color CLARO_TEXT_PRIMARY = COLOR_EVERGREEN;          // #243119
    public static final Color CLARO_TEXT_SECONDARY = new Color(61, 75, 49);  // #3D4B31
    public static final Color CLARO_BORDER = COLOR_MUTED_OLIVE;              // #96BE8C
    public static final Color CLARO_PRIMARY = new Color(55, 102, 55);        // #376637

    // --- Tokens Modo Oscuro ---
    public static final Color OSCURO_BG = new Color(11, 22, 3);             // #0B1603 (Fondo principal nocturno)
    public static final Color OSCURO_SURFACE = new Color(23, 35, 12);        // #17230C (Tarjetas y superficies)
    public static final Color OSCURO_SURFACE_LOW = new Color(19, 31, 9);     // #131F09 (Sidebar persistente)
    public static final Color OSCURO_SURFACE_HIGH = new Color(33, 46, 22);   // #212E16 (Encabezados y tablas)
    public static final Color OSCURO_TEXT_PRIMARY = new Color(216, 232, 196);// #D8E8C4 (Texto claro legible)
    public static final Color OSCURO_TEXT_SECONDARY = new Color(193, 201, 188); // #C1C9BC (Texto secundario)
    public static final Color OSCURO_BORDER = new Color(65, 73, 63);         // #41493F (Borde sutil)
    public static final Color OSCURO_PRIMARY = COLOR_LIGHT_GREEN;            // #ACECA1 (Acento claro primario)

    public static final Color COLOR_PRIMARIO = CLARO_PRIMARY;
    public static final Color COLOR_PRIMARIO_HOVER = COLOR_SAGE_GREEN;
    public static final Color COLOR_NEUTRO_BORDE = COLOR_MUTED_OLIVE;

    private static boolean modoOscuroActivo = false;

    private TemaUtil() {
    }

    public static void inicializarTema() {
        Configuracion config = Configuracion.getInstancia();
        String preferencia = config.getTemaDefault();

        if ("DARK".equalsIgnoreCase(preferencia)) {
            aplicarTemaOscuro();
        } else {
            aplicarTemaClaro();
        }
    }

    public static void aplicarTemaClaro() {
        try {
            modoOscuroActivo = false;
            // IMPORTANTE: FlatLaf.setup() PRIMERO, luego sobreescribir UIManager
            FlatLightLaf.setup();
            configurarAjustesModoClaro();
            actualizarVentanasAbiertas();
            LOGGER.info("Tema Botánico Claro aplicado exitosamente.");
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "No fue posible aplicar FlatLightLaf. Usando tema nativo del sistema.", t);
            aplicarTemaFallback();
        }
    }

    public static void aplicarTemaOscuro() {
        try {
            modoOscuroActivo = true;
            // IMPORTANTE: FlatLaf.setup() PRIMERO, luego sobreescribir UIManager
            FlatDarkLaf.setup();
            configurarAjustesModoOscuro();
            actualizarVentanasAbiertas();
            LOGGER.info("Tema Botánico Oscuro aplicado exitosamente.");
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "No fue posible aplicar FlatDarkLaf. Usando tema nativo del sistema.", t);
            aplicarTemaFallback();
        }
    }

    public static boolean alternarTema() {
        if (modoOscuroActivo) {
            aplicarTemaClaro();
            Configuracion.getInstancia().set("app.tema.default", "LIGHT");
        } else {
            aplicarTemaOscuro();
            Configuracion.getInstancia().set("app.tema.default", "DARK");
        }
        Configuracion.getInstancia().guardar();
        return modoOscuroActivo;
    }

    public static boolean esModoOscuro() {
        return modoOscuroActivo;
    }

    // --- Getters de Color Dinámicos ---
    public static Color getColorFondo() {
        return modoOscuroActivo ? OSCURO_BG : CLARO_BG;
    }

    public static Color getColorSidebar() {
        return modoOscuroActivo ? OSCURO_SURFACE_LOW : CLARO_SURFACE_LOW;
    }

    public static Color getColorSuperficie() {
        return modoOscuroActivo ? OSCURO_SURFACE : CLARO_SURFACE;
    }

    public static Color getColorSuperficieContenedor() {
        return modoOscuroActivo ? OSCURO_SURFACE_HIGH : CLARO_SURFACE_LOW;
    }

    public static Color getColorSuperficieDestacada() {
        return modoOscuroActivo ? new Color(44, 61, 30) : CLARO_SURFACE_HIGH;
    }

    public static Color getColorTextoPrincipal() {
        return modoOscuroActivo ? OSCURO_TEXT_PRIMARY : CLARO_TEXT_PRIMARY;
    }

    public static Color getColorTextoSecundario() {
        return modoOscuroActivo ? OSCURO_TEXT_SECONDARY : CLARO_TEXT_SECONDARY;
    }

    public static Color getColorBorde() {
        return modoOscuroActivo ? OSCURO_BORDER : CLARO_BORDER;
    }

    public static Color getColorPrimario() {
        return modoOscuroActivo ? OSCURO_PRIMARY : CLARO_PRIMARY;
    }

    public static Color getColorPrimarioTexto() {
        return modoOscuroActivo ? COLOR_EVERGREEN : Color.WHITE;
    }

    private static void configurarAjustesGlobalesComunes() {
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("Table.rowHeight", 32);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 12));
    }

    private static void configurarAjustesModoClaro() {
        configurarAjustesGlobalesComunes();

        UIManager.put("@accentColor", "#629460");
        UIManager.put("Component.focusColor", COLOR_SAGE_GREEN);
        UIManager.put("Component.borderColor", CLARO_BORDER);

        UIManager.put("Panel.background", CLARO_BG);
        UIManager.put("Label.foreground", CLARO_TEXT_PRIMARY);

        UIManager.put("Table.background", CLARO_SURFACE);
        UIManager.put("Table.foreground", CLARO_TEXT_PRIMARY);
        UIManager.put("Table.alternateRowColor", new Color(246, 251, 244));
        UIManager.put("Table.selectionBackground", COLOR_TEA_GREEN);
        UIManager.put("Table.selectionForeground", CLARO_TEXT_PRIMARY);
        UIManager.put("Table.gridColor", CLARO_SURFACE_HIGH);

        UIManager.put("TableHeader.background", CLARO_SURFACE_HIGH);
        UIManager.put("TableHeader.foreground", CLARO_TEXT_PRIMARY);

        UIManager.put("TextField.background", CLARO_SURFACE);
        UIManager.put("TextField.foreground", CLARO_TEXT_PRIMARY);
        UIManager.put("TextField.caretColor", CLARO_TEXT_PRIMARY);
        UIManager.put("PasswordField.background", CLARO_SURFACE);
        UIManager.put("PasswordField.foreground", CLARO_TEXT_PRIMARY);

        UIManager.put("ComboBox.background", CLARO_SURFACE);
        UIManager.put("ComboBox.foreground", CLARO_TEXT_PRIMARY);

        UIManager.put("ScrollPane.background", CLARO_BG);
        UIManager.put("Viewport.background", CLARO_SURFACE);
        UIManager.put("SplitPane.background", CLARO_BG);

        UIManager.put("TabbedPane.background", CLARO_SURFACE_LOW);
        UIManager.put("TabbedPane.foreground", CLARO_TEXT_PRIMARY);
        UIManager.put("TabbedPane.selectedBackground", CLARO_SURFACE_HIGH);
        UIManager.put("TabbedPane.selectedForeground", CLARO_PRIMARY);
        UIManager.put("TabbedPane.underlineColor", CLARO_PRIMARY);

        UIManager.put("Button.background", CLARO_SURFACE_LOW);
        UIManager.put("Button.foreground", CLARO_TEXT_PRIMARY);
        UIManager.put("Button.borderColor", CLARO_BORDER);
    }

    private static void configurarAjustesModoOscuro() {
        configurarAjustesGlobalesComunes();

        UIManager.put("@accentColor", "#ACECA1");
        UIManager.put("Component.focusColor", OSCURO_PRIMARY);
        UIManager.put("Component.borderColor", OSCURO_BORDER);

        UIManager.put("Panel.background", OSCURO_BG);
        UIManager.put("Label.foreground", OSCURO_TEXT_PRIMARY);

        UIManager.put("Table.background", OSCURO_SURFACE);
        UIManager.put("Table.foreground", OSCURO_TEXT_PRIMARY);
        UIManager.put("Table.alternateRowColor", OSCURO_SURFACE_HIGH);
        UIManager.put("Table.selectionBackground", new Color(44, 79, 39));
        UIManager.put("Table.selectionForeground", COLOR_TEA_GREEN);
        UIManager.put("Table.gridColor", OSCURO_BORDER);

        UIManager.put("TableHeader.background", OSCURO_SURFACE_HIGH);
        UIManager.put("TableHeader.foreground", OSCURO_TEXT_PRIMARY);

        UIManager.put("TextField.background", OSCURO_SURFACE);
        UIManager.put("TextField.foreground", OSCURO_TEXT_PRIMARY);
        UIManager.put("TextField.caretColor", OSCURO_PRIMARY);
        UIManager.put("PasswordField.background", OSCURO_SURFACE);
        UIManager.put("PasswordField.foreground", OSCURO_TEXT_PRIMARY);

        UIManager.put("ComboBox.background", OSCURO_SURFACE);
        UIManager.put("ComboBox.foreground", OSCURO_TEXT_PRIMARY);

        UIManager.put("ScrollPane.background", OSCURO_BG);
        UIManager.put("Viewport.background", OSCURO_SURFACE);
        UIManager.put("SplitPane.background", OSCURO_BG);

        UIManager.put("TabbedPane.background", OSCURO_SURFACE_LOW);
        UIManager.put("TabbedPane.foreground", OSCURO_TEXT_PRIMARY);
        UIManager.put("TabbedPane.selectedBackground", OSCURO_SURFACE_HIGH);
        UIManager.put("TabbedPane.selectedForeground", OSCURO_PRIMARY);
        UIManager.put("TabbedPane.underlineColor", OSCURO_PRIMARY);

        UIManager.put("Button.background", OSCURO_SURFACE_HIGH);
        UIManager.put("Button.foreground", OSCURO_TEXT_PRIMARY);
        UIManager.put("Button.borderColor", OSCURO_BORDER);
    }

    public static void actualizarVentanasAbiertas() {
        SwingUtilities.invokeLater(() -> {
            FlatLaf.updateUI();
            for (Window ventana : Window.getWindows()) {
                if (ventana instanceof MenuPrincipalFrame mpf) {
                    mpf.refrescarTemaUI();
                } else if (ventana instanceof LoginFrame lf) {
                    lf.refrescarTemaUI();
                }
            }
        });
    }

    private static void aplicarTemaFallback() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
