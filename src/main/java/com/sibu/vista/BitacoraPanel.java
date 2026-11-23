package com.sibu.vista;

import com.sibu.modelo.AccionBitacora;
import com.sibu.modelo.Bitacora;
import com.sibu.servicio.BitacoraServicio;
import com.sibu.util.CsvExporter;
import com.sibu.vista.componentes.UIHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 * Panel para la auditoría y consulta de la bitácora de operaciones del sistema (RF7).
 * <p>
 * Registra quién, cuándo, desde qué módulo, qué acción realizó y el detalle exacto.
 * Permite filtrar por módulo, tipo de evento y rangos de fechas, con exportación a CSV.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class BitacoraPanel extends JPanel {

    private final BitacoraServicio bitacoraServicio;

    private JComboBox<String> cmbModulo;
    private JComboBox<String> cmbAccion;
    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JButton btnFiltrar;
    private JButton btnLimpiar;
    private JButton btnExportar;

    private JTable tablaBitacora;
    private DefaultTableModel modeloBitacora;
    private JLabel lblTotalRegistros;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BitacoraPanel() {
        this.bitacoraServicio = new BitacoraServicio();
        inicializarUI();
        cargarBitacoraReciente();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(UIHelper.crearEncabezadoModulo(
                "Bitácora y Trazabilidad de Auditoría del Sistema",
                "Registro inmutable de operaciones, autenticaciones y transacciones de base de datos (RF7)."
        ), BorderLayout.NORTH);

        // ---------------------------------------------------------------------
        // Barra Superior: Filtros
        // ---------------------------------------------------------------------
        JPanel panelFiltros = UIHelper.crearTarjeta();
        panelFiltros.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Fila 0: Etiquetas
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 0.25;
        panelFiltros.add(new JLabel("Módulo del Sistema:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.25;
        panelFiltros.add(new JLabel("Acción Realizada:"), gbc);

        gbc.gridx = 2; gbc.weightx = 0.50;
        panelFiltros.add(new JLabel("Rango de Fechas (YYYY-MM-DD):"), gbc);

        // Fila 1: Controles
        gbc.gridy = 1;
        gbc.gridx = 0;
        cmbModulo = new JComboBox<>(new String[]{
                "TODOS LOS MÓDULOS", "CATALOGO", "CIRCULACION", "USUARIOS", "SEGURIDAD", "RESPALDO", "ADMIN_DATOS"
        });
        cmbModulo.setFont(UIHelper.FUENTE_REGULAR);
        panelFiltros.add(cmbModulo, gbc);

        gbc.gridx = 1;
        cmbAccion = new JComboBox<>();
        cmbAccion.setFont(UIHelper.FUENTE_REGULAR);
        cmbAccion.addItem("TODAS LAS ACCIONES");
        for (AccionBitacora acc : AccionBitacora.values()) {
            cmbAccion.addItem(acc.name());
        }
        panelFiltros.add(cmbAccion, gbc);

        gbc.gridx = 2;
        JPanel panelFechas = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        txtFechaInicio = UIHelper.crearCampoTexto(10);
        txtFechaFin = UIHelper.crearCampoTexto(10);
        panelFechas.add(txtFechaInicio);
        panelFechas.add(new JLabel("a"));
        panelFechas.add(txtFechaFin);
        panelFiltros.add(panelFechas, gbc);

        // Fila 2: Botones
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 3;
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));

        btnLimpiar = UIHelper.crearBotonSecundario("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarFiltros());

        btnFiltrar = UIHelper.crearBotonPrimario("Aplicar Filtro");
        btnFiltrar.addActionListener(e -> filtrarBitacora());

        btnExportar = UIHelper.crearBotonExito("Exportar a CSV");
        btnExportar.addActionListener(e -> exportarCsv());

        panelBotones.add(btnLimpiar);
        panelBotones.add(btnFiltrar);
        panelBotones.add(btnExportar);
        panelFiltros.add(panelBotones, gbc);

        // ---------------------------------------------------------------------
        // Centro: Tabla de Bitácora
        // ---------------------------------------------------------------------
        String[] columnas = {"ID", "Fecha / Hora", "Módulo", "Acción", "Usuario / Operador", "Detalle de la Operación", "Host / IP"};
        modeloBitacora = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaBitacora = new JTable(modeloBitacora);
        UIHelper.estilizarTabla(tablaBitacora);
        tablaBitacora.getColumnModel().getColumn(5).setPreferredWidth(350); // Columna detalle más ancha
        JScrollPane scrollTabla = new JScrollPane(tablaBitacora);

        // Pie
        JPanel panelPie = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        lblTotalRegistros = new JLabel("Registros cargados: 0");
        lblTotalRegistros.setFont(UIHelper.FUENTE_LABEL);
        panelPie.add(lblTotalRegistros);

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.add(panelFiltros, BorderLayout.NORTH);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);
        panelCentro.add(panelPie, BorderLayout.SOUTH);

        add(panelCentro, BorderLayout.CENTER);
    }

    private void cargarBitacoraReciente() {
        modeloBitacora.setRowCount(0);
        try {
            List<Bitacora> lista = bitacoraServicio.listarRecientes(150);
            poblarTabla(lista);
        } catch (SQLException e) {
            UIHelper.mostrarError(this, "Fallo al consultar la bitácora: " + e.getMessage());
        }
    }

    private void filtrarBitacora() {
        String modulo = (String) cmbModulo.getSelectedItem();
        if (modulo != null && modulo.startsWith("TODOS")) {
            modulo = null;
        }

        String accionStr = (String) cmbAccion.getSelectedItem();
        AccionBitacora accion = null;
        if (accionStr != null && !accionStr.startsWith("TODAS")) {
            accion = AccionBitacora.valueOf(accionStr);
        }

        LocalDate inicio = null;
        LocalDate fin = null;

        try {
            if (!txtFechaInicio.getText().trim().isEmpty()) {
                inicio = LocalDate.parse(txtFechaInicio.getText().trim());
            }
            if (!txtFechaFin.getText().trim().isEmpty()) {
                fin = LocalDate.parse(txtFechaFin.getText().trim());
            }
        } catch (Exception e) {
            UIHelper.mostrarAdvertencia(this, "Las fechas deben cumplir el formato YYYY-MM-DD.");
            return;
        }

        modeloBitacora.setRowCount(0);
        try {
            List<Bitacora> lista = bitacoraServicio.filtrarBitacora(modulo, accion, inicio, fin);
            poblarTabla(lista);
        } catch (SQLException e) {
            UIHelper.mostrarError(this, "Error al filtrar bitácora: " + e.getMessage());
        }
    }

    private void poblarTabla(List<Bitacora> lista) {
        for (Bitacora b : lista) {
            String operador = (b.getNombreUsuario() != null)
                    ? b.getNombreUsuario() + " (" + (b.getIdentificacionUsuario() != null ? b.getIdentificacionUsuario() : "ID:" + b.getIdUsuario()) + ")"
                    : "Sistema / Desconocido";

            modeloBitacora.addRow(new Object[]{
                    b.getIdBitacora(),
                    b.getFecha() != null ? b.getFecha().format(FORMATO_HORA) : "",
                    b.getModulo(),
                    b.getAccion().name(),
                    operador,
                    b.getDetalle(),
                    b.getIpHost()
            });
        }
        lblTotalRegistros.setText("Total registros visualizados: " + lista.size());
    }

    private void limpiarFiltros() {
        cmbModulo.setSelectedIndex(0);
        cmbAccion.setSelectedIndex(0);
        txtFechaInicio.setText("");
        txtFechaFin.setText("");
        cargarBitacoraReciente();
    }

    private void exportarCsv() {
        if (modeloBitacora.getRowCount() == 0) {
            UIHelper.mostrarAdvertencia(this, "No hay registros en la tabla para exportar.");
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar Bitácora a CSV");
        fc.setFileFilter(new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));
        fc.setSelectedFile(new File("bitacora_sibu_" + System.currentTimeMillis() + ".csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dest = fc.getSelectedFile();
            if (!dest.getName().toLowerCase().endsWith(".csv")) {
                dest = new File(dest.getAbsolutePath() + ".csv");
            }

            try {
                int total = CsvExporter.exportarTabla(tablaBitacora, dest);
                UIHelper.mostrarExito(this, "Bitácora exportada exitosamente (" + total + " registros) en:\n" + dest.getAbsolutePath());
            } catch (Exception e) {
                UIHelper.mostrarError(this, "Fallo al exportar bitácora: " + e.getMessage());
            }
        }
    }
}
