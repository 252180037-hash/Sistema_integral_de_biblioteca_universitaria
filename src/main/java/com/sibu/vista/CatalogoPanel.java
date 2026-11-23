package com.sibu.vista;

import com.sibu.modelo.Ejemplar;
import com.sibu.modelo.EstadoEjemplar;
import com.sibu.modelo.Libro;
import com.sibu.modelo.Usuario;
import com.sibu.servicio.CatalogoServicio;
import com.sibu.servicio.exception.ValidacionException;
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
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Panel para la gestión de libros y ejemplares físicos (RF1).
 * <p>
 * Incluye gestión de fichas bibliográficas y administración independiente de inventario
 * físico con códigos de barra y estados de disponibilidad.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class CatalogoPanel extends JPanel {

    private final CatalogoServicio catalogoServicio;
    private final Usuario usuarioActual;

    // Componentes - Pestaña Libros
    private JTable tablaLibros;
    private DefaultTableModel modeloLibros;
    private JTextField txtBuscarLibro;
    private JTextField txtIdLibro;
    private JTextField txtIsbn;
    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JTextField txtEditorial;
    private JTextField txtAnio;
    private JTextField txtMateria;
    private JTextField txtDescripcion;
    private JButton btnNuevoLibro;
    private JButton btnGuardarLibro;
    private JButton btnEliminarLibro;

    // Componentes - Pestaña Ejemplares
    private JTable tablaEjemplares;
    private DefaultTableModel modeloEjemplares;
    private JComboBox<LibroItem> cmbLibrosParaEjemplar;
    private JTextField txtIdEjemplar;
    private JTextField txtCodigoInventario;
    private JTextField txtUbicacion;
    private JComboBox<EstadoEjemplar> cmbEstadoEjemplar;
    private JTextField txtObservacionesEjemplar;
    private JButton btnNuevoEjemplar;
    private JButton btnGuardarEjemplar;
    private JButton btnEliminarEjemplar;

    public CatalogoPanel(Usuario usuarioActual) {
        this.catalogoServicio = new CatalogoServicio();
        this.usuarioActual = usuarioActual;
        inicializarUI();
        cargarLibros();
        cargarEjemplares();
        actualizarComboLibros();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Encabezado
        add(UIHelper.crearEncabezadoModulo(
                "Gestión de Catálogo e Inventario Bibliográfico",
                "Administre las obras registradas (RF1) y el estado físico de sus copias individuales."
        ), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIHelper.FUENTE_LABEL);

        tabbedPane.addTab("Catálogo de Libros", crearPestanaLibros());
        tabbedPane.addTab("Inventario de Ejemplares Físicos", crearPestanaEjemplares());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // PESTAÑA 1: CATÁLOGO DE LIBROS
    // =========================================================================

    private JPanel crearPestanaLibros() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Barra de búsqueda superior
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusqueda.add(new JLabel("Buscar libro:"));
        txtBuscarLibro = UIHelper.crearCampoTexto(22);
        JButton btnBuscar = UIHelper.crearBotonSecundario("Buscar");
        btnBuscar.addActionListener(e -> buscarLibros());
        JButton btnRecargar = UIHelper.crearBotonSecundario("Recargar Todo");
        btnRecargar.addActionListener(e -> cargarLibros());

        panelBusqueda.add(txtBuscarLibro);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnRecargar);
        panel.add(panelBusqueda, BorderLayout.NORTH);

        // Tabla de libros
        String[] columnas = {"ID", "ISBN", "Título de la Obra", "Autor", "Editorial", "Año", "Materia", "Copias", "Disponibles"};
        modeloLibros = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaLibros = new JTable(modeloLibros);
        UIHelper.estilizarTabla(tablaLibros);
        tablaLibros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaLibros.getSelectedRow() != -1) {
                seleccionarFilaLibro();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaLibros);

        // Formulario de edición/creación
        JPanel panelForm = UIHelper.crearTarjeta();
        panelForm.setLayout(new GridBagLayout());
        panelForm.setPreferredSize(new Dimension(340, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        int fila = 0;
        gbc.gridx = 0; gbc.gridy = fila++; gbc.gridwidth = 2;
        JLabel lblTituloForm = new JLabel("Ficha Bibliográfica");
        lblTituloForm.setFont(UIHelper.FUENTE_SECCION);
        panelForm.add(lblTituloForm, gbc);

        gbc.gridwidth = 1;
        txtIdLibro = UIHelper.crearCampoTexto(5);
        txtIdLibro.setEditable(false);
        txtIdLibro.setEnabled(false);

        agregarCampoForm(panelForm, gbc, fila++, "ID Libro:", txtIdLibro);
        txtIsbn = UIHelper.crearCampoTexto(15);
        agregarCampoForm(panelForm, gbc, fila++, "ISBN (10 o 13):", txtIsbn);
        txtTitulo = UIHelper.crearCampoTexto(15);
        agregarCampoForm(panelForm, gbc, fila++, "Título:", txtTitulo);
        txtAutor = UIHelper.crearCampoTexto(15);
        agregarCampoForm(panelForm, gbc, fila++, "Autor:", txtAutor);
        txtEditorial = UIHelper.crearCampoTexto(15);
        agregarCampoForm(panelForm, gbc, fila++, "Editorial:", txtEditorial);
        txtAnio = UIHelper.crearCampoTexto(6);
        agregarCampoForm(panelForm, gbc, fila++, "Año Publicación:", txtAnio);
        txtMateria = UIHelper.crearCampoTexto(15);
        agregarCampoForm(panelForm, gbc, fila++, "Materia / Área:", txtMateria);
        txtDescripcion = UIHelper.crearCampoTexto(15);
        agregarCampoForm(panelForm, gbc, fila++, "Descripción:", txtDescripcion);

        // Botones de acción
        gbc.gridx = 0; gbc.gridy = fila++; gbc.gridwidth = 2;
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));

        btnNuevoLibro = UIHelper.crearBotonSecundario("Limpiar");
        btnNuevoLibro.addActionListener(e -> limpiarFormLibro());

        btnGuardarLibro = UIHelper.crearBotonPrimario("Guardar");
        btnGuardarLibro.addActionListener(e -> guardarLibro());

        btnEliminarLibro = UIHelper.crearBotonPeligro("Eliminar");
        btnEliminarLibro.addActionListener(e -> eliminarLibro());

        panelBotones.add(btnNuevoLibro);
        panelBotones.add(btnGuardarLibro);
        panelBotones.add(btnEliminarLibro);

        panelForm.add(panelBotones, gbc);

        // Split divisor
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTabla, panelForm);
        split.setResizeWeight(0.65);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // PESTAÑA 2: INVENTARIO DE EJEMPLARES FÍSICOS
    // =========================================================================

    private JPanel crearPestanaEjemplares() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Barra superior
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnRecargar = UIHelper.crearBotonSecundario("Actualizar Inventario");
        btnRecargar.addActionListener(e -> {
            cargarEjemplares();
            actualizarComboLibros();
        });
        panelTop.add(btnRecargar);
        panel.add(panelTop, BorderLayout.NORTH);

        // Tabla de ejemplares
        String[] columnas = {"ID", "Cód. Inventario", "Título del Libro", "Ubicación en Sala", "Estado", "Observaciones"};
        modeloEjemplares = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaEjemplares = new JTable(modeloEjemplares);
        UIHelper.estilizarTabla(tablaEjemplares);
        tablaEjemplares.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaEjemplares.getSelectedRow() != -1) {
                seleccionarFilaEjemplar();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaEjemplares);

        // Formulario de ejemplares
        JPanel panelForm = UIHelper.crearTarjeta();
        panelForm.setLayout(new GridBagLayout());
        panelForm.setPreferredSize(new Dimension(360, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        int fila = 0;
        gbc.gridx = 0; gbc.gridy = fila++; gbc.gridwidth = 2;
        JLabel lblTituloForm = new JLabel("Control de Copia Física");
        lblTituloForm.setFont(UIHelper.FUENTE_SECCION);
        panelForm.add(lblTituloForm, gbc);

        gbc.gridwidth = 1;
        txtIdEjemplar = UIHelper.crearCampoTexto(5);
        txtIdEjemplar.setEditable(false);
        txtIdEjemplar.setEnabled(false);
        agregarCampoForm(panelForm, gbc, fila++, "ID Ejemplar:", txtIdEjemplar);

        cmbLibrosParaEjemplar = new JComboBox<>();
        cmbLibrosParaEjemplar.setFont(UIHelper.FUENTE_REGULAR);
        agregarCampoForm(panelForm, gbc, fila++, "Libro Asociado:", cmbLibrosParaEjemplar);

        txtCodigoInventario = UIHelper.crearCampoTexto(15);
        agregarCampoForm(panelForm, gbc, fila++, "Cód. Inventario / Barras:", txtCodigoInventario);

        txtUbicacion = UIHelper.crearCampoTexto(15);
        agregarCampoForm(panelForm, gbc, fila++, "Ubicación (Estante/Nivel):", txtUbicacion);

        cmbEstadoEjemplar = new JComboBox<>(EstadoEjemplar.values());
        cmbEstadoEjemplar.setFont(UIHelper.FUENTE_REGULAR);
        agregarCampoForm(panelForm, gbc, fila++, "Estado Físico:", cmbEstadoEjemplar);

        txtObservacionesEjemplar = UIHelper.crearCampoTexto(15);
        agregarCampoForm(panelForm, gbc, fila++, "Observaciones:", txtObservacionesEjemplar);

        // Botones ejemplares
        gbc.gridx = 0; gbc.gridy = fila++; gbc.gridwidth = 2;
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));

        btnNuevoEjemplar = UIHelper.crearBotonSecundario("Limpiar");
        btnNuevoEjemplar.addActionListener(e -> limpiarFormEjemplar());

        btnGuardarEjemplar = UIHelper.crearBotonPrimario("Guardar");
        btnGuardarEjemplar.addActionListener(e -> guardarEjemplar());

        btnEliminarEjemplar = UIHelper.crearBotonPeligro("Eliminar / Baja");
        btnEliminarEjemplar.addActionListener(e -> eliminarEjemplar());

        panelBotones.add(btnNuevoEjemplar);
        panelBotones.add(btnGuardarEjemplar);
        panelBotones.add(btnEliminarEjemplar);

        panelForm.add(panelBotones, gbc);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTabla, panelForm);
        split.setResizeWeight(0.65);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private void agregarCampoForm(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, javax.swing.JComponent campo) {
        gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(UIHelper.FUENTE_LABEL);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(campo, gbc);
    }

    // =========================================================================
    // LÓGICA DE NEGOCIO Y EVENTOS - LIBROS
    // =========================================================================

    private void cargarLibros() {
        modeloLibros.setRowCount(0);
        try {
            List<Libro> lista = catalogoServicio.listarTodosLosLibros();
            for (Libro l : lista) {
                modeloLibros.addRow(new Object[]{
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
            UIHelper.mostrarError(this, "Error al cargar libros: " + e.getMessage());
        }
    }

    private void buscarLibros() {
        String texto = txtBuscarLibro.getText().trim();
        modeloLibros.setRowCount(0);
        try {
            List<Libro> lista = catalogoServicio.consultarCatalogo(texto, texto, texto, null);
            for (Libro l : lista) {
                modeloLibros.addRow(new Object[]{
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
            UIHelper.mostrarError(this, "Fallo al consultar libros: " + e.getMessage());
        }
    }

    private void seleccionarFilaLibro() {
        int r = tablaLibros.getSelectedRow();
        if (r >= 0) {
            txtIdLibro.setText(String.valueOf(modeloLibros.getValueAt(r, 0)));
            txtIsbn.setText(String.valueOf(modeloLibros.getValueAt(r, 1)));
            txtTitulo.setText(String.valueOf(modeloLibros.getValueAt(r, 2)));
            txtAutor.setText(String.valueOf(modeloLibros.getValueAt(r, 3)));
            txtEditorial.setText(String.valueOf(modeloLibros.getValueAt(r, 4)));
            txtAnio.setText(String.valueOf(modeloLibros.getValueAt(r, 5)));
            txtMateria.setText(String.valueOf(modeloLibros.getValueAt(r, 6)));
            txtDescripcion.setText("");
        }
    }

    private void limpiarFormLibro() {
        txtIdLibro.setText("");
        txtIsbn.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtEditorial.setText("");
        txtAnio.setText("");
        txtMateria.setText("");
        txtDescripcion.setText("");
        tablaLibros.clearSelection();
        txtIsbn.requestFocus();
    }

    private void guardarLibro() {
        try {
            String isbn = txtIsbn.getText().trim();
            String titulo = txtTitulo.getText().trim();
            String autor = txtAutor.getText().trim();
            String editorial = txtEditorial.getText().trim();
            String materia = txtMateria.getText().trim();
            String desc = txtDescripcion.getText().trim();

            int anio;
            try {
                anio = Integer.parseInt(txtAnio.getText().trim());
            } catch (NumberFormatException e) {
                throw new ValidacionException("El año de publicación debe ser un número entero válido.");
            }

            Libro libro = new Libro(isbn, titulo, autor, editorial, anio, materia, desc);

            if (txtIdLibro.getText().isEmpty()) {
                // Nuevo
                catalogoServicio.registrarLibro(libro, usuarioActual.getIdUsuario());
                UIHelper.mostrarExito(this, "Libro registrado exitosamente en el catálogo.");
            } else {
                // Actualizar
                libro.setIdLibro(Integer.parseInt(txtIdLibro.getText()));
                catalogoServicio.actualizarLibro(libro, usuarioActual.getIdUsuario());
                UIHelper.mostrarExito(this, "Libro actualizado correctamente.");
            }

            limpiarFormLibro();
            cargarLibros();
            actualizarComboLibros();

        } catch (ValidacionException e) {
            UIHelper.mostrarAdvertencia(this, e.getMessage());
        } catch (Exception e) {
            UIHelper.mostrarError(this, "Error al guardar libro: " + e.getMessage());
        }
    }

    private void eliminarLibro() {
        if (txtIdLibro.getText().isEmpty()) {
            UIHelper.mostrarAdvertencia(this, "Seleccione un libro de la tabla para eliminar.");
            return;
        }

        int id = Integer.parseInt(txtIdLibro.getText());
        String titulo = txtTitulo.getText();

        if (UIHelper.confirmarAccion(this, "¿Está seguro de eliminar el libro \"" + titulo + "\" del catálogo?", "Confirmar Eliminación")) {
            try {
                catalogoServicio.eliminarLibro(id, usuarioActual.getIdUsuario());
                UIHelper.mostrarExito(this, "Libro eliminado correctamente del catálogo.");
                limpiarFormLibro();
                cargarLibros();
                actualizarComboLibros();
            } catch (ValidacionException ve) {
                UIHelper.mostrarAdvertencia(this, ve.getMessage());
            } catch (Exception e) {
                UIHelper.mostrarError(this, "Error al eliminar: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // LÓGICA DE NEGOCIO Y EVENTOS - EJEMPLARES
    // =========================================================================

    private void cargarEjemplares() {
        modeloEjemplares.setRowCount(0);
        try {
            List<Ejemplar> lista = catalogoServicio.listarEjemplaresPorLibro(0); // 0 lista todos o usamos DAO
            // Listar todos a través del servicio
            List<Libro> libros = catalogoServicio.listarTodosLosLibros();
            for (Libro l : libros) {
                List<Ejemplar> ejemplares = catalogoServicio.listarEjemplaresPorLibro(l.getIdLibro());
                for (Ejemplar ej : ejemplares) {
                    modeloEjemplares.addRow(new Object[]{
                            ej.getIdEjemplar(),
                            ej.getCodigoInventario(),
                            l.getTitulo(),
                            ej.getUbicacion(),
                            ej.getEstado().getEtiqueta(),
                            ej.getObservaciones()
                    });
                }
            }
        } catch (SQLException e) {
            UIHelper.mostrarError(this, "Error al cargar inventario de ejemplares: " + e.getMessage());
        }
    }

    private void actualizarComboLibros() {
        cmbLibrosParaEjemplar.removeAllItems();
        try {
            List<Libro> libros = catalogoServicio.listarTodosLosLibros();
            for (Libro l : libros) {
                cmbLibrosParaEjemplar.addItem(new LibroItem(l.getIdLibro(), l.getTitulo(), l.getIsbn()));
            }
        } catch (SQLException ignored) {
        }
    }

    private void seleccionarFilaEjemplar() {
        int r = tablaEjemplares.getSelectedRow();
        if (r >= 0) {
            txtIdEjemplar.setText(String.valueOf(modeloEjemplares.getValueAt(r, 0)));
            txtCodigoInventario.setText(String.valueOf(modeloEjemplares.getValueAt(r, 1)));
            txtUbicacion.setText(String.valueOf(modeloEjemplares.getValueAt(r, 3)));
            String estadoStr = String.valueOf(modeloEjemplares.getValueAt(r, 4));
            for (EstadoEjemplar est : EstadoEjemplar.values()) {
                if (est.getEtiqueta().equalsIgnoreCase(estadoStr)) {
                    cmbEstadoEjemplar.setSelectedItem(est);
                    break;
                }
            }
            Object obs = modeloEjemplares.getValueAt(r, 5);
            txtObservacionesEjemplar.setText(obs != null ? obs.toString() : "");
        }
    }

    private void limpiarFormEjemplar() {
        txtIdEjemplar.setText("");
        txtCodigoInventario.setText("");
        txtUbicacion.setText("");
        txtObservacionesEjemplar.setText("");
        cmbEstadoEjemplar.setSelectedItem(EstadoEjemplar.DISPONIBLE);
        tablaEjemplares.clearSelection();
        txtCodigoInventario.requestFocus();
    }

    private void guardarEjemplar() {
        try {
            LibroItem selectedLibro = (LibroItem) cmbLibrosParaEjemplar.getSelectedItem();
            if (selectedLibro == null) {
                throw new ValidacionException("Debe seleccionar un libro para asociar el ejemplar.");
            }

            String codigo = txtCodigoInventario.getText().trim();
            String ubicacion = txtUbicacion.getText().trim();
            EstadoEjemplar estado = (EstadoEjemplar) cmbEstadoEjemplar.getSelectedItem();
            String obs = txtObservacionesEjemplar.getText().trim();

            Ejemplar ej = new Ejemplar(selectedLibro.idLibro, codigo, ubicacion, estado, obs);

            if (txtIdEjemplar.getText().isEmpty()) {
                catalogoServicio.registrarEjemplar(ej, usuarioActual.getIdUsuario());
                UIHelper.mostrarExito(this, "Ejemplar ingresado exitosamente al inventario.");
            } else {
                ej.setIdEjemplar(Integer.parseInt(txtIdEjemplar.getText()));
                catalogoServicio.actualizarEjemplar(ej, usuarioActual.getIdUsuario());
                UIHelper.mostrarExito(this, "Ejemplar actualizado correctamente.");
            }

            limpiarFormEjemplar();
            cargarEjemplares();
            cargarLibros(); // Refresca conteo de disponibles

        } catch (ValidacionException ve) {
            UIHelper.mostrarAdvertencia(this, ve.getMessage());
        } catch (Exception e) {
            UIHelper.mostrarError(this, "Error al guardar ejemplar: " + e.getMessage());
        }
    }

    private void eliminarEjemplar() {
        if (txtIdEjemplar.getText().isEmpty()) {
            UIHelper.mostrarAdvertencia(this, "Seleccione un ejemplar de la tabla para eliminar o dar de baja.");
            return;
        }

        int id = Integer.parseInt(txtIdEjemplar.getText());
        String cod = txtCodigoInventario.getText();

        if (UIHelper.confirmarAccion(this, "¿Desea eliminar o desincorporar definitivamente el ejemplar [" + cod + "]?", "Confirmar Baja")) {
            try {
                catalogoServicio.eliminarEjemplar(id, usuarioActual.getIdUsuario());
                UIHelper.mostrarExito(this, "Ejemplar desincorporado exitosamente.");
                limpiarFormEjemplar();
                cargarEjemplares();
                cargarLibros();
            } catch (ValidacionException ve) {
                UIHelper.mostrarAdvertencia(this, ve.getMessage());
            } catch (Exception e) {
                UIHelper.mostrarError(this, "Error al eliminar ejemplar: " + e.getMessage());
            }
        }
    }

    private static class LibroItem {
        final int idLibro;
        final String titulo;
        final String isbn;

        LibroItem(int idLibro, String titulo, String isbn) {
            this.idLibro = idLibro;
            this.titulo = titulo;
            this.isbn = isbn;
        }

        @Override
        public String toString() {
            return titulo + " (ISBN: " + isbn + ")";
        }
    }
}
