package com.sibu.vista;

import com.sibu.modelo.Ejemplar;
import com.sibu.modelo.EstadoEjemplar;
import com.sibu.modelo.Libro;
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Panel de consulta rápida y búsqueda avanzada del catálogo bibliográfico (RF4).
 * <p>
 * Permite filtros combinables por título, autor, ISBN y materia, mostrando
 * en tiempo real la cantidad de ejemplares físicos disponibles en anaquel.
 * Incluye gestión interactiva de préstamos y devoluciones directas desde la tabla
 * de copias físicas físicas (RF3/RF4).
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 2.2.0
 */
public class BusquedaCatalogoPanel extends JPanel {

    private final CatalogoServicio catalogoServicio;
    private final CirculacionServicio circulacionServicio;
    private final UsuarioServicio usuarioServicio;
    private final Usuario usuarioActual;

    private JTextField txtFiltroTitulo;
    private JTextField txtFiltroAutor;
    private JTextField txtFiltroIsbn;
    private JComboBox<String> cmbFiltroMateria;
    private JButton btnBuscar;
    private JButton btnLimpiar;

    private JTable tablaResultados;
    private DefaultTableModel modeloResultados;

    private JTable tablaCopiasDetalle;
    private DefaultTableModel modeloCopiasDetalle;
    private JLabel lblDetalleLibro;
    private JButton btnPedirPrestado;
    private JButton btnDevolverCopia;

    public BusquedaCatalogoPanel() {
        this(null);
    }

    public BusquedaCatalogoPanel(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        this.catalogoServicio = new CatalogoServicio();
        this.circulacionServicio = new CirculacionServicio();
        this.usuarioServicio = new UsuarioServicio();
        inicializarUI();
        cargarMaterias();
        ejecutarBusqueda();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(UIHelper.crearEncabezadoModulo(
                "Búsqueda y Consulta en Tiempo Real del Catálogo",
                "Consulte la disponibilidad física de obras combinando filtros por título, autor, ISBN y materia (RF4)."
        ), BorderLayout.NORTH);

        // ---------------------------------------------------------------------
        // Barra Superior: Filtros Combinables
        // ---------------------------------------------------------------------
        JPanel panelFiltros = UIHelper.crearTarjeta();
        panelFiltros.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Fila 0: Etiquetas
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 0.28;
        panelFiltros.add(crearLabelFiltro("Título de la obra:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.24;
        panelFiltros.add(crearLabelFiltro("Autor:"), gbc);

        gbc.gridx = 2; gbc.weightx = 0.20;
        panelFiltros.add(crearLabelFiltro("ISBN:"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.28;
        panelFiltros.add(crearLabelFiltro("Materia / Disciplina:"), gbc);

        // Fila 1: Campos
        gbc.gridy = 1;
        gbc.gridx = 0;
        txtFiltroTitulo = UIHelper.crearCampoTexto(15);
        panelFiltros.add(txtFiltroTitulo, gbc);

        gbc.gridx = 1;
        txtFiltroAutor = UIHelper.crearCampoTexto(15);
        panelFiltros.add(txtFiltroAutor, gbc);

        gbc.gridx = 2;
        txtFiltroIsbn = UIHelper.crearCampoTexto(12);
        panelFiltros.add(txtFiltroIsbn, gbc);

        gbc.gridx = 3;
        cmbFiltroMateria = new JComboBox<>();
        cmbFiltroMateria.setFont(UIHelper.FUENTE_REGULAR);
        cmbFiltroMateria.addItem("TODAS LAS MATERIAS");
        panelFiltros.add(cmbFiltroMateria, gbc);

        // Fila 2: Botones
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 4;
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnLimpiar = UIHelper.crearBotonSecundario("Limpiar Filtros");
        btnLimpiar.addActionListener(e -> limpiarFiltros());

        btnBuscar = UIHelper.crearBotonPrimario("Filtrar Catálogo");
        btnBuscar.addActionListener(e -> ejecutarBusqueda());

        panelBotones.add(btnLimpiar);
        panelBotones.add(btnBuscar);
        panelFiltros.add(panelBotones, gbc);

        // ---------------------------------------------------------------------
        // Centro: Tabla Principal de Resultados
        // ---------------------------------------------------------------------
        String[] columnasLibro = {"ID", "ISBN", "Título", "Autor", "Editorial", "Año", "Materia", "Copias Totales", "Disponibles en Sala"};
        modeloResultados = new DefaultTableModel(columnasLibro, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaResultados = new JTable(modeloResultados);
        UIHelper.estilizarTabla(tablaResultados);
        tablaResultados.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaResultados.getSelectedRow() != -1) {
                mostrarDetalleCopias();
            }
        });
        JScrollPane scrollResultados = new JScrollPane(tablaResultados);

        // ---------------------------------------------------------------------
        // Inferior: Detalle de Copias Físicas del Libro Seleccionado
        // ---------------------------------------------------------------------
        JPanel panelInferior = UIHelper.crearTarjeta();
        panelInferior.setLayout(new BorderLayout(5, 5));
        panelInferior.setPreferredSize(new Dimension(0, 200));

        JPanel panelInferiorTop = new JPanel(new BorderLayout(5, 5));
        panelInferiorTop.setOpaque(false);

        lblDetalleLibro = new JLabel("Seleccione una obra en la tabla superior para consultar la ubicación física de sus ejemplares.");
        lblDetalleLibro.setFont(UIHelper.FUENTE_LABEL);
        lblDetalleLibro.setForeground(com.sibu.util.TemaUtil.getColorTextoPrincipal());
        panelInferiorTop.add(lblDetalleLibro, BorderLayout.CENTER);

        panelInferior.add(panelInferiorTop, BorderLayout.NORTH);

        String[] columnasCopia = {"ID", "Código de Barras / Inventario", "Ubicación Física (Estante/Nivel)", "Estado de Disponibilidad", "Observaciones"};
        modeloCopiasDetalle = new DefaultTableModel(columnasCopia, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaCopiasDetalle = new JTable(modeloCopiasDetalle);
        UIHelper.estilizarTabla(tablaCopiasDetalle);

        // Barra inferior de acciones directas para el ejemplar seleccionado
        JPanel panelAccionesCopias = new JPanel(new BorderLayout(8, 4));
        panelAccionesCopias.setOpaque(false);

        JLabel lblHintClick = new JLabel("💡 Haga clic en 'Disponible' para pedir prestado o en 'Prestado' para tramitar devolución.");
        lblHintClick.setFont(new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 11));
        lblHintClick.setForeground(com.sibu.util.TemaUtil.getColorTextoSecundario());
        panelAccionesCopias.add(lblHintClick, BorderLayout.WEST);

        JPanel panelBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        panelBotonesAccion.setOpaque(false);

        btnPedirPrestado = UIHelper.crearBotonPrimario("📖 Solicitar Préstamo");
        btnPedirPrestado.setEnabled(false);
        btnPedirPrestado.addActionListener(e -> ejecutarPrestamoDesdeCatalogo());

        btnDevolverCopia = UIHelper.crearBotonExito("🔄 Registrar Devolución");
        btnDevolverCopia.setEnabled(false);
        btnDevolverCopia.addActionListener(e -> ejecutarDevolucionDesdeCatalogo());

        panelBotonesAccion.add(btnPedirPrestado);
        panelBotonesAccion.add(btnDevolverCopia);
        panelAccionesCopias.add(panelBotonesAccion, BorderLayout.EAST);

        panelInferior.add(panelAccionesCopias, BorderLayout.SOUTH);

        // Listener de selección en tabla de copias
        tablaCopiasDetalle.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonesAccionCopias();
            }
        });

        // Clic directo o doble clic sobre la tabla de ejemplares (RF3/RF4)
        tablaCopiasDetalle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tablaCopiasDetalle.rowAtPoint(e.getPoint());
                int col = tablaCopiasDetalle.columnAtPoint(e.getPoint());
                if (row >= 0) {
                    String estado = (String) modeloCopiasDetalle.getValueAt(row, 3);
                    // Si hace clic en la columna de estado o doble clic en la fila
                    if (col == 3 || e.getClickCount() == 2) {
                        if ("Disponible".equalsIgnoreCase(estado)) {
                            ejecutarPrestamoDesdeCatalogo();
                        } else if ("Prestado".equalsIgnoreCase(estado) || (estado != null && estado.contains("Vencido"))) {
                            ejecutarDevolucionDesdeCatalogo();
                        }
                    }
                }
            }
        });

        JScrollPane scrollCopias = new JScrollPane(tablaCopiasDetalle);
        panelInferior.add(scrollCopias, BorderLayout.CENTER);

        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollResultados, panelInferior);
        splitVertical.setResizeWeight(0.70);

        JPanel contenedorCentro = new JPanel(new BorderLayout(10, 10));
        contenedorCentro.add(panelFiltros, BorderLayout.NORTH);
        contenedorCentro.add(splitVertical, BorderLayout.CENTER);

        add(contenedorCentro, BorderLayout.CENTER);

        // Atajo Enter en campos
        java.awt.event.KeyAdapter enterListener = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    ejecutarBusqueda();
                }
            }
        };
        txtFiltroTitulo.addKeyListener(enterListener);
        txtFiltroAutor.addKeyListener(enterListener);
        txtFiltroIsbn.addKeyListener(enterListener);
    }

    private JLabel crearLabelFiltro(String texto) {
        JLabel l = new JLabel(texto) {
            @Override
            public void updateUI() {
                super.updateUI();
                setForeground(com.sibu.util.TemaUtil.getColorTextoPrincipal());
            }
        };
        l.setFont(UIHelper.FUENTE_LABEL);
        l.setForeground(com.sibu.util.TemaUtil.getColorTextoPrincipal());
        return l;
    }

    private void cargarMaterias() {
        try {
            List<String> materias = catalogoServicio.obtenerMaterias();
            for (String m : materias) {
                cmbFiltroMateria.addItem(m);
            }
        } catch (SQLException ignored) {
        }
    }

    private void ejecutarBusqueda() {
        String titulo = txtFiltroTitulo.getText().trim();
        String autor = txtFiltroAutor.getText().trim();
        String isbn = txtFiltroIsbn.getText().trim();

        String materia = (String) cmbFiltroMateria.getSelectedItem();
        if (materia != null && materia.startsWith("TODAS")) {
            materia = null;
        }

        modeloResultados.setRowCount(0);
        modeloCopiasDetalle.setRowCount(0);
        lblDetalleLibro.setText("Seleccione una obra en la tabla superior para consultar la ubicación física de sus ejemplares.");

        try {
            List<Libro> libros = catalogoServicio.consultarCatalogo(titulo, autor, isbn, materia);
            for (Libro l : libros) {
                modeloResultados.addRow(new Object[]{
                        l.getIdLibro(),
                        l.getIsbn(),
                        l.getTitulo(),
                        l.getAutor(),
                        l.getEditorial(),
                        l.getAnioPublicacion(),
                        l.getMateria(),
                        l.getTotalEjemplares(),
                        l.getEjemplaresDisponibles()
                });
            }
        } catch (SQLException e) {
            UIHelper.mostrarError(this, "Fallo al consultar el catálogo: " + e.getMessage());
        }
    }

    private void mostrarDetalleCopias() {
        int r = tablaResultados.getSelectedRow();
        if (r >= 0) {
            int idLibro = (int) modeloResultados.getValueAt(r, 0);
            String titulo = (String) modeloResultados.getValueAt(r, 2);
            lblDetalleLibro.setText("Copias Físicas registradas para la obra: \"" + titulo + "\"");

            modeloCopiasDetalle.setRowCount(0);
            try {
                List<Ejemplar> ejemplares = catalogoServicio.listarEjemplaresPorLibro(idLibro);
                for (Ejemplar ej : ejemplares) {
                    modeloCopiasDetalle.addRow(new Object[]{
                            ej.getIdEjemplar(),
                            ej.getCodigoInventario(),
                            ej.getUbicacion(),
                            ej.getEstado().getEtiqueta(),
                            ej.getObservaciones() != null ? ej.getObservaciones() : ""
                    });
                }
            } catch (SQLException e) {
                UIHelper.mostrarError(this, "Error al cargar copias físicas: " + e.getMessage());
            }
        }
    }

    private void actualizarBotonesAccionCopias() {
        int r = tablaCopiasDetalle.getSelectedRow();
        if (r < 0) {
            btnPedirPrestado.setEnabled(false);
            btnDevolverCopia.setEnabled(false);
            return;
        }
        String estado = (String) modeloCopiasDetalle.getValueAt(r, 3);
        if ("Disponible".equalsIgnoreCase(estado)) {
            btnPedirPrestado.setEnabled(true);
            btnDevolverCopia.setEnabled(false);
        } else if ("Prestado".equalsIgnoreCase(estado) || (estado != null && estado.contains("Vencido"))) {
            btnPedirPrestado.setEnabled(false);
            btnDevolverCopia.setEnabled(true);
        } else {
            btnPedirPrestado.setEnabled(false);
            btnDevolverCopia.setEnabled(false);
        }
    }

    private void ejecutarPrestamoDesdeCatalogo() {
        int r = tablaCopiasDetalle.getSelectedRow();
        if (r < 0) {
            UIHelper.mostrarAdvertencia(this, "Seleccione un ejemplar con estado 'Disponible' para solicitarlo en préstamo.");
            return;
        }

        int idEjemplar = (int) modeloCopiasDetalle.getValueAt(r, 0);
        String codigoInventario = (String) modeloCopiasDetalle.getValueAt(r, 1);
        String estado = (String) modeloCopiasDetalle.getValueAt(r, 3);

        if (!"Disponible".equalsIgnoreCase(estado)) {
            UIHelper.mostrarAdvertencia(this, "El ejemplar [" + codigoInventario + "] no se encuentra disponible (Estado: " + estado + ").");
            return;
        }

        int rLibro = tablaResultados.getSelectedRow();
        String tituloLibro = (rLibro >= 0) ? (String) modeloResultados.getValueAt(rLibro, 2) : "Libro seleccionado";

        int diasLimite = Configuracion.getInstancia().getDiasLimitePrestamo();

        // Si el usuario autenticado es un LECTOR
        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.LECTOR) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea tramitar el préstamo del ejemplar [" + codigoInventario + "] a su nombre?\n\n"
                    + "• Obra: " + tituloLibro + "\n"
                    + "• Lector solicitante: " + usuarioActual.getNombreCompleto() + " (" + usuarioActual.getIdentificacion() + ")\n"
                    + "• Plazo reglamentario: " + diasLimite + " días naturales",
                    "Confirmación de Préstamo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // ID 2 es el Bibliotecario por defecto asignado para préstamos en línea/terminal
                    int idOperador = 2;
                    Prestamo p = circulacionServicio.registrarPrestamo(
                            usuarioActual.getIdUsuario(),
                            idEjemplar,
                            idOperador,
                            "Préstamo solicitado directamente desde catálogo en tiempo real"
                    );

                    UIHelper.mostrarExito(
                            this,
                            "¡Préstamo concedido exitosamente!\n\n"
                            + "• Folio de Préstamo: #" + p.getIdPrestamo() + "\n"
                            + "• Ejemplar asignado: " + codigoInventario + "\n"
                            + "• Fecha límite de entrega: " + p.getFechaLimite() + "\n\n"
                            + "Puede pasar a ventanilla de biblioteca a retirar la obra física."
                    );

                    // Recargar datos para refrescar conteos de disponibilidad
                    mostrarDetalleCopias();
                    ejecutarBusqueda();

                } catch (CirculacionException | ValidacionException ce) {
                    UIHelper.mostrarAdvertencia(this, ce.getMessage());
                } catch (Exception ex) {
                    UIHelper.mostrarError(this, "Fallo al procesar el préstamo: " + ex.getMessage());
                }
            }
        } else {
            // Usuario Administrador o Bibliotecario: puede prestar a cualquier lector
            String identLector = JOptionPane.showInputDialog(
                    this,
                    "Préstamo en Sala / Mostrador — Ejemplar [" + codigoInventario + "]\n\n"
                    + "Obra: " + tituloLibro + "\n"
                    + "Ingrese la Matrícula o Identificación del Lector solicitante:",
                    "Asignar Préstamo",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (identLector != null && !identLector.trim().isEmpty()) {
                try {
                    List<Usuario> usuarios = usuarioServicio.buscarPorCriterio(identLector.trim());
                    Usuario lector = null;
                    for (Usuario u : usuarios) {
                        if (u.getIdentificacion().equalsIgnoreCase(identLector.trim())) {
                            lector = u;
                            break;
                        }
                    }

                    if (lector == null) {
                        UIHelper.mostrarAdvertencia(this, "No se encontró ningún lector con la identificación: " + identLector.trim());
                        return;
                    }

                    int idOperador = (usuarioActual != null) ? usuarioActual.getIdUsuario() : 1;
                    Prestamo p = circulacionServicio.registrarPrestamo(
                            lector.getIdUsuario(),
                            idEjemplar,
                            idOperador,
                            "Préstamo otorgado en mostrador vía catálogo"
                    );

                    UIHelper.mostrarExito(
                            this,
                            "Préstamo #" + p.getIdPrestamo() + " procesado con éxito.\n"
                            + "• Lector: " + lector.getNombreCompleto() + "\n"
                            + "• Ejemplar: " + codigoInventario + "\n"
                            + "• Fecha Límite: " + p.getFechaLimite()
                    );

                    mostrarDetalleCopias();
                    ejecutarBusqueda();

                } catch (CirculacionException | ValidacionException ce) {
                    UIHelper.mostrarAdvertencia(this, ce.getMessage());
                } catch (Exception ex) {
                    UIHelper.mostrarError(this, "Fallo al procesar el préstamo: " + ex.getMessage());
                }
            }
        }
    }

    private void ejecutarDevolucionDesdeCatalogo() {
        int r = tablaCopiasDetalle.getSelectedRow();
        if (r < 0) {
            UIHelper.mostrarAdvertencia(this, "Seleccione un ejemplar prestado para registrar su devolución.");
            return;
        }

        int idEjemplar = (int) modeloCopiasDetalle.getValueAt(r, 0);
        String codigoInventario = (String) modeloCopiasDetalle.getValueAt(r, 1);

        try {
            Prestamo prestamoActivo = circulacionServicio.buscarPrestamoActivoPorEjemplar(idEjemplar);
            if (prestamoActivo == null) {
                UIHelper.mostrarAdvertencia(this, "No se encontró registro de préstamo activo para el ejemplar [" + codigoInventario + "].");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Confirmar la recepción y devolución del ejemplar [" + codigoInventario + "]?\n\n"
                    + "• Obra: " + prestamoActivo.getTituloLibro() + "\n"
                    + "• Lector que devuelve: " + prestamoActivo.getNombreLector() + " (" + prestamoActivo.getIdentificacionLector() + ")\n"
                    + "• Fecha Límite prevista: " + prestamoActivo.getFechaLimite(),
                    "Recepción de Ejemplar Físico",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                int dano = JOptionPane.showConfirmDialog(
                        this,
                        "¿El ejemplar devuelto presenta daños materiales o deterioro físico?",
                        "Inspección de Estado Físico",
                        JOptionPane.YES_NO_OPTION
                );

                String obs = (dano == JOptionPane.YES_OPTION) 
                        ? "Devolución con daño físico reportado desde catálogo" 
                        : "Devolución en buen estado físico";

                int idOperador = (usuarioActual != null) ? usuarioActual.getIdUsuario() : 1;
                Sancion sancion = circulacionServicio.registrarDevolucion(prestamoActivo.getIdPrestamo(), idOperador, obs);

                if (sancion != null) {
                    UIHelper.mostrarAdvertencia(
                            this,
                            "Devolución registrada con RETRASO de " + sancion.getDiasRetraso() + " día(s).\n\n"
                            + "Se generó sanción automática por: $" + sancion.getMonto() + "\n"
                            + "El usuario queda bloqueado hasta saldar su multa."
                    );
                } else {
                    UIHelper.mostrarExito(
                            this,
                            "Devolución procesada con éxito a tiempo.\n"
                            + "El ejemplar [" + codigoInventario + "] ha sido liberado y queda nuevamente DISPONIBLE."
                    );
                }

                mostrarDetalleCopias();
                ejecutarBusqueda();
            }

        } catch (CirculacionException | ValidacionException ce) {
            UIHelper.mostrarAdvertencia(this, ce.getMessage());
        } catch (Exception ex) {
            UIHelper.mostrarError(this, "Error al procesar la devolución: " + ex.getMessage());
        }
    }

    private void limpiarFiltros() {
        txtFiltroTitulo.setText("");
        txtFiltroAutor.setText("");
        txtFiltroIsbn.setText("");
        cmbFiltroMateria.setSelectedIndex(0);
        ejecutarBusqueda();
    }
}
