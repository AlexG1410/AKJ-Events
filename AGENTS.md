# Instrucciones de desarrollo — AKJ Events

Estas instrucciones están dirigidas a integrantes y agentes de IA que trabajen en la aplicación Android **AKJ Events**. Actualícelas cuando el profesor entregue nuevas guías o cambie el alcance de la entrega.

> **Uso con OpenCode:** este `AGENTS.md`, ubicado en la raíz del repositorio, es la fuente principal de instrucciones y se carga automáticamente al iniciar OpenCode desde la carpeta del proyecto. `Instrucciones.md` es un archivo anterior y no debe duplicar ni contradecir estas reglas.

## 1. Contexto y alcance

- Aplicación Android nativa en **Kotlin y Jetpack Compose** para descubrir, publicar y participar en eventos comunitarios.
- Paquete de AKJ Events: `co.edu.uniquindio.akjevents`. El paquete `demoapp` del profesor es un ejemplo; adapte el código al paquete real.
- Roles: **Usuario** y **Moderador**. El moderador verifica o rechaza eventos con motivo, y puede finalizarlos. Los eventos rechazados no deben aparecer en el feed público.
- Los usuarios pueden registrarse, iniciar sesión, recuperar contraseña, explorar eventos en lista o mapa, filtrar, publicar y gestionar sus eventos, mostrar interés, comentar, confirmar asistencia, recibir notificaciones y consultar perfil, estadísticas, puntos, niveles e insignias.
- Un evento tiene título, categoría, descripción, ubicación con latitud y longitud, al menos una imagen, fecha/hora de inicio y fin, y cupo máximo opcional. Categorías: Deportes, Cultura, Académico, Voluntariado y Social. Niveles: Espectador, Participante, Organizador y Líder Comunitario.
- Necesidades adicionales: check-in con QR y sugerencia automática de categoría basada en título y descripción, con opción de cambiarla manualmente.

**Cada tarea tiene su propio alcance.** Las funciones anteriores describen el producto completo: no obligan a implementarlo todo en la entrega actual. Consulte el encargo y la rúbrica vigente antes de decidir qué hacer. No presente pantallas de muestra o acciones simuladas como funcionalidades terminadas.

## 2. Fuentes de trabajo y prioridad

1. **Requisitos y rúbrica vigentes:** indican qué comportamiento debe funcionar en la entrega.
2. **Guías del profesor:** orientan la forma de programar, organizar el código y definir la navegación. Consulte las guías pertinentes para la tarea.
3. **Proyecto base del profesor:** referencia de convenciones y distribución del código; adapte sus ejemplos a eventos.
4. **Código actual de AKJ Events:** verifique lo que ya existe y funciona antes de modificarlo.
5. **Mockups de Stitch y `DESIGN.md`:** referencia visual y de recorridos; no son código Android ni prueba de que la funcionalidad existe.

Si una guía contradice un requisito, o dos fuentes indican estructuras incompatibles, señale el conflicto concreto y proponga una solución antes de alterar esa parte. Si una fuente no está disponible, dígalo y no le atribuya reglas que no pudo comprobar.

### Guías disponibles hasta ahora

| Guía | Tema y fuente | Consultar cuando se trabaje en… |
| --- | --- | --- |
| 04 | Programación Android en Kotlin; archivo suministrado `guía-04-programación-de-aplicaciones-android-en-kotlin.txt` | Activity, arquitectura, capas y ViewModel. |
| [05](https://caflorezvi.github.io/guias-apps-moviles/05.primera-app.html) | Primera aplicación | Configuración Android, `MainActivity` y recursos. |
| [06](https://caflorezvi.github.io/guias-apps-moviles/06.composables.html) | Composables | Pantallas, componentes reutilizables y tema. |
| [07](https://caflorezvi.github.io/guias-apps-moviles/07.formularios-basicos.html) | Formularios básicos | Estado, validación, login, registro y ViewModels. |
| [08](https://caflorezvi.github.io/guias-apps-moviles/08.formularios-avanzados.html) | Formularios avanzados | Desplegables, diálogos, iconos e imágenes. |
| [09](https://caflorezvi.github.io/guias-apps-moviles/09.entidades-dominio.html) | Entidades de dominio | Modelos de usuario, evento, ubicación, categorías y estados. |
| [10](https://caflorezvi.github.io/guias-apps-moviles/10.navegacion-1.html) | Navegación I | Destinos y conexiones entre pantallas. |
| [11](https://caflorezvi.github.io/guias-apps-moviles/11.navegacion-2.html) | Navegación II | Tabs, barra inferior, dashboard, roles y navegación anidada. |
| [14](https://caflorezvi.github.io/guias-apps-moviles/14.strings.html) | Cadenas de texto e idiomas | Recursos de textos, `stringResource`, mensajes de ViewModels y traducciones inglés/español. |
| [15](https://caflorezvi.github.io/guias-apps-moviles/15.manejo-permisos.html) | Permisos e imágenes | Cámara, galería sin permisos amplios, `FileProvider`, foto de perfil/evento y carga remota. |
| [16](https://caflorezvi.github.io/guias-apps-moviles/16.mapas.html) | Mapas y geolocalización | Mapbox reutilizable, permisos de ubicación, marcadores, selección de coordenadas y geocodificación inversa. |
| [18](https://caflorezvi.github.io/guias-apps-moviles/18.apis-rest.html) | APIs REST con Retrofit | DTO, repositorio remoto, casos de uso, Hilt y estados de red. Fuente web revisada para la integración de IA. |
| [21](https://caflorezvi.github.io/guias-apps-moviles/21.generar-apk.html) | Release y distribución | Firma, R8, protección de DTO por reflexión, pruebas release y App Distribution. Fuente revisada; no implica release ya generado. |

El repositorio actual incluye los mockups en `mockups/`. Las versiones de texto de las guías **04–08** y el proyecto base `proyecto-guía-moviles-20262-main.zip` no están guardados en este repositorio; si una tarea los requiere, localice su ruta real en el equipo o solicítela antes de atribuirles reglas. El proyecto base usa `demoapp` y reportes; aproveche su estructura sin copiar su funcionalidad ni reemplazar avances de AKJ Events.

**Cuando llegue una nueva guía:** añada una fila con número, enlace o archivo y tema; revise qué cambia para el código, la estructura o la navegación; actualice las reglas correspondientes. No dé por aplicada una guía que todavía no se haya entregado.

## 3. Organización del proyecto y del código

El proyecto actual usa una sola `MainActivity` que aloja Compose, Hilt para inyección de dependencias y casos de uso para las operaciones de dominio. Conserve esta distribución y compruebe los paquetes existentes antes de crear otros:

```text
app/src/main/java/co/edu/uniquindio/akjevents/
├── AKJEventsApp.kt      # Application con Hilt
├── MainActivity.kt
├── core/
│   ├── component/       # composables reutilizables
│   ├── theme/           # color, tipografía y tema
│   └── util/            # utilidades compartidas
├── navigation/          # destinos tipados y grafo principal
├── di/                  # módulos Hilt: repositorios, Firebase, red, recursos y corrutinas
├── domain/
│   ├── model/           # entidades, categorías, roles y estados
│   ├── repository/      # contratos de repositorios
│   └── usecase/         # reglas de negocio y operaciones por funcionalidad
├── data/
│   ├── demo/            # datos de ejemplo y catálogo local de lugares
│   ├── remote/          # DTO de Firestore y API de Cloudinary
│   └── repository/      # en memoria para pruebas; local/ (DataStore) y remote/ (servicios)
└── features/            # pantallas y ViewModels por funcionalidad
```

- Conserve el código existente si cumple los requisitos y funciona. Evite componentes duplicados o mover paquetes por una preferencia personal.
- Coloque la presentación en `*Screen.kt`. Para formularios y operaciones con estado, use `*ViewModel.kt` y `UiState` siguiendo las guías; la pantalla observa el estado y envía acciones.
- Ponga entidades compartidas en `domain/model`, reglas de negocio en `domain/usecase` y fuentes de datos e integraciones externas en `data`. Los ViewModels usan casos de uso, no repositorios directamente. Conserve el flujo pantalla → ViewModel → caso de uso → interfaz de repositorio → implementación.
- Use `@HiltViewModel`, constructores inyectados y `hiltViewModel()` siguiendo el código existente. Vincule implementaciones en `di/RepositoryModule.kt`; no vuelva a crear repositorios compartidos manualmente en las pantallas. Mantenga las operaciones `suspend` y sus llamadas desde corrutinas, sin bloquear el hilo principal; no oculte la cancelación como un error de negocio.
- Coloque en `core/component` únicamente componentes que se reutilicen; los específicos van en su `feature`. Respete la configuración existente de Gradle y el catálogo de versiones al añadir dependencias.

### Criterios incorporados de las guías 14–16

- Textos de interfaz, accesibilidad y mensajes: usar recursos en `res/values/strings.xml` (inglés) y `res/values-es/strings.xml` (español), con todas las claves en el archivo predeterminado. Resolverlos con `stringResource` en Compose y las utilidades existentes `ResourceProvider`, `UiText` y `Labels` donde corresponda. No introducir un contexto de Activity en ViewModels. Conservar argumentos y plurales equivalentes entre idiomas y `res/xml/locales_config.xml`. No traducir contenido introducido por usuarios como si fuera texto fijo de la aplicación.
- Permisos: solicitarlos desde la UI al usar la función, mantener alternativas cuando se denieguen y explicar cómo habilitarlos desde Ajustes. Elegir una imagen con `GetContent` no requiere permisos de almacenamiento. Para el flujo de cámara de la guía, usar `TakePicture`, URI temporal con `FileProvider` y conservar ese URI frente a recreaciones; no presentar el URI temporal como almacenamiento remoto definitivo.
- Mapas: adaptar el componente reutilizable de Mapbox a eventos y conservar callbacks y estado en las pantallas/ViewModels. Admitir ubicación aproximada o precisa, volver a comprobar permisos al regresar a primer plano y permitir selección manual sin GPS. Mapbox usa longitud/latitud; el modelo `Location` usa latitud/longitud. La geocodificación inversa debe iniciarse desde el ViewModel, con estados de carga y error, dejando la integración externa en `data`.
- Configuración: la guía 16 usa Mapbox 11.22.0 con variantes `-ndk27`, repositorio Maven de Mapbox y token público `pk.` desde `local.properties`, generado como recurso mediante Gradle. No subirlo al repositorio: este token público sí termina en el APK y no debe confundirse con una clave secreta de servidor. Mapbox puede funcionar con eventos en memoria, sin esperar un backend.
- Los ejemplos 14–15 presuponen Hilt y casos de uso, que ya existen en el proyecto actualizado. El código y el README también hacen referencia a las guías 12–13 y 19–20; antes de atribuirles reglas nuevas, consultar los documentos concretos y registrar sus enlaces verificados en la tabla. Las fuentes web 18 y 21 ya se revisaron para preparar IA y distribución.
- Estado actual: inglés/español, selector de cámara/galería, permiso de cámara y subida a Cloudinary están implementados en código. Mapbox está integrado en Explorar, selector y detalle (pasos 5–10); permisos aproximado/preciso y GPS opcional implementados y probados manualmente por el usuario en sus recorridos, no en todos los escenarios. Geocodificación inversa implementada con `Geocoder` de Android como adaptación de la actividad 1, sin activar geocodificación permanente/facturación de Mapbox. No confundir el permiso de cámara para fotos con un escáner QR real.

## 4. Rutas y navegación

En el proyecto real, `navigation/MainRoutes.kt` define destinos tipados con `@Serializable`, y `navigation/AppNavigation.kt` contiene `NavHost`, `composable<...>` y `toRoute<...>` para los argumentos. Siga este patrón:

- Defina cada destino navegable una sola vez, cuando la pantalla exista: inicio, autenticación, detalle, mapa, creación, perfil o moderación según el alcance de la tarea.
- Mantenga el destino inicial y el grafo en `AppNavigation`. Entregue callbacks de navegación a las pantallas en vez de crear varios `NavController` independientes.
- Abra el detalle mediante el ID del evento y cargue allí su información; no dependa de datos de demostración ni pase un objeto completo en la ruta.
- Compruebe que los accesos desde feed, mapa, notificaciones y perfil apunten al destino correcto. Revise el botón Atrás y la pila tras login, logout y acciones de moderación.
- Si un destino o flujo todavía no existe, repórtelo como pendiente; una pantalla vacía no completa la función.

## 5. Diseño visual y mockups

En `mockups/`, cada pantalla tiene un archivo `.png` (apariencia) y otro `.html` (disposición de referencia) con el mismo nombre. `mockups/DESIGN.md` registra colores, tipografía, espaciado y formas, y `mockups/README.md` identifica el alcance de cada pantalla. Hay mockups de inicio, detalle, mapa, crear/editar evento, autenticación, perfil, notificaciones, estadísticas, moderación y QR.

Al implementar una pantalla, consulte **su** imagen, su HTML y el sistema de diseño. Traslade jerarquía visual, textos y estados pertinentes a Compose y Material 3; adapte el resultado a Android. Centralice valores compartidos en `core/theme` y componentes repetidos en `core/component`. No inserte HTML, clases CSS ni URLs de imágenes de ejemplo como si fueran la implementación final.

## 6. Flujo para cada cambio

1. Determine la funcionalidad solicitada y los criterios de la entrega actual. Lea únicamente las guías, el código del profesor y el mockup pertinentes; no vuelva a cargar todo el material en cada sesión.
2. Inspeccione el código actual: paquetes, modelos, pantallas, rutas, ViewModels, dependencias y recursos. Distinga lo real, lo simulado y lo pendiente.
3. Explique de forma breve qué archivos va a modificar y por qué. Implemente sin romper rutas ni flujos existentes.
4. Si intervienen autenticación, mapas, almacenamiento de imágenes, notificaciones u otros servicios, indique la configuración necesaria. No publique credenciales reales, tokens ni claves en el repositorio; las credenciales locales de demostración deben estar claramente identificadas como datos sin valor de seguridad.
5. Compruebe que compile con Gradle y, para cambios visibles, recorra el flujo en emulador o dispositivo si está disponible. Diga exactamente qué verificó y qué no pudo verificar.
6. Informe archivos cambiados, comportamiento conseguido, guía aplicada y pendientes. No afirme que una entrega está «al 100 %» sin contrastarla con su rúbrica.

## 7. Estado técnico y precauciones de la fase 3

Estado revisado el **9 de noviembre de 2026**, hasta el commit `351416e`. Esta sección describe el código disponible, no certifica la configuración ni el funcionamiento de los proyectos remotos.

### Integraciones actuales

- **Cuentas:** `FirebaseUserRepository` usa Firebase Authentication para registro, login, logout y recuperación por correo; guarda perfiles e historial de puntos en Firestore. El cambio de correo requiere un enlace de confirmación. Los moderadores se crean y asignan desde la consola según el README; no suponga que las antiguas credenciales locales existen en Firebase ni permita elegir el rol Moderador desde Registro.
- **Sesión:** `DataStoreSessionPreferences` conserva «Recordar este dispositivo» y `RestoreSessionUseCase` decide si mantener la sesión al abrir la app. Respete la espera de inicialización antes de decidir la navegación.
- **Eventos y participación:** `FirestoreEventRepository` escucha eventos, comentarios, asistencias e intereses en tiempo real. Usa transacciones para conteos y cupos. `FirestoreNotificationRepository` guarda avisos internos por usuario; no es una integración de notificaciones push con FCM. Las implementaciones en memoria se conservan para pruebas, no son la fuente activa de la app.
- **Fotos:** `ImagePickerBottomSheet` y `rememberImagePicker` se reutilizan en perfil y eventos. Se admiten hasta tres fotos por evento y una foto de perfil. `UploadImagesUseCase` y `CloudinaryImageRepository` suben las nuevas imágenes mediante Retrofit, reducidas a 1600 px por lado y JPEG de calidad 85; se guardan URLs públicas en Firestore. La subida depende de `cloudinary.cloudName` y `cloudinary.uploadPreset` en `local.properties`. Actualmente se usa un preset sin firma; no hay clave secreta en Android ni debe añadirse.
- **Recomendaciones:** Inicio/lista comparten fuente y ViewModel en `AppNavigation`; `RemoteRecommendationRanker` usa `/recommend-events`, `LocalRecommendationRanker` es respaldo determinista, no IA. Conservar filtros, revalidación, 24 candidatos/20 interacciones y DTO mínimo sin identidad/dirección/coordenadas/fotos/distancia exacta. Etiquetas distinguen remoto/caché de local. Proyección semántica, debounce 750 ms, caché 5 min/60 s e intento cada 60 s evitan consumo por navegación. Destacados sigue por votos. El equipo reportó revisión `ai-dual-2026-11-11-2` desplegada y éxito de ambas rutas; no hay un redespliegue pendiente confirmado. Quedan estabilidad 429/503 y POST reales por navegación; no se verificó producción en el cierre debug.
- **IA de categorías:** `SuggestEventCategoryUseCase` → `RemoteCategorySuggestionRepository` → Worker en `worker/src/index.js`. El cliente usa su propio Retrofit sin redirecciones y un token Firebase; no reutilizarlo para Cloudinary ni introducir claves de OpenRouter en el APK. `ai.baseUrl` en `local.properties` contiene solo la URL HTTPS pública. El Worker valida tokens RS256 del proyecto indicado y usa exclusivamente `openrouter/free`; no acepta tokens sin firma del emulador. La sugerencia no selecciona categoría automáticamente, no bloquea el envío y respeta elección manual/edición. Consultar `worker/README.md` para despliegue, pruebas, cuotas y límites: el throttle en memoria es por instancia, no global, y la validación JWT no consulta revocación/deshabilitación. No afirmar que el servicio real funciona solo por pasar simulaciones locales.
- **Diagnóstico IA:** revisión actual `ai-dual-2026-11-11-2`, ambas rutas con `redirect: manual` y decodificación UTF-8 `fatal: true, ignoreBOM: false`. El usuario reportó éxito real de categorías y un 502/HTTP externo 200; el viejo `OPENROUTER_RESPONSE` no permite atribuir causa única. Códigos `AI_*` separan lectura, envelope/error en 200, generación incompleta, contenido vacío/JSON y categorías/ranking inválidos. Solo loguear etapa/código fijo/HTTP y metadatos permitidos `finishReason`/`model`, nunca cuerpos, prompts, tokens, encabezados ni UID. Un BOM interior fue reproducido y normalizado en pruebas, no probado como causa del incidente real. El Worker valida el contrato de recomendaciones pero no consulta Firestore ni certifica elegibilidad de clientes manipulados.
- **Entorno de prueba:** existen `firebase.json`, `firestore.rules` y `seed/seed.mjs`. Consultar el README para usar los emuladores de Auth/Firestore y cargar ejemplos. Por defecto la app apunta al proyecto real: no ejecutar escrituras de prueba, carga de datos, borrados ni despliegues sin autorización explícita. No guardar claves de cuentas de servicio en el repositorio.
- **Costo:** el proyecto debe mantenerse dentro de opciones gratuitas, incluido un eventual despliegue. No activar facturación, planes pagos ni servicios con cargos automáticos sin acuerdo del usuario. Las cuotas gratuitas no equivalen a disponibilidad ilimitada ni garantizada.
- **Mapbox:** `core/component/MapBox.kt` muestra eventos filtrados, agrupa marcadores y devuelve IDs/puntos por callbacks; no consulta repositorios. `LocationPermission.kt` gestiona permisos opcionales y el botón GPS. El selector usa toque para elegir (arrastrar no cambia la selección) y GPS puede actualizar el punto elegido; el detalle no es interactivo. El token público `MAPBOX_ACCESS_TOKEN` de `local.properties` se genera como recurso en Gradle. Conservar logotipo/atribución y las rutas existentes. «Cerca de mí» y las distancias siguen usando la ubicación del perfil, no la del dispositivo. Falta verificar completamente GPS/permisos/retorno a formularios en ejecución.
- **Direcciones:** `AndroidGeocodingRepository` usa el servicio `Geocoder` del dispositivo, no la API de Mapbox. `ReverseGeocodeUseCase` se llama desde `PickLocationViewModel`, con carga, error, sin resultados, reintento y edición manual. Cancelar/ignorar respuestas anteriores al cambiar punto o editar texto; no reemplazar las coordenadas elegidas ni inventar municipios con `SamplePlaces`. El catálogo sigue disponible para búsqueda local y respaldo de referencias elegidas expresamente. `PickedLocation` transporta ciudad mediante JSON compatible con resultados antiguos; vaciar el resultado con `set(null)`, no `remove()`, para conservar el flujo observado. Registro/perfil conservan la ciudad recibida; eventos conservan puntos de encuentro escritos a mano. Geocoder no necesita claves nuevas, pero depende del proveedor instalado y no garantiza dirección, disponibilidad ni exactitud.

### Pendientes y riesgos conocidos

- Explorar, selector y detalle tienen mapa real; Geocoder admite texto manual. Búsqueda y filtro ciudad siguen con `SamplePlaces`, no límites municipales/búsqueda remota. No migrar direcciones antiguas. Faltan GPS/permisos completos y Android 9–12. QR con cámara real y swipe/autoplay de detalle reportados en Moto; falta saldo/historial del reintento misma cuenta y otro dispositivo. Ambas IA con éxito reportado, no verificadas externamente por el agente. Release/R8/firma/App Distribution son la etapa siguiente. Ver matriz `docs/FASE3_AUDITORIA.md`, sin rúbrica con puntajes.
- **Seguridad Firestore:** reglas candidatas locales prueban contadores/participación vinculados, historial QR y moderación acotada, pero NO están desplegadas. Nuevo premio QR guarda `eventId` (DTO compatible con documentos anteriores; clientes viejos sin campo no crean premio bajo reglas nuevas). Coordinar compatibilidad antes de desplegar. Reputación genérica/insignias/historial no QR siguen manipulables por autenticados: test reproduce riesgo. Requiere premiación idempotente de servidor y denegar escrituras cliente, sin romper creación/comentarios/votos/moderación. No considerar cerrado ni poner Admin SDK en APK.
- **Imágenes:** el preset sin firma requiere restricciones contra abuso. Se guarda la URL, pero no hay gestión de borrado remoto de imágenes reemplazadas o descartadas. No afirmar que borrar una foto o cuenta libera automáticamente espacio en Cloudinary ni que subir archivos y guardar Firestore sea una operación atómica.
- **Eliminación:** `DeleteAccountUseCase(uid,password)` reautentica antes de toda escritura; cancelar/incorrecta no limpian. Marca `deletionPending`, limpia y confirma Auth al final; compensa perfil si falla Auth y mantiene sesión para reintento. Contraseña solo en memoria. No existe atomicidad Auth/Firestore: cierre/red también en compensación, sesión invalidada, concurrencia y volumen grande requieren recuperación durable de servidor; no declarar flujo robusto universal ni borrado Cloudinary. Guía `docs/CIERRE_DEBUG_FASE3.md`.
- **Documentación:** contrastar README y documentos del feature con el código; pueden conservar descripciones de etapas anteriores. No reemplazar avances actuales por implementaciones locales siguiendo documentación desactualizada.

### Cierre debug del 15 de noviembre de 2026

- Base `fase3-ia-categorias`/`000ec9d`, PR #7 borrador según equipo. Se conservó el cambio previo en `CategorySuggestionRepositoryTest.kt`. Comando exacto con JBR 25/`--rerun-tasks` pasó **7+9 HTTP** sin retirar HttpServer. Editor Studio no reproducido; no atribuirle causa sin evidencia.
- Reautenticación con contraseña antes de toda escritura al eliminar, marca `deletionPending`, limpieza/reintento y compensación de perfil al fallar Auth; 3 nuevas instrumentadas con SDK/repositorios Android demo y diálogo aprobadas. No garantizar recuperación universal (red/proceso/concurrencia/volumen) sin servidor durable. Reglas candidatas locales con nuevo `eventId` QR; riesgo de reputación genérica probado, no resuelto ni desplegado.
- `assembleDebug`, **200 unitarias**, `lintDebug` (**0 errores, 62 advertencias, 1 sugerencia**); **20 instrumentadas en dos ejecuciones** (3 eliminación +17 regresiones), **16 Auth/Firestore demo** y **42 Worker** correctas; diff sin errores. Audit Worker 0, tooling emulador 15 moderadas/0 altas. Sin supresiones, staging/commit/push/merge, despliegues ni pruebas contra producción. R8/firma/release/Distribution aplazados.
- `docs/CIERRE_DEBUG_FASE3.md` consolida proveedores, archivos y recorrido manual corto por dispositivo/cuenta; `FASE3_AUDITORIA.md` separa evidencia automatizada, reportada y pendientes. Mantener saldo/historial del reintento QR y misma cuenta entre dispositivos como pendientes manuales; 117/146 no son antes/después.

### Ajuste previo del 15 de noviembre de 2026 (histórico)

- Usuario reportó QR con cámara en Moto G15 (Feria de emprendimiento: confirmación/+15/movimiento +15), mensaje ya registrado al repetir, y swipe/autoplay del detalle (Concurso de talentos local, posiciones 1/3 y 3/3 y controles visibles). No dar por probados saldo/historial invariantes del reintento ni misma cuenta en otro dispositivo; capturas 117/146 no son antes/después. Esta evidencia sustituye el pendiente genérico de lectura física, no los demás escenarios.
- Subtítulo QR corregido en español/inglés (guía 14). La barra del detalle muestra estado deshabilitado tras check-in en lugar de Cancelar; antes se conserva cancelar. Política/casos de uso/repositorios ya lo prohibían; no cambia recompensas. Nuevo ajuste visual aún no probado en Moto.
- `assembleDebug`, **196 unitarias**, **2 instrumentadas** `AttendanceBarSmokeTest` en Pixel_9a, **5 Firestore local** y **42 Worker** aprobadas. Diff sin errores. Lint y las 10 instrumentadas anteriores no se repitieron; se conservan como evidencia histórica. Sin staging/commit/push, despliegues ni escrituras reales por el agente. Riesgos de reglas/eliminación de cuenta siguen abiertos. Ver `docs/REVISION_COMMIT_FASE3.md` para compartir también el código IA previo sin credenciales.

### Actualización local de galerías y QR (11 de noviembre de 2026)

- `EventPhotoGallery` reutiliza Foundation Pager/Coil: orden original/portada, flechas en filas, swipe en detalle/lista y control compacto en Próximos. Detalle con autoplay circular de 5 s, pausa por toque, botón, visibilidad/ciclo de vida y preferencia de animaciones. No cambia esquema ni subidas.
- El escáner **ya no es simulado**: ZXing Embedded 4.3.0, permiso/Ajustes, cancelación, validación del evento esperado y alternativa manual. Elegir evento no fabrica lectura. Las menciones anteriores a escaneo simulado son históricas y quedan reemplazadas por este estado. Falta lectura óptica física y recorrido de dos teléfonos.
- `CheckInRepository`/`FirestoreCheckInRepository` confirman asistencia, saldo e historial `checkin_<eventId>_<userId>` en una transacción. Reintentos/carreras solo se recuperan con confirmación de servidor; avisos/insignias son secundarios. Edición de perfil actualiza solo campos personales para no pisar reputación concurrente. Reglas sin cambios: permanece el riesgo de escrituras manipuladas. No se reparan check-ins parciales históricos automáticamente. Se conserva llegada sin inscripción con cupo, prohibición de propio evento, moderador sin puntos y ausencia de ventana horaria adicional.
- Usuario reportó revisión Worker `ai-dual-2026-11-11-2` desplegada y éxito real de categorías/recomendaciones remotas/respaldo local. Esto reemplaza el pendiente de despliegue reportado anteriormente, no certifica estabilidad: 429/503 intermitentes siguen pendientes. Navegación Inicio → Ver todos probada con una llamada simulada; falta contar POST reales.
- **195 unitarias**, `assembleDebug` y `lintDebug` correctos (**0 errores, 61 advertencias, 1 sugerencia**); **10 instrumentadas** Pixel_9a (8 galerías/cámara/navegación), **5 Firestore local** con reglas existentes y **42 Worker** aprobadas. Firestore JS reproduce protocolo, no integración Android completa. Tooling nuevo de emulador: 15 vulnerabilidades moderadas, 0 altas (grpc 1.14.7); no confundir con audit Worker. Sin despliegues, escrituras reales, commits/push ni cambio AGP 9.3.0. Guía manual: `docs/PRUEBAS_GALERIAS_QR.md`; auditoría actualizada.

### Última comprobación local

- Dos IA y auditoría (11 de noviembre de 2026): **190 pruebas unitarias aprobadas**, `assembleDebug` y `lintDebug` correctos (**0 errores, 58 advertencias y 1 sugerencia**, sin baseline/supresiones). **42 pruebas Worker aprobadas** (35 Node/7 workerd), auditoría npm sin vulnerabilidades. **5 pruebas de componentes Compose** aprobadas en Pixel_9a/API 37 (categoría y procedencia remoto/local), no equivalen a recorrido autenticado completo. Nuevas pruebas cubren contrato mínimo/HTTP loopback, respaldo por error/cuota/timeout/invalidez, caché, cambios irrelevantes, cambio de usuario/contexto y pérdida de elegibilidad en vuelo. No hubo consultas reales al LLM, escrituras Firebase/Cloudinary, despliegues, commits ni push en esta pasada. Continúan advertencias de deprecación de la regla Compose de los smoke tests. Fuentes oficiales PF/G21 consultadas; auditoría completa en `docs/FASE3_AUDITORIA.md`.

- Diagnóstico Worker IA (11 de noviembre de 2026): se reprodujo `503 service_unavailable` en workerd por `redirect: "error"` no soportado; las dos consultas salientes usan ahora `manual` y rechazan 3xx sin seguirlos. **33 pruebas Worker aprobadas** (29 Node/4 workerd), `npm audit` sin vulnerabilidades y `assembleDebug` correcto; sin cambios en Android, AGP 9.3.0 conservado. Miniflare es solo dependencia de desarrollo con lockfile y overrides de seguridad. Registro 5xx restringido a etapa/código fijo/HTTP externo; encabezado de revisión `ai-fix-redirect-2026-11-11-1`. El usuario reportó despliegue y bindings existentes, pero falta actualizar el código y verificar POST 200 + «Usar sugerencia» en producción. No se accedió a secretos ni se desplegó remotamente. Ver `worker/README.md`.

- IA de categorías (10 de noviembre de 2026): **180 pruebas unitarias aprobadas** (15 nuevas), `assembleDebug` y `lintDebug` correctos; **0 errores, 58 advertencias y 1 sugerencia**, sin baseline ni supresiones. Worker: **27 pruebas locales aprobadas** con JWT firmados por una clave efímera y servicios simulados, sin bypass de autenticación en producción. `CategorySuggestionSmokeTest`: **3 pruebas aprobadas** en Pixel_9a/API 37, solo del componente con estados simulados. Se mantiene la deprecación de `createComposeRule` en los smoke tests. No hubo despliegue, uso de clave real ni escrituras en Firebase/Cloudinary. Falta validar Worker desplegado, LLM gratuito real, recorrido autenticado completo y release. Instrucciones desde «Crear aplicación» en `worker/README.md`.

- Asistencia del organizador y Lint (10 de noviembre de 2026): **165 pruebas unitarias aprobadas**, `assembleDebug` y `lintDebug` correctos; **0 errores, 59 advertencias y 1 sugerencia**, sin baseline ni supresiones. Corregidos los dos `LocalContextGetResourceValueCall` con `stringResource` en crear evento y editar perfil (guía 14). El organizador no puede confirmar asistencia ni hacer check-in propio: casos de uso y repositorios Android lo bloquean, incluida la transacción de Firestore; no equivale a seguridad del servidor. El detalle permite cancelar una inscripción histórica propia sin check-in, sin borrado automático ni reinscripción; no ofrece check-in propio pendiente. Cinco pruebas nuevas cubren estas reglas. No hubo cambios/despliegues de reglas ni escrituras de prueba en Firebase. Sin dispositivo conectado para repetir instrumentación o comprobar visualmente este ajuste. Los resultados de Lint anteriores quedan como referencia histórica.
- Geocodificación inversa (10 de noviembre de 2026): `testDebugUnitTest` con **160 pruebas aprobadas**, `assembleDebug` correcto; `GeocodingSmokeTest` con **4 pruebas aprobadas** (incluida consulta real del servicio Android) y `MapBoxSmokeTest` con **3 pruebas aprobadas**, en ejecuciones separadas en Pixel_9a. Se comprobó el selector con respuesta encontrada y edición manual durante una consulta pendiente, sin escrituras en Firebase/Cloudinary. Falta recorrido completo en teléfono y prueba de Android 9–12. Lint permanece aplazado.
- Tras incorporar pasos 7, 9 y 10: `testDebugUnitTest` con **141 pruebas aprobadas** y `assembleDebug` correcto. `MapBoxSmokeTest` pasó sus **3 pruebas** en Pixel_9a: marcador por ID, selección/reselección de coordenadas y no selección en modo de solo lectura. No verifican permisos/GPS ni el retorno completo a formularios. Lint no se volvió a ejecutar; sus errores previos siguen aplazados. Las advertencias del destino Compose también aparecen en los efectos y anotaciones nuevos del mapa.
- Resultados del bloque anterior (referencia histórica):
- `testDebugUnitTest`: **139 pruebas aprobadas**, sin fallos, tras el primer bloque de Mapbox.
- `assembleDebug`: APK generado.
- `lintDebug`: **2 errores, 54 advertencias y 1 sugerencia** en la ejecución con el primer bloque de Mapbox. Los errores son `LocalContextGetResourceValueCall` en `CreateEventScreen.kt:205` y `EditProfileScreen.kt:131`, al obtener el mensaje de permiso de cámara. Las líneas pueden cambiar: buscar el identificador de Lint al corregirlos. No desactivar Lint ni crear una baseline para ocultarlos.
- `MapBoxSmokeTest` pasó en emulador: carga real de Mapbox y selección de un marcador por ID con un evento local, sin acceso a Firebase. La agrupación y la navegación completa desde Explorar siguen sin verificación de ejecución. El compilador advierte sobre el destino Compose al usar `PointAnnotationGroup` con las versiones actuales; no ocultar esa advertencia ni confundir el smoke test con una validación completa.
- No se verificaron las operaciones contra Firebase/Cloudinary reales. Volver a ejecutar las comprobaciones tras cambios: estos resultados no son una garantía permanente ni pruebas de integración de esos servicios.

Este archivo explica **cómo** trabajar en AKJ Events; la tarea y rúbrica actuales determinan **qué** construir ahora.
