# Mockups de AKJ Events

Prototipos de la Fase 1, exportados desde Google Stitch. Cada pantalla tiene dos archivos con el mismo nombre:

- `.png`: imagen de la pantalla, para ver el diseño.
- `.html`: código de la maqueta, con los textos, colores, tamaños y espaciados exactos.

`DESIGN.md` es el sistema de diseño: colores, tipografía (Plus Jakarta Sans), formas, sombras y componentes.

## Pantallas de la entrega actual (Fase 2)

| Archivo | Pantalla | Punto de la entrega |
|---|---|---|
| 01_splash | Pantalla inicial con logo y descripción | 3 |
| 02_login | Inicio de sesión | 5 |
| 03_registro | Registro (el usuario empieza como Espectador, nivel 1) | 6 |
| 04_recuperar_contrasena | Recuperar contraseña | 7 |
| 05_inicio_feed | Feed de eventos (Inicio) | 8 |
| 06_detalle_evento | Detalle del evento | 8 |
| 07_crear_evento | Crear evento | 9 |

## Pantallas de fases posteriores

| Archivo | Pantalla |
|---|---|
| 08_editar_evento | Editar evento |
| 09_seleccionar_ubicacion_mapa | Seleccionar ubicación en el mapa |
| 10_explorar_mapa | Explorar eventos en el mapa |
| 11_mis_eventos | Mis eventos |
| 12_perfil_usuario | Perfil de usuario y niveles |
| 13_editar_perfil | Editar perfil |
| 14_notificaciones | Notificaciones y alertas |
| 15_mis_estadisticas | Mis estadísticas |
| 16_checkin_qr_reputacion | Check-in con QR y reputación |
| 17_panel_moderacion | Panel de moderación |

## Notas para implementar

- **Alcance de la Fase 2:** en esta entrega no se implementan mapas ni carga de imágenes. En registro, detalle y crear evento, el mapa y la subida de fotos se dejan fuera o como espacio reservado. La imagen del evento nuevo es temporal y aleatoria.
- **Logo:** en los PNG el logo aparece como un recuadro gris con el texto "img". El logo real está enlazado en el HTML de 01_splash y 02_login:
  https://lh3.googleusercontent.com/aida/AEtjO1XVoX0JQIhC8l2ZWB9xRGjGXXxRnRgMJSQ9CIZi2ys0V23rN_085wSDLBopZ7ZJrufhc175lxsnvIDOVMImRLVazJf7fRkKH9RWJkiSXjsM79dQbiMlMYluv-c6v3AVeyOMx_lw7M23aP_PPbfYa1VTNCEDBwTGN-RZ0jdcZ-MhDJhWD3IUOQrGKg3PSaWK7l3O_lImkMy5HO2bV6zF0aMSaz77HstSiPEgYCC__0nXqiFJDeh7vjAhIzE
  Se debe guardar como PNG y usar en la pantalla inicial, el login y el ícono de la app.
- **04_recuperar_contrasena:** la barra superior que dice "Detalle..." con una foto de perfil es un error de la exportación de Stitch. No forma parte de esta pantalla.
- **Colores:** `DESIGN.md` tiene dos tonos de terracota: los tokens del encabezado (primary `#9C3E26`) y los de la descripción (`#D96B4F`). Las pantallas usan los dos. Se debe mantener el tema que ya existe en `core/theme` y revisar el HTML de cada pantalla cuando haya dudas.
- **Barra inferior:** Inicio, Mapa, Crear, Alertas y Perfil.
