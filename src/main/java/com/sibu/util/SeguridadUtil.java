package com.sibu.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Utilidades criptográficas y de validación de seguridad para el sistema SIBU.
 * <p>
 * Implementa el estándar de <b>Seguridad</b> de la norma <b>ISO/IEC 25010:2023</b>:
 * <ul>
 *   <li>Almacenamiento de contraseñas mediante función Hash SHA-256 combinada con Salt criptográfico único por usuario.</li>
 *   <li>Comparación en tiempo constante ({@link MessageDigest#isEqual}) para mitigar ataques de temporización (timing attacks).</li>
 *   <li>Validación estricta de formatos (Email, ISBN-10, ISBN-13) y sanitización preventiva de entradas.</li>
 * </ul>
 * </p>
 * 
 * @author SIBU Arquitectura
 * @version 1.0.0
 */
public final class SeguridadUtil {

    private static final SecureRandom RANDOM_SEGURO = new SecureRandom();
    private static final int LONGITUD_SALT_BYTES = 16;
    private static final Pattern PATRON_EMAIL = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    /**
     * Constructor privado para prevenir instanciación de clase utilitaria.
     */
    private SeguridadUtil() {
    }

    /**
     * Genera un Salt criptográficamente seguro de 16 bytes, retornado como
     * una cadena en formato hexadecimal de 32 caracteres.
     * 
     * @return Cadena hexadecimal con el Salt generado aleatoriamente.
     */
    public static String generarSalt() {
        byte[] salt = new byte[LONGITUD_SALT_BYTES];
        RANDOM_SEGURO.nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * Calcula el Hash SHA-256 de una contraseña combinada con su Salt único.
     * 
     * @param password Contraseña en texto plano como arreglo de caracteres (para permitir limpieza segura en memoria).
     * @param saltHex Salt único en formato hexadecimal.
     * @return Hash SHA-256 resultante en formato hexadecimal (64 caracteres en minúsculas).
     */
    public static String hashPassword(char[] password, String saltHex) {
        if (password == null || saltHex == null) {
            throw new IllegalArgumentException("La contraseña y el salt no pueden ser nulos.");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = hexToBytes(saltHex.trim());
            md.update(saltBytes);

            // Convertir char[] a bytes UTF-8 de forma controlada
            byte[] passBytes = new String(password).getBytes(StandardCharsets.UTF_8);
            byte[] hashBytes = md.digest(passBytes);

            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo criptográfico SHA-256 no disponible en la JVM.", e);
        }
    }

    /**
     * Sobrecarga de conveniencia para calcular el Hash SHA-256 a partir de un {@link String}.
     * 
     * @param password Contraseña en texto plano.
     * @param saltHex Salt único en formato hexadecimal.
     * @return Hash SHA-256 en formato hexadecimal.
     */
    public static String hashPassword(String password, String saltHex) {
        if (password == null) {
            throw new IllegalArgumentException("La contraseña no puede ser nula.");
        }
        return hashPassword(password.toCharArray(), saltHex);
    }

    /**
     * Verifica si una contraseña proporcionada coincide con el hash almacenado para un Salt determinado,
     * utilizando comparación de tiempo constante.
     * 
     * @param password Contraseña en texto plano a verificar.
     * @param saltHex Salt asociado a la cuenta.
     * @param hashEsperado Hash SHA-256 persistido en base de datos.
     * @return {@code true} si la contraseña es válida; {@code false} en caso contrario.
     */
    public static boolean verificarPassword(char[] password, String saltHex, String hashEsperado) {
        if (password == null || saltHex == null || hashEsperado == null) {
            return false;
        }
        String hashCalculado = hashPassword(password, saltHex);
        return MessageDigest.isEqual(
                hashCalculado.getBytes(StandardCharsets.UTF_8),
                hashEsperado.trim().toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Sobrecarga de verificación utilizando {@link String}.
     * 
     * @param password Contraseña a verificar.
     * @param saltHex Salt asociado a la cuenta.
     * @param hashEsperado Hash almacenado.
     * @return {@code true} si coincide exactamente; {@code false} en caso contrario.
     */
    public static boolean verificarPassword(String password, String saltHex, String hashEsperado) {
        if (password == null) {
            return false;
        }
        return verificarPassword(password.toCharArray(), saltHex, hashEsperado);
    }

    /**
     * Convierte un arreglo de bytes en una cadena hexadecimal en minúsculas.
     * 
     * @param bytes Arreglo de bytes.
     * @return Cadena hexadecimal representativa.
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Convierte una cadena hexadecimal en su correspondiente arreglo de bytes.
     * 
     * @param hex Cadena hexadecimal.
     * @return Arreglo de bytes decodificado.
     */
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("La longitud de la cadena hexadecimal debe ser par.");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Valida si un correo electrónico cumple con el formato internacional estándar.
     * 
     * @param email Correo electrónico a evaluar.
     * @return {@code true} si el formato es correcto; {@code false} de lo contrario.
     */
    public static boolean esEmailValido(String email) {
        if (email == null) {
            return false;
        }
        return PATRON_EMAIL.matcher(email.trim()).matches();
    }

    /**
     * Valida el formato y el dígito verificador de un código ISBN (admite ISBN-10 e ISBN-13).
     * 
     * @param isbn Código ISBN con o sin guiones.
     * @return {@code true} si el ISBN es estructuralmente válido; {@code false} en caso contrario.
     */
    public static boolean esIsbnValido(String isbn) {
        if (isbn == null) {
            return false;
        }
        String limpio = isbn.replaceAll("[\\s-]", "").toUpperCase();

        if (limpio.length() == 10) {
            return validarIsbn10(limpio);
        } else if (limpio.length() == 13) {
            return validarIsbn13(limpio);
        }
        return false;
    }

    private static boolean validarIsbn10(String isbn) {
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            char c = isbn.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            suma += (c - '0') * (10 - i);
        }
        char ultimo = isbn.charAt(9);
        if (ultimo == 'X') {
            suma += 10;
        } else if (Character.isDigit(ultimo)) {
            suma += (ultimo - '0');
        } else {
            return false;
        }
        return (suma % 11 == 0);
    }

    private static boolean validarIsbn13(String isbn) {
        int suma = 0;
        for (int i = 0; i < 12; i++) {
            char c = isbn.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            int digito = c - '0';
            suma += (i % 2 == 0) ? digito : digito * 3;
        }
        char ultimo = isbn.charAt(12);
        if (!Character.isDigit(ultimo)) {
            return false;
        }
        int digitoControlEsperado = (10 - (suma % 10)) % 10;
        return (ultimo - '0') == digitoControlEsperado;
    }

    /**
     * Sanitiza una cadena eliminando espacios sobrantes y caracteres de control no imprimibles
     * para mitigar inyecciones y fallos de renderizado.
     * 
     * @param entrada Texto a sanitizar.
     * @return Texto limpio y normalizado, o cadena vacía si la entrada era nula.
     */
    public static String sanitizarTexto(String entrada) {
        if (entrada == null) {
            return "";
        }
        // Remueve caracteres de control (excepto tab y salto de línea básicos) y recorta extremos
        return entrada.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
    }
}
