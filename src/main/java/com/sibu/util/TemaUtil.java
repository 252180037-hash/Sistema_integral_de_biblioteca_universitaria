package com.sibu.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
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
 * @version 2.1.0
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
    public static final Color COLOR_PRIMARIO = new Color(55, 102, 55);       // Deep Sage (#376637)
    public static final Color COLOR_PRIMARIO_HOVER = COLOR_SAGE_GREEN;       // Sage Green (#629460)
    public static final Color COLOR_EXITO = new Color(20, 108, 46);          // Verde Éxito (#146C2E)
    public static final Color COLOR_PELIGRO = new Color(186, 26, 26);        // Carmesí / Error (#BA1A1A)
    public static final Color COLOR_ADVERTENCIA = new Color(140, 80, 0);     // Ámbar Advertencia (#8C5000)
    public static final Color COLOR_NEUTRO_BORDE = COLOR_MUTED_OLIVE;        // #96BE8C

    private static boolean modoOscuroActivo = false;

    private TemaUtil() {
    }

    /**
     * Inicializa el tema visual de la aplicación según la preferencia almacenada en {@link Configuracion}.
     */
    public static void inicializarTema() {
        Configuracion config = Configuracion.getInstancia();
        String preferencia = config.getTemaDefault();

        if ("DARK".equalsIgnoreCase(preferencia)) {
            aplicarTemaOscuro();
        } else {
            aplicarTemaClaro();
        }
    }

    /**
     * Aplica el Look & Feel de Modo Claro moderno basado en la paleta botánica.
     */
    public static void aplicarTemaClaro() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            modoOscuroActivo = false;
            configurarAjustesModoClaro();
            actualizarVentanasAbiertas();
            LOGGER.info("Tema Botánico Claro aplicado exitosamente.");
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "No fue posible aplicar FlatLightLaf. Usando tema nativo del sistema.", t);
            aplicarTemaFallback();
        }
    }

    /**
     * Aplica el Look & Feel de Modo Oscuro moderno basado en la paleta botánica.
     */
    public static void aplicarTemaOscuro() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            modoOscuroActivo = true;
            configurarAjustesModoOscuro();
            actualizarVentanasAbiertas();
            LOGGER.info("Tema Botánico Oscuro aplicado exitosamente.");
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "No fue posible aplicar FlatDarkLaf. Usando tema nativo del sistema.", t);
            aplicarTemaFallback();
        }
    }

    /**
     * Alterna dinámicamente entre Modo Claro y Modo Oscuro en tiempo de ejecución.
     * 
     * @return {@code true} si el tema resultante es Oscuro; {@code false} si es Claro.
     */
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

    // --- Selectores de Color Dinámicos ---
    public static Color getColorFondo() {
        return modoOscuroActivo ? new Color(19, 31, 9) : new Color(244, 251, 242);
    }

    public static Color getColorSuperficie() {
        return modoOscuroActivo ? new Color(26, 40, 16) : Color.WHITE;
    }

    public static Color getColorSuperficieContenedor() {
        return modoOscuroActivo ? new Color(33, 46, 22) : new Color(235, 245, 232);
    }

    public static Color getColorSuperficieDestacada() {
        return modoOscuroActivo ? new Color(44, 61, 30) : new Color(226, 240, 222);
    }

    public static Color getColorTextoPrincipal() {
        return modoOscuroActivo ? new Color(216, 232, 196) : COLOR_EVERGREEN;
    }

    public static Color getColorTextoSecundario() {
        return modoOscuroActivo ? new Color(168, 184, 150) : new Color(61, 75, 49);
    }

    public static Color getColorBorde() {
        return modoOscuroActivo ? new Color(62, 77, 48) : COLOR_MUTED_OLIVE;
    }

    public static Color getColorPrimario() {
        return modoOscuroActivo ? COLOR_LIGHT_GREEN : COLOR_PRIMARIO;
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

        Color bg = new Color(244, 251, 242);
        Color fg = COLOR_EVERGREEN;
        Color border = COLOR_MUTED_OLIVE;

        UIManager.put("@accentColor", "#629460");
        UIManager.put("Component.focusColor", COLOR_SAGE_GREEN);
        UIManager.put("Component.borderColor", border);

        UIManager.put("Panel.background", bg);
        UIManager.put("Label.foreground", fg);

        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", fg);
        UIManager.put("Table.alternateRowColor", new Color(246, 251, 244));
        UIManager.put("Table.selectionBackground", COLOR_TEA_GREEN);
        UIManager.put("Table.selectionForeground", fg);
        UIManager.put("Table.gridColor", new Color(226, 240, 222));

        UIManager.put("TableHeader.background", new Color(226, 240, 222));
        UIManager.put("TableHeader.foreground", fg);

        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", fg);
        UIManager.put("TextField.caretColor", fg);
        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("PasswordField.foreground", fg);

        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", fg);

        UIManager.put("ScrollPane.background", bg);
        UIManager.put("Viewport.background", Color.WHITE);
        UIManager.put("SplitPane.background", bg);

        UIManager.put("TabbedPane.selectedBackground", new Color(235, 245, 232));
        UIManager.put("TabbedPane.underlineColor", COLOR_PRIMARIO);
    }

    private static void configurarAjustesModoOscuro() {
        configurarAjustesGlobalesComunes();

        Color bg = new Color(19, 31, 9);           // Fondo oscuro botánico (#131F09)
        Color surface = new Color(26, 40, 16);      // Superficie tarjeta (#1A2810)
        Color fg = new Color(216, 232, 196);        // Texto claro nítido (#D8E8C4)
        Color border = new Color(62, 77, 48);       // Borde visible (#3E4D30)

        UIManager.put("@accentColor", "#ACECA1");
        UIManager.put("Component.focusColor", COLOR_LIGHT_GREEN);
        UIManager.put("Component.borderColor", border);

        UIManager.put("Panel.background", bg);
        UIManager.put("Label.foreground", fg);

        UIManager.put("Table.background", surface);
        UIManager.put("Table.foreground", fg);
        UIManager.put("Table.alternateRowColor", new Color(33, 46, 22));
        UIManager.put("Table.selectionBackground", new Color(44, 79, 39));
        UIManager.put("Table.selectionForeground", COLOR_TEA_GREEN);
        UIManager.put("Table.gridColor", new Color(38, 54, 25));

        UIManager.put("TableHeader.background", new Color(33, 46, 22));
        UIManager.put("TableHeader.foreground", fg);

        UIManager.put("TextField.background", surface);
        UIManager.put("TextField.foreground", fg);
        UIManager.put("TextField.caretColor", COLOR_LIGHT_GREEN);
        UIManager.put("PasswordField.background", surface);
        UIManager.put("PasswordField.foreground", fg);

        UIManager.put("ComboBox.background", surface);
        UIManager.put("ComboBox.foreground", fg);

        UIManager.put("ScrollPane.background", bg);
        UIManager.put("Viewport.background", surface);
        UIManager.put("SplitPane.background", bg);

        UIManager.put("TabbedPane.selectedBackground", new Color(33, 46, 22));
        UIManager.put("TabbedPane.underlineColor", COLOR_LIGHT_GREEN);
    }

    private static void actualizarVentanasAbiertas() {
        SwingUtilities.invokeLater(() -> {
            for (Window ventana : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(ventana);
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
