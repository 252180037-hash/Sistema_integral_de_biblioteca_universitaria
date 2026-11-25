package com.sibu.util;

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
 * Gestor de presentación visual y tema unificado en Modo Claro del sistema SIBU.
 * <p>
 * Diseñado conforme a los estándares de diseño moderno de la industria y la norma
 * <b>ISO/IEC 25010:2023 - Usabilidad y Confort Visual</b>:
 * <ul>
 *   <li><b>Modo Claro Institucional:</b> Fondo gris perla suave (#F6F8FA), tarjetas y hojas
 *       blancas limpias (#FFFFFF), tipografía nítida de alto contraste (#1F2328 / #656D76),
 *       verde institucional (#1F883D) y bordes sutiles (#D0D7DE).</li>
 * </ul>
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 4.0.0
 */
public final class TemaUtil {

    private static final Logger LOGGER = Logger.getLogger(TemaUtil.class.getName());

    // --- Paleta Institucional Tradicional (Compatibilidad) ---
    public static final Color COLOR_TEA_GREEN   = new Color(201, 242, 199); // #C9F2C7
    public static final Color COLOR_LIGHT_GREEN = new Color(172, 236, 161); // #ACECA1
    public static final Color COLOR_MUTED_OLIVE = new Color(150, 190, 140); // #96BE8C
    public static final Color COLOR_SAGE_GREEN  = new Color(98, 148, 96);   // #629460
    public static final Color COLOR_EVERGREEN   = new Color(36, 49, 25);    // #243119

    // --- Colores Semánticos Base ---
    public static final Color COLOR_EXITO       = new Color(31, 136, 61);   // Verde Éxito (#1F883D)
    public static final Color COLOR_PELIGRO     = new Color(207, 34, 46);   // Carmesí (#CF222E)
    public static final Color COLOR_ADVERTENCIA = new Color(154, 103, 0);   // Ámbar (#9A6700)

    // =========================================================================
    // Tokens Modo Claro Institucional (Clean Modern / GitHub Light)
    // =========================================================================
    public static final Color CLARO_BG             = new Color(246, 248, 250); // #F6F8FA (Canvas default)
    public static final Color CLARO_SURFACE        = Color.WHITE;              // #FFFFFF (Cards / Sheets / Inputs)
    public static final Color CLARO_SURFACE_LOW    = new Color(246, 248, 250); // #F6F8FA (Sidebar)
    public static final Color CLARO_SURFACE_HIGH   = new Color(234, 238, 242); // #EAEEF2 (Headers / Hover)
    public static final Color CLARO_TEXT_PRIMARY   = new Color(31, 35, 40);    // #1F2328 (Texto principal de alto contraste)
    public static final Color CLARO_TEXT_SECONDARY = new Color(101, 109, 118); // #656D76 (Texto secundario legible)
    public static final Color CLARO_BORDER         = new Color(208, 215, 222); // #D0D7DE (Borde 1px nítido)
    public static final Color CLARO_PRIMARY        = new Color(31, 136, 61);   // #1F883D (Verde institucional)

    public static final Color COLOR_PRIMARIO       = CLARO_PRIMARY;
    public static final Color COLOR_PRIMARIO_HOVER = new Color(26, 127, 55);
    public static final Color COLOR_NEUTRO_BORDE   = CLARO_BORDER;

    private TemaUtil() {
    }

    /**
     * Inicializa el tema visual predeterminado en Modo Claro.
     */
    public static void inicializarTema() {
        aplicarTemaClaro();
    }

    /**
     * Aplica el tema moderno claro institucional con FlatLightLaf.
     */
    public static void aplicarTemaClaro() {
        try {
            FlatLightLaf.setup();
            configurarAjustesModoClaro();
            actualizarVentanasAbiertas();
            LOGGER.info("Tema Moderno Claro Institucional aplicado exitosamente.");
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "No fue posible aplicar FlatLightLaf. Usando tema nativo del sistema.", t);
            aplicarTemaFallback();
        }
    }

    /**
     * Compatibilidad: Redirige a aplicarTemaClaro ya que el modo oscuro fue retirado.
     */
    public static void aplicarTemaOscuro() {
        aplicarTemaClaro();
    }

    /**
     * Compatibilidad: Retorna false ya que el modo oscuro fue retirado.
     */
    public static boolean alternarTema() {
        aplicarTemaClaro();
        return false;
    }

    /**
     * Consulta si el modo oscuro está activo. Siempre retorna false.
     */
    public static boolean esModoOscuro() {
        return false;
    }

    // --- Getters de Color Consistentes (Modo Claro) ---

    public static Color getColorFondo() {
        return CLARO_BG;
    }

    public static Color getColorSidebar() {
        return CLARO_SURFACE_LOW;
    }

    public static Color getColorSuperficie() {
        return CLARO_SURFACE;
    }

    public static Color getColorSuperficieContenedor() {
        return CLARO_SURFACE_HIGH;
    }

    public static Color getColorSuperficieDestacada() {
        return CLARO_SURFACE_HIGH;
    }

    public static Color getColorTextoPrincipal() {
        return CLARO_TEXT_PRIMARY;
    }

    public static Color getColorTextoSecundario() {
        return CLARO_TEXT_SECONDARY;
    }

    public static Color getColorBorde() {
        return CLARO_BORDER;
    }

    public static Color getColorPrimario() {
        return CLARO_PRIMARY;
    }

    public static Color getColorPrimarioTexto() {
        return Color.WHITE;
    }

    public static Color getColorExito() {
        return COLOR_EXITO;
    }

    public static Color getColorPeligro() {
        return COLOR_PELIGRO;
    }

    public static Color getColorAdvertencia() {
        return COLOR_ADVERTENCIA;
    }

    // =========================================================================
    // Configuración de UIManager para FlatLaf Modo Claro
    // =========================================================================

    private static void configurarAjustesGlobalesComunes() {
        UIManager.put("Button.arc", 6);
        UIManager.put("Component.arc", 6);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("Table.rowHeight", 34);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 12));
    }

    private static void configurarAjustesModoClaro() {
        configurarAjustesGlobalesComunes();

        UIManager.put("@accentColor", "#1F883D");
        UIManager.put("Component.focusColor", new Color(9, 105, 218));
        UIManager.put("Component.borderColor", CLARO_BORDER);

        UIManager.put("Panel.background", CLARO_BG);
        UIManager.put("Label.foreground", CLARO_TEXT_PRIMARY);

        UIManager.put("Table.background", CLARO_SURFACE);
        UIManager.put("Table.foreground", CLARO_TEXT_PRIMARY);
        UIManager.put("Table.alternateRowColor", new Color(246, 248, 250));
        UIManager.put("Table.selectionBackground", new Color(221, 244, 255));
        UIManager.put("Table.selectionForeground", CLARO_TEXT_PRIMARY);
        UIManager.put("Table.gridColor", CLARO_BORDER);

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

        UIManager.put("TabbedPane.background", CLARO_SURFACE_HIGH);
        UIManager.put("TabbedPane.foreground", CLARO_TEXT_PRIMARY);
        UIManager.put("TabbedPane.selectedBackground", CLARO_SURFACE);
        UIManager.put("TabbedPane.selectedForeground", CLARO_PRIMARY);
        UIManager.put("TabbedPane.underlineColor", CLARO_PRIMARY);

        UIManager.put("Button.background", CLARO_SURFACE);
        UIManager.put("Button.foreground", CLARO_TEXT_PRIMARY);
        UIManager.put("Button.borderColor", CLARO_BORDER);
    }

    public static void actualizarVentanasAbiertas() {
        SwingUtilities.invokeLater(() -> {
            FlatLaf.updateUI();
            for (Window ventana : Window.getWindows()) {
                if (ventana instanceof MenuPrincipalFrame) {
                    ((MenuPrincipalFrame) ventana).refrescarTemaUI();
                } else if (ventana instanceof LoginFrame) {
                    ((LoginFrame) ventana).refrescarTemaUI();
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
