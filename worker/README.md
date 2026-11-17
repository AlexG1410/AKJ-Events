# Sugerencia de categorías — AKJ Events

## Arquitectura y alcance

Android → `SuggestEventCategoryUseCase` → `CategorySuggestionRepository` → Worker HTTPS → OpenRouter.

El Worker verifica **criptográficamente** ID tokens de Firebase Authentication (RS256, firma con las claves públicas de Google, `aud`, `iss`, `sub`, `exp`, `iat`, `auth_time` y `nbf` si existe). No acepta tokens sin firma del emulador de Auth. No necesita cuenta de servicio, permisos de Firestore ni Firebase Functions/Blaze. Esta verificación no consulta si una cuenta fue deshabilitada o si el token fue revocado: un token ya emitido puede seguir siendo válido hasta su vencimiento. No es una autorización para modificar datos de Firebase.

Existen dos rutas independientes: `POST /suggest-category` y `POST /recommend-events`, ambas con ID token Firebase en `Authorization: Bearer …`. Categorías recibe JSON:

```json
{"title":"Taller comunitario","description":"Aprenderemos juntos sobre huertos urbanos."}
```

Respuesta: `{"category":"ACADEMIC"}`. Categorías: `SPORTS`, `CULTURE`, `ACADEMIC`, `VOLUNTEERING`, `SOCIAL`.

Se envían exclusivamente título y descripción al modelo; no se envían el token Firebase, UID, correo, fotos, ubicación ni historial. No introducir datos personales sensibles en el texto. No se registran prompts, respuestas ni secretos desde este código; las políticas del proveedor siguen aplicando.

El modelo está fijado en el código a **`openrouter/free`**. No admite parámetros para cambiarlo, listas de modelos de respaldo pagos, plugins ni herramientas. No comprar créditos, activar recarga automática ni cambiar a Workers Paid. Los modelos gratuitos pueden estar saturados o dejar de estar disponibles. Según la documentación consultada, una cuenta sin compras tiene 50 peticiones gratuitas diarias compartidas y 20 por minuto; verificar nuevamente las condiciones antes de entregar:

- <https://openrouter.ai/docs/api-reference/limits>
- <https://openrouter.ai/docs/faq>
- <https://developers.cloudflare.com/workers/platform/pricing/>

Protecciones: entrada ≤4096 bytes y límites de texto (5–80 título, 20–600 descripción), salida acotada, timeout de 12 s hacia IA y 5 s hacia claves, sin redirecciones, respuesta JSON con categoría estricta y sin mensajes internos del proveedor. Las claves públicas se cachean respetando su vigencia (hasta una hora), con protección básica frente a `kid` inventados.

**Limitación de cuota:** el throttle en memoria impone, **por instancia**, 5 s entre solicitudes del mismo UID, 10 solicitudes por UID/día UTC y 40 totales/día. No es un contador global: reinicios o instancias/regiones distintas pueden permitir superar esos valores. La cuota global gratuita la impone OpenRouter; no se garantiza reservar solicitudes para la demostración. Android usa debounce y caché de hasta 8 respuestas en el formulario. No presentar esto como protección antiabuso de producción; un contador distribuido sería una mejora independiente.

## Comportamiento Android

- Consulta automática tras 1,5 s sin cambios y al menos 5/20 caracteres de título/descripción.
- Una respuesta **no selecciona** categoría: se debe pulsar «Usar sugerencia» o elegir un chip manual.
- Elegir/aceptar categoría cancela la consulta pendiente y detiene nuevas consultas automáticas; queda el botón explícito para consultar otra vez.
- Al editar, se conserva la categoría guardada y solo se consulta por petición explícita.
- Cambiar texto invalida la sugerencia previa. Una respuesta obsoleta no sobrescribe el formulario.
- Los estados y errores de IA son independientes de guardar el evento. Sin red, cuota, sesión o endpoint se puede elegir manualmente y publicar con las validaciones habituales.
- No hay una clasificación local simulada en producción ni claves de OpenRouter en Android.

## Pruebas locales (sin credenciales ni servicios reales)

Desde `worker/`, ejecutar `npm ci` y luego `npm test` con Node 22 o superior. Miniflare/workerd se usa únicamente como dependencia de desarrollo para probar el runtime de Workers; no se incluye en el código que se pega en Cloudflare. Las pruebas usan Request/Response/Web Crypto reales, un par RSA efímero y respuestas de Google/OpenRouter simuladas, con red externa bloqueada en workerd. No se usa el par de pruebas en el Worker desplegado ni existe un modo de omitir autenticación. `package-lock.json` fija las dependencias; overrides de Undici/Sharp corrigen avisos de seguridad transitivos de la herramienta de pruebas.

Desde la raíz, ejecutar:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --max-workers=2
```

`CategorySuggestionRepositoryTest` incluye un servidor HTTP efímero de **loopback**, solo dentro de la JVM, para comprobar el contrato Retrofit, sin credenciales reales. `CategorySuggestionViewModelTest` verifica debounce, aceptación explícita, selección manual, edición, errores, caché, sesión y respuestas tardías. `CategorySuggestionSmokeTest` verifica únicamente el componente Compose con estados simulados en un dispositivo: no demuestra la integración remota ni el formulario completo.

Comprobación local del 10 de noviembre de 2026:

- `npm test`: **27 pruebas aprobadas**, sin llamadas reales a Google/OpenRouter.
- `testDebugUnitTest`: **180 pruebas aprobadas**, 0 fallos/errores/omitidas; 15 pruebas nuevas de IA. Incluye publicación en memoria tras un fallo de IA y selección manual.
- `assembleDebug`: correcto.
- `lintDebug`: correcto, **0 errores, 58 advertencias y 1 sugerencia**, sin baseline ni supresiones.
- `CategorySuggestionSmokeTest`: **3 pruebas aprobadas** en Pixel_9a (API 37): mostrar/aceptar sugerencia, carga sin solicitudes repetidas y error con reintento. Se conserva la advertencia de deprecación de `createComposeRule` usada por los smoke tests actuales.
- Sin despliegues, claves reales, escrituras en Firebase/Cloudinary ni pruebas con un LLM real. La validación en el runtime de Cloudflare y el recorrido autenticado completo siguen pendientes.

## Desplegar desde «Crear aplicación» en Cloudflare

Los nombres pueden cambiar según el idioma y la versión del panel. Si aparece una solicitud de pago, detenerse; no activar planes pagos.

1. En **Workers & Pages → Crear aplicación**, elegir **Worker / Create Worker** y la opción **Empezar con Hello World / Start with Hello World** (no Pages, un repositorio ni un servicio de IA de pago).
2. Usar un nombre disponible, por ejemplo `akj-events-ai`, y pulsar **Desplegar / Deploy**. Esto publica solo el ejemplo inicial de Cloudflare, no la integración.
3. Abrir el Worker y pulsar **Editar código / Edit code**. Reemplazar el archivo principal completo por el contenido de [`src/index.js`](src/index.js). Es un módulo ES, no usar `addEventListener`.
4. Pulsar **Desplegar / Deploy** para publicar el código. Sin variables devuelve `503 not_configured`; no acepta solicitudes de IA todavía.
5. Ir a **Configuración / Settings → Variables y secretos / Variables and Secrets → Añadir / Add**.
6. Añadir una variable de tipo **Texto / Text** llamada `FIREBASE_PROJECT_ID`, con el ID exacto del proyecto que aparece en Firebase → Configuración del proyecto. El repositorio actual usa `akj---events`; verificarlo antes de guardar. No es el nombre visible ni el ID de la aplicación Android.
7. Añadir una variable de tipo **Secreto / Secret** llamada **`OPENROUTER_API_KEY`**. Pegar la clave privada guardada, sin comillas. No usar tipo Text ni ponerla en el código.
8. Guardar y pulsar **Desplegar / Deploy** si el panel lo solicita. Revisar que el secreto se muestre oculto. Nunca enviarlo por chat ni capturarlo en pantalla.
9. Copiar la URL pública **de producción** `https://akj-events-ai.<subdominio>.workers.dev` desde la página del Worker, no una URL temporal de vista previa. No hace falta dominio propio ni bindings.
10. No probar con «Send» sin un ID token: un `POST` sin sesión debe devolver **401**. Abrir el endpoint en un navegador hace GET y devuelve **405**; no significa que falle. No pegar tokens reales en páginas externas o capturas.

Alternativa opcional por CLI: el archivo `wrangler.toml` describe el mismo Worker; autenticar con Wrangler y subir el secreto de manera interactiva. No se instaló Wrangler ni se desplegó nada durante la preparación local.

## Conectar Android después del despliegue

Agregar únicamente la URL pública a `local.properties` (ignorado por Git):

```properties
ai.baseUrl=https://akj-events-ai.TU-SUBDOMINIO.workers.dev/
```

Conservar HTTPS y la barra final; no añadir `/suggest-category`, parámetros ni credenciales. Cada integrante necesita esa propiedad para usar IA; sin ella debug sigue compilando y el formulario permite selección manual. **No agregar `OPENROUTER_API_KEY` a `local.properties` ni a `BuildConfig`.**

Sincronizar Gradle, compilar e instalar. Iniciar sesión en Firebase real (no su emulador), escribir un título y descripción de prueba, esperar la sugerencia y aceptarla. Verificar también cambio manual, edición y fallo de red. No hace falta publicar un evento para probar la sugerencia: no escribe en Firestore ni Cloudinary. El endpoint acepta solo tokens del proyecto configurado.

## Pendientes después de las pruebas locales

- Actualizar código del Worker ya desplegado por el usuario, conservando secreto y URL real.
- Comprobación de una respuesta de un LLM gratuito real, calidad en español/inglés y latencia.
- Recorrido completo en Android con sesión real y pruebas del APK release.
- Revisar preferencias de privacidad de OpenRouter sin habilitar almacenamiento/entrenamiento solo para obtener más proveedores.

Las pruebas simuladas no certifican la disponibilidad, calidad ni cuotas del proveedor externo.

## Dos funciones y diagnóstico de HTTP 200 inválido — revisión actual

Revisión para desplegar: **`ai-dual-2026-11-11-2`**. Conserva `redirect: manual` y `new TextDecoder("utf-8", { fatal: true, ignoreBOM: false })`; esta configuración del decodificador ya estaba en el archivo local al iniciar esta pasada.

El usuario reportó éxito real de categorías (POST 200, «Deportes») y un rechazo 502 con HTTP externo 200, requestId `ed91fb1ea4f2ceb57bed30c8f192425a`, 11/11/2026 12:25:30 Colombia. Las dos filas de una invocación no demuestran dos peticiones; el HTTP externo 400 de otro intento no se usa para explicar este 502. El código anterior agrupaba todos los motivos bajo `OPENROUTER_RESPONSE`, por lo que **no se puede atribuir ese intento a truncamiento, BOM u otro motivo concreto sin nuevos metadatos**.

Petición real del código: `model: openrouter/free`, `response_format: {type: json_object}`, `stream: false`, `reasoning: {enabled: false}`, `temperature: 0`. Categorías mantiene `max_tokens: 512`; recomendaciones usa 4096 para ordenar hasta 24 candidatos. No se aumentó el límite de categorías sin evidencia de truncamiento. Algunos proveedores/modelos pueden ignorar o no admitir parámetros: se controlan como error, nunca se cambia a pago ni se reintenta automáticamente.

Los errores de cuerpo HTTP 200 ahora se distinguen por código fijo:

| Código | Motivo |
|---|---|
| `AI_ERROR_IN_200` | Campo `error` no vacío dentro de respuesta HTTP 200 |
| `AI_BODY_MISSING` / `AI_BODY_READ` / `AI_BODY_TOO_LARGE` | Cuerpo ausente, error leyendo stream o límite excedido |
| `AI_BODY_UTF8` / `AI_BODY_JSON` | UTF-8 inválido o JSON exterior inválido |
| `AI_ENVELOPE_SHAPE` / `AI_MISSING_CHOICE` | Envelope sin estructura o elección válida |
| `AI_INCOMPLETE` | `finish_reason` distinto de `stop` (incluido `length`, filtrado o fallo) |
| `AI_EMPTY_CONTENT` / `AI_CONTENT_TYPE` | Contenido ausente/vacío o no textual |
| `AI_CONTENT_JSON` | Texto interior no es JSON válido; no se aceptan Markdown ni prosa |
| `AI_CATEGORY_SHAPE` / `AI_CATEGORY_NOT_ALLOWED` | Objeto incorrecto/extra campos o categoría fuera de las cinco |
| `AI_RANKING_SHAPE` / `AI_RANKING_ITEM` | Lista incompleta, IDs ajenos/duplicados o motivos inválidos |
| `AI_RESPONSE_UNEXPECTED` | Excepción no prevista durante la validación |

Se conservan solo `stage`, `code`, `externalHttp` y, si pudieron leerse con formato permitido, `finishReason` y `model`. No imprimir cuerpos, texto de entrada, mensajes de error del proveedor, tokens, encabezados ni UID. Dos filas de log de una invocación siguen siendo una sola solicitud.

Se reprodujo en pruebas un JSON interior correcto con BOM inicial que antes rechazaba `JSON.parse`; se elimina únicamente ese BOM de formato. Esto **no demuestra** que el BOM causara el incidente real. Se mantienen rechazo de categorías desconocidas, JSON truncado, campos extra y texto arbitrario.

### Contrato de recomendaciones

```json
{
  "language": "es",
  "interests": ["SPORTS"],
  "interactions": [{"title":"Ciclopaseo anterior","description":"Recorrido comunitario","category":"SPORTS","checkedIn":true}],
  "candidates": [{"eventId":"id-existente","title":"Ciclopaseo","description":"Actividad comunitaria","category":"SPORTS"}]
}
```

Respuesta: `{"items":[{"eventId":"id-existente","reason":"Por tus intereses deportivos"}]}`. Hasta 24 candidatos y 20 interacciones; títulos ≤100, descripciones ≤200, IDs ≤128, motivos de una línea ≤180, idioma `es`/`en`. Sin IDs duplicados, categorías inventadas ni campos extra. Se exige exactamente una salida por candidato. Candidatos vacíos devuelven lista vacía sin inferencia. Toda entrada se autentica y valida antes de consumir cuota; ambas rutas comparten contador.

Android filtra datos de sus repositorios Firebase: verificados, futuros, con cupo, ajenos al usuario y sin inscripción. Resuelve únicamente IDs recibidos y revalida a la vuelta. **El Worker no lee Firestore**: valida el contrato y pertenencia de salida a candidatos, pero no certifica que un cliente manipulado haya enviado candidatos reales/elegibles. No tiene acceso administrativo, herramientas ni capacidad de escribir publicaciones.

### Estado actual y procedimiento de despliegue (solo si se decide actualizar)

Actualización de documentación 15/11: el equipo reportó desplegada `ai-dual-2026-11-11-2` y éxito real de categoría y etiquetas remota/local en Pixel. No se requiere redesplegar por los cambios Android de eliminación/reglas. El agente no verificó producción en esta pasada. 429/503 intermitentes siguen abiertos hasta tener diagnóstico de la misma petición. Los pasos siguientes son una referencia operativa futura, **no una acción autorizada de esta etapa local**.

1. Worker existente → **Editar código**, pegar el archivo completo **`worker/src/index.js`** y pulsar **Implementar** en Producción; conservar las variables/secreto.
2. GET de cualquiera de las dos rutas: 405 y encabezado `X-AKJ-Worker-Revision: ai-dual-2026-11-11-2`.
3. Después sincronizar/compilar Android e instalar el APK debug. No cambiar `ai.baseUrl` ni agregar secretos a Android. El Worker nuevo es compatible con la ruta de categorías de la app anterior.
4. Categorías: sesión real, título/descripción válidos → POST `/suggest-category` 200 con categoría válida y «Usar sugerencia». Aceptar y comprobar elección manual. No hace falta guardar/publicar el evento.
5. Recomendaciones: usuario con eventos elegibles → POST `/recommend-events` 200 con `items`; después, etiqueta **«Ordenados con IA remota»** en Inicio y lista completa. Abrir la lista no debe generar otro POST solo por navegar. La etiqueta también puede representar una respuesta remota aún cacheada; comprobar Network Inspector para demostrar una llamada nueva.
6. Respaldo: ver el procedimiento offline y límites de caché en `features/recommendations/README.md`. No cambiar la clave de producción ni las reglas para simular fallos. Las pruebas locales ya simulan fallo, cuota, timeout, invalidez, sesión y pérdida de elegibilidad sin tocar Firebase real.
7. Si falla: copiar solo el JSON del mensaje personalizado con los campos permitidos, la ruta, HTTP y revisión. Una nueva prueba con `AI_INCOMPLETE` + `finishReason: length` permitiría atribuir truncamiento; el viejo `OPENROUTER_RESPONSE` no lo permite.

La quota gratuita es compartida: esperar al menos 5 s entre ambas rutas. Recomendaciones incorpora caché remota de 5 min, fallo de 60 s, ventana mínima de intento de 60 s por usuario y agrupación de cambios de 750 ms. No prometer que cada «Actualizar» consuma una nueva inferencia ni que haya cuota reservada.

Verificación de esta revisión: **42 pruebas Worker aprobadas** (35 Node/7 workerd), `npm audit` sin vulnerabilidades. Android: **190 unitarias**, `assembleDebug` y `lintDebug` correctos (0 errores, 58 advertencias, 1 sugerencia); **5 smoke tests de componentes** en Pixel_9a/API 37. Contratos HTTP con loopback y servicios simulados, sin LLM real ni escrituras remotas. No se desplegó código desde el agente. La auditoría del resto de la entrega está en `docs/FASE3_AUDITORIA.md`.

## Diagnóstico y corrección del 503 — 11 de noviembre de 2026

El usuario reportó un POST de producción con `503 {"error":"service_unavailable"}`, versión de Cloudflare `e70b7d17-dead-4378-930c-cf2c8c04bbab`, wallTime 2 ms y CPU 1 ms. El GET público devuelve 405. No se dispone del código privado de esa versión: un GET 405 no demuestra igualdad byte a byte con el archivo local.

Se reprodujo el mismo error con el archivo local en **workerd**, antes de OpenRouter. Causa demostrada: el runtime rechaza `redirect: "error"` con `TypeError: Invalid redirect value…`; solo admite `follow` o `manual`. Las pruebas anteriores en Node no detectaban esa diferencia. El tiempo corto es compatible con este fallo, pero no se usó como prueba de la causa en producción.

Corregidas las dos llamadas salientes a `redirect: "manual"`. Las comprobaciones de `response.ok` rechazan 3xx y nunca reenvían credenciales a la URL de una redirección. Sin cambios en Firebase, modelo gratuito, claves o Android.

### Salidas de la versión anterior

- Ruta/método: 404/405 antes de leer configuración.
- Configuración inválida o ausente: 503 `not_configured`. Un error inesperado al leer bindings o invocar `.trim()` sí escapaba al catch general.
- Token inválido, malformado, vencido o de otro proyecto: 401 `invalid_session`. Lectura de claims/reloj o errores inesperados de Web Crypto fuera del bloque de decodificación escapaban al catch general.
- Google: HTTP no satisfactorio, estructura sin claves o caché no disponible: 503 `session_verification_unavailable`. **La creación de fetch con `redirect:error`, rechazos de red/timeout y errores inesperados de lectura del stream/importación de clave escapaban al catch general**. El JSON inválido/acotado produce sus errores controlados, no necesariamente 503.
- OpenRouter: errores dentro de su bloque de fetch y HTTP no satisfactorio: 503 `ai_unavailable`; 429 se conserva; salida inválida: 502 `invalid_ai_response`. No son `service_unavailable`.
- **Solo el catch exterior escribía literalmente `503 service_unavailable` para excepciones que no fueran `HttpError`**. También cubre fallos inesperados de lectura de entrada, cuota o construcción de respuesta fuera de los catches internos.

### Registro mínimo e identificación

Solo al generar errores 5xx se emite un objeto JSON con `stage`, `code` y, si ya hay respuesta externa, `externalHttp`. Son códigos fijos, nunca el mensaje/stack de la excepción. No se registran claves, tokens, encabezados, UID ni contenido del evento.

Ejemplos: `configuration/CONFIG_READ`, `google_keys/GOOGLE_KEYS_FETCH`, `google_keys/GOOGLE_KEYS_HTTP`, `google_keys/GOOGLE_KEYS_BODY`, `firebase_key_import/FIREBASE_KEY_IMPORT`, `firebase_signature/FIREBASE_SIGNATURE_CHECK`, `openrouter/OPENROUTER_FETCH`, `openrouter/OPENROUTER_HTTP`.

Todas las respuestas incluyen `X-AKJ-Worker-Revision: ai-fix-redirect-2026-11-11-1`, para identificar esta revisión al desplegarla (no sustituye el ID de versión de Cloudflare ni es un hash del archivo).

Verificación local: **33 pruebas aprobadas** (29 Node y 4 workerd), incluyendo reproducción de la opción rechazada, POST autenticado simulado con HTTP 200 y categoría válida, redirecciones rechazadas y ausencia de datos sensibles en los logs. `npm audit`: 0 vulnerabilidades después de aplicar overrides. `assembleDebug`: correcto. No se repitieron las pruebas Android ni Lint porque no cambió código Android; sus resultados del 10 de noviembre son históricos.

### Actualizar el Worker existente y cerrar la comprobación real (procedimiento histórico)

La revisión siguiente reemplaza este identificador. Para el despliegue actual seguir **«Orden de despliegue y prueba en Pixel»** en la sección de dos funciones.

1. Workers & Pages → **akj-events-ai** → **Editar código**.
2. Reemplazar el archivo principal completo con **`worker/src/index.js`** del proyecto local. No pegar los archivos de pruebas.
3. Pulsar **Implementar / Deploy** (no dejarlo solo en vista previa). Si aparece elección de entorno, seleccionar **Producción**. Conservar las dos variables existentes, incluido el secreto cifrado; no volver a copiarlo por chat.
4. Abrir `/suggest-category` con GET y comprobar en Network Inspector/DevTools el encabezado **`X-AKJ-Worker-Revision`** con el valor indicado. HTTP 405 sigue siendo correcto para GET.
5. En Android, con sesión Firebase real, solicitar una categoría una sola vez (esperar al menos 5 s entre intentos). No hace falta modificar la URL ni publicar el evento.
6. Resultado necesario: **POST HTTP 200 con una de las cinco categorías y botón «Usar sugerencia» visible**. Todavía no se da por verificado en producción.
7. Si vuelve a fallar, abrir el registro del nuevo POST en Observabilidad/Logs y copiar **solo el JSON del mensaje personalizado** (`stage`, `code`, `externalHttp` si existe), más el HTTP y la revisión. No copiar el evento completo de Cloudflare, encabezados, tokens ni texto del formulario.
