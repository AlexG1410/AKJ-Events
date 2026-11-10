# AKJ Events — Fase 3

Aplicación Android para descubrir, crear y participar en eventos comunitarios del Quindío. Es parte del proyecto de clase: una única `MainActivity` con Jetpack Compose, Material 3, `ViewModel` + `StateFlow` y Navigation Compose con rutas tipadas. Las pantallas replican los mockups de la carpeta `mockups/`.

Estamos en la fase 3 (entrega final), que se hace por pasos. La app usa **Firebase**: registro, inicio de sesión, cierre de sesión y recuperación de contraseña con **Firebase Authentication**, y los datos en **Cloud Firestore**: perfiles, historial de puntos, eventos, comentarios, asistencias, intereses y notificaciones. Los cambios se ven en tiempo real y los permisos se aplican en la base de datos con reglas de seguridad (`firestore.rules`). La sesión se mantiene al cerrar la app si se marca «Recordar este dispositivo» (preferencia guardada con DataStore).

## Cuentas

- **Usuarios:** se crean desde la pantalla de Registro, que siempre crea cuentas con el rol Usuario.
- **Moderadores (precargados):** en la consola de Firebase → Authentication → Usuarios → «Agregar usuario», se crea la cuenta con su correo y contraseña. La persona inicia sesión una vez (la app crea su perfil como Usuario) y luego, en Firestore → `users` → su documento, se cambia el campo `role` a `MODERATOR`.
- **Datos de ejemplo:** los eventos de muestra y sus organizadores se cargan una vez con el script `seed/seed.mjs` (ver «Cómo ejecutar»). Esos organizadores solo tienen perfil, no cuenta: nadie inicia sesión con ellos.
- **Recuperar contraseña:** Firebase envía al correo un enlace para crear una nueva contraseña. El idioma del correo se configura en Authentication → Plantillas.

## Qué incluye esta entrega

**Autenticación y roles**
- **Splash**, **Login**, **Registro** (crea la cuenta y abre la sesión; el correo no se puede repetir y la ubicación se elige obligatoriamente en el mapa real) y **Recuperar contraseña**.
- Al iniciar sesión, el usuario entra al feed y el moderador a su **panel de moderación**. La barra inferior cambia según el rol: el botón central es **Crear** para el usuario y **Moderar** para el moderador.

**Usuario**
- **Feed (Home):** eventos verificados, destacados por votos "Me interesa", búsqueda, filtro por categoría y **filtro por ubicación** (cerca de mí, a 5 km, o en mi ciudad), "Me interesa" y acceso al mapa.
- **Mapa (explorar):** mapa real con Mapbox, marcadores agrupados de eventos verificados, filtros de cercanía, categoría y "esta semana", tarjeta del evento elegido con distancia y "Cómo llegar". Tocar una zona vacía oculta la tarjeta; para seleccionar eventos agrupados, primero se acerca el mapa.
- **Detalle de evento:** información completa, **ubicación exacta en el mapa**, organizador, cupos, confirmar o cancelar asistencia, "Me interesa" y **comentarios**. El autor y los moderadores pueden ver eventos pendientes o rechazados (con su estado y el motivo).
- **Asistencia del organizador:** no puede confirmar asistencia ni hacer check-in como participante de su propio evento. El detalle lo explica y oculta la inscripción; una inscripción anterior sin check-in puede cancelarse para liberar el cupo, sin posibilidad de reinscribirse. No se borran registros históricos automáticamente. La validación existe en casos de uso y repositorios Android (incluida la transacción de Firestore), pero no sustituye reglas de seguridad del servidor.
- **Crear y editar evento:** título, categoría, descripción, **hasta 3 fotos** (cámara o galería; la primera es la principal), fechas, lugar, **punto en el mapa** y cupo opcional. Todo evento nuevo o editado queda pendiente de verificación.
- **Mis eventos:** pestañas por estado. Según el estado se puede editar, eliminar, reenviar un rechazado, **marcar como finalizado**, mostrar el **QR de asistencia**, ver los inscritos y enviarles un aviso.
- **Notificaciones:** comentarios en mis eventos, verificación o rechazo (con el motivo), avisos de organizadores, eventos finalizados, insignias y subidas de nivel. Se marcan como leídas o no leídas y abren el evento relacionado.
- **Perfil:** nivel, puntos, ruta de los 4 niveles, estadísticas, insignias, eventos recientes y cierre de sesión.
- **Editar perfil:** **foto de perfil** (cámara o galería), datos personales, ciudad, **ubicación en el mapa**, intereses y **eliminar la cuenta** (borra también sus eventos, comentarios y asistencias).
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

- **Búsqueda de lugares:** el buscador del selector usa `SamplePlaces` (referencias locales), no búsqueda remota. El mapa y la geocodificación inversa sí son reales. Solo se usa una dirección del catálogo como respaldo al elegir expresamente uno de sus lugares; no se asigna una ciudad local a cualquier punto del mundo.
- **Filtros de ubicación y fecha:** "En mi ciudad" usa las coordenadas: respeta el municipio de los lugares conocidos del catálogo local y, para otros puntos, aproxima por el centro más cercano. No depende del texto de la dirección ni usa límites municipales reales. "Esta semana" muestra inicios desde el momento actual hasta antes de siete días después.
- **QR:** los códigos QR son reales (librería ZXing), pero no se usa la cámara: el escaneo se simula escribiendo el código o eligiendo un evento confirmado.
- **Imágenes de ejemplo:** los eventos de muestra usan fotos de picsum.photos. Las fotos que suben los usuarios sí van a la nube (ver «Fotos en la nube»).

## Mapbox: integración incremental

Explorar muestra el mapa real y recibe exclusivamente los eventos filtrados de `MapViewModel`. Conserva los filtros, el acceso al detalle y la tarjeta existente; el componente no consulta Firebase directamente. Los marcadores usan el color terracota y se agrupan con un radio de 60 píxeles. La cámara se centra en el evento seleccionado o en la ubicación del perfil al solicitar el filtro de cercanía. El logotipo y la atribución de Mapbox permanecen visibles.

Se requiere el token **público** de Mapbox en `local.properties` (no se sube al repositorio):

```properties
MAPBOX_ACCESS_TOKEN=<token público pk.>
```

Gradle genera `mapbox_access_token` como recurso con `resValues = true`. Cada integrante debe configurar su token y sincronizar; no hace falta ninguna clave secreta en Android. Se conservan las dependencias 11.22.0 `-ndk27` y las ABI `arm64-v8a`/`x86_64` de la guía. La carga del mapa requiere acceso al servicio y queda sujeta a sus cuotas.

**Pasos 7, 9 y 10 implementados en código:** botón «Mi ubicación actual» en mapas interactivos, solicitud conjunta de permisos aproximado/preciso (cualquiera basta), comprobación al regresar a primer plano y alternativa manual con acceso a Ajustes si se deniega. Una petición centra la cámara en la primera posición recibida; en el selector también actualiza el punto elegido, sin seguimiento continuo. Si no llega posición en 15 segundos se muestra un aviso. En el emulador se configura la ubicación en Extended Controls → Location. El filtro «Cerca de mí» y las distancias siguen tomando la ubicación guardada del perfil, no el GPS. El mapa interactivo solicita prioridad de gestos sobre el scroll; el detalle permite desplazar la pantalla sin mover el mapa.

### Dirección y ciudad del punto elegido (guía 16, actividad 1 adaptada)

Mapbox sigue mostrando el mapa; `AndroidGeocodingRepository` usa **`android.location.Geocoder`**, con inyección en Hilt y el flujo pantalla → `PickLocationViewModel` → `ReverseGeocodeUseCase` → repositorio. No se activó facturación ni se añadieron claves/dependencias. Se eligió este proveedor en lugar de la API de Mapbox porque [guardar sus resultados requiere geocodificación permanente](https://docs.mapbox.com/api/search/geocoding/#storing-geocoding-results), con tarjeta registrada o contrato empresarial. Esta adaptación no reproduce literalmente el proveedor de la actividad del profesor.

- Al elegir un punto, usar GPS o elegir una referencia local, se consulta su dirección y municipio/región. Arrastrar solo cambia la cámara. Una dirección existente se conserva al reabrir; «Consultar dirección» permite actualizarla voluntariamente.
- La selección espera 350 ms antes de consultar; las respuestas de selecciones anteriores y las cancelaciones no sobrescriben la selección actual ni una dirección escrita manualmente. La consulta no cambia las coordenadas por las del edificio más cercano.
- Se muestran carga, resultado aproximado, ausencia de dirección y error. Durante la consulta se puede usar «Editar dirección» o «Usar coordenadas». No se promete exactitud ni cobertura universal; revisar antes de confirmar.
- «Editar dirección» permite escribir dirección y ciudad sin mover el punto, incluso sin servicio. No se elige automáticamente la ciudad más próxima del catálogo si el proveedor no devuelve municipio. Registro/perfil siguen requiriendo ciudad: si solo se confirman coordenadas sin ella, debe completarse desde el selector.
- `PickedLocation` y el resultado JSON de navegación transportan también la ciudad. Se aceptan resultados antiguos y se limpia el resultado sin desconectar el `StateFlow`, permitiendo volver a elegir. Registro/perfil conservan la ciudad obtenida, incluso fuera del Eje Cafetero; los documentos existentes no se migran automáticamente.
- Crear/editar eventos actualiza la dirección rellenada por el mapa al reseleccionar, pero conserva un punto de encuentro modificado a mano. El detalle muestra la dirección guardada, sin nuevas consultas ni cambios de esquema en Firestore.
- La API asíncrona se usa desde Android 13; en Android 9–12 se usa la API anterior en IO. Se solicita un timeout de 10 s, aunque en esas versiones antiguas una llamada bloqueante del proveedor puede tardar más en retornar. Siempre queda disponible la alternativa manual. Ver [limitaciones oficiales de Geocoder](https://developer.android.com/reference/android/location/Geocoder): depende del servicio instalado y no garantiza disponibilidad, precisión ni idioma.

**Pendiente:** búsqueda real de direcciones, personalización visual adicional y comprobación manual completa de permisos/GPS y retorno del selector en registro, perfil y eventos en el teléfono. El filtro «En mi ciudad» continúa usando la aproximación local descrita arriba, no la ciudad devuelta por geocodificación ni límites municipales.

`MapBoxSmokeTest` contiene tres pruebas instrumentadas aisladas, sin escrituras en Firebase: carga real y selección de un evento local por ID, selección/reselección de coordenadas por toque y bloqueo de selección en modo de solo lectura. Se repitieron correctamente en Pixel_9a. `GeocodingSmokeTest` pasó otras cuatro: consulta real del servicio Android para un punto público, mapeo de dirección/municipio, confirmar una dirección encontrada en la UI y edición manual durante una consulta pendiente. Se revisó una captura del selector. No verifican agrupación, GPS/permisos ni navegación autenticada completa. Las advertencias de destino Compose de Mapbox y la deprecación de la regla Compose usada en instrumentación siguen pendientes.

Última comprobación (10 de noviembre de 2026, tras corregir inscripción propia y mensajes de permiso de cámara): **165 pruebas unitarias aprobadas**, `assembleDebug` y `lintDebug` correctos. Lint reporta **0 errores, 59 advertencias y 1 sugerencia**, sin supresiones ni baseline. Los mensajes de cámara en crear evento y editar perfil usan `stringResource` (guía 14). Las cinco pruebas nuevas cubren bloqueo del organizador en asistencia/check-in, ausencia de puntos o cupos consumidos, UI state, cancelación de inscripción antigua y exclusión del check-in pendiente. No se repitieron pruebas instrumentadas ni el recorrido visual de estos últimos cambios: no había dispositivo conectado. No hubo escrituras ni despliegues de Firebase.

## Fotos en la nube

Las fotos de los eventos y de perfil se toman con la **cámara** o se eligen de la **galería** (guía 15: permiso de cámara en tiempo de ejecución, `FileProvider` e `ImagePickerBottomSheet`; si se niega el permiso, la galería sigue funcionando). Al guardar, la app las reduce a 1600 px, las comprime en JPEG y las sube a **Cloudinary** con **Retrofit** (guía 18, `CloudinaryApi` y `NetworkModule`). En Firestore solo se guarda la URL pública. Si la subida falla, no se guarda nada y se muestra el error.

La subida usa un *upload preset* sin firma, así que la app no lleva ninguna clave secreta. Para compilar con la subida activa, agrega a `local.properties` (no se sube al repositorio):

```
cloudinary.cloudName=<cloud name de la cuenta>
cloudinary.uploadPreset=<preset sin firma>
```

El preset se crea en Cloudinary → Settings → Upload → Upload presets, con *Signing mode* = Unsigned. Sin estos datos la app funciona, pero no puede guardar fotos nuevas.

Solo los eventos `VERIFIED` se muestran públicamente. Los rechazados y pendientes no aparecen en el feed ni en el mapa.

## Cómo ejecutar la app

1. En Android Studio, selecciona **Open** y abre la carpeta del proyecto (la que contiene `settings.gradle.kts`).
2. Espera a que termine **Gradle Sync**. El proyecto usa JDK 17, Gradle 9.6 y Android SDK 37; instala los componentes que Android Studio indique.
3. Elige un emulador o dispositivo con Android 9 (API 28) o superior y pulsa **Run 'app'**.
4. Crea una cuenta desde Registro o inicia sesión con una cuenta existente.

El archivo `app/google-services.json` conecta la app con el proyecto de Firebase `akj---events`.

**Probar con el emulador local de Firebase** (no toca el proyecto real): instala las herramientas con `npm install -g firebase-tools`, y desde la raíz del proyecto ejecuta `firebase emulators:start --only auth,firestore`. Luego conecta el teléfono con `adb reverse tcp:9099 tcp:9099` y `adb reverse tcp:8080 tcp:8080`, e instala la app con `./gradlew installDebug -PuseFirebaseEmulator=true -PfirebaseEmulatorHost=127.0.0.1`. La consola del emulador queda en http://127.0.0.1:4000, y allí aparecen las cuentas, los datos y los enlaces de recuperación de contraseña. El emulador usa las reglas de `firestore.rules` y arranca vacío: carga los datos de ejemplo cada vez que lo inicies (ver abajo).

**Datos de ejemplo** (requiere Node.js): `cd seed`, `npm install` y luego:

```
FIRESTORE_EMULATOR_HOST=127.0.0.1:8080 node seed.mjs                       # emulador local
GOOGLE_APPLICATION_CREDENTIALS=<ruta a la clave> node seed.mjs             # proyecto real
```

Para el proyecto real, la clave se descarga en la consola de Firebase → Configuración del proyecto → Cuentas de servicio → «Generar nueva clave privada». Guárdala fuera del repositorio: da acceso total a la base de datos. Si la colección `events` ya tiene datos, el script no hace nada (con `--force` sobrescribe los ejemplos).

**Reglas de seguridad en el proyecto real:** `firebase deploy --only firestore:rules` (después de `firebase login`).

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
- `data/`: los repositorios, que solo guardan y consultan datos: `FirebaseUserRepository` (Authentication + Firestore), `FirestoreEventRepository` y `FirestoreNotificationRepository`, con sus DTO en `data/remote/`; la preferencia de sesión con DataStore; los repositorios en memoria, que usan las pruebas unitarias, y los datos de demostración.
- `di/`: los módulos de Hilt: `RepositoryModule` (qué implementación usar para cada repositorio), `FirebaseModule` (instancias de Firebase), `NetworkModule` (Retrofit para Cloudinary) y `CoroutinesModule`.
- `features/`: una carpeta por pantalla, cada una con su `Screen` y su `ViewModel` (`@HiltViewModel`) con su `UiState`. Los ViewModels solo usan casos de uso, nunca los repositorios directamente.
- `navigation/`: navegación principal, rutas tipadas y el `SessionViewModel` (rol y alertas sin leer).

Las dependencias las crea Hilt: pantalla (`hiltViewModel()`) → ViewModel → casos de uso → interfaz del repositorio → implementación (Firebase o en memoria).

## Pasos de la fase 3

1. ~~Repositorios y casos de uso `suspend`.~~
2. ~~Internacionalización en inglés y español.~~
3. ~~Firebase Authentication con recuperación de contraseña y sesión recordada.~~
4. ~~Eventos, comentarios, asistencias y notificaciones en Firestore, con reglas de seguridad y datos de ejemplo.~~
5. ~~Fotos de eventos y de perfil con cámara o galería, subidas a Cloudinary.~~
6. Mapa real con Mapbox y permiso de ubicación: Explorar, selector y detalle integrados; GPS opcional y geocodificación inversa con Geocoder de Android. Pendientes búsqueda remota, mejoras visuales y validación completa en teléfono.
7. Sugerencia de categoría con IA (OpenRouter), siempre con opción de cambiarla.
8. APK release distribuido con Firebase App Distribution.
9. Documentación de los servicios, bibliotecas y herramientas usadas.
