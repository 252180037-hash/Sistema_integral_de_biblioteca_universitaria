package com.sibu;

import com.sibu.modelo.EstadoPrestamo;
import com.sibu.modelo.Prestamo;
import com.sibu.modelo.RolUsuario;
import com.sibu.modelo.Usuario;
import com.sibu.servicio.UsuarioServicio;
import com.sibu.servicio.exception.ValidacionException;
import com.sibu.util.Configuracion;
import com.sibu.util.CsvExporter;
import com.sibu.util.SeguridadUtil;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Batería de pruebas unitarias e integración repetibles para los servicios principales de SIBU.
 * <p>
 * Diseñada bajo el criterio de <b>Mantenibilidad</b> y <b>Fiabilidad</b> de la norma <b>ISO/IEC 25010:2023</b>.
 * Valida de forma determinística la criptografía (Hash SHA-256 + Salt), las reglas de negocio de circulación,
 * el cálculo de sanciones por atraso, las validaciones de formato (ISBN/Email) y el exportador CSV.
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public class SibuServicioTest {

    private static File archivoTemporalCsv;

    @BeforeAll
    public static void setUp() {
        archivoTemporalCsv = new File("test_exportacion.csv");
    }

    @AfterAll
    public static void tearDown() {
        if (archivoTemporalCsv != null && archivoTemporalCsv.exists()) {
            archivoTemporalCsv.delete();
        }
    }

    @Test
    @DisplayName("CP01 - Criptografía de contraseñas: Generación de Salt único y verificación constante")
    public void testCriptografiaSeguridad() {
        String salt1 = SeguridadUtil.generarSalt();
        String salt2 = SeguridadUtil.generarSalt();

        // 1. Los salts generados deben ser únicos y de 32 caracteres hexadecimales
        assertNotNull(salt1);
        assertNotNull(salt2);
        assertEquals(32, salt1.length());
        assertEquals(32, salt2.length());
        assertFalse(salt1.equalsIgnoreCase(salt2), "Dos llamadas a generarSalt no deben producir el mismo valor.");

        // 2. Cálculo de Hash SHA-256
        String passPlana = "Admin123*";
        String hash1 = SeguridadUtil.hashPassword(passPlana, salt1);
        String hash2 = SeguridadUtil.hashPassword(passPlana, salt2);

        assertEquals(64, hash1.length(), "El hash SHA-256 debe medir exactamente 64 caracteres hex.");
        assertFalse(hash1.equalsIgnoreCase(hash2), "Misma contraseña con distinto Salt debe producir hashes diferentes.");

        // 3. Verificación de coincidencia
        assertTrue(SeguridadUtil.verificarPassword(passPlana, salt1, hash1), "La contraseña correcta debe validar en true.");
        assertFalse(SeguridadUtil.verificarPassword("ClaveErronea*", salt1, hash1), "Una contraseña errónea debe validar en false.");
    }

    @Test
    @DisplayName("CP02 - Validaciones de formato: Expresión regular de Email y Checksum de ISBN")
    public void testValidacionEmailEIsbn() {
        // Correos válidos e inválidos
        assertTrue(SeguridadUtil.esEmailValido("carlos.mendoza@sibu.edu"));
        assertTrue(SeguridadUtil.esEmailValido("admin@sibu.edu.mx"));
        assertFalse(SeguridadUtil.esEmailValido("correo_sin_arroba.com"));
        assertFalse(SeguridadUtil.esEmailValido("admin@"));
        assertFalse(SeguridadUtil.esEmailValido(null));

        // ISBN-10 válido con y sin guiones (0-306-40615-2)
        assertTrue(SeguridadUtil.esIsbnValido("0-306-40615-2"));
        assertTrue(SeguridadUtil.esIsbnValido("0306406152"));

        // ISBN-13 válido (Clean Code: 978-0132350884)
        assertTrue(SeguridadUtil.esIsbnValido("978-0132350884"));
        assertTrue(SeguridadUtil.esIsbnValido("9780132350884"));

        // ISBN alterado (dígito verificador incorrecto)
        assertFalse(SeguridadUtil.esIsbnValido("978-0132350885"));
        assertFalse(SeguridadUtil.esIsbnValido("12345"));
        assertFalse(SeguridadUtil.esIsbnValido(null));
    }

    @Test
    @DisplayName("CP03 - Configuración externa: Carga de parámetros del sistema")
    public void testConfiguracionPropiedades() {
        Configuracion config = Configuracion.getInstancia();
        assertNotNull(config, "El Singleton de configuración debe estar instanciado.");

        assertTrue(config.getMultaDiaria() > 0, "La multa diaria debe ser mayor a 0.");
        assertEquals(3, config.getMaxLibrosPrestamo(), "El límite por defecto de préstamos simultáneos debe ser 3.");
        assertEquals(7, config.getDiasLimitePrestamo(), "El plazo reglamentario por defecto debe ser 7 días.");
        assertNotNull(config.getDbUrl(), "La URL JDBC no puede ser nula.");
    }

    @Test
    @DisplayName("CP04 - Regla de negocio de Circulación: Cálculo de días de atraso y multas")
    public void testCalculoDiasAtrasoYMultas() {
        Prestamo p = new Prestamo();
        p.setIdPrestamo(100);
        // Préstamo vencido hace 5 días
        p.setFechaLimite(LocalDate.now().minusDays(5));
        p.setEstado(EstadoPrestamo.ACTIVO);

        assertTrue(p.estaVencido(), "El préstamo debe figurar como vencido.");
        assertEquals(5, p.calcularDiasAtraso(), "Debe computar exactamente 5 días de retraso.");

        double multaDiaria = Configuracion.getInstancia().getMultaDiaria();
        BigDecimal multaCalculada = BigDecimal.valueOf(p.calcularDiasAtraso() * multaDiaria);
        assertEquals(BigDecimal.valueOf(25.00).setScale(2), multaCalculada.setScale(2), "La multa debe ser 5 días * $5.00 = $25.00");
    }

    @Test
    @DisplayName("CP05 - Exportador CSV: Estructura RFC 4180 y Byte Order Mark (BOM) UTF-8")
    public void testExportadorCsv() throws Exception {
        String[] encabezados = {"ID", "Título", "Autor"};
        List<String[]> filas = new ArrayList<>();
        filas.add(new String[]{"1", "Cien años de soledad", "Gabriel García Márquez"});
        filas.add(new String[]{"2", "Clean Code; Edición Especial", "Robert C. \"Uncle Bob\" Martin"});

        int exportadas = CsvExporter.exportarDatos(encabezados, filas, archivoTemporalCsv);
        assertEquals(2, exportadas, "Deben haberse exportado exactamente 2 filas de datos.");
        assertTrue(archivoTemporalCsv.exists());

        // Verificar presencia del BOM UTF-8 (EF BB BF) en los primeros 3 bytes
        byte[] buffer = new byte[3];
        try (FileInputStream fis = new FileInputStream(archivoTemporalCsv)) {
            fis.read(buffer);
        }
        byte[] bomEsperado = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        assertArrayEquals(bomEsperado, buffer, "El archivo debe iniciar con el BOM UTF-8 para compatibilidad en Excel.");
    }

    @Test
    @DisplayName("CP06 - Validaciones de Servicio: Prevención de usuarios con datos incompletos o inválidos")
    public void testValidacionesUsuarioServicio() {
        UsuarioServicio servicio = new UsuarioServicio();

        // 1. Intento de registro con correo inválido
        Usuario usuarioInvalido = new Usuario("Pedro", "Infante", "PED-999", "correo_invalido", "555-9999", "", "", RolUsuario.LECTOR, null);
        assertThrows(ValidacionException.class, () -> {
            servicio.registrarUsuario(usuarioInvalido, "Password123*", 1);
        }, "Debe arrojar ValidacionException ante formato de correo inválido.");

        // 2. Intento de registro con contraseña corta
        Usuario usuarioValido = new Usuario("Pedro", "Infante", "PED-999", "pedro@sibu.edu", "555-9999", "", "", RolUsuario.LECTOR, null);
        assertThrows(ValidacionException.class, () -> {
            servicio.registrarUsuario(usuarioValido, "123", 1);
        }, "Debe arrojar ValidacionException ante contraseña menor a 6 caracteres.");
    }
}
