CREATE DATABASE IF NOT EXISTS parklibre CHARACTER SET utf8mb4;
USE parklibre;

CREATE TABLE IF NOT EXISTS vehiculo (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    tipo          ENUM('CARRO', 'MOTO', 'BICICLETA') NOT NULL,
    identificador VARCHAR(10) NOT NULL,
    propietario   VARCHAR(100) NOT NULL,
    fecha_ingreso DATETIME NOT NULL,
    fecha_salida  DATETIME NULL,
    lavado        BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_identificador (identificador),
    INDEX idx_salida (fecha_salida)
);
