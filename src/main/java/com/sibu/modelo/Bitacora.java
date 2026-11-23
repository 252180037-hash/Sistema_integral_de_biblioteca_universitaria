package com.sibu.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa un registro de auditoría en la bitácora del sistema (RF7).
 * Garantiza la trazabilidad exigida por la norma ISO/IEC 25010:2023.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class Bitacora implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idBitacora;
    private Integer idUsuario;
    private String modulo;
    private AccionBitacora accion;
    private String detalle;
    private String ipHost;
    private LocalDateTime fecha;

    // Campos de visualización
    private String nombreUsuario;
    private String identificacionUsuario;
    private String emailUsuario;
    private String rolUsuario;

    public Bitacora() {
        this.fecha = LocalDateTime.now();
        this.ipHost = "LOCALHOST";
    }

    public Bitacora(Integer idUsuario, String modulo, AccionBitacora accion, String detalle) {
        this.idUsuario = idUsuario;
        this.modulo = modulo;
        this.accion = accion;
        this.detalle = detalle;
        this.ipHost = "LOCALHOST";
        this.fecha = LocalDateTime.now();
    }

    public Bitacora(Integer idUsuario, String modulo, AccionBitacora accion, String detalle, String ipHost) {
        this.idUsuario = idUsuario;
        this.modulo = modulo;
        this.accion = accion;
        this.detalle = detalle;
        this.ipHost = (ipHost != null && !ipHost.isEmpty()) ? ipHost : "LOCALHOST";
        this.fecha = LocalDateTime.now();
    }

    public Bitacora(Integer idBitacora, Integer idUsuario, String modulo, AccionBitacora accion,
                    String detalle, String ipHost, LocalDateTime fecha) {
        this.idBitacora = idBitacora;
        this.idUsuario = idUsuario;
        this.modulo = modulo;
        this.accion = accion;
        this.detalle = detalle;
        this.ipHost = ipHost;
        this.fecha = fecha;
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================

    public Integer getIdBitacora() {
        return idBitacora;
    }

    public void setIdBitacora(Integer idBitacora) {
        this.idBitacora = idBitacora;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public AccionBitacora getAccion() {
        return accion;
    }

    public void setAccion(AccionBitacora accion) {
        this.accion = accion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getIpHost() {
        return ipHost;
    }

    public void setIpHost(String ipHost) {
        this.ipHost = ipHost;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
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

    public String getRolUsuario() {
        return rolUsuario;
    }

    public void setRolUsuario(String rolUsuario) {
        this.rolUsuario = rolUsuario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bitacora bitacora)) return false;
        return Objects.equals(idBitacora, bitacora.idBitacora);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idBitacora);
    }

    @Override
    public String toString() {
        return "[" + fecha + "] [" + modulo + "] " + accion + ": " + detalle;
    }
}
