package com.sibu.vista.componentes;

import com.sibu.util.TemaUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Proveedor de estilos visuales uniformes y componentes desacoplados para la interfaz Swing de SIBU.
 * <p>
 * Diseñado conforme a la norma <b>ISO/IEC 25010:2023 (Usabilidad y Estética de Interfaz)</b>.
 * Evita el uso de colores neón y pasteles, aplicando una paleta corporativa neutra basada en
 * azul pizarra (Slate), grafito y gris humo con excelente contraste tanto en modo claro como en modo oscuro.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public final class UIHelper {

    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FUENTE_SECCION = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FUENTE_LABEL = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FUENTE_REGULAR = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FUENTE_BOTON = new Font("SansSerif", Font.BOLD, 12);

    private UIHelper() {
    }

    /**
     * Crea un panel contenedor con margen interno uniforme.
     */
    public static JPanel crearPanelContenedor() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }

    /**
     * Crea una tarjeta con borde suave y relleno interno.
     */
    public static JPanel crearTarjeta() {
        JPanel card = new JPanel();
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUtil.COLOR_NEUTRO_BORDE, 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return card;
    }

    /**
     * Crea un encabezado de módulo con título y descripción.
     */
    public static JPanel crearEncabezadoModulo(String titulo, String descripcion) {
        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FUENTE_TITULO);

        JLabel lblDesc = new JLabel(descripcion);
        lblDesc.setFont(FUENTE_SUBTITULO);
        lblDesc.setForeground(new Color(100, 116, 139));

        panel.add(lblTitulo);
        panel.add(javax.swing.Box.createVerticalStrut(3));
        panel.add(lblDesc);

        return panel;
    }

    /**
     * Crea un botón de acción principal con acento corporativo.
     */
    public static JButton crearBotonPrimario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(FUENTE_BOTON);
        btn.setBackground(TemaUtil.COLOR_PRIMARIO);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 32));
        return btn;
    }

    /**
     * Crea un botón de éxito (verde bosque sobrio para confirmaciones).
     */
    public static JButton crearBotonExito(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(FUENTE_BOTON);
        btn.setBackground(TemaUtil.COLOR_EXITO);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 32));
        return btn;
    }

    /**
     * Crea un botón de peligro (rojo carmesí apagado para cancelaciones o eliminaciones).
     */
    public static JButton crearBotonPeligro(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(FUENTE_BOTON);
        btn.setBackground(TemaUtil.COLOR_PELIGRO);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 32));
        return btn;
    }

    /**
     * Crea un botón secundario / neutro.
     */
    public static JButton crearBotonSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(FUENTE_BOTON);
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
        tf.setMargin(new Insets(4, 6, 4, 6));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 30));
        return tf;
    }

    /**
     * Aplica configuraciones visuales modernas a una tabla `JTable`.
     */
    public static void estilizarTabla(JTable tabla) {
        tabla.setFont(FUENTE_REGULAR);
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(FUENTE_LABEL);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 30));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Centrar las columnas de ID y estados habitualmente cortas
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            String colName = tabla.getColumnName(i).toLowerCase();
            if (colName.contains("id") || colName.contains("año") || colName.contains("estado") || colName.contains("código")) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    /**
     * Diálogo modal informativo de éxito.
     */
    public static void mostrarExito(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Diálogo modal de error descriptivo.
     */
    public static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error en la Operación", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Diálogo modal de advertencia preventiva.
     */
    public static void mostrarAdvertencia(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Cuadro de diálogo de confirmación obligatorio antes de acciones destructivas (ISO 25010 Usabilidad).
     */
    public static boolean confirmarAccion(Component padre, String mensaje, String titulo) {
        int r = JOptionPane.showConfirmDialog(padre, mensaje, titulo, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }
}
