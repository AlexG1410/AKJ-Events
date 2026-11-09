# AKJ Events — Fase 3

Aplicación Android para descubrir, crear y participar en eventos comunitarios del Quindío. Es parte del proyecto de clase: una única `MainActivity` con Jetpack Compose, Material 3, `ViewModel` + `StateFlow` y Navigation Compose con rutas tipadas. Las pantallas replican los mockups de la carpeta `mockups/`.

Estamos en la fase 3 (entrega final), que se hace por pasos. Las **cuentas ya usan Firebase**: registro, inicio de sesión, cierre de sesión y recuperación de contraseña con **Firebase Authentication**, y los perfiles en **Cloud Firestore** (colección `users`). La sesión se mantiene al cerrar la app si se marca «Recordar este dispositivo» (preferencia guardada con DataStore). Los eventos, comentarios, asistencias y notificaciones siguen en memoria hasta el paso 4.

## Cuentas

- **Usuarios:** se crean desde la pantalla de Registro, que siempre crea cuentas con el rol Usuario.
- **Moderadores (precargados):** en la consola de Firebase → Authentication → Usuarios → «Agregar usuario», se crea la cuenta con su correo y contraseña. La persona inicia sesión una vez (la app crea su perfil como Usuario) y luego, en Firestore → `users` → su documento, se cambia el campo `role` a `MODERATOR`.
- **Recuperar contraseña:** Firebase envía al correo un enlace para crear una nueva contraseña. El idioma del correo se configura en Authentication → Plantillas.

## Qué incluye esta entrega

**Autenticación y roles**
- **Splash**, **Login**, **Registro** (crea la cuenta y abre la sesión; el correo no se puede repetir y la dirección se elige obligatoriamente en el mapa simulado) y **Recuperar contraseña**.
- Al iniciar sesión, el usuario entra al feed y el moderador a su **panel de moderación**. La barra inferior cambia según el rol: el botón central es **Crear** para el usuario y **Moderar** para el moderador.

**Usuario**
- **Feed (Home):** eventos verificados, destacados por votos "Me interesa", búsqueda, filtro por categoría y **filtro por ubicación** (cerca de mí, a 5 km, o en mi ciudad), "Me interesa" y acceso al mapa.
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
- **Panel de moderación:** cola de pendientes por orden de llegada, búsqueda, pestañas por estado, ficha completa del evento, **verificar**, **rechazar con causal y motivo escrito obligatorio** y **marcar cualquier evento como finalizado**. Cada decisión se notifica al organizador.

**Reputación**
- **Puntos:** crear un evento (+10), evento verificado (+20), evento finalizado (+10), comentar (+5), recibir un "Me interesa" (+2) y asistir con QR (+15).
- La recompensa por verificación se entrega una sola vez por evento; editarlo y aprobarlo de nuevo no repite esos puntos. El cupo no puede reducirse por debajo de los asistentes confirmados.
- **Niveles:** Espectador (0), Participante (100), Organizador (250) y Líder Comunitario (500 puntos).
- **Insignias:** Primer evento, Primer aporte, Primera asistencia, 10 Verificados y Destacado del mes.
- **Idiomas:** la app está en inglés (`res/values/strings.xml`, idioma por defecto) y español (`res/values-es/strings.xml`), según el idioma del teléfono. Desde Android 13 también se puede elegir en Ajustes > Apps > AKJ Events > Idioma. Las notificaciones y el historial de puntos guardan datos, no frases, así que cada persona los lee en su idioma.

## Qué está simulado

- **Eventos y notificaciones en memoria:** `InMemoryEventRepository` e `InMemoryNotificationRepository` arrancan con los datos de `data/demo/` y se pierden al cerrar la app (pasan a Firestore en el paso 4). Mientras tanto, los organizadores de los eventos de ejemplo se muestran con sus datos de muestra.
- **Mapa:** `core/component/SimulatedMap` dibuja un mapa del Eje Cafetero con coordenadas reales: se puede mover, acercar, ubicar eventos y elegir un punto. La búsqueda usa lugares de referencia locales (`SamplePlaces`) en lugar de un servicio de mapas.
- **Filtros de ubicación y fecha:** "En mi ciudad" usa las coordenadas: respeta el municipio de los lugares conocidos del catálogo local y, para otros puntos, aproxima por el centro más cercano. No depende del texto de la dirección ni usa límites municipales reales. "Esta semana" muestra inicios desde el momento actual hasta antes de siete días después.
- **QR:** los códigos QR son reales (librería ZXing), pero no se usa la cámara: el escaneo se simula escribiendo el código o eligiendo un evento confirmado.
- **Imágenes:** las fotos salen de URLs de ejemplo de picsum.photos, con una imagen local de respaldo. La foto de perfil se muestra con las iniciales.

Solo los eventos `VERIFIED` se muestran públicamente. Los rechazados y pendientes no aparecen en el feed ni en el mapa.

## Cómo ejecutar la app

1. En Android Studio, selecciona **Open** y abre la carpeta del proyecto (la que contiene `settings.gradle.kts`).
2. Espera a que termine **Gradle Sync**. El proyecto usa JDK 17, Gradle 9.6 y Android SDK 37; instala los componentes que Android Studio indique.
3. Elige un emulador o dispositivo con Android 9 (API 28) o superior y pulsa **Run 'app'**.
4. Crea una cuenta desde Registro o inicia sesión con una cuenta existente.

El archivo `app/google-services.json` conecta la app con el proyecto de Firebase `akj---events`.

**Probar con el emulador local de Firebase** (no toca el proyecto real): instala las herramientas con `npm install -g firebase-tools`, y desde la raíz del proyecto ejecuta `firebase emulators:start --only auth,firestore`. Luego conecta el teléfono con `adb reverse tcp:9099 tcp:9099` y `adb reverse tcp:8080 tcp:8080`, e instala la app con `./gradlew installDebug -PuseFirebaseEmulator=true -PfirebaseEmulatorHost=127.0.0.1`. La consola del emulador queda en http://127.0.0.1:4000, y allí aparecen las cuentas, los perfiles y los enlaces de recuperación de contraseña.

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
- `domain/`: modelos (`CommunityEvent`, `User`, `Comment`, `AppNotification`, insignias, niveles, puntos...), las interfaces de los repositorios y los casos de uso (`domain/usecase/`), que aplican las reglas: permisos, puntos, insignias y notificaciones.
- `data/`: los repositorios, que solo guardan y consultan datos: `FirebaseUserRepository` (Authentication + Firestore) y sus DTO en `data/remote/`, la preferencia de sesión con DataStore, los repositorios en memoria y los datos de demostración.
- `di/`: los módulos de Hilt: `RepositoryModule` (qué implementación usar para cada repositorio), `FirebaseModule` (instancias de Firebase) y `CoroutinesModule`.
- `features/`: una carpeta por pantalla, cada una con su `Screen` y su `ViewModel` (`@HiltViewModel`) con su `UiState`. Los ViewModels solo usan casos de uso, nunca los repositorios directamente.
- `navigation/`: navegación principal, rutas tipadas y el `SessionViewModel` (rol y alertas sin leer).

Las dependencias las crea Hilt: pantalla (`hiltViewModel()`) → ViewModel → casos de uso → interfaz del repositorio → implementación (Firebase o en memoria).

## Pasos de la fase 3

1. ~~Repositorios y casos de uso `suspend`.~~
2. ~~Internacionalización en inglés y español.~~
3. ~~Firebase Authentication con recuperación de contraseña y sesión recordada.~~
4. Eventos, comentarios, asistencias y notificaciones en Firestore.
5. Imágenes de eventos y de perfil en la nube.
6. Mapa real con Mapbox y permiso de ubicación.
7. Sugerencia de categoría con IA (OpenRouter), siempre con opción de cambiarla.
8. APK release distribuido con Firebase App Distribution.
9. Documentación de los servicios, bibliotecas y herramientas usadas.
