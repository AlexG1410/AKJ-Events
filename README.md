# AKJ Events — Fase 2

Aplicación Android para descubrir, crear y participar en eventos comunitarios del Quindío. Es parte del proyecto de clase: una única `MainActivity` con Jetpack Compose, Material 3, `ViewModel` + `StateFlow` y Navigation Compose con rutas tipadas. Las pantallas replican los mockups de la carpeta `mockups/`.

En esta fase la aplicación es funcional de punta a punta con los datos en memoria: no hay base de datos, mapas reales, carga de imágenes, permisos ni sesiones persistentes (el enunciado no los pide todavía).

## Cuentas de demostración

| Rol | Correo | Contraseña |
|---|---|---|
| Usuario | `camilo.rodriguez@akjevents.co` | `AkjEvents123` |
| Moderador | `moderador@akjevents.co` | `Moderador123` |
| Moderador | `moderacion@akjevents.co` | `Moderador123` |

Los moderadores están precargados; el Registro siempre crea cuentas de Usuario. Estas contraseñas solo existen en memoria y no tienen valor de seguridad.

## Qué incluye esta entrega

**Autenticación y roles**
- **Splash**, **Login**, **Registro** (crea la cuenta y abre la sesión; el correo no se puede repetir) y **Recuperar contraseña**.
- Al iniciar sesión, el usuario entra al feed y el moderador a su **panel de moderación**. La barra inferior cambia según el rol: el botón central es **Crear** para el usuario y **Moderar** para el moderador.

**Usuario**
- **Feed (Home):** eventos verificados, búsqueda, filtro por categoría y **filtro por ubicación** (cerca de mí, a 5 km, o en mi ciudad), "Me interesa" y acceso al mapa.
- **Mapa (explorar):** los eventos sobre el mapa, con filtros de cercanía, categoría y "esta semana", tarjeta del evento elegido con distancia y "Cómo llegar".
- **Detalle de evento:** información completa, **ubicación exacta en el mapa**, organizador, cupos, confirmar o cancelar asistencia, "Me interesa" y **comentarios**. El autor y los moderadores pueden ver eventos pendientes o rechazados (con su estado y el motivo).
- **Crear y editar evento:** título, categoría, descripción, imagen, fechas, lugar, **punto en el mapa** y cupo opcional. Todo evento nuevo o editado queda pendiente de verificación.
- **Mis eventos:** pestañas por estado. Según el estado se puede editar, eliminar, reenviar un rechazado, **marcar como finalizado**, mostrar el **QR de asistencia**, ver los inscritos y enviarles un aviso.
- **Notificaciones:** comentarios en mis eventos, verificación o rechazo (con el motivo), avisos de organizadores, eventos finalizados, insignias y subidas de nivel. Se marcan como leídas o no leídas y abren el evento relacionado.
- **Perfil:** nivel, puntos, ruta de los 4 niveles, estadísticas, insignias, eventos recientes y cierre de sesión.
- **Editar perfil:** datos personales, ciudad, **ubicación en el mapa**, intereses y **eliminar la cuenta** (borra también sus eventos, comentarios y asistencias).
- **Mis estadísticas:** eventos activos, finalizados y pendientes, asistencias con QR, comentarios, alcance, tasa de cumplimiento, gráfica por categoría e historial de puntos.
- **Check-in con QR:** el organizador muestra el QR de su evento; el asistente valida el código y suma puntos. También está el QR personal del usuario.

**Moderador**
- **Panel de moderación:** cola de pendientes por orden de llegada, búsqueda, pestañas por estado, ficha completa del evento, **verificar**, **rechazar con motivo obligatorio** (causales y mensaje) y **marcar cualquier evento como finalizado**. Cada decisión se notifica al organizador.

**Reputación**
- **Puntos:** crear un evento (+10), evento verificado (+20), evento finalizado (+10), comentar (+5), recibir un "Me interesa" (+2) y asistir con QR (+15).
- **Niveles:** Espectador (0), Participante (100), Organizador (250) y Líder Comunitario (500 puntos).
- **Insignias:** Primer evento, Primer aporte, Primera asistencia, 10 Verificados y Destacado del mes.

## Qué está simulado

- **Datos y sesión en memoria:** los repositorios `InMemoryUserRepository`, `InMemoryEventRepository` e `InMemoryNotificationRepository` arrancan con los datos de `data/demo/`. Los cambios se comparten entre pantallas, pero se pierden al cerrar la app.
- **Mapa:** `core/component/SimulatedMap` dibuja un mapa del Eje Cafetero con coordenadas reales: se puede mover, acercar, ubicar eventos y elegir un punto. La búsqueda usa lugares de referencia locales (`SamplePlaces`) en lugar de un servicio de mapas.
- **QR:** los códigos QR son reales (librería ZXing), pero no se usa la cámara: el escaneo se simula escribiendo el código o eligiendo un evento confirmado.
- **Imágenes:** las fotos salen de URLs de ejemplo de picsum.photos, con una imagen local de respaldo. La foto de perfil se muestra con las iniciales.
- **Recuperar contraseña:** valida el correo y simula el envío del enlace.

Solo los eventos `VERIFIED` se muestran públicamente. Los rechazados y pendientes no aparecen en el feed ni en el mapa.

## Cómo ejecutar la app

1. En Android Studio, selecciona **Open** y abre la carpeta del proyecto (la que contiene `settings.gradle.kts`).
2. Espera a que termine **Gradle Sync**. El proyecto usa JDK 17, Gradle 9.6 y Android SDK 37; instala los componentes que Android Studio indique.
3. Elige un emulador o dispositivo con Android 9 (API 28) o superior y pulsa **Run 'app'**.
4. Inicia sesión con una de las cuentas de demostración.

Desde la terminal, en la raíz del proyecto (`.\gradlew.bat` en PowerShell):

```
./gradlew assembleDebug        # compila el APK de depuración
./gradlew installDebug         # lo instala en el emulador o dispositivo conectado
./gradlew testDebugUnitTest    # pruebas unitarias
./gradlew lint                 # análisis de Android Lint
```

## Organización del código

Dentro de `app/src/main/java/co/edu/uniquindio/akjevents/`:

- `core/`: tema, componentes compartidos (barras, tarjetas, mapa simulado, QR) y utilidades (`RequestResult`, validación, formatos, búsqueda).
- `domain/`: modelos (`CommunityEvent`, `User`, `Comment`, `AppNotification`, insignias, niveles, puntos...) y las interfaces de los repositorios.
- `data/`: los repositorios en memoria, que también aplican las reglas (puntos, insignias, notificaciones y permisos), y los datos de demostración.
- `features/`: una carpeta por pantalla, cada una con su `Screen` y su `ViewModel` con su `UiState`.
- `navigation/`: navegación principal, rutas tipadas y el `SessionViewModel` (rol y alertas sin leer).

## Siguientes etapas

1. Conectar un servicio para autenticación, datos, notificaciones e imágenes (Supabase o Firebase), con los permisos aplicados en la base de datos.
2. Cambiar el mapa simulado por Mapbox o Google Maps y pedir el permiso de ubicación.
3. Subir imágenes propias de los eventos y de perfil a un servicio externo.
4. Escanear el QR con la cámara y verificar el check-in en el servidor.
5. Sugerir la categoría con IA a partir del título y la descripción, siempre con opción de cambiarla.
