package com.sibu.vista;

import com.sibu.modelo.Usuario;
import com.sibu.servicio.AdministracionDatosServicio;
import com.sibu.util.Configuracion;
import com.sibu.vista.componentes.UIHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Panel para la administración de datos, respaldos, restauraciones y seeders (RF8).
 * <p>
 * Exclusivo para el perfil <b>Administrador</b>. Cuenta con advertencias de confirmación
 * obligatorias antes de ejecutar restauraciones o reinicio con seeders (ISO/IEC 25010:2023 - Usabilidad y Fiabilidad).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class AdminDatosPanel extends JPanel {

    private final AdministracionDatosServicio adminDatosServicio;
    private final Usuario operadorActual;

    private JTextField txtRutaRespaldo;
    private JTextField txtRutaRestaurar;
    private JLabel lblEstadoOperacion;

    public AdminDatosPanel(Usuario operadorActual) {
        this.adminDatosServicio = new AdministracionDatosServicio();
        this.operadorActual = operadorActual;
        inicializarUI();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(UIHelper.crearEncabezadoModulo(
                "Administración de Datos, Respaldos y Restauración",
                "Gestione copias de seguridad (.sql), restauración transaccional y población con seeders (RF8)."
        ), BorderLayout.NORTH);

        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));

        // ---------------------------------------------------------------------
        // 1. Tarjeta: Generación de Respaldo (Backup)
        // ---------------------------------------------------------------------
        JPanel cardBackup = UIHelper.crearTarjeta();
        cardBackup.setLayout(new GridBagLayout());
        cardBackup.setMaximumSize(new Dimension(850, 160));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        JLabel lblTituloBackup = new JLabel("Generación de Copia de Seguridad (.sql)");
        lblTituloBackup.setFont(UIHelper.FUENTE_SECCION);
        cardBackup.add(lblTituloBackup, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.2;
        cardBackup.add(new JLabel("Archivo de Salida:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtRutaRespaldo = UIHelper.crearCampoTexto(25);
        String fecha = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        txtRutaRespaldo.setText(new File(Configuracion.getInstancia().getBackupDir(), "sibu_backup_" + fecha + ".sql").getAbsolutePath());
        cardBackup.add(txtRutaRespaldo, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        JButton btnExaminarBackup = UIHelper.crearBotonSecundario("Examinar...");
        btnExaminarBackup.addActionListener(e -> examinarRutaRespaldo());
        cardBackup.add(btnExaminarBackup, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        JPanel panelBtnBackup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton btnEjecutarBackup = UIHelper.crearBotonPrimario("Generar Respaldo Ahora");
        btnEjecutarBackup.addActionListener(e -> ejecutarBackup());
        panelBtnBackup.add(btnEjecutarBackup);
        cardBackup.add(panelBtnBackup, gbc);

        panelCentral.add(cardBackup);
        panelCentral.add(Box.createVerticalStrut(15));

        // ---------------------------------------------------------------------
        // 2. Tarjeta: Restauración de Base de Datos (Restore)
        // ---------------------------------------------------------------------
        JPanel cardRestore = UIHelper.crearTarjeta();
        cardRestore.setLayout(new GridBagLayout());
        cardRestore.setMaximumSize(new Dimension(850, 160));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        JLabel lblTituloRestore = new JLabel("Restauración de Base de Datos (.sql)");
        lblTituloRestore.setFont(UIHelper.FUENTE_SECCION);
        cardRestore.add(lblTituloRestore, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.2;
        cardRestore.add(new JLabel("Archivo .sql a Restaurar:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtRutaRestaurar = UIHelper.crearCampoTexto(25);
        cardRestore.add(txtRutaRestaurar, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        JButton btnExaminarRestore = UIHelper.crearBotonSecundario("Examinar...");
        btnExaminarRestore.addActionListener(e -> examinarRutaRestaurar());
        cardRestore.add(btnExaminarRestore, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        JPanel panelBtnRestore = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton btnEjecutarRestore = UIHelper.crearBotonPeligro("Restaurar Base de Datos");
        btnEjecutarRestore.addActionListener(e -> ejecutarRestore());
        panelBtnRestore.add(btnEjecutarRestore);
        cardRestore.add(panelBtnRestore, gbc);

        panelCentral.add(cardRestore);
        panelCentral.add(Box.createVerticalStrut(15));

        // ---------------------------------------------------------------------
        // 3. Tarjeta: Poblado y Reinicio con Seeders
        // ---------------------------------------------------------------------
        JPanel cardSeeders = UIHelper.crearTarjeta();
        cardSeeders.setLayout(new BorderLayout(10, 10));
        cardSeeders.setMaximumSize(new Dimension(850, 110));

        JPanel panelTextoSeeder = new JPanel();
        panelTextoSeeder.setLayout(new BoxLayout(panelTextoSeeder, BoxLayout.Y_AXIS));
        JLabel lblTituloSeeders = new JLabel("Restablecimiento con Datos de Prueba (Seeders)");
        lblTituloSeeders.setFont(UIHelper.FUENTE_SECCION);
        JLabel lblDescSeeders = new JLabel("Reconstruye las tablas e inserta los usuarios de prueba, catálogo de 10 libros, ejemplares y préstamos iniciales.");
        lblDescSeeders.setFont(UIHelper.FUENTE_SUBTITULO);

        panelTextoSeeder.add(lblTituloSeeders);
        panelTextoSeeder.add(Box.createVerticalStrut(4));
        panelTextoSeeder.add(lblDescSeeders);
        cardSeeders.add(panelTextoSeeder, BorderLayout.CENTER);

        JButton btnEjecutarSeeders = UIHelper.crearBotonSecundario("Ejecutar Seeders");
        btnEjecutarSeeders.addActionListener(e -> ejecutarSeeders());
        cardSeeders.add(btnEjecutarSeeders, BorderLayout.EAST);

        panelCentral.add(cardSeeders);
        panelCentral.add(Box.createVerticalStrut(15));

        // Etiqueta de estado
        lblEstadoOperacion = new JLabel("Listo para operaciones de respaldo y administración.");
        lblEstadoOperacion.setFont(UIHelper.FUENTE_LABEL);
        panelCentral.add(lblEstadoOperacion);

        JScrollPane scrollCentral = new JScrollPane(panelCentral);
        scrollCentral.setBorder(null);
        scrollCentral.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollCentral, BorderLayout.CENTER);
    }

    private void examinarRutaRespaldo() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar Ubicación para Copia de Seguridad");
        fc.setFileFilter(new FileNameExtensionFilter("Archivos SQL (*.sql)", "sql"));
        fc.setSelectedFile(new File(txtRutaRespaldo.getText()));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".sql")) {
                f = new File(f.getAbsolutePath() + ".sql");
            }
            txtRutaRespaldo.setText(f.getAbsolutePath());
        }
    }

    private void examinarRutaRestaurar() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar Archivo de Respaldo a Restaurar");
        fc.setFileFilter(new FileNameExtensionFilter("Archivos SQL (*.sql)", "sql"));
        fc.setCurrentDirectory(new File(Configuracion.getInstancia().getBackupDir()));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtRutaRestaurar.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void ejecutarBackup() {
        String ruta = txtRutaRespaldo.getText().trim();
        if (ruta.isEmpty()) {
            UIHelper.mostrarAdvertencia(this, "Especifique la ruta destino del respaldo.");
            return;
        }

        File destino = new File(ruta);
        lblEstadoOperacion.setText("Generando copia de seguridad en: " + destino.getName() + "...");

        SwingUtilities.invokeLater(() -> {
            try {
                adminDatosServicio.generarRespaldo(destino, operadorActual.getIdUsuario());
                lblEstadoOperacion.setText("Respaldo generado exitosamente.");
                UIHelper.mostrarExito(this, "Copia de seguridad completada exitosamente.\nArchivo: " + destino.getAbsolutePath() + "\nTamaño: " + destino.length() + " bytes");
            } catch (Exception e) {
                lblEstadoOperacion.setText("Fallo en la generación del respaldo.");
                UIHelper.mostrarError(this, "Fallo al generar respaldo: " + e.getMessage());
            }
        });
    }

    private void ejecutarRestore() {
        String ruta = txtRutaRestaurar.getText().trim();
        if (ruta.isEmpty()) {
            UIHelper.mostrarAdvertencia(this, "Seleccione un archivo de respaldo .sql para restaurar.");
            return;
        }

        File origen = new File(ruta);
        if (!origen.exists()) {
            UIHelper.mostrarAdvertencia(this, "El archivo seleccionado no existe.");
            return;
        }

        boolean confirma = UIHelper.confirmarAccion(
                this,
                "ADVERTENCIA DE SEGURIDAD:\n"
                + "La restauración reemplazará el contenido actual de la base de datos por el contenido del respaldo:\n"
                + origen.getName() + "\n\n¿Desea proceder?",
                "Confirmar Restauración Total"
        );

        if (confirma) {
            lblEstadoOperacion.setText("Restaurando base de datos...");
            SwingUtilities.invokeLater(() -> {
                try {
                    adminDatosServicio.restaurarBaseDeDatos(origen, operadorActual.getIdUsuario());
                    lblEstadoOperacion.setText("Restauración completada con éxito.");
                    UIHelper.mostrarExito(this, "Base de datos restaurada correctamente.");
                } catch (Exception e) {
                    lblEstadoOperacion.setText("Error en la restauración.");
                    UIHelper.mostrarError(this, "Error durante la restauración: " + e.getMessage());
                }
            });
        }
    }

    private void ejecutarSeeders() {
        boolean confirma = UIHelper.confirmarAccion(
                this,
                "¿Está seguro de reiniciar y sembrar los datos de prueba iniciales?\n"
                + "Se restablecerán los usuarios, catálogo bibliográfico, ejemplares y préstamos iniciales.",
                "Confirmar Ejecución de Seeders"
        );

        if (confirma) {
            lblEstadoOperacion.setText("Ejecutando script de Seeders...");
            SwingUtilities.invokeLater(() -> {
                try {
                    adminDatosServicio.ejecutarSeeders(operadorActual.getIdUsuario());
                    lblEstadoOperacion.setText("Seeders ejecutados con éxito.");
                    UIHelper.mostrarExito(this, "Base de datos poblada exitosamente con los seeders de prueba.");
                } catch (Exception e) {
                    lblEstadoOperacion.setText("Error al ejecutar seeders.");
                    UIHelper.mostrarError(this, "Error al ejecutar seeders: " + e.getMessage());
                }
            });
        }
    }
}
