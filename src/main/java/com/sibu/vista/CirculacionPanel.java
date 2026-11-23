package com.sibu.vista;

import com.sibu.modelo.Ejemplar;
import com.sibu.modelo.EstadoEjemplar;
import com.sibu.modelo.Prestamo;
import com.sibu.modelo.RolUsuario;
import com.sibu.modelo.Sancion;
import com.sibu.modelo.Usuario;
import com.sibu.servicio.CatalogoServicio;
import com.sibu.servicio.CirculacionServicio;
import com.sibu.servicio.UsuarioServicio;
import com.sibu.servicio.exception.CirculacionException;
import com.sibu.servicio.exception.ValidacionException;
import com.sibu.util.Configuracion;
import com.sibu.util.TemaUtil;
import com.sibu.vista.componentes.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Panel visual para el módulo de circulación (RF3).
 * <p>
 * Implementa las operaciones críticas de Préstamo transaccional, Devolución con cálculo
 * automatizado de sanciones por retraso, Renovación condicionada y Cobro de multas.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class CirculacionPanel extends JPanel {

    private final CirculacionServicio circulacionServicio;
    private final UsuarioServicio usuarioServicio;
    private final CatalogoServicio catalogoServicio;
    private final Usuario operadorActual;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Componentes - Pestaña Nuevo Préstamo
    private JTextField txtIdentificacionLector;
    private JLabel lblNombreLectorInfo;
    private JTextField txtCodigoEjemplar;
    private JLabel lblLibroInfo;
    private JLabel lblFechaLimiteInfo;
    private JTextField txtObservacionesPrestamo;
    private JButton btnRegistrarPrestamo;
    private Usuario lectorSeleccionado;
    private Ejemplar ejemplarSeleccionado;

    // Componentes - Pestaña Préstamos en Curso y Devoluciones
    private JTable tablaPrestamos;
    private DefaultTableModel modeloPrestamos;
    private JTextField txtFiltroPrestamo;
    private JButton btnDevolucion;
    private JButton btnRenovar;

    // Componentes - Pestaña Gestión de Sanciones
    private JTable tablaSanciones;
    private DefaultTableModel modeloSanciones;
    private JButton btnCobrarSancion;
    private JButton btnCondonarSancion;

    public CirculacionPanel(Usuario operadorActual) {
        this.circulacionServicio = new CirculacionServicio();
        this.usuarioServicio = new UsuarioServicio();
        this.catalogoServicio = new CatalogoServicio();
        this.operadorActual = operadorActual;

        inicializarUI();
        cargarPrestamosEnCurso();
        cargarSanciones();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(UIHelper.crearEncabezadoModulo(
                "Módulo de Circulación Bibliotecaria",
                "Gestione préstamos, devoluciones con control de mora, renovaciones y liquidación de sanciones (RF3)."
        ), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIHelper.FUENTE_LABEL);

        tabbedPane.addTab("1. Nuevo Préstamo", crearPestanaNuevoPrestamo());
        tabbedPane.addTab("2. Préstamos en Curso y Devoluciones", crearPestanaDevoluciones());
        tabbedPane.addTab("3. Liquidación de Sanciones y Multas", crearPestanaSanciones());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. PESTAÑA: NUEVO PRÉSTAMO
    // =========================================================================

    private JPanel crearPestanaNuevoPrestamo() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        JPanel tarjeta = UIHelper.crearTarjeta();
        tarjeta.setLayout(new GridBagLayout());
        tarjeta.setPreferredSize(new Dimension(650, 420));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 6, 6, 6);

        int fila = 0;
        c.gridx = 0; c.gridy = fila++; c.gridwidth = 3;
        JLabel lblTitulo = new JLabel("Formulario de Préstamo a Domicilio");
        lblTitulo.setFont(UIHelper.FUENTE_SECCION);
        tarjeta.add(lblTitulo, c);

        c.gridwidth = 1;

        // 1. Selector de Lector
        c.gridx = 0; c.gridy = fila; c.weightx = 0.25;
        JLabel lblLector = new JLabel("Matrícula / Identificación Lector:");
        lblLector.setFont(UIHelper.FUENTE_LABEL);
        tarjeta.add(lblLector, c);

        c.gridx = 1; c.weightx = 0.55;
        txtIdentificacionLector = UIHelper.crearCampoTexto(15);
        tarjeta.add(txtIdentificacionLector, c);

        c.gridx = 2; c.weightx = 0.20;
        JButton btnVerificarLector = UIHelper.crearBotonSecundario("Verificar");
        btnVerificarLector.addActionListener(e -> verificarLector());
        tarjeta.add(btnVerificarLector, c);

        fila++;
        c.gridx = 1; c.gridy = fila++; c.gridwidth = 2;
        lblNombreLectorInfo = new JLabel("Ingrese la identificación y presione Verificar.");
        lblNombreLectorInfo.setFont(UIHelper.FUENTE_SUBTITULO);
        lblNombreLectorInfo.setForeground(TemaUtil.getColorTextoSecundario());
        tarjeta.add(lblNombreLectorInfo, c);

        c.gridwidth = 1;

        // 2. Selector de Ejemplar
        c.gridx = 0; c.gridy = fila; c.weightx = 0.25;
        JLabel lblEjemplar = new JLabel("Código de Inventario / Barras:");
        lblEjemplar.setFont(UIHelper.FUENTE_LABEL);
        tarjeta.add(lblEjemplar, c);

        c.gridx = 1; c.weightx = 0.55;
        txtCodigoEjemplar = UIHelper.crearCampoTexto(15);
        tarjeta.add(txtCodigoEjemplar, c);

        c.gridx = 2; c.weightx = 0.20;
        JButton btnVerificarEjemplar = UIHelper.crearBotonSecundario("Verificar");
        btnVerificarEjemplar.addActionListener(e -> verificarEjemplar());
        tarjeta.add(btnVerificarEjemplar, c);

        fila++;
        c.gridx = 1; c.gridy = fila++; c.gridwidth = 2;
        lblLibroInfo = new JLabel("Escanee o ingrese el código y presione Verificar.");
        lblLibroInfo.setFont(UIHelper.FUENTE_SUBTITULO);
        lblLibroInfo.setForeground(TemaUtil.getColorTextoSecundario());
        tarjeta.add(lblLibroInfo, c);

        c.gridwidth = 1;

        // 3. Plazo y Fecha Límite
        c.gridx = 0; c.gridy = fila;
        JLabel lblPlazo = new JLabel("Fecha Límite Prevista:");
        lblPlazo.setFont(UIHelper.FUENTE_LABEL);
        tarjeta.add(lblPlazo, c);

        c.gridx = 1; c.gridwidth = 2;
        int dias = Configuracion.getInstancia().getDiasLimitePrestamo();
        LocalDate limiteEstimado = LocalDate.now().plusDays(dias);
        lblFechaLimiteInfo = new JLabel(limiteEstimado.format(FORMATO_FECHA) + " (" + dias + " días reglamentarios)");
        lblFechaLimiteInfo.setFont(UIHelper.FUENTE_LABEL);
        lblFechaLimiteInfo.setForeground(TemaUtil.COLOR_PRIMARIO);
        tarjeta.add(lblFechaLimiteInfo, c);

        c.gridwidth = 1;
        fila++;

        // 4. Observaciones
        c.gridx = 0; c.gridy = fila;
        JLabel lblObs = new JLabel("Observaciones de Salida:");
        lblObs.setFont(UIHelper.FUENTE_LABEL);
        tarjeta.add(lblObs, c);

        c.gridx = 1; c.gridwidth = 2;
        txtObservacionesPrestamo = UIHelper.crearCampoTexto(20);
        tarjeta.add(txtObservacionesPrestamo, c);

        fila++;
        c.gridx = 0; c.gridy = fila++; c.gridwidth = 3;
        btnRegistrarPrestamo = UIHelper.crearBotonPrimario("Procesar Préstamo");
        btnRegistrarPrestamo.setPreferredSize(new Dimension(btnRegistrarPrestamo.getPreferredSize().width, 36));
        btnRegistrarPrestamo.addActionListener(e -> ejecutarRegistroPrestamo());
        tarjeta.add(btnRegistrarPrestamo, c);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(tarjeta, gbc);

        return panel;
    }

    private void verificarLector() {
        String ident = txtIdentificacionLector.getText().trim();
        if (ident.isEmpty()) {
            UIHelper.mostrarAdvertencia(this, "Ingrese la identificación del lector.");
            return;
        }
        try {
            List<Usuario> lista = usuarioServicio.buscarPorCriterio(ident);
            Usuario hallado = null;
            for (Usuario u : lista) {
                if (u.getIdentificacion().equalsIgnoreCase(ident)) {
                    hallado = u;
                    break;
                }
            }

            if (hallado != null) {
                lectorSeleccionado = hallado;
                lblNombreLectorInfo.setText(hallado.getNombreCompleto() + " | Estado: " 
                        + hallado.getEstado() + " | Rol: " + hallado.getRol());
                if (hallado.getEstado() != com.sibu.modelo.EstadoUsuario.ACTIVO) {
                    lblNombreLectorInfo.setForeground(TemaUtil.COLOR_PELIGRO);
                } else {
                    lblNombreLectorInfo.setForeground(TemaUtil.COLOR_EXITO);
                }
            } else {
                lectorSeleccionado = null;
                lblNombreLectorInfo.setText("No se encontró ningún lector con identificación: " + ident);
                lblNombreLectorInfo.setForeground(TemaUtil.COLOR_PELIGRO);
            }
        } catch (SQLException e) {
            UIHelper.mostrarError(this, "Error al buscar lector: " + e.getMessage());
        }
    }

    private void verificarEjemplar() {
        String cod = txtCodigoEjemplar.getText().trim();
        if (cod.isEmpty()) {
            UIHelper.mostrarAdvertencia(this, "Ingrese el código de inventario del ejemplar.");
            return;
        }
        try {
            Ejemplar ej = catalogoServicio.buscarEjemplarPorCodigo(cod);
            if (ej != null) {
                ejemplarSeleccionado = ej;
                lblLibroInfo.setText("\"" + ej.getTituloLibro() + "\" | Ubicación: " 
                        + ej.getUbicacion() + " | Estado: " + ej.getEstado().getEtiqueta());
                if (ej.getEstado() != EstadoEjemplar.DISPONIBLE) {
                    lblLibroInfo.setForeground(TemaUtil.COLOR_PELIGRO);
                } else {
                    lblLibroInfo.setForeground(TemaUtil.COLOR_EXITO);
                }
            } else {
                ejemplarSeleccionado = null;
                lblLibroInfo.setText("No se encontró ningún ejemplar con código: " + cod);
                lblLibroInfo.setForeground(TemaUtil.COLOR_PELIGRO);
            }
        } catch (SQLException e) {
            UIHelper.mostrarError(this, "Error al buscar ejemplar: " + e.getMessage());
        }
    }

    private void ejecutarRegistroPrestamo() {
        if (lectorSeleccionado == null) {
            verificarLector();
            if (lectorSeleccionado == null) return;
        }
        if (ejemplarSeleccionado == null) {
            verificarEjemplar();
            if (ejemplarSeleccionado == null) return;
        }

        String obs = txtObservacionesPrestamo.getText().trim();

        try {
            Prestamo p = circulacionServicio.registrarPrestamo(
                    lectorSeleccionado.getIdUsuario(),
                    ejemplarSeleccionado.getIdEjemplar(),
                    operadorActual.getIdUsuario(),
                    obs
            );

            UIHelper.mostrarExito(this, "Préstamo #" + p.getIdPrestamo() + " registrado exitosamente.\n"
                    + "Lector: " + lectorSeleccionado.getNombreCompleto() + "\n"
                    + "Fecha de vencimiento: " + p.getFechaLimite());

            // Limpieza
            txtIdentificacionLector.setText("");
            txtCodigoEjemplar.setText("");
            txtObservacionesPrestamo.setText("");
            lblNombreLectorInfo.setText("Ingrese la identificación y presione Verificar.");
            lblNombreLectorInfo.setForeground(TemaUtil.getColorTextoSecundario());
            lblLibroInfo.setText("Escanee o ingrese el código y presione Verificar.");
            lblLibroInfo.setForeground(TemaUtil.getColorTextoSecundario());
            lectorSeleccionado = null;
            ejemplarSeleccionado = null;

            cargarPrestamosEnCurso();

        } catch (CirculacionException | ValidacionException ce) {
            UIHelper.mostrarAdvertencia(this, ce.getMessage());
        } catch (Exception e) {
            UIHelper.mostrarError(this, "Fallo al procesar préstamo: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2. PESTAÑA: DEVOLUCIONES Y RENOVACIONES
    // =========================================================================

    private JPanel crearPestanaDevoluciones() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Barra superior
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFiltro.add(new JLabel("Filtrar préstamos activos:"));
        txtFiltroPrestamo = UIHelper.crearCampoTexto(20);
        JButton btnRecargar = UIHelper.crearBotonSecundario("Actualizar");
        btnRecargar.addActionListener(e -> cargarPrestamosEnCurso());
        panelFiltro.add(txtFiltroPrestamo);
        panelFiltro.add(btnRecargar);
        panel.add(panelFiltro, BorderLayout.NORTH);

        // Tabla préstamos
        String[] columnas = {"ID", "Ejemplar", "Título de la Obra", "Identificación Lector", "Lector", "Fecha Préstamo", "Fecha Límite", "Estado", "Renovaciones"};
        modeloPrestamos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaPrestamos = new JTable(modeloPrestamos);
        UIHelper.estilizarTabla(tablaPrestamos);
        JScrollPane scroll = new JScrollPane(tablaPrestamos);
        panel.add(scroll, BorderLayout.CENTER);

        // Barra inferior de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        btnRenovar = UIHelper.crearBotonSecundario("Renovar Préstamo (+7 Días)");
        btnRenovar.addActionListener(e -> ejecutarRenovacion());

        btnDevolucion = UIHelper.crearBotonExito("Registrar Devolución de Ejemplar");
        btnDevolucion.addActionListener(e -> ejecutarDevolucion());

        panelBotones.add(btnRenovar);
        panelBotones.add(btnDevolucion);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarPrestamosEnCurso() {
        modeloPrestamos.setRowCount(0);
        try {
            List<Prestamo> lista = circulacionServicio.listarPrestamosEnCurso();
            for (Prestamo p : lista) {
                modeloPrestamos.addRow(new Object[]{
                        p.getIdPrestamo(),
                        p.getCodigoInventario(),
                        p.getTituloLibro(),
                        p.getIdentificacionLector(),
                        p.getNombreLector(),
                        p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(FORMATO_HORA) : "",
                        p.getFechaLimite() != null ? p.getFechaLimite().format(FORMATO_FECHA) : "",
                        p.estaVencido() ? "VENCIDO" : p.getEstado().getEtiqueta(),
                        p.getRenovaciones()
                });
            }
        } catch (SQLException e) {
            UIHelper.mostrarError(this, "Error al cargar préstamos en curso: " + e.getMessage());
        }
    }

    private void ejecutarDevolucion() {
        int r = tablaPrestamos.getSelectedRow();
        if (r < 0) {
            UIHelper.mostrarAdvertencia(this, "Seleccione un préstamo de la tabla para registrar su devolución.");
            return;
        }

        int idPrestamo = (int) modeloPrestamos.getValueAt(r, 0);
        String ejemplar = (String) modeloPrestamos.getValueAt(r, 1);
        String lector = (String) modeloPrestamos.getValueAt(r, 4);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Confirmar la recepción del ejemplar [" + ejemplar + "] devuelto por " + lector + "?",
                "Recepción de Ejemplar",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Preguntar si hubo daños físicos
            int danado = JOptionPane.showConfirmDialog(
                    this,
                    "¿El ejemplar presenta algún daño o deterioro físico que requiera enviarlo a taller?",
                    "Inspección de Estado",
                    JOptionPane.YES_NO_OPTION
            );
            String obs = (danado == JOptionPane.YES_OPTION) ? "Devolución con daño físico reportado" : "Devolución en buen estado";

            try {
                Sancion sancion = circulacionServicio.registrarDevolucion(idPrestamo, operadorActual.getIdUsuario(), obs);

                if (sancion != null) {
                    UIHelper.mostrarAdvertencia(
                            this,
                            "Devolución registrada con RETRASO de " + sancion.getDiasRetraso() + " día(s).\n"
                            + "Se ha generado automáticamente una sanción por: $" + sancion.getMonto() + "\n"
                            + "El usuario queda bloqueado hasta que liquide la multa."
                    );
                } else {
                    UIHelper.mostrarExito(this, "Devolución completada exitosamente sin retrasos. Ejemplar liberado.");
                }

                cargarPrestamosEnCurso();
                cargarSanciones();

            } catch (CirculacionException | ValidacionException ce) {
                UIHelper.mostrarAdvertencia(this, ce.getMessage());
            } catch (Exception e) {
                UIHelper.mostrarError(this, "Error al procesar devolución: " + e.getMessage());
            }
        }
    }

    private void ejecutarRenovacion() {
        int r = tablaPrestamos.getSelectedRow();
        if (r < 0) {
            UIHelper.mostrarAdvertencia(this, "Seleccione un préstamo de la tabla para renovar.");
            return;
        }

        int idPrestamo = (int) modeloPrestamos.getValueAt(r, 0);
        try {
            circulacionServicio.realizarRenovacion(idPrestamo, operadorActual.getIdUsuario());
            UIHelper.mostrarExito(this, "Préstamo renovado exitosamente. Se ha extendido la fecha límite.");
            cargarPrestamosEnCurso();
        } catch (CirculacionException | ValidacionException ce) {
            UIHelper.mostrarAdvertencia(this, ce.getMessage());
        } catch (Exception e) {
            UIHelper.mostrarError(this, "Error al renovar préstamo: " + e.getMessage());
        }
    }

    // =========================================================================
    // 3. PESTAÑA: LIQUIDACIÓN DE SANCIONES
    // =========================================================================

    private JPanel crearPestanaSanciones() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnRecargar = UIHelper.crearBotonSecundario("Actualizar Sanciones");
        btnRecargar.addActionListener(e -> cargarSanciones());
        panelTop.add(btnRecargar);
        panel.add(panelTop, BorderLayout.NORTH);

        String[] columnas = {"ID", "Identificación Lector", "Lector", "Ejemplar", "Días Retraso", "Monto ($)", "Motivo", "Estado", "Generación"};
        modeloSanciones = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaSanciones = new JTable(modeloSanciones);
        UIHelper.estilizarTabla(tablaSanciones);
        JScrollPane scroll = new JScrollPane(tablaSanciones);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        btnCondonarSancion = UIHelper.crearBotonSecundario("Condonar Multa (Solo Admin)");
        btnCondonarSancion.setEnabled(operadorActual.getRol() == RolUsuario.ADMINISTRADOR);
        btnCondonarSancion.addActionListener(e -> ejecutarCondonacion());

        btnCobrarSancion = UIHelper.crearBotonPrimario("Cobrar / Liquidar Sanción");
        btnCobrarSancion.addActionListener(e -> ejecutarLiquidacion());

        panelBotones.add(btnCondonarSancion);
        panelBotones.add(btnCobrarSancion);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarSanciones() {
        modeloSanciones.setRowCount(0);
        try {
            List<Sancion> lista = circulacionServicio.listarSancionesPendientes();
            for (Sancion s : lista) {
                modeloSanciones.addRow(new Object[]{
                        s.getIdSancion(),
                        s.getIdentificacionUsuario(),
                        s.getNombreUsuario(),
                        s.getCodigoInventario(),
                        s.getDiasRetraso(),
                        String.format("%.2f", s.getMonto()),
                        s.getMotivo(),
                        s.getEstado().getEtiqueta(),
                        s.getFechaGeneracion() != null ? s.getFechaGeneracion().format(FORMATO_HORA) : ""
                });
            }
        } catch (SQLException e) {
            UIHelper.mostrarError(this, "Error al cargar sanciones: " + e.getMessage());
        }
    }

    private void ejecutarLiquidacion() {
        int r = tablaSanciones.getSelectedRow();
        if (r < 0) {
            UIHelper.mostrarAdvertencia(this, "Seleccione una sanción de la tabla para registrar su pago.");
            return;
        }

        int idSancion = (int) modeloSanciones.getValueAt(r, 0);
        String lector = (String) modeloSanciones.getValueAt(r, 2);
        String monto = (String) modeloSanciones.getValueAt(r, 5);

        if (UIHelper.confirmarAccion(this, "¿Confirmar el cobro de $" + monto + " al lector " + lector + "?", "Confirmar Cobro")) {
            try {
                circulacionServicio.liquidarSancion(idSancion, operadorActual.getIdUsuario());
                UIHelper.mostrarExito(this, "Sanción liquidada con éxito. El usuario ha sido reactivado si no tiene más deudas.");
                cargarSanciones();
            } catch (Exception e) {
                UIHelper.mostrarError(this, "Error al liquidar sanción: " + e.getMessage());
            }
        }
    }

    private void ejecutarCondonacion() {
        int r = tablaSanciones.getSelectedRow();
        if (r < 0) {
            UIHelper.mostrarAdvertencia(this, "Seleccione una sanción para condonar.");
            return;
        }

        int idSancion = (int) modeloSanciones.getValueAt(r, 0);
        String motivo = JOptionPane.showInputDialog(this, "Ingrese la justificación oficial para la condonación:", "Condonación de Multa", JOptionPane.QUESTION_MESSAGE);
        if (motivo != null && !motivo.trim().isEmpty()) {
            try {
                circulacionServicio.condonarSancion(idSancion, operadorActual.getIdUsuario(), motivo.trim());
                UIHelper.mostrarExito(this, "Sanción condonada formalmente con registro en bitácora.");
                cargarSanciones();
            } catch (Exception e) {
                UIHelper.mostrarError(this, "Error al condonar: " + e.getMessage());
            }
        }
    }
}
