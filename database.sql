-- =============================================================================
-- SIBU — Sistema Integrado de Bibliotecas Universitarias
-- Base de datos: sibu_db  |  Motor: MariaDB 13.0  |  Charset: utf8mb4
-- Generado automáticamente — compatible con InnoDB + ACID
-- =============================================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;
SET sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------------------------------
-- 1. Base de datos
-- -----------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS sibu_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE sibu_db;

-- -----------------------------------------------------------------------------
-- 2. Tabla: usuarios
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS bitacora;
DROP TABLE IF EXISTS sanciones;
DROP TABLE IF EXISTS prestamos;
DROP TABLE IF EXISTS ejemplares;
DROP TABLE IF EXISTS libros;
DROP TABLE IF EXISTS usuarios;

CREATE TABLE usuarios (
    id_usuario       INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre           VARCHAR(80)  NOT NULL,
    apellido         VARCHAR(80)  NOT NULL,
    identificacion   VARCHAR(40)  NOT NULL COMMENT 'Matricula o nomina universitaria',
    email            VARCHAR(120) NOT NULL,
    telefono         VARCHAR(20)  DEFAULT NULL,
    password_hash    CHAR(64)     NOT NULL COMMENT 'SHA-256 hex lowercase',
    salt             CHAR(32)     NOT NULL COMMENT 'Hex 16 bytes',
    rol              ENUM('ADMINISTRADOR','BIBLIOTECARIO','LECTOR') NOT NULL DEFAULT 'LECTOR',
    estado           ENUM('ACTIVO','INACTIVO','SANCIONADO') NOT NULL DEFAULT 'ACTIVO',
    fecha_registro   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuarios    PRIMARY KEY (id_usuario),
    CONSTRAINT uq_identificacion UNIQUE (identificacion),
    CONSTRAINT uq_email       UNIQUE (email),
    INDEX idx_rol (rol),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. Tabla: libros
-- -----------------------------------------------------------------------------
CREATE TABLE libros (
    id_libro         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    isbn             VARCHAR(17)  NOT NULL COMMENT 'ISBN-10 o ISBN-13 sin guiones',
    titulo           VARCHAR(255) NOT NULL,
    autor            VARCHAR(150) NOT NULL,
    editorial        VARCHAR(120) DEFAULT NULL,
    anio_publicacion YEAR         DEFAULT NULL,
    materia          VARCHAR(100) DEFAULT NULL,
    descripcion      TEXT         DEFAULT NULL,
    fecha_registro   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_libros      PRIMARY KEY (id_libro),
    CONSTRAINT uq_isbn        UNIQUE (isbn),
    INDEX idx_titulo (titulo(60)),
    INDEX idx_autor  (autor(60)),
    INDEX idx_materia (materia(40))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. Tabla: ejemplares
-- -----------------------------------------------------------------------------
CREATE TABLE ejemplares (
    id_ejemplar       INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_libro          INT UNSIGNED NOT NULL,
    codigo_inventario VARCHAR(30)  NOT NULL,
    ubicacion         VARCHAR(60)  DEFAULT NULL COMMENT 'Estante / Pasillo',
    estado            ENUM('DISPONIBLE','PRESTADO','DANADO','BAJA') NOT NULL DEFAULT 'DISPONIBLE',
    observaciones     TEXT         DEFAULT NULL,
    CONSTRAINT pk_ejemplares  PRIMARY KEY (id_ejemplar),
    CONSTRAINT uq_codigo      UNIQUE (codigo_inventario),
    CONSTRAINT fk_ej_libro    FOREIGN KEY (id_libro) REFERENCES libros(id_libro)
                              ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_ej_estado (estado),
    INDEX idx_ej_libro  (id_libro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. Tabla: prestamos
-- -----------------------------------------------------------------------------
CREATE TABLE prestamos (
    id_prestamo       INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_usuario        INT UNSIGNED NOT NULL COMMENT 'Lector',
    id_ejemplar       INT UNSIGNED NOT NULL,
    id_bibliotecario  INT UNSIGNED NOT NULL,
    fecha_prestamo    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_limite      DATE         NOT NULL,
    fecha_devolucion  DATETIME     DEFAULT NULL,
    estado            ENUM('ACTIVO','DEVUELTO','VENCIDO') NOT NULL DEFAULT 'ACTIVO',
    renovaciones      TINYINT UNSIGNED NOT NULL DEFAULT 0,
    observaciones     TEXT         DEFAULT NULL,
    CONSTRAINT pk_prestamos   PRIMARY KEY (id_prestamo),
    CONSTRAINT fk_pr_usuario  FOREIGN KEY (id_usuario)       REFERENCES usuarios(id_usuario) ON UPDATE CASCADE,
    CONSTRAINT fk_pr_ejemplar FOREIGN KEY (id_ejemplar)      REFERENCES ejemplares(id_ejemplar) ON UPDATE CASCADE,
    CONSTRAINT fk_pr_biblio   FOREIGN KEY (id_bibliotecario) REFERENCES usuarios(id_usuario)   ON UPDATE CASCADE,
    INDEX idx_pr_usuario  (id_usuario),
    INDEX idx_pr_estado   (estado),
    INDEX idx_pr_fecha    (fecha_limite)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. Tabla: sanciones
-- -----------------------------------------------------------------------------
CREATE TABLE sanciones (
    id_sancion        INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_prestamo       INT UNSIGNED NOT NULL,
    id_usuario        INT UNSIGNED NOT NULL,
    dias_retraso      SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    monto             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    motivo            VARCHAR(255)  DEFAULT NULL,
    estado            ENUM('PENDIENTE','PAGADA','CONDONADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_generacion  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_pago        DATETIME     DEFAULT NULL,
    CONSTRAINT pk_sanciones   PRIMARY KEY (id_sancion),
    CONSTRAINT fk_sa_prestamo FOREIGN KEY (id_prestamo) REFERENCES prestamos(id_prestamo) ON UPDATE CASCADE,
    CONSTRAINT fk_sa_usuario  FOREIGN KEY (id_usuario)  REFERENCES usuarios(id_usuario)   ON UPDATE CASCADE,
    INDEX idx_sa_usuario (id_usuario),
    INDEX idx_sa_estado  (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. Tabla: bitacora
-- -----------------------------------------------------------------------------
CREATE TABLE bitacora (
    id_bitacora   INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_usuario    INT UNSIGNED DEFAULT NULL,
    modulo        VARCHAR(40)  NOT NULL,
    accion        VARCHAR(40)  NOT NULL,
    detalle       TEXT         DEFAULT NULL,
    host_ip       VARCHAR(45)  DEFAULT NULL,
    fecha_evento  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bitacora    PRIMARY KEY (id_bitacora),
    CONSTRAINT fk_bi_usuario  FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_bi_modulo (modulo),
    INDEX idx_bi_fecha  (fecha_evento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET foreign_key_checks = 1;

-- =============================================================================
-- SEEDERS — Datos iniciales de prueba
-- Hashes pre-calculados con SeguridadUtil.hashPassword (SHA-256 con salt)
-- Algoritmo: SHA-256( saltBytes || passwordUTF8 )  — salt primero (update), luego digest
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Usuarios de prueba (3 roles)
-- Admin123*  / salt=a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6
-- Biblio123* / salt=b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7
-- Lector123* / salt=c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8
-- -----------------------------------------------------------------------------
INSERT INTO usuarios (nombre, apellido, identificacion, email, telefono, password_hash, salt, rol, estado) VALUES
('Administrador',  'SIBU',     'ADM-001',  'admin@sibu.edu',          '555-0001',
 '0336da87f303b4860af80fbef555c30347ce8335792a3419f7c4ece52a03129a',
 'a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6', 'ADMINISTRADOR', 'ACTIVO'),

('Carlos',         'Mendoza',  'BIB-001',  'bibliotecario@sibu.edu',  '555-0002',
 '5224e5ff4957e9c0c5b1ad64a77f658a67cde63923fce400a5c791c3df6b5fbd',
 'b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7', 'BIBLIOTECARIO', 'ACTIVO'),

('María',          'García',   'LEC-001',  'lector1@sibu.edu',        '555-0003',
 '859653615fe269a24789e1de61d10f9318e20b2e4b63f2c7794c197773f352c7',
 'c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8', 'LECTOR',         'ACTIVO'),

('Pedro',          'Ramírez',  'LEC-002',  'lector2@sibu.edu',        NULL,
 '859653615fe269a24789e1de61d10f9318e20b2e4b63f2c7794c197773f352c7',
 'c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8', 'LECTOR',         'ACTIVO'),

('Ana',            'Torres',   'LEC-003',  'lector3@sibu.edu',        NULL,
 '859653615fe269a24789e1de61d10f9318e20b2e4b63f2c7794c197773f352c7',
 'c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8', 'LECTOR',         'INACTIVO');

-- -----------------------------------------------------------------------------
-- Libros (5 representativos)
-- -----------------------------------------------------------------------------
INSERT INTO libros (isbn, titulo, autor, editorial, anio_publicacion, materia, descripcion) VALUES
('9780134685991', 'Effective Java',
 'Joshua Bloch', 'Addison-Wesley', 2018, 'Programación',
 'Guía de buenas prácticas para programadores Java. Tercera edición.'),

('9780132350884', 'Clean Code: A Handbook of Agile Software Craftsmanship',
 'Robert C. Martin', 'Prentice Hall', 2008, 'Ingeniería de Software',
 'Principios, patrones y prácticas para escribir código limpio.'),

('9789702608882', 'Fundamentos de Bases de Datos',
 'Abraham Silberschatz', 'McGraw-Hill', 2010, 'Bases de Datos',
 'Texto universitario estándar sobre sistemas de gestión de bases de datos.'),

('9789681859459', 'El Nombre de la Rosa',
 'Umberto Eco', 'Lumen', 1980, 'Literatura',
 'Novela histórica ambientada en un monasterio medieval italiano.'),

('9786071503084', 'Introducción a los Algoritmos',
 'Thomas H. Cormen', 'MIT Press / CECSA', 2009, 'Algoritmia',
 'Texto de referencia en diseño y análisis de algoritmos. Tercera edición.');

-- -----------------------------------------------------------------------------
-- Ejemplares (10 con estados variados)
-- -----------------------------------------------------------------------------
INSERT INTO ejemplares (id_libro, codigo_inventario, ubicacion, estado, observaciones) VALUES
(1, 'EJ-0001', 'A-01', 'DISPONIBLE', NULL),
(1, 'EJ-0002', 'A-01', 'DISPONIBLE', NULL),
(2, 'EJ-0003', 'A-02', 'DISPONIBLE', NULL),
(2, 'EJ-0004', 'A-02', 'PRESTADO',   'Prestado en campaña de lectura'),
(3, 'EJ-0005', 'B-01', 'DISPONIBLE', NULL),
(3, 'EJ-0006', 'B-01', 'PRESTADO',   NULL),
(4, 'EJ-0007', 'C-03', 'DISPONIBLE', NULL),
(4, 'EJ-0008', 'C-03', 'DANADO',     'Lomo despegado, pendiente reparación'),
(5, 'EJ-0009', 'D-02', 'DISPONIBLE', NULL),
(5, 'EJ-0010', 'D-02', 'BAJA',       'Ejemplar dado de baja por deterioro severo');

-- -----------------------------------------------------------------------------
-- Préstamos de muestra
-- EJ-0004 prestado a lector1 (id=3), procesado por bibliotecario (id=2)
-- EJ-0006 prestado a lector2 (id=4), procesado por bibliotecario (id=2)
-- -----------------------------------------------------------------------------
INSERT INTO prestamos (id_usuario, id_ejemplar, id_bibliotecario, fecha_prestamo, fecha_limite, fecha_devolucion, estado, renovaciones, observaciones) VALUES
(3, 4, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY),  NULL, 'ACTIVO',   0, 'Préstamo de demostración — sin novedad'),
(4, 6, 2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), NULL, 'VENCIDO',  0, 'Préstamo vencido — pendiente devolución');

-- Entrada en bitácora por los seeders
INSERT INTO bitacora (id_usuario, modulo, accion, detalle, host_ip) VALUES
(1, 'SISTEMA', 'INSERT', 'Carga inicial de datos (seeders SIBU v2)', '127.0.0.1');
