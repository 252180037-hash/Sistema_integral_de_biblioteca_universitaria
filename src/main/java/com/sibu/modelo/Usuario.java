package com.sibu.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa un usuario registrado en el sistema SIBU.
 * <p>
 * Maneja credenciales mediante Hash y Salt, y administra los perfiles diferenciados:
 * Administrador, Bibliotecario y Lector.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String identificacion;
    private String email;
    private String telefono;
    private String passwordHash;
    private String salt;
    private RolUsuario rol;
    private EstadoUsuario estado;
    private LocalDateTime fechaRegistro;

    /**
     * Constructor por defecto.
     */
    public Usuario() {
        this.estado = EstadoUsuario.ACTIVO;
        this.rol = RolUsuario.LECTOR;
        this.fechaRegistro = LocalDateTime.now();
    }

    /**
     * Constructor para creación de nuevos usuarios sin ID autogenerado.
     */
    public Usuario(String nombre, String apellido, String identificacion, String email,
                   String telefono, String passwordHash, String salt, RolUsuario rol, EstadoUsuario estado) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.identificacion = identificacion;
        this.email = email;
        this.telefono = telefono;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.rol = (rol != null) ? rol : RolUsuario.LECTOR;
        this.estado = (estado != null) ? estado : EstadoUsuario.ACTIVO;
        this.fechaRegistro = LocalDateTime.now();
    }

    /**
     * Constructor completo para hidratación desde base de datos.
     */
    public Usuario(Integer idUsuario, String nombre, String apellido, String identificacion,
                   String email, String telefono, String passwordHash, String salt,
                   RolUsuario rol, EstadoUsuario estado, LocalDateTime fechaRegistro) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.identificacion = identificacion;
        this.email = email;
        this.telefono = telefono;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.rol = rol;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Retorna el nombre y apellido concatenados.
     */
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(idUsuario, usuario.idUsuario) ||
               (email != null && email.equalsIgnoreCase(usuario.email));
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, email);
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + (rol != null ? rol.getEtiqueta() : "Sin Rol") + ")";
    }
}
