# Mockups (prompts para IA)

Cada archivo JSON describe una pantalla del sistema. El campo `image_generation_prompt`
se pega tal cual en un generador de imágenes (o en una IA de diseño). El resto del JSON
(sections, realistic_data, negative_prompt) sirve para ajustar el resultado.

| Archivo | Pantalla | Ruta |
|---|---|---|
| 00_design_system.json | Estilo compartido (colores, tipografía, componentes) | - |
| 01_index.json | Disponibilidad (principal) | / |
| 02_entry.json | Registrar entrada | /entry |
| 03_inside.json | Vehículos adentro | /inside |
| 04_history.json | Historial y reportes | /history |
| 05_zones.json | Zonas | /zones |
| 06_vehicles.json | Vehículos | /vehicles |

Consejos:
1. Genera primero la pantalla 01 y, si te gusta, adjúntala como referencia al generar las demás para mantener el mismo estilo.
2. Si el texto sale con errores, pide a la IA que regenere "con el texto exacto del prompt" o corrige los textos en el HTML final.
3. Usa el logo placeholder 'P'; no uses logos reales de la universidad sin autorización.
