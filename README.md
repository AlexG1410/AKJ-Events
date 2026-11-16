# AKJ Events — Fase 3

Aplicación Android para descubrir, crear y participar en eventos comunitarios del Quindío. Es parte del proyecto de clase: una única `MainActivity` con Jetpack Compose, Material 3, `ViewModel` + `StateFlow` y Navigation Compose con rutas tipadas. Las pantallas replican los mockups de la carpeta `mockups/`.

Estamos cerrando la fase 3 **en debug**; R8, firma, release y App Distribution se preparan después. La app usa **Firebase Authentication** para cuentas/recuperación y **Cloud Firestore** para perfiles, historial, eventos, comentarios, participación y notificaciones internas. Usa listeners y DataStore para recordar el dispositivo. Las reglas locales se han endurecido y probado, pero **no están desplegadas y aún queda un bloqueo de seguridad en reputación genérica**. Ver [cierre funcional debug](docs/CIERRE_DEBUG_FASE3.md) para proveedores, recuperación de eliminación, evidencia y recorrido pendiente.

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
- **Editar perfil:** foto, datos, mapa, intereses y eliminación con **contraseña actual antes de borrar datos**. Cancelar/contraseña incorrecta no inicia limpieza. Fallos posteriores pueden ser parciales: reintentar con la misma cuenta; límites/recuperación asistida en la guía de cierre. Cloudinary no se borra automáticamente.
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
- **QR:** ya no es simulado: ZXing Embedded abre la cámara y decodifica offline; se conserva entrada manual. Elegir un evento abre el escáner y exige su código, no fabrica una lectura. El usuario reportó lectura con cámara en Moto G15 y confirmación/+15/historial; saldo del reintento y misma cuenta en otro dispositivo siguen pendientes.
- **Imágenes de ejemplo:** los eventos de muestra usan fotos de picsum.photos. Las fotos que suben los usuarios sí van a la nube (ver «Fotos en la nube»).

## Galerías y check-in: actualización local

Las fotos mantienen el orden de `imageUrls` y la primera como portada. Destacados y vista previa de recomendaciones usan flechas para no interferir con el swipe entre eventos; la lista completa permite deslizar fotos; Próximos usa un control compacto. El detalle permite swipe y avance circular cada 5 s, con pausa/reanudación, pausa al tocar y detención fuera de vista/segundo plano o con animaciones desactivadas. Se reutilizan Compose Foundation (BOM actual) y Coil, sin cambiar subidas ni esquema Firestore.

`CheckInUseCase` usa `FirestoreCheckInRepository`: asistencia, saldo (+15 para usuario, 0 para moderador) e historial determinista se confirman en una transacción. Los reintentos no vuelven a premiar; una carrera se confirma con lecturas de servidor, nunca con éxito offline supuesto. Insignias/avisos son secundarios. Se conserva acceso sin inscripción si hay cupo, prohibición de evento propio y ausencia de ventana horaria explícita. Los check-ins históricos parciales no se reparan automáticamente.

**Cierre debug 15/11:** `assembleDebug`, **200 unitarias**, **20 instrumentadas** en Pixel_9a (3 eliminación/diálogo contra emuladores + 17 regresiones), **16 Auth/Firestore locales** y **42 Worker** correctos. Lint: **0 errores, 62 advertencias, 1 sugerencia**; sin baseline/supresiones. Comando HTTP solicitado con JBR 25 pasó sus 7+9 pruebas conservando HttpServer. `git diff --check` correcto. Reglas modificadas solo localmente, sin despliegues/escrituras reales. Seguridad residual y recorridos externos pendientes en [cierre debug](docs/CIERRE_DEBUG_FASE3.md) y [auditoría](docs/FASE3_AUDITORIA.md).

El equipo reportó desplegada y usada la revisión IA `ai-dual-2026-11-11-2`, con categorías y recomendaciones remotas/locales visibles. No se ha vuelto a consultar/desplegar el Worker en esta pasada; no hay un pendiente confirmado de volverlo a desplegar. 429/503 y POST reales Inicio → Ver todos siguen por medir. La propuesta local de reglas/contrato QR requiere revisión coordinada con clientes antes de cualquier despliegue futuro, no activación de facturación.

**Actualización 15/11:** Moto G15: QR y swipe/autoplay del detalle reportados por el usuario. El mensaje de reintento ya registrado no certifica todavía saldo invariable; capturas de 117/146 no son un antes/después. Se corrigió el subtítulo QR en ambos idiomas y se sustituyó Cancelar asistencia por estado deshabilitado tras check-in, conforme a la prohibición ya existente; cancelar antes sigue permitido. Evidencia/pendientes en la guía de pruebas. Para compartir todos los cambios previos, ver [selección sugerida de commit](docs/REVISION_COMMIT_FASE3.md).

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

Referencia histórica (10 de noviembre de 2026, no comprobación actual): 165 unitarias y debug/Lint correctos (0 errores/59 advertencias/1 sugerencia) tras corregir inscripción propia y mensajes de permiso de cámara. Las comprobaciones actuales se registran en la auditoría, separadas de la evidencia manual.

## Fotos en la nube

Las fotos de los eventos y de perfil se toman con la **cámara** o se eligen de la **galería** (guía 15: permiso de cámara en tiempo de ejecución, `FileProvider` e `ImagePickerBottomSheet`; si se niega el permiso, la galería sigue funcionando). Al guardar, la app las reduce a 1600 px, las comprime en JPEG y las sube a **Cloudinary** con **Retrofit** (guía 18, `CloudinaryApi` y `NetworkModule`). En Firestore solo se guarda la URL pública. Si la subida falla, no se guarda nada y se muestra el error.

La subida usa un *upload preset* sin firma, así que la app no lleva ninguna clave secreta. Para compilar con la subida activa, agrega a `local.properties` (no se sube al repositorio):

```
cloudinary.cloudName=<cloud name de la cuenta>
cloudinary.uploadPreset=<preset sin firma>
```

El preset se crea en Cloudinary → Settings → Upload → Upload presets, con *Signing mode* = Unsigned. Sin estos datos la app funciona, pero no puede guardar fotos nuevas.

## Sugerencia de categoría con IA

Crear/editar evento incluye sugerencia mediante Cloudflare Worker, ID tokens Firebase y `openrouter/free`. El equipo reportó desplegada `ai-dual-2026-11-11-2` y éxito con aceptación explícita; quedan estabilidad y diagnóstico de fallos intermitentes, no un despliegue pendiente confirmado. No confundir sugerencia con ranking de eventos ni tests simulados con éxito externo.

La consulta automática espera 1,5 s sin cambios, con al menos 5 caracteres de título y 20 de descripción. La sugerencia requiere aceptación explícita; elegir una categoría manualmente cancela la consulta y detiene nuevas consultas automáticas. Al editar se conserva la categoría guardada y se consulta solo con el botón. Si falla la IA, el formulario sigue permitiendo selección manual y publicación normal.

Para conectar el servicio desplegado, agregar su URL pública a `local.properties`:

```properties
ai.baseUrl=https://akj-events-ai.TU-SUBDOMINIO.workers.dev/
```

**La clave de OpenRouter nunca va en Android.** Se guarda como secreto `OPENROUTER_API_KEY` en el Worker. Instrucciones desde «Crear aplicación», límites gratuitos, privacidad, pruebas y limitaciones de autenticación/cuotas en [`worker/README.md`](worker/README.md). Sin URL, la app compila y explica que se debe elegir categoría manualmente. No se ha activado facturación ni se han desplegado servicios desde este cambio.

### Recomendaciones de eventos con IA

Inicio y «Recomendados para ti» comparten ViewModel y repositorio. Hilt conecta ahora `RemoteRecommendationRanker` a `POST /recommend-events`; `LocalRecommendationRanker` sigue como respaldo ante error, cuota, respuesta inválida o espera de actualización. Las pantallas indican «Ordenados con IA remota» o «Recomendaciones locales» sin presentar el respaldo como IA. «Eventos Destacados» conserva el orden por votos.

Se mantienen filtros de elegibilidad, preselección de 24 candidatos, hasta 20 interacciones, validación de IDs/motivos y revalidación de sesión/contexto. Solo se envían textos breves de eventos, categorías, actividad e idioma; no perfil, dirección ni coordenadas. Caché remota 5 minutos, respaldo 60 segundos y máximo un intento por usuario/minuto en el repositorio; la cuota del Worker/OpenRouter se comparte con categorías. No hay consultas duplicadas solo por abrir la pantalla completa. Detalles y prueba remota/respaldo en [`features/recommendations/README.md`](app/src/main/java/co/edu/uniquindio/akjevents/features/recommendations/README.md).

Estado: ambas rutas probadas localmente y éxito externo reportado por equipo; sin consultas reales ni despliegues en esta pasada. La matriz está en [`docs/FASE3_AUDITORIA.md`](docs/FASE3_AUDITORIA.md).

Solo los eventos `VERIFIED` se muestran públicamente. Los rechazados y pendientes no aparecen en el feed ni en el mapa.

## Cómo ejecutar la app

1. En Android Studio, selecciona **Open** y abre la carpeta del proyecto (la que contiene `settings.gradle.kts`).
2. En **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**, selecciona el JDK integrado de Android Studio (`jbr`). Espera a que termine **Gradle Sync**. El proyecto usa Gradle 9.6 y Android SDK 37; instala los componentes que Android Studio indique. La compilación local se comprobó con el JBR 25 incluido en Android Studio.
3. Elige un emulador o dispositivo con Android 9 (API 28) o superior y pulsa **Run 'app'**.
4. Crea una cuenta desde Registro o inicia sesión con una cuenta existente.

**Si la terminal muestra `JAVA_HOME is set to an invalid directory`:** la variable apunta a una instalación de Java inexistente. Configura `JAVA_HOME` en las variables de entorno de tu usuario con la carpeta del JDK integrado de tu instalación de Android Studio (por ejemplo, `C:\Program Files\Android\Android Studio\jbr`, sin añadir `\bin`). Cierra y vuelve a abrir Android Studio y la terminal para cargar el cambio. No guardes rutas de Java específicas de tu equipo en `gradle.properties`.

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

- `core/`: tema, componentes compartidos (barras, tarjetas, Mapbox, galerías, QR) y utilidades (`RequestResult`, validación, formatos, búsqueda).
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
7. Dos funciones IA implementadas, éxito externo reportado; cerrar recorridos/estabilidad sin consumir cuota en bucles. Ver `worker/README.md`.
8. APK release/App Distribution: **siguiente etapa**, no parte del cierre debug actual.
9. Documentación de los servicios, bibliotecas y herramientas usadas.
