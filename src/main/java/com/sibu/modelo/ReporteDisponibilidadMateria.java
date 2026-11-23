package com.sibu.modelo;

import java.io.Serializable;

/**
 * Objeto de transferencia de datos (DTO) para el Reporte 5: Disponibilidad de ejemplares por materia.
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class ReporteDisponibilidadMateria implements Serializable {

    private static final long serialVersionUID = 1L;

    private String materia;
    private int totalTitulos;
    private int totalEjemplares;
    private int disponibles;
    private int prestados;
    private int noDisponibles; // Dañados o Baja
    private double porcentajeDisponibilidad;

    public ReporteDisponibilidadMateria() {
    }

    public ReporteDisponibilidadMateria(String materia, int totalTitulos, int totalEjemplares,
                                        int disponibles, int prestados, int noDisponibles) {
        this.materia = materia;
        this.totalTitulos = totalTitulos;
        this.totalEjemplares = totalEjemplares;
        this.disponibles = disponibles;
        this.prestados = prestados;
        this.noDisponibles = noDisponibles;
        this.porcentajeDisponibilidad = (totalEjemplares > 0)
                ? ((double) disponibles / totalEjemplares) * 100.0
                : 0.0;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public int getTotalTitulos() {
        return totalTitulos;
    }

    public void setTotalTitulos(int totalTitulos) {
        this.totalTitulos = totalTitulos;
    }

    public int getTotalEjemplares() {
        return totalEjemplares;
    }

    public void setTotalEjemplares(int totalEjemplares) {
        this.totalEjemplares = totalEjemplares;
    }

    public int getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(int disponibles) {
        this.disponibles = disponibles;
    }

    public int getPrestados() {
        return prestados;
    }

    public void setPrestados(int prestados) {
        this.prestados = prestados;
    }

    public int getNoDisponibles() {
        return noDisponibles;
    }

    public void setNoDisponibles(int noDisponibles) {
        this.noDisponibles = noDisponibles;
    }

    public double getPorcentajeDisponibilidad() {
        return porcentajeDisponibilidad;
    }

    public void setPorcentajeDisponibilidad(double porcentajeDisponibilidad) {
        this.porcentajeDisponibilidad = porcentajeDisponibilidad;
    }
}
