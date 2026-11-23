package com.sibu.vista;

import com.sibu.servicio.ReporteServicio;
import com.sibu.servicio.exception.ValidacionException;
import com.sibu.util.Configuracion;
import com.sibu.vista.componentes.UIHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.time.LocalDate;
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
 * Panel para la generación, visualización y exportación de reportes analíticos (RF5).
 * <p>
 * Implementa los 5 reportes institucionales requeridos:
 * <ol>
 *   <li>Préstamos por periodo (rango de fechas).</li>
 *   <li>Préstamos vencidos actualmente (con cálculo de días de mora).</li>
 *   <li>Ranking de libros más prestados.</li>
 *   <li>Usuarios sancionados (activos e históricos).</li>
 *   <li>Disponibilidad de ejemplares agrupada por materia.</li>
 * </ol>
 * Cuenta con exportación directa a formato CSV compatible con Microsoft Excel (BOM UTF-8).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class ReportesPanel extends JPanel {

    private final ReporteServicio reporteServicio;

    private JComboBox<String> cmbTipoReporte;
    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JTextField txtLimiteRanking;
    private JLabel lblFiltroFecha;
    private JLabel lblFiltroLimite;

    private JTable tablaReporte;
    private DefaultTableModel modeloReporte;
    private JLabel lblTotalRegistros;

    private String[] encabezadosActuales;
    private List<String[]> datosActuales;

    public ReportesPanel() {
        this.reporteServicio = new ReporteServicio();
        inicializarUI();
        cambiarTipoReporte();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(UIHelper.crearEncabezadoModulo(
                "Centro de Reportes Estadísticos y Exportación",
                "Genere reportes analíticos de circulación, mora, ranking y catálogo con exportación a CSV (RF5)."
        ), BorderLayout.NORTH);

        // ---------------------------------------------------------------------
        // Barra Superior: Selector de Reporte y Parámetros
        // ---------------------------------------------------------------------
        JPanel panelControles = UIHelper.crearTarjeta();
        panelControles.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Fila 0: Selector de Reporte
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.40;
        JLabel lblTipo = new JLabel("Seleccione el Tipo de Reporte:");
        lblTipo.setFont(UIHelper.FUENTE_LABEL);
        panelControles.add(lblTipo, gbc);

        gbc.gridx = 1; gbc.weightx = 0.30;
        lblFiltroFecha = new JLabel("Rango de Fechas (YYYY-MM-DD):");
        lblFiltroFecha.setFont(UIHelper.FUENTE_LABEL);
        panelControles.add(lblFiltroFecha, gbc);

        gbc.gridx = 2; gbc.weightx = 0.30;
        lblFiltroLimite = new JLabel("Límite de Obras:");
        lblFiltroLimite.setFont(UIHelper.FUENTE_LABEL);
        panelControles.add(lblFiltroLimite, gbc);

        // Fila 1: Campos
        gbc.gridy = 1;
        gbc.gridx = 0;
        cmbTipoReporte = new JComboBox<>(new String[]{
                "1. Préstamos por Periodo",
                "2. Préstamos Vencidos Actualmente",
                "3. Libros Más Prestados (Ranking)",
                "4. Usuarios Sancionados (Activos e Históricos)",
                "5. Disponibilidad de Ejemplares por Materia"
        });
        cmbTipoReporte.setFont(UIHelper.FUENTE_REGULAR);
        cmbTipoReporte.addActionListener(e -> cambiarTipoReporte());
        panelControles.add(cmbTipoReporte, gbc);

        gbc.gridx = 1;
        JPanel panelFechas = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        txtFechaInicio = UIHelper.crearCampoTexto(10);
        txtFechaInicio.setText(LocalDate.now().minusDays(30).toString());
        txtFechaFin = UIHelper.crearCampoTexto(10);
        txtFechaFin.setText(LocalDate.now().toString());
        panelFechas.add(txtFechaInicio);
        panelFechas.add(new JLabel("a"));
        panelFechas.add(txtFechaFin);
        panelControles.add(panelFechas, gbc);

        gbc.gridx = 2;
        txtLimiteRanking = UIHelper.crearCampoTexto(6);
        txtLimiteRanking.setText("20");
        panelControles.add(txtLimiteRanking, gbc);

        // Fila 2: Botones
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 3;
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));

        JButton btnGenerar = UIHelper.crearBotonPrimario("Generar Reporte");
        btnGenerar.addActionListener(e -> generarReporte());

        JButton btnExportar = UIHelper.crearBotonExito("Exportar a CSV (.csv)");
        btnExportar.addActionListener(e -> exportarCsv());

        panelBotones.add(btnGenerar);
        panelBotones.add(btnExportar);
        panelControles.add(panelBotones, gbc);

        // ---------------------------------------------------------------------
        // Centro: Visualización en JTable
        // ---------------------------------------------------------------------
        modeloReporte = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaReporte = new JTable(modeloReporte);
        UIHelper.estilizarTabla(tablaReporte);
        JScrollPane scrollTabla = new JScrollPane(tablaReporte);

        // Pie de página
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        lblTotalRegistros = new JLabel("Total registros encontrados: 0");
        lblTotalRegistros.setFont(UIHelper.FUENTE_LABEL);
        panelInferior.add(lblTotalRegistros);

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.add(panelControles, BorderLayout.NORTH);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);
        panelCentro.add(panelInferior, BorderLayout.SOUTH);

        add(panelCentro, BorderLayout.CENTER);
    }

    private void cambiarTipoReporte() {
        int index = cmbTipoReporte.getSelectedIndex();
        boolean usaFechas = (index == 0);
        boolean usaLimite = (index == 2);

        txtFechaInicio.setEnabled(usaFechas);
        txtFechaFin.setEnabled(usaFechas);
        lblFiltroFecha.setEnabled(usaFechas);

        txtLimiteRanking.setEnabled(usaLimite);
        lblFiltroLimite.setEnabled(usaLimite);
    }

    private void generarReporte() {
        int index = cmbTipoReporte.getSelectedIndex();

        try {
            switch (index) {
                case 0 -> { // Préstamos por Periodo
                    LocalDate inicio = LocalDate.parse(txtFechaInicio.getText().trim());
                    LocalDate fin = LocalDate.parse(txtFechaFin.getText().trim());
                    encabezadosActuales = reporteServicio.getEncabezadosPrestamosPeriodo();
                    datosActuales = reporteServicio.generarDatosPrestamosPeriodo(inicio, fin);
                }
                case 1 -> { // Préstamos Vencidos
                    double multaDiaria = Configuracion.getInstancia().getMultaDiaria();
                    encabezadosActuales = reporteServicio.getEncabezadosPrestamosVencidos();
                    datosActuales = reporteServicio.generarDatosPrestamosVencidos(multaDiaria);
                }
                case 2 -> { // Ranking Libros
                    int limite = 20;
                    try {
                        limite = Integer.parseInt(txtLimiteRanking.getText().trim());
                    } catch (NumberFormatException ignored) {
                    }
                    encabezadosActuales = reporteServicio.getEncabezadosRankingLibros();
                    datosActuales = reporteServicio.generarDatosRankingLibros(limite);
                }
                case 3 -> { // Usuarios Sancionados
                    encabezadosActuales = reporteServicio.getEncabezadosUsuariosSancionados();
                    datosActuales = reporteServicio.generarDatosUsuariosSancionados();
                }
                case 4 -> { // Disponibilidad por Materia
                    encabezadosActuales = reporteServicio.getEncabezadosDisponibilidadMateria();
                    datosActuales = reporteServicio.generarDatosDisponibilidadMateria();
                }
            }

            // Poblar JTable
            modeloReporte.setDataVector(
                    datosActuales.toArray(new Object[0][0]),
                    encabezadosActuales
            );
            UIHelper.estilizarTabla(tablaReporte);
            lblTotalRegistros.setText("Total registros encontrados: " + datosActuales.size());

        } catch (java.time.format.DateTimeParseException e) {
            UIHelper.mostrarAdvertencia(this, "Formato de fecha inválido. Ingrese fechas con formato YYYY-MM-DD (ejemplo: 2026-05-15).");
        } catch (ValidacionException ve) {
            UIHelper.mostrarAdvertencia(this, ve.getMessage());
        } catch (Exception e) {
            UIHelper.mostrarError(this, "Error al generar reporte: " + e.getMessage());
        }
    }

    private void exportarCsv() {
        if (datosActuales == null || datosActuales.isEmpty()) {
            UIHelper.mostrarAdvertencia(this, "Debe generar primero un reporte con datos antes de exportar.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte en Formato CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));
        fileChooser.setSelectedFile(new File("reporte_sibu_" + System.currentTimeMillis() + ".csv"));

        int seleccion = fileChooser.showSaveDialog(this);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".csv")) {
                archivo = new File(archivo.getAbsolutePath() + ".csv");
            }

            try {
                int totalExportadas = reporteServicio.exportarReporteCsv(encabezadosActuales, datosActuales, archivo);
                UIHelper.mostrarExito(
                        this,
                        "Reporte exportado exitosamente con codificación UTF-8 para Excel.\n"
                        + "Total de registros exportados: " + totalExportadas + "\n"
                        + "Ubicación: " + archivo.getAbsolutePath()
                );
            } catch (Exception e) {
                UIHelper.mostrarError(this, "Fallo al exportar archivo CSV: " + e.getMessage());
            }
        }
    }
}
