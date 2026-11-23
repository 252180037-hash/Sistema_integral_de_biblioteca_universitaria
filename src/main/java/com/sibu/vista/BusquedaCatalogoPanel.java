package com.sibu.vista;

import com.sibu.modelo.Ejemplar;
import com.sibu.modelo.Libro;
import com.sibu.servicio.CatalogoServicio;
import com.sibu.vista.componentes.UIHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
 * Accesible para todos los perfiles de usuario, incluyendo Lectores.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class BusquedaCatalogoPanel extends JPanel {

    private final CatalogoServicio catalogoServicio;

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

    public BusquedaCatalogoPanel() {
        this.catalogoServicio = new CatalogoServicio();
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
        panelInferior.setPreferredSize(new Dimension(0, 180));

        lblDetalleLibro = new JLabel("Seleccione una obra en la tabla superior para consultar la ubicación física de sus ejemplares.");
        lblDetalleLibro.setFont(UIHelper.FUENTE_LABEL);
        lblDetalleLibro.setForeground(com.sibu.util.TemaUtil.getColorTextoPrincipal());
        panelInferior.add(lblDetalleLibro, BorderLayout.NORTH);

        String[] columnasCopia = {"ID", "Código de Barras / Inventario", "Ubicación Física (Estante/Nivel)", "Estado de Disponibilidad", "Observaciones"};
        modeloCopiasDetalle = new DefaultTableModel(columnasCopia, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaCopiasDetalle = new JTable(modeloCopiasDetalle);
        UIHelper.estilizarTabla(tablaCopiasDetalle);
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

    private void limpiarFiltros() {
        txtFiltroTitulo.setText("");
        txtFiltroAutor.setText("");
        txtFiltroIsbn.setText("");
        cmbFiltroMateria.setSelectedIndex(0);
        ejecutarBusqueda();
    }
}
