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
 * Emplea FlatLaf para brindar una interfaz moderna, limpia y profesional, evitando
 * saturaciones visuales, colores neón y tonos pasteles. Utiliza una paleta sobria
 * basada en tonos pizarra (Slate), grafito y acentos azul corporativo de alto contraste.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public final class TemaUtil {

    private static final Logger LOGGER = Logger.getLogger(TemaUtil.class.getName());

    // Paleta de Colores Corporativos Sobrios (Neutros, elegantes, sin estridencias)
    public static final Color COLOR_PRIMARIO = new Color(30, 58, 138);       // Azul institucional profundo (#1E3A8A)
    public static final Color COLOR_PRIMARIO_HOVER = new Color(29, 78, 216); // Azul intermedio (#1D4ED8)
    public static final Color COLOR_EXITO = new Color(22, 101, 52);          // Verde bosque sobrio (#166534)
    public static final Color COLOR_PELIGRO = new Color(153, 27, 27);        // Rojo carmesí apagado (#991B1B)
    public static final Color COLOR_ADVERTENCIA = new Color(161, 98, 7);     // Ámbar oscuro (#A16207)
    public static final Color COLOR_NEUTRO_BORDE = new Color(203, 213, 225); // Gris neutro suave (#CBD5E1)

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

        configurarAjustesGlobalesUI();
    }

    /**
     * Aplica el Look & Feel de Modo Claro moderno.
     */
    public static void aplicarTemaClaro() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            modoOscuroActivo = false;
            actualizarVentanasAbiertas();
            LOGGER.info("Tema Claro (FlatLightLaf) aplicado exitosamente.");
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "No fue posible aplicar FlatLightLaf. Usando tema nativo del sistema.", t);
            aplicarTemaFallback();
        }
    }

    /**
     * Aplica el Look & Feel de Modo Oscuro moderno.
     */
    public static void aplicarTemaOscuro() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            modoOscuroActivo = true;
            actualizarVentanasAbiertas();
            LOGGER.info("Tema Oscuro (FlatDarkLaf) aplicado exitosamente.");
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

    /**
     * Indica si el modo oscuro está activo actualmente.
     * 
     * @return {@code true} si está en modo oscuro; {@code false} en caso contrario.
     */
    public static boolean esModoOscuro() {
        return modoOscuroActivo;
    }

    /**
     * Refresca la jerarquía de componentes Swing en todas las ventanas activas para
     * aplicar el cambio de Look & Feel en caliente sin parpadeos ni reinicio.
     */
    private static void actualizarVentanasAbiertas() {
        SwingUtilities.invokeLater(() -> {
            for (Window ventana : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(ventana);
            }
        });
    }

    /**
     * Aplica ajustes tipográficos y de redondeo sutil a los componentes estándar de Swing.
     */
    private static void configurarAjustesGlobalesUI() {
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 12));
    }

    /**
     * Fallback seguro en caso de que FlatLaf no esté disponible en el Classpath.
     */
    private static void aplicarTemaFallback() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
