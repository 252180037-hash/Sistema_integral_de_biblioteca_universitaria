package com.sibu.vista.componentes;

import com.sibu.util.TemaUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Proveedor de estilos visuales uniformes y componentes desacoplados para la interfaz Swing de SIBU.
 * <p>
 * Diseñado conforme a la norma <b>ISO/IEC 25010:2023 (Usabilidad y Estética de Interfaz)</b>.
 * Implementa la paleta botánica institucional (Evergreen, Sage, Tea Green, Muted Olive, Light Green)
 * con soporte reactivo tanto en Modo Claro como en Modo Oscuro.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 2.1.0
 */
public final class UIHelper {

    public static final Font FUENTE_DISPLAY = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 17);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FUENTE_SECCION = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FUENTE_LABEL = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FUENTE_REGULAR = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FUENTE_BOTON = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FUENTE_MONO = new Font("Monospaced", Font.PLAIN, 11);

    private UIHelper() {
    }

    /**
     * Crea un panel contenedor con margen interno uniforme y fondo reactivo.
     */
    public static JPanel crearPanelContenedor() {
        JPanel panel = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorFondo());
            }
        };
        panel.setBackground(TemaUtil.getColorFondo());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }

    /**
     * Crea una tarjeta con borde suave y relleno interno adaptada a la paleta botánica.
     */
    public static JPanel crearTarjeta() {
        JPanel card = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorSuperficie());
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)
                ));
            }
        };
        card.setBackground(TemaUtil.getColorSuperficie());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return card;
    }

    /**
     * Crea una tarjeta KPI destacada con reactividad total al cambio de tema.
     */
    public static JPanel crearKpiCard(String titulo, String valor, String subtitulo, String iconChar, Color iconColor) {
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FUENTE_SUBTITULO);
        lblTitulo.setForeground(TemaUtil.getColorTextoSecundario());

        JLabel lblIcon = new JLabel(iconChar);
        lblIcon.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblIcon.setForeground(iconColor != null ? iconColor : TemaUtil.getColorPrimario());

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblValor.setForeground(TemaUtil.getColorTextoPrincipal());

        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(TemaUtil.getColorTextoSecundario());

        JPanel card = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorSuperficie());
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)
                ));
                if (lblTitulo != null) lblTitulo.setForeground(TemaUtil.getColorTextoSecundario());
                if (lblValor != null) lblValor.setForeground(TemaUtil.getColorTextoPrincipal());
                if (lblSub != null) lblSub.setForeground(TemaUtil.getColorTextoSecundario());
                if (lblIcon != null) lblIcon.setForeground(iconColor != null ? iconColor : TemaUtil.getColorPrimario());
            }
        };
        card.setBackground(TemaUtil.getColorSuperficie());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUtil.getColorBorde(), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.setOpaque(false);
        top.add(lblTitulo, BorderLayout.WEST);
        top.add(lblIcon, BorderLayout.EAST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(lblValor);
        center.add(Box.createVerticalStrut(3));
        center.add(lblSub);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    /**
     * Crea un encabezado de módulo con título y descripción institucional reactivo.
     */
    public static JPanel crearEncabezadoModulo(String titulo, String descripcion) {
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FUENTE_TITULO);
        lblTitulo.setForeground(TemaUtil.getColorTextoPrincipal());

        JLabel lblDesc = new JLabel(descripcion);
        lblDesc.setFont(FUENTE_SUBTITULO);
        lblDesc.setForeground(TemaUtil.getColorTextoSecundario());

        JPanel panel = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                if (lblTitulo != null) lblTitulo.setForeground(TemaUtil.getColorTextoPrincipal());
                if (lblDesc != null) lblDesc.setForeground(TemaUtil.getColorTextoSecundario());
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        panel.add(lblTitulo);
        panel.add(Box.createVerticalStrut(2));
        panel.add(lblDesc);

        return panel;
    }

    /**
     * Crea un botón de acción principal con acento botánico Deep Sage.
     */
    public static JButton crearBotonPrimario(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorPrimario());
                setForeground(TemaUtil.getColorPrimarioTexto());
            }
        };
        btn.setFont(FUENTE_BOTON);
        btn.setBackground(TemaUtil.getColorPrimario());
        btn.setForeground(TemaUtil.getColorPrimarioTexto());
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 32));
        return btn;
    }

    /**
     * Crea un botón de éxito (verde institucional para confirmaciones).
     */
    public static JButton crearBotonExito(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorExito());
                setForeground(Color.WHITE);
            }
        };
        btn.setFont(FUENTE_BOTON);
        btn.setBackground(TemaUtil.getColorExito());
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 32));
        return btn;
    }

    /**
     * Crea un botón de peligro (carmesí para cancelaciones o bajas).
     */
    public static JButton crearBotonPeligro(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorPeligro());
                setForeground(Color.WHITE);
            }
        };
        btn.setFont(FUENTE_BOTON);
        btn.setBackground(TemaUtil.getColorPeligro());
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 32));
        return btn;
    }

    /**
     * Crea un botón secundario / neutro con borde sutil.
     */
    public static JButton crearBotonSecundario(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(TemaUtil.getColorSuperficieContenedor());
                setForeground(TemaUtil.getColorTextoPrincipal());
            }
        };
        btn.setFont(FUENTE_BOTON);
        btn.setBackground(TemaUtil.getColorSuperficieContenedor());
        btn.setForeground(TemaUtil.getColorTextoPrincipal());
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 16, 32));
        return btn;
    }

    /**
     * Crea un campo de texto estilizado con tamaño estándar.
     */
    public static JTextField crearCampoTexto(int columnas) {
        JTextField tf = new JTextField(columnas);
        tf.setFont(FUENTE_REGULAR);
        tf.setMargin(new Insets(4, 8, 4, 8));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 32));
        return tf;
    }

    /**
     * Aplica configuraciones visuales modernas a una tabla `JTable`, incluyendo
     * renderizado de badges para estados de circulación y disponibilidad.
     */
    public static void estilizarTabla(JTable tabla) {
        tabla.setFont(FUENTE_REGULAR);
        tabla.setRowHeight(32);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(FUENTE_LABEL);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        // Renderer con formato de píldoras / badges
        DefaultTableCellRenderer badgeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String valStr = value != null ? value.toString().toUpperCase() : "";

                if (!isSelected) {
                    if (valStr.contains("DISPONIBLE") || valStr.contains("ACTIVO") || valStr.contains("DEVUELTO")) {
                        l.setForeground(TemaUtil.getColorExito());
                        l.setFont(FUENTE_LABEL);
                    } else if (valStr.contains("PRESTADO") || valStr.contains("EN MORA") || valStr.contains("RENOVADO")) {
                        l.setForeground(TemaUtil.getColorAdvertencia());
                        l.setFont(FUENTE_LABEL);
                    } else if (valStr.contains("VENCIDO") || valStr.contains("SANCIONADO") || valStr.contains("DAÑADO") || valStr.contains("BAJA")) {
                        l.setForeground(TemaUtil.getColorPeligro());
                        l.setFont(FUENTE_LABEL);
                    } else {
                        l.setForeground(TemaUtil.getColorTextoPrincipal());
                        l.setFont(FUENTE_REGULAR);
                    }
                }
                return l;
            }
        };

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            String colName = tabla.getColumnName(i).toLowerCase();
            if (colName.contains("estado") || colName.contains("disponib") || colName.contains("sanción") || colName.contains("retraso")) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(badgeRenderer);
            } else if (colName.contains("id") || colName.contains("año") || colName.contains("código") || colName.contains("fecha") || colName.contains("copias")) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    public static void mostrarExito(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "SIBU — Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "SIBU — Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void mostrarAdvertencia(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "SIBU — Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmarAccion(Component padre, String mensaje, String titulo) {
        int r = JOptionPane.showConfirmDialog(padre, mensaje, titulo, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }
}
