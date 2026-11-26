# ParkLibre

- Estudiante: salas
- Variante 1: patron Factory Method para crear los vehiculos.
- Motor de base de datos: MySQL.

## Preparar MySQL

Ejecute `database/schema.sql` en MySQL. La conexion usa `localhost:3306`, la base
`parklibre`, el usuario `root` y clave vacia. Si su instalacion usa otras
credenciales, actualice `src/main/java/BaseDatos/Conexion.java`.

## Ejecutar

En NetBeans, ejecute el proyecto Maven. Desde una terminal con Maven instalado:

```text
mvn compile exec:java
```

La aplicacion abre `Vista.InicioVista`; no requiere iniciar `VentanaPrincipal`.
