package com.sibu.modelo;

import java.io.Serializable;

/**
 * Objeto de transferencia de datos (DTO) para el Reporte 3: Ranking de libros más prestados.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class ReporteLibroRanking implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idLibro;
    private String isbn;
    private String titulo;
    private String autor;
    private String materia;
    private int totalPrestamos;
    private int ejemplaresTotales;
    private int ejemplaresDisponibles;

    public ReporteLibroRanking() {
    }

    public ReporteLibroRanking(Integer idLibro, String isbn, String titulo, String autor,
                                String materia, int totalPrestamos, int ejemplaresTotales,
                                int ejemplaresDisponibles) {
        this.idLibro = idLibro;
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.materia = materia;
        this.totalPrestamos = totalPrestamos;
        this.ejemplaresTotales = ejemplaresTotales;
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }

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

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public int getTotalPrestamos() {
        return totalPrestamos;
    }

    public void setTotalPrestamos(int totalPrestamos) {
        this.totalPrestamos = totalPrestamos;
    }

    public int getEjemplaresTotales() {
        return ejemplaresTotales;
    }

    public void setEjemplaresTotales(int ejemplaresTotales) {
        this.ejemplaresTotales = ejemplaresTotales;
    }

    public int getEjemplaresDisponibles() {
        return ejemplaresDisponibles;
    }

    public void setEjemplaresDisponibles(int ejemplaresDisponibles) {
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }
}
