# Pruebas manuales — galerías, IA y QR

Actualización: 15 de noviembre de 2026, rama local `fase3-ia-categorias`. Se distingue la evidencia manual reportada de los recorridos que aún están pendientes.

## Evidencia manual reportada — Moto G15

El usuario reportó lo siguiente; no es una ejecución nueva del agente ni se conoce aquí la versión exacta del APK/Android instalado:

- **Feria de emprendimiento:** lectura del QR con cámara, pantalla «¡Asistencia confirmada!», +15 pts y un movimiento de +15 en el historial.
- **Repetición del mismo código:** apareció «Ya registraste tu asistencia». **Sigue pendiente comparar saldo antes y después del reintento con la misma cuenta**; no se afirma aún invariancia manual del saldo ni del historial en ese reintento. Las capturas de **117 y 146 puntos no son un antes/después** y no se usan para deducir un incremento.
- **Concurso de talentos local:** se vieron «Foto 1 de 3» y «Foto 3 de 3», flechas y control de pausa. El usuario reportó funcionamiento del deslizamiento y avance automático en el detalle del Moto. Esto no verifica por sí solo pausa efectiva, ciclo exacto de 5 s, segundo plano o movimiento reducido.
- Se observó a la vez «Asistencia registrada con QR» y «Cancelar asistencia». Corrección local del 15/11: tras check-in la barra muestra estado deshabilitado, no una acción de cancelación; antes del check-in se conserva cancelar. Es alineación con `CancelAttendanceUseCase`, ViewModel y repositorio Firestore, que ya prohibían cancelar después; no cambia premios ni política. Pendiente comprobar este ajuste visual en el Moto con el nuevo APK.

### Pendientes manuales separados

1. Comparación de saldo del reintento **en la misma cuenta**; confirmar también que no se añade otro movimiento de historial.
2. Reintento de esa misma cuenta en **otro dispositivo**: no reportado como realizado. No confundir con la lectura QR ya probada en el Moto ni con concurrencia automatizada local.
3. Permisos denegados/Ajustes, QR inválido/de otro evento, cupos y desconexión; galería en Inicio/lista/miniaturas, pausa, segundo plano, movimiento reducido e idiomas/accesibilidad siguen como recorridos no certificados manualmente.
4. Validar el subtítulo corregido y cancelar antes/no cancelar después del check-in en el APK nuevo.

Los riesgos generales de **reglas Firestore permisivas** y **eliminación parcial de cuenta** permanecen abiertos; ver `FASE3_AUDITORIA.md`. No se cambiaron reglas ni eliminación de cuenta en este ajuste.

## Preparación

- Compilar e instalar el debug actualizado en ambos teléfonos; no basta con actualizar el Worker. APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Usar cuentas y eventos de prueba autorizados: un organizador y un asistente **distinto**, evento verificado con cupo y sin check-in previo del asistente. Cualquier alta/asistencia modifica datos reales si Android apunta al proyecto real: hacerla solo con autorización.
- No se requieren reglas, índices, facturación ni despliegue Worker nuevos para estos cambios. La revisión IA reportada por el usuario es `ai-dual-2026-11-11-2`.
- Registrar dispositivo, versión Android/app, idioma y resultado. Repetir accesibilidad/textos en español e inglés. No incluir tokens, claves o información personal en capturas/logs.

## Galerías

Preparar eventos con una, dos y tres fotos distinguibles, en orden conocido. Para imagen fallida, usar un fixture local/autorizado; no modificar documentos de producción al azar.

| Sección | Acción | Resultado esperado |
|---|---|---|
| Detalle | Abrir cada evento | Primera foto como portada; orden original, mismas dimensiones y contenido del evento |
| Detalle con varias fotos | Esperar sin tocar | Avance cada 5 s y regreso de la última a la primera |
| Detalle | Swipe, flechas, mantener un dedo y soltar | Cambio manual; no avanza mientras se toca/arrastra; nueva espera completa de 5 s al terminar |
| Detalle | Pausar, esperar, reanudar | Pausa efectiva y botón accesible con estado/acción correctos |
| Detalle | Desplazar hero fuera de vista, ir a Inicio del teléfono y regresar, salir del detalle | No reproduce fuera de vista/segundo plano; no conserva temporizador activo al salir |
| Detalle | Activar Quitar animaciones/escala de animador 0 | Sin autoplay ni transiciones animadas; flechas siguen permitiendo cambiar foto |
| Inicio → Destacados | Flechas de foto y swipe horizontal de fila | Flechas cambian fotos; swipe cambia evento. Fecha, categoría, interés e inscripción siguen visibles y funcionan |
| Inicio → Recomendados | Flechas y swipe de fila | Mismo comportamiento; controles de foto no abren detalle ni marcan interés |
| Recomendados → Ver todos | Swipe sobre foto y flechas | Cambia foto dentro de la tarjeta; scroll vertical de lista y acceso al detalle funcionan |
| Inicio → Próximos | Pulsar control pequeño de posición | Avanza foto sin abrir detalle; miniatura conserva tamaño |
| Todas | Una foto, URL fallida y reordenar eventos | Una foto sin controles extra; fallo con placeholder; estado asociado al ID, no a la posición de tarjeta |
| Todas | TalkBack, español/inglés y tamaño de texto aumentado | Descripciones de foto/posición y acciones traducidas, controles utilizables y sin bloquear acciones de tarjeta |

No se cambia el límite de tres fotos, la subida Cloudinary ni el esquema `imageUrls`. La prueba automática de error usa una imagen local inexistente.

## QR: dos teléfonos

1. **Organizador, teléfono A:** Mis eventos → evento verificado → mostrar QR de asistencia. Usar el QR **del evento**, no el QR personal. Anotar título/código para comparar.
2. **Asistente, teléfono B:** anotar saldo en Perfil e historial en **Perfil → Mis estadísticas**. Abrir check-in desde el evento o seleccionar el evento pendiente; no debe aparecer éxito ni código autocompletado.
3. Pulsar Escanear QR con cámara, aceptar permiso y enfocar el QR de A. Esperar confirmación: título correcto y +15 puntos. Comprobar saldo aumentado exactamente 15 y una nueva entrada de asistencia en el historial; la asistencia queda con check-in.
4. Escanear otra vez: debe indicar asistencia ya registrada, sin otro incremento ni movimiento. El mensaje ya se reportó en Moto; comparar saldo/historial del reintento sigue pendiente. Reabrir la app y repetir.
5. Para reintento entre dispositivos, iniciar **la misma cuenta asistente** en un tercer dispositivo (o en A después de guardar el QR fuera de la app/cambiar sesión). Repetir el código: mismo saldo e historial único. No confundir esto con dos asistentes distintos, que sí pueden recibir su propio premio.
6. Como organizador, intentar su propio código: rechazo sin premio. Un moderador ajeno puede participar, pero recibe 0 puntos según la política existente.

### Casos negativos

- **Cancelar:** abrir cámara y pulsar Atrás; aviso de cancelación, sin asistencia/premio.
- **Permiso denegado:** negar cámara; explicación y código manual siguen disponibles. Denegar permanentemente, abrir Ajustes, conceder y regresar; un nuevo intento debe abrir cámara.
- **Código inválido:** QR de una URL/texto ajeno y código manual inexistente; error sin cambios.
- **Otro evento:** seleccionar evento X y leer QR de Y; rechazo por evento distinto. Al reiniciar el flujo puede elegirse otro evento.
- **Cupo:** sin inscripción y cupo lleno, rechazo; con inscripción confirmada previa, conserva su plaza y puede hacer check-in. Sin inscripción y con cupo, se permite llegada directa.
- **Estado:** evento finalizado/no público no permite check-in. No existe ventana horaria explícita adicional: no evaluar como fallo una restricción que el producto aún no define.
- **Red:** desactivar antes de confirmar; no asumir éxito offline. Restablecer y reintentar: como máximo un premio. Si el servidor confirmó pero se perdió la respuesta, el siguiente intento debe recuperar «ya registrado».
- **Lecturas repetidas:** mantener QR en cámara/pulsar varias veces; sin solicitudes simultáneas desde la UI ni premio repetido.
- **Cancelar inscripción:** antes del check-in, Cancelar asistencia debe eliminar la inscripción; después del check-in no debe ofrecer cancelación ni alterar asistencia/saldo/historial.

La consistencia usa transacción de asistencia/saldo/historial fijo `checkin_<eventId>_<userId>`. Avisos/insignias son secundarios. No reparar manualmente check-ins históricos sin analizar si ya recibieron puntos. Las reglas actuales aún permiten abuso desde clientes manipulados: estas pruebas no certifican seguridad integral.

## IA y consumo

- Con candidatos elegibles, abrir Inicio y verificar etiqueta remota o local; subtítulo neutral en ambos casos. Destacados debe seguir por votos.
- Con Network Inspector activo, contar POST `/recommend-events` antes/después de Ver todos: navegar por sí solo no debe añadir una petición. Cambios reales de sesión/elegibilidad, expiración/caché y espera pueden afectar una medición larga.
- No usar bucles para provocar 429/503. Si ocurren, registrar revisión/etapa/código fijo/HTTP de la misma petición, no cuerpos ni tokens. El respaldo local no se etiqueta como IA.

## Evidencia automatizada y reproducción

- **15/11:** `assembleDebug` y **196 unitarias** correctos; **2 pruebas nuevas** de `AttendanceBarSmokeTest` aprobadas en Pixel_9a (sin cancelar tras QR/con cancelar antes). Firestore local **5** y Worker **42** aprobadas nuevamente. `git diff --check` sin errores. No se instaló/verificó este ajuste en Moto. Lint y las 10 instrumentadas históricas no se repitieron en esta pasada.
- **11/11 (histórico):** **195 unitarias**, debug correcto, Lint **0 errores, 61 advertencias y 1 sugerencia**.
- Pixel_9a/API 37: **10 instrumentadas** (8 galerías, 1 apertura de cámara/Atrás, 1 navegación con API simulada contada). No verifican lectura óptica física ni check-in Android/Firebase completo.
- Firestore local: **5 pruebas** con reglas reales sin modificar, concurrencia entre clientes de la misma cuenta, rollback, cupo/propio/finalizado/inscrito, acceso no autenticado/historial y moderador sin premio. Clientes JS reproducen el protocolo Android; no son integración del SDK Android.
- Worker: **42 pruebas**; código/revisión sin cambios en esta tarea.

En PowerShell, desde `firebase-tests/`:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
npm ci
npm test
```

El script usa `demo-akj-checkin`, configuración `firebase.checkin-test.json`, Firestore `127.0.0.1:8180` y rechaza hosts no loopback. No usa cuentas de servicio ni datos reales. Solo limpia datos del emulador de prueba. Tooling fijado en lockfile: 15 vulnerabilidades npm moderadas, ninguna alta; override grpc 1.14.7. No aplicar `npm audit fix --force` sin evaluar cambios incompatibles.

Para instrumentación, apuntar explícitamente a `ANDROID_SERIAL=emulator-5554` y a las clases `EventPhotoGallerySmokeTest`, `QrCameraSmokeTest`, `RecommendationNavigationSmokeTest`. Había también un Moto conectado: el primer intento sin selección falló allí con «No compose hierarchies found» y **no** cuenta como validación del teléfono.

El agente no realizó despliegues, escrituras reales, commits ni push en estas pasadas. La prueba manual reportada por el usuario sí registró una asistencia; no debe confundirse con pruebas locales sin escrituras. Release firmado y App Distribution siguen pendientes.
