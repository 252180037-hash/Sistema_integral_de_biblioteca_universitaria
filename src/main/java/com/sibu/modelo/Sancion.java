package com.sibu.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa una sanción económica o multa impuesta
 * automáticamente por entrega fuera de plazo (RF3).
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class Sancion implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idSancion;
    private Integer idPrestamo;
    private Integer idUsuario;
    private int diasRetraso;
    private BigDecimal monto;
    private String motivo;
    private EstadoSancion estado;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaPago;

    // Campos de visualización
    private String nombreUsuario;
    private String identificacionUsuario;
    private String emailUsuario;
    private String codigoInventario;
    private String tituloLibro;

    public Sancion() {
        this.estado = EstadoSancion.PENDIENTE;
        this.fechaGeneracion = LocalDateTime.now();
    }

    public Sancion(Integer idPrestamo, Integer idUsuario, int diasRetraso,
                   BigDecimal monto, String motivo) {
        this.idPrestamo = idPrestamo;
        this.idUsuario = idUsuario;
        this.diasRetraso = diasRetraso;
        this.monto = monto;
        this.motivo = motivo;
        this.estado = EstadoSancion.PENDIENTE;
        this.fechaGeneracion = LocalDateTime.now();
    }

    public Sancion(Integer idSancion, Integer idPrestamo, Integer idUsuario, int diasRetraso,
                   BigDecimal monto, String motivo, EstadoSancion estado,
                   LocalDateTime fechaGeneracion, LocalDateTime fechaPago) {
        this.idSancion = idSancion;
        this.idPrestamo = idPrestamo;
        this.idUsuario = idUsuario;
        this.diasRetraso = diasRetraso;
        this.monto = monto;
        this.motivo = motivo;
        this.estado = estado;
        this.fechaGeneracion = fechaGeneracion;
        this.fechaPago = fechaPago;
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================

    public Integer getIdSancion() {
        return idSancion;
    }

    public void setIdSancion(Integer idSancion) {
        this.idSancion = idSancion;
    }

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

    public int getDiasRetraso() {
        return diasRetraso;
    }

    public void setDiasRetraso(int diasRetraso) {
        this.diasRetraso = diasRetraso;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoSancion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSancion estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getIdentificacionUsuario() {
        return identificacionUsuario;
    }

    public void setIdentificacionUsuario(String identificacionUsuario) {
        this.identificacionUsuario = identificacionUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public String getCodigoInventario() {
        return codigoInventario;
    }

    public void setCodigoInventario(String codigoInventario) {
        this.codigoInventario = codigoInventario;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sancion)) return false;
        Sancion sancion = (Sancion) o;
        return Objects.equals(idSancion, sancion.idSancion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSancion);
    }

    @Override
    public String toString() {
        return "Sanción #" + idSancion + " [" + motivo + "] Monto: $" + monto + " (" + estado + ")";
    }
}
