CREATE DATABASE IF NOT EXISTS parklibre
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE parklibre;

CREATE TABLE IF NOT EXISTS vehiculo (
    id INT NOT NULL AUTO_INCREMENT,
    tipo VARCHAR(20) NOT NULL,
    identificador VARCHAR(20) NOT NULL,
    propietario VARCHAR(120) NOT NULL,
    fecha_ingreso DATETIME NOT NULL,
    fecha_salida DATETIME NULL,
    lavado BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_vehiculo_identificador_salida (identificador, fecha_salida),
    INDEX idx_vehiculo_fecha_salida (fecha_salida)
);
