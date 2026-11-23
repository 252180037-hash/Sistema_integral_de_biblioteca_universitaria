package com.sibu.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Entidad de dominio que representa una transacción de circulación (préstamo, renovación y devolución).
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class Prestamo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idPrestamo;
    private Integer idUsuario;
    private Integer idEjemplar;
    private Integer idBibliotecario;
    private LocalDateTime fechaPrestamo;
    private LocalDate fechaLimite;
    private LocalDateTime fechaDevolucion;
    private EstadoPrestamo estado;
    private int renovaciones;
    private String observaciones;

    // Campos de visualización para tablas y reportes de circulación
    private String nombreLector;
    private String identificacionLector;
    private String emailLector;
    private String tituloLibro;
    private String isbnLibro;
    private String codigoInventario;
    private String nombreBibliotecario;

    public Prestamo() {
        this.fechaPrestamo = LocalDateTime.now();
        this.estado = EstadoPrestamo.ACTIVO;
        this.renovaciones = 0;
    }

    public Prestamo(Integer idUsuario, Integer idEjemplar, Integer idBibliotecario,
                    LocalDate fechaLimite, String observaciones) {
        this.idUsuario = idUsuario;
        this.idEjemplar = idEjemplar;
        this.idBibliotecario = idBibliotecario;
        this.fechaPrestamo = LocalDateTime.now();
        this.fechaLimite = fechaLimite;
        this.estado = EstadoPrestamo.ACTIVO;
        this.renovaciones = 0;
        this.observaciones = observaciones;
    }

    public Prestamo(Integer idPrestamo, Integer idUsuario, Integer idEjemplar, Integer idBibliotecario,
                    LocalDateTime fechaPrestamo, LocalDate fechaLimite, LocalDateTime fechaDevolucion,
                    EstadoPrestamo estado, int renovaciones, String observaciones) {
        this.idPrestamo = idPrestamo;
        this.idUsuario = idUsuario;
        this.idEjemplar = idEjemplar;
        this.idBibliotecario = idBibliotecario;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaLimite = fechaLimite;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
        this.renovaciones = renovaciones;
        this.observaciones = observaciones;
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================

    public Integer getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(Integer idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(Integer idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public Integer getIdBibliotecario() {
        return idBibliotecario;
    }

    public void setIdBibliotecario(Integer idBibliotecario) {
        this.idBibliotecario = idBibliotecario;
    }

    public LocalDateTime getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDateTime fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDateTime fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public int getRenovaciones() {
        return renovaciones;
    }

    public void setRenovaciones(int renovaciones) {
        this.renovaciones = renovaciones;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getNombreLector() {
        return nombreLector;
    }

    public void setNombreLector(String nombreLector) {
        this.nombreLector = nombreLector;
    }

    public String getIdentificacionLector() {
        return identificacionLector;
    }

    public void setIdentificacionLector(String identificacionLector) {
        this.identificacionLector = identificacionLector;
    }

    public String getEmailLector() {
        return emailLector;
    }

    public void setEmailLector(String emailLector) {
        this.emailLector = emailLector;
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

    public String getCodigoInventario() {
        return codigoInventario;
    }

    public void setCodigoInventario(String codigoInventario) {
        this.codigoInventario = codigoInventario;
    }

    public String getNombreBibliotecario() {
        return nombreBibliotecario;
    }

    public void setNombreBibliotecario(String nombreBibliotecario) {
        this.nombreBibliotecario = nombreBibliotecario;
    }

    /**
     * Determina si el préstamo se encuentra vencido respecto a la fecha actual
     * o respecto a la fecha en que fue entregado.
     */
    public boolean estaVencido() {
        if (fechaDevolucion != null) {
            return fechaDevolucion.toLocalDate().isAfter(fechaLimite);
        }
        return LocalDate.now().isAfter(fechaLimite);
    }

    /**
     * Calcula los días exactos de atraso generados.
     * Retorna 0 si aún está a tiempo.
     */
    public long calcularDiasAtraso() {
        LocalDate fechaCorte = (fechaDevolucion != null) ? fechaDevolucion.toLocalDate() : LocalDate.now();
        if (fechaCorte.isAfter(fechaLimite)) {
            return ChronoUnit.DAYS.between(fechaLimite, fechaCorte);
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prestamo prestamo)) return false;
        return Objects.equals(idPrestamo, prestamo.idPrestamo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPrestamo);
    }

    @Override
    public String toString() {
        return "Préstamo #" + idPrestamo + " [" + codigoInventario + " -> " + nombreLector + "] Estado: " + estado;
    }
}
