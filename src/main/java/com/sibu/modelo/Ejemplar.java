package com.sibu.modelo;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entidad de dominio que representa una copia física individual (ejemplar) de un libro.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class Ejemplar implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idEjemplar;
    private Integer idLibro;
    private String codigoInventario;
    private String ubicacion;
    private EstadoEjemplar estado;
    private String observaciones;

    // Campos de navegación/vista procedentes del Libro asociado
    private String tituloLibro;
    private String isbnLibro;
    private String autorLibro;

    public Ejemplar() {
        this.estado = EstadoEjemplar.DISPONIBLE;
    }

    public Ejemplar(Integer idLibro, String codigoInventario, String ubicacion,
                    EstadoEjemplar estado, String observaciones) {
        this.idLibro = idLibro;
        this.codigoInventario = codigoInventario;
        this.ubicacion = ubicacion;
        this.estado = (estado != null) ? estado : EstadoEjemplar.DISPONIBLE;
        this.observaciones = observaciones;
    }

    public Ejemplar(Integer idEjemplar, Integer idLibro, String codigoInventario,
                    String ubicacion, EstadoEjemplar estado, String observaciones) {
        this.idEjemplar = idEjemplar;
        this.idLibro = idLibro;
        this.codigoInventario = codigoInventario;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================

    public Integer getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(Integer idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

    public String getCodigoInventario() {
        return codigoInventario;
    }

    public void setCodigoInventario(String codigoInventario) {
        this.codigoInventario = codigoInventario;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public EstadoEjemplar getEstado() {
        return estado;
    }

    public void setEstado(EstadoEjemplar estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }

    public String getIsbnLibro() {
        return isbnLibro;
    }

    public void setIsbnLibro(String isbnLibro) {
        this.isbnLibro = isbnLibro;
    }

    public String getAutorLibro() {
        return autorLibro;
    }

    public void setAutorLibro(String autorLibro) {
        this.autorLibro = autorLibro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ejemplar)) return false;
        Ejemplar ejemplar = (Ejemplar) o;
        return Objects.equals(idEjemplar, ejemplar.idEjemplar) ||
               (codigoInventario != null && codigoInventario.equalsIgnoreCase(ejemplar.codigoInventario));
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEjemplar, codigoInventario);
    }

    @Override
    public String toString() {
        return codigoInventario + " - " + ubicacion + " [" + (estado != null ? estado.getEtiqueta() : "") + "]";
    }
}
