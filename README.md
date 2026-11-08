# parqueadero-hexagonal

Proyecto integrador - Desarrollo Orientado a Servicios 2026-2 (Universidad Santo Tomás, Tunja).

**Problema:** nadie sabe si hay cupos en el parqueadero; algunos usuarios llegan a la portería y los devuelven.
**Usuarios:** docentes, directivas, estudiantes y vigilantes de portería.
**Datos:** vehículos, espacios del parqueadero y registros de entrada/salida.

Java 21 + Quarkus + MySQL (XAMPP) + Qute/Bootstrap + OpenPDF + Swagger. Identificadores en inglés; mensajes y comentarios en español.

## Estructura (hexagonal)

```
src/main/java/usta/parking/
|-- domain/                       Núcleo: modelo y reglas de negocio (sin framework).
|-- application/                  Casos de uso y puertos (port/in, port/out).
|-- infrastructure/adapter/
    |-- in/web/                   Única entrada: vistas HTML (resource, mapper, dto, report).
    |-- out/persistence/          JPA + MySQL (entity, repository, adapter, mapper).
```

Regla: todas las dependencias apuntan hacia el dominio.

## Ejecutar

1. Encender MySQL en XAMPP.
2. `./mvnw quarkus:dev`
3. Abrir http://localhost:8080 — Swagger en http://localhost:8080/q/swagger-ui
