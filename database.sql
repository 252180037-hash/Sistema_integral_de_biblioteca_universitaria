-- ============================================================================
-- SIBU: SISTEMA INTEGRAL DE BIBLIOTECA UNIVERSITARIA
-- Script de Creación de Base de Datos, Esquema, Índices y Seeders Iniciales
-- Estándar de Calidad: ISO/IEC 25010:2023 (Eficiencia, Seguridad, Fiabilidad)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS sibu_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sibu_db;

-- ----------------------------------------------------------------------------
-- 1. TABLA: usuarios
-- Gestión de cuentas de usuario y perfiles con contraseñas seguras (Hash + Salt)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS bitacora;
DROP TABLE IF EXISTS sanciones;
DROP TABLE IF EXISTS prestamos;
DROP TABLE IF EXISTS ejemplares;
DROP TABLE IF EXISTS libros;
DROP TABLE IF EXISTS usuarios;

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    identificacion VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    telefono VARCHAR(25) NULL,
    password_hash VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'BIBLIOTECARIO', 'LECTOR') NOT NULL,
    estado ENUM('ACTIVO', 'INACTIVO', 'SANCIONADO') NOT NULL DEFAULT 'ACTIVO',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_usuarios_email (email),
    INDEX idx_usuarios_identificacion (identificacion),
    INDEX idx_usuarios_rol (rol),
    INDEX idx_usuarios_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2. TABLA: libros
-- Catálogo bibliográfico centralizado
-- ----------------------------------------------------------------------------
CREATE TABLE libros (
    id_libro INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    titulo VARCHAR(200) NOT NULL,
    autor VARCHAR(150) NOT NULL,
    editorial VARCHAR(100) NOT NULL,
    anio_publicacion INT NOT NULL,
    materia VARCHAR(100) NOT NULL,
    descripcion TEXT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_libros_isbn (isbn),
    INDEX idx_libros_titulo (titulo),
    INDEX idx_libros_autor (autor),
    INDEX idx_libros_materia (materia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3. TABLA: ejemplares
-- Copias físicas individuales vinculadas a un libro del catálogo
-- ----------------------------------------------------------------------------
CREATE TABLE ejemplares (
    id_ejemplar INT AUTO_INCREMENT PRIMARY KEY,
    id_libro INT NOT NULL,
    codigo_inventario VARCHAR(50) NOT NULL UNIQUE,
    ubicacion VARCHAR(100) NOT NULL,
    estado ENUM('DISPONIBLE', 'PRESTADO', 'DANADO', 'BAJA') NOT NULL DEFAULT 'DISPONIBLE',
    observaciones VARCHAR(255) NULL,
    CONSTRAINT fk_ejemplares_libro FOREIGN KEY (id_libro)
        REFERENCES libros (id_libro) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_ejemplares_estado (estado),
    INDEX idx_ejemplares_libro_estado (id_libro, estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 4. TABLA: prestamos
-- Transacciones de circulación (préstamo, devolución y renovación)
-- ----------------------------------------------------------------------------
CREATE TABLE prestamos (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_ejemplar INT NOT NULL,
    id_bibliotecario INT NOT NULL,
    fecha_prestamo DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_limite DATE NOT NULL,
    fecha_devolucion DATETIME NULL,
    estado ENUM('ACTIVO', 'DEVUELTO', 'VENCIDO') NOT NULL DEFAULT 'ACTIVO',
    renovaciones INT NOT NULL DEFAULT 0,
    observaciones VARCHAR(255) NULL,
    CONSTRAINT fk_prestamos_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_prestamos_ejemplar FOREIGN KEY (id_ejemplar)
        REFERENCES ejemplares (id_ejemplar) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_prestamos_bibliotecario FOREIGN KEY (id_bibliotecario)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_prestamos_usuario_estado (id_usuario, estado),
    INDEX idx_prestamos_ejemplar_estado (id_ejemplar, estado),
    INDEX idx_prestamos_fecha_limite (fecha_limite, estado),
    INDEX idx_prestamos_fecha_prestamo (fecha_prestamo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 5. TABLA: sanciones
-- Multas y sanciones generadas automáticamente por retraso en devoluciones
-- ----------------------------------------------------------------------------
CREATE TABLE sanciones (
    id_sancion INT AUTO_INCREMENT PRIMARY KEY,
    id_prestamo INT NOT NULL,
    id_usuario INT NOT NULL,
    dias_retraso INT NOT NULL,
    monto DECIMAL(10, 2) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    estado ENUM('PENDIENTE', 'PAGADA', 'CONDONADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_generacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_pago DATETIME NULL,
    CONSTRAINT fk_sanciones_prestamo FOREIGN KEY (id_prestamo)
        REFERENCES prestamos (id_prestamo) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_sanciones_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_sanciones_usuario_estado (id_usuario, estado),
    INDEX idx_sanciones_estado (estado),
    INDEX idx_sanciones_fecha (fecha_generacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 6. TABLA: bitacora
-- Trazabilidad y auditoría estricta de operaciones del sistema
-- ----------------------------------------------------------------------------
CREATE TABLE bitacora (
    id_bitacora INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NULL,
    modulo VARCHAR(50) NOT NULL,
    accion ENUM('INSERT', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'PRESTAMO', 'DEVOLUCION', 'RENOVACION', 'BACKUP', 'RESTORE') NOT NULL,
    detalle TEXT NOT NULL,
    ip_host VARCHAR(50) NOT NULL DEFAULT 'LOCALHOST',
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bitacora_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_bitacora_fecha (fecha),
    INDEX idx_bitacora_usuario (id_usuario),
    INDEX idx_bitacora_modulo_accion (modulo, accion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- USUARIO CON PRIVILEGIOS MÍNIMOS (Principio de Mínimo Privilegio ISO 25010)
-- ============================================================================
CREATE USER IF NOT EXISTS 'sibu_user'@'localhost' IDENTIFIED BY 'Sibu2024*Pass';
GRANT SELECT, INSERT, UPDATE, DELETE ON sibu_db.* TO 'sibu_user'@'localhost';
FLUSH PRIVILEGES;

-- ============================================================================
-- SEEDERS: POBLADO DE DATOS INICIALES DE PRUEBA
-- ============================================================================

-- Contraseñas configuradas:
-- Admin:          Admin123*
-- Bibliotecario:  Biblio123*
-- Lectores:       Lector123*

INSERT INTO usuarios (id_usuario, nombre, apellido, identificacion, email, telefono, password_hash, salt, rol, estado) VALUES
(1, 'Carlos', 'Mendoza', 'ADM-001', 'admin@sibu.edu', '555-0101', 'd927d6c3402cf866cebee70a9605a90566912a299065c746a7dfb584e7f4b945', 'a1b2c3d4e5f60718293a4b5c6d7e8f90', 'ADMINISTRADOR', 'ACTIVO'),
(2, 'Beatriz', 'Salazar', 'BIB-002', 'bibliotecario@sibu.edu', '555-0102', '15d3089451437e5171116b3d4e550df30039825a93cd9838e5002ef7905a2f65', 'b2c3d4e5f60718293a4b5c6d7e8f90a1', 'BIBLIOTECARIO', 'ACTIVO'),
(3, 'Juan', 'Perez', 'LEC-003', 'lector1@sibu.edu', '555-0103', 'a9bf4c0c8328edf9b3ac586bcae453f425881aeae6e1c029c0e2c9ecb838de0a', 'c3d4e5f60718293a4b5c6d7e8f90a1b2', 'LECTOR', 'ACTIVO'),
(4, 'Maria', 'Gomez', 'LEC-004', 'lector2@sibu.edu', '555-0104', '735b464ad5431fd8780559df78fb1ee897536e0c32e14414e0eb13c6af686108', 'd4e5f60718293a4b5c6d7e8f90a1b2c3', 'LECTOR', 'SANCIONADO');

-- Catálogo de Libros (10 obras académicas y literarias en diversas materias)
INSERT INTO libros (id_libro, isbn, titulo, autor, editorial, anio_publicacion, materia, descripcion) VALUES
(1, '978-0132350884', 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', 'Prentice Hall', 2008, 'Ingeniería de Software', 'Principios, patrones y mejores prácticas para la escritura de código limpio y profesional.'),
(2, '978-0201633610', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', 'Addison-Wesley', 1994, 'Ingeniería de Software', 'Catálogo de patrones de diseño clásicos fundamentales para la arquitectura orientada a objetos.'),
(3, '978-0078022159', 'Database System Concepts', 'Abraham Silberschatz, Henry F. Korth, S. Sudarshan', 'McGraw-Hill', 2019, 'Bases de Datos', 'Conceptos fundamentales y avanzados sobre modelos de bases de datos relacionales, SQL y transacciones.'),
(4, '978-0262046305', 'Introduction to Algorithms', 'Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein', 'MIT Press', 2022, 'Algoritmos', 'Texto de referencia exhaustivo sobre diseño, análisis y complejidad de algoritmos.'),
(5, '978-0137523214', 'Computer Networks', 'Andrew S. Tanenbaum, Nick Feamster, David J. Wetherall', 'Pearson', 2021, 'Redes', 'Estudio estructurado por capas del funcionamiento de redes de computadoras e Internet.'),
(6, '978-0134610992', 'Artificial Intelligence: A Modern Approach', 'Stuart Russell, Peter Norvig', 'Pearson', 2020, 'Inteligencia Artificial', 'La obra de referencia global en teoría, búsqueda, aprendizaje automático y agentes inteligentes.'),
(7, '978-1119456339', 'Operating System Concepts', 'Abraham Silberschatz, Peter B. Galvin, Greg Gagne', 'Wiley', 2018, 'Sistemas Operativos', 'Estructura de kernels, administración de procesos, memoria virtual, almacenamiento y seguridad.'),
(8, '978-0307474728', 'Cien años de soledad', 'Gabriel García Márquez', 'Editorial Sudamericana', 1967, 'Literatura', 'Obra cumbre del realismo mágico y de la literatura latinoamericana universal.'),
(9, '978-6075265506', 'Cálculo de una variable: Trascendentes tempranas', 'James Stewart', 'Cengage Learning', 2016, 'Matemáticas', 'Fundamentos de límites, derivadas, integrales y sus aplicaciones en ciencias e ingeniería.'),
(10, '978-6073244398', 'Física Universitaria con Física Moderna', 'Hugh D. Young, Roger A. Freedman', 'Pearson', 2018, 'Física', 'Mecánica clásica, termodinámica, electromagnetismo, óptica y física contemporánea.');

-- Ejemplares Físicos (Inventario con códigos de barra y ubicaciones)
INSERT INTO ejemplares (id_ejemplar, id_libro, codigo_inventario, ubicacion, estado, observaciones) VALUES
-- Ejemplares para Clean Code (Libro 1)
(1, 1, 'EJEM-CC-001', 'Estante A-1, Nivel 2', 'DISPONIBLE', 'Copia en excelente estado de conservación.'),
(2, 1, 'EJEM-CC-002', 'Estante A-1, Nivel 2', 'PRESTADO', 'Prestado a usuario Juan Perez.'),
(3, 1, 'EJEM-CC-003', 'Estante A-1, Nivel 2', 'DISPONIBLE', 'Nuevo ingreso al catálogo.'),
-- Ejemplares para Design Patterns (Libro 2)
(4, 2, 'EJEM-DP-001', 'Estante A-1, Nivel 3', 'DISPONIBLE', 'Ejemplar encuadernado de tapa dura.'),
(5, 2, 'EJEM-DP-002', 'Estante A-1, Nivel 3', 'PRESTADO', 'Prestado con retraso a Maria Gomez.'),
-- Ejemplares para Database System Concepts (Libro 3)
(6, 3, 'EJEM-BD-001', 'Estante B-2, Nivel 1', 'DISPONIBLE', 'En anaquel principal.'),
(7, 3, 'EJEM-BD-002', 'Estante B-2, Nivel 1', 'DISPONIBLE', 'En anaquel principal.'),
(8, 3, 'EJEM-BD-003', 'Estante B-2, Nivel 1', 'DANADO', 'Lomo desgastado; enviado a taller de restauración.'),
-- Ejemplares para Introduction to Algorithms (Libro 4)
(9, 4, 'EJEM-AL-001', 'Estante B-3, Nivel 2', 'DISPONIBLE', 'Ejemplar de consulta general.'),
(10, 4, 'EJEM-AL-002', 'Estante B-3, Nivel 2', 'DISPONIBLE', 'Ejemplar de reserva bibliográfica.'),
-- Ejemplares para Computer Networks (Libro 5)
(11, 5, 'EJEM-RD-001', 'Estante C-1, Nivel 1', 'DISPONIBLE', 'Ubicación central de redes.'),
(12, 5, 'EJEM-RD-002', 'Estante C-1, Nivel 1', 'BAJA', 'Ejemplar extraviado en periodo 2023.'),
-- Ejemplares para Artificial Intelligence (Libro 6)
(13, 6, 'EJEM-IA-001', 'Estante C-2, Nivel 3', 'DISPONIBLE', 'Ejemplar nuevo.'),
(14, 6, 'EJEM-IA-002', 'Estante C-2, Nivel 3', 'DISPONIBLE', 'Ejemplar en circulación regular.'),
-- Ejemplares para Operating Systems (Libro 7)
(15, 7, 'EJEM-SO-001', 'Estante D-1, Nivel 2', 'DISPONIBLE', 'Ejemplar para consulta en sala.'),
(16, 7, 'EJEM-SO-002', 'Estante D-1, Nivel 2', 'DISPONIBLE', 'Ejemplar en circulación regular.'),
-- Ejemplares para Cien años de soledad (Libro 8)
(17, 8, 'EJEM-LIT-001', 'Estante E-1, Nivel 1', 'DISPONIBLE', 'Colección narrativa hispánica.'),
(18, 8, 'EJEM-LIT-002', 'Estante E-1, Nivel 1', 'DISPONIBLE', 'Edición conmemorativa.'),
-- Ejemplares para Cálculo (Libro 9)
(19, 9, 'EJEM-MAT-001', 'Estante F-1, Nivel 1', 'DISPONIBLE', 'Texto obligatorio ciclo básico.'),
(20, 9, 'EJEM-MAT-002', 'Estante F-1, Nivel 1', 'DISPONIBLE', 'Texto obligatorio ciclo básico.'),
-- Ejemplares para Física (Libro 10)
(21, 10, 'EJEM-FIS-001', 'Estante F-2, Nivel 2', 'DISPONIBLE', 'Tomo integral.'),
(22, 10, 'EJEM-FIS-002', 'Estante F-2, Nivel 2', 'DISPONIBLE', 'Tomo integral.');

-- Préstamos (Ejemplos de activo, devuelto y vencido)
INSERT INTO prestamos (id_prestamo, id_usuario, id_ejemplar, id_bibliotecario, fecha_prestamo, fecha_limite, fecha_devolucion, estado, renovaciones, observaciones) VALUES
-- Préstamo 1: Activo en vigencia (Juan Perez)
(1, 3, 2, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), NULL, 'ACTIVO', 0, 'Préstamo regular por 7 días.'),
-- Préstamo 2: Devuelto correctamente en el pasado
(2, 3, 1, 2, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), 'DEVUELTO', 0, 'Devolución anticipada sin novedades.'),
-- Préstamo 3: Vencido con 4 días de retraso (Maria Gomez)
(3, 4, 5, 2, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(CURDATE(), INTERVAL 4 DAY), NULL, 'VENCIDO', 0, 'Lector no se presentó a renovar ni devolver.');

-- Sanciones
INSERT INTO sanciones (id_sancion, id_prestamo, id_usuario, dias_retraso, monto, motivo, estado, fecha_generacion, fecha_pago) VALUES
-- Sanción generada para el préstamo vencido 3
(1, 3, 4, 4, 20.00, 'Retraso de 4 días en devolución de ejemplar EJEM-DP-002 ($5.00/día)', 'PENDIENTE', DATE_SUB(NOW(), INTERVAL 4 DAY), NULL),
-- Sanción histórica ya pagada por Juan Perez
(2, 2, 3, 3, 15.00, 'Retraso en préstamo anterior liquidado en caja', 'PAGADA', DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 39 DAY));

-- Bitácora Inicial de Operaciones
INSERT INTO bitacora (id_usuario, modulo, accion, detalle, ip_host, fecha) VALUES
(1, 'SISTEMA', 'INSERT', 'Despliegue inicial de base de datos e inserción de seeders del sistema SIBU.', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 'USUARIOS', 'INSERT', 'Registro inicial del usuario Administrador (carlos.mendoza).', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 'USUARIOS', 'INSERT', 'Registro del personal de biblioteca (beatriz.salazar).', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 29 DAY)),
(2, 'CATALOGO', 'INSERT', 'Alta de catálogo bibliográfico con 10 libros y 22 ejemplares físicos.', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(2, 'CIRCULACION', 'PRESTAMO', 'Registro de préstamo de ejemplar EJEM-CC-002 a Juan Perez.', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 'CIRCULACION', 'PRESTAMO', 'Registro de préstamo de ejemplar EJEM-DP-002 a Maria Gomez.', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 11 DAY));
