-- A. Cupos por zona
SELECT z.name AS zona, z.capacity AS capacidad,
       COUNT(r.id) AS ocupados,
       z.capacity - COUNT(r.id) AS libres
FROM parking_zone z
LEFT JOIN parking_record r ON r.zone_id = z.id AND r.status = 'ACTIVE'
GROUP BY z.id, z.name, z.capacity;

-- B. Registros con sus relaciones
SELECT r.id, v.plate AS placa, v.owner_name AS propietario,
       z.name AS zona, r.entry_time, r.exit_time, r.status
FROM parking_record r
JOIN vehicle v ON v.id = r.vehicle_id
JOIN parking_zone z ON z.id = r.zone_id
ORDER BY r.entry_time DESC;

-- C. Sin duplicados (ejecutar despues de reiniciar)
SELECT (SELECT COUNT(*) FROM parking_zone)   AS zonas,
       (SELECT COUNT(*) FROM vehicle)        AS vehiculos,
       (SELECT COUNT(*) FROM parking_record) AS registros;

-- D. Llaves foraneas en texto
SHOW CREATE TABLE parking_record;
