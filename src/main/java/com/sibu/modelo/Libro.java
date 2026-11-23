package com.sibu.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa una obra o título dentro del catálogo bibliográfico.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class Libro implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idLibro;
    private String isbn;
    private String titulo;
    private String autor;
    private String editorial;
    private Integer anioPublicacion;
    private String materia;
    private String descripcion;
    private LocalDateTime fechaRegistro;

    // Campos calculados para visualización en catálogo y reportes
    private int totalEjemplares;
    private int ejemplaresDisponibles;

    public Libro() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public Libro(String isbn, String titulo, String autor, String editorial,
                 Integer anioPublicacion, String materia, String descripcion) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
        this.materia = materia;
        this.descripcion = descripcion;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Libro(Integer idLibro, String isbn, String titulo, String autor, String editorial,
                 Integer anioPublicacion, String materia, String descripcion, LocalDateTime fechaRegistro) {
        this.idLibro = idLibro;
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
        this.materia = materia;
        this.descripcion = descripcion;
        this.fechaRegistro = fechaRegistro;
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================

    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public Integer getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(Integer anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getTotalEjemplares() {
        return totalEjemplares;
    }

    public void setTotalEjemplares(int totalEjemplares) {
        this.totalEjemplares = totalEjemplares;
    }

    public int getEjemplaresDisponibles() {
        return ejemplaresDisponibles;
    }

    public void setEjemplaresDisponibles(int ejemplaresDisponibles) {
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Libro libro)) return false;
        return Objects.equals(idLibro, libro.idLibro) ||
               (isbn != null && isbn.equalsIgnoreCase(libro.isbn));
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLibro, isbn);
    }

    @Override
    public String toString() {
        return titulo + " - " + autor + " (" + anioPublicacion + ")";
    }
}
