package com.sibu.servicio;

import com.sibu.dao.BitacoraDAO;
import com.sibu.modelo.AccionBitacora;
import com.sibu.modelo.Bitacora;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de lógica de negocio para la administración y consulta de la bitácora
 * de auditoría del sistema (RF7).
 * <p>
 * Asegura la trazabilidad estricta exigida por la norma ISO/IEC 25010:2023.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class BitacoraServicio {

    private final BitacoraDAO bitacoraDAO;

    public BitacoraServicio() {
        this.bitacoraDAO = new BitacoraDAO();
    }

    public BitacoraServicio(BitacoraDAO bitacoraDAO) {
        this.bitacoraDAO = bitacoraDAO;
    }

    /**
     * Registra un nuevo evento de auditoría en el sistema.
     */
    public void registrarEvento(Integer idUsuario, String modulo, AccionBitacora accion, String detalle) {
        Bitacora log = new Bitacora(idUsuario, modulo, accion, detalle);
        bitacoraDAO.registrar(log);
    }

    /**
     * Obtiene los registros más recientes de auditoría para la vista general.
     */
    public List<Bitacora> listarRecientes(int limite) throws SQLException {
        return bitacoraDAO.listarRecientes(limite > 0 ? limite : 100);
    }

    /**
     * Filtra la bitácora según criterios específicos (módulo, acción y rango de fechas).
     */
    public List<Bitacora> filtrarBitacora(String modulo, AccionBitacora accion, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        return bitacoraDAO.filtrar(modulo, accion, fechaInicio, fechaFin);
    }
}
