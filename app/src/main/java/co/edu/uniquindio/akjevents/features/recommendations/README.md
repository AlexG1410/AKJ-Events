# Recomendados para ti

Implementación actual en la rama local: `RemoteRecommendationRanker` conectado por Hilt al Worker `POST /recommend-events`, con `LocalRecommendationRanker` como respaldo. Inicio y la pantalla completa siguen compartiendo un solo `RecommendationsViewModel` desde `AppNavigation`. Los destacados conservan su orden por votos en `HomeViewModel`, sin intervención de IA.

`LocalRecommendationRepository` toma candidatos de `publicEvents`: verificados, inicio futuro, no propios, sin asistencia confirmada y con cupo. Usa intereses declarados y las categorías, títulos y descripciones de eventos marcados con «Me interesa» o con check-in. El historial solo admite verificados y finalizados; se limita a 20 eventos. La afinidad local combina categorías, coincidencia de palabras (no similitud semántica), distancia, proporción de cupos libres y penalización de categorías repetidas.

Antes de clasificar se preseleccionan como máximo 24 candidatos. `RecommendationRequest` no incluye identidad, correo, dirección ni coordenadas exactas del usuario. Las respuestas se validan contra esos IDs: no se aceptan duplicados, IDs inventados ni explicaciones vacías o excesivas. Un error o respuesta inválida activa la clasificación local. Antes de publicar, componer y abrir el detalle se comprueba de nuevo la elegibilidad actual. Los cambios de sesión, participación y eventos actualizan la fuente compartida; al comenzar un evento recomendado también se actualiza.

## Integración remota y privacidad

`RemoteRecommendationRanker` reutiliza el Retrofit propio de IA y el ID token Firebase; la clave de OpenRouter sigue exclusivamente en el Worker. Se envían intereses, hasta 20 interacciones (título ≤100, descripción ≤200, categoría, check-in), hasta 24 candidatos (ID, título ≤100, descripción ≤200, categoría) e idioma `es`/`en`. No se serializa el modelo completo de usuario/candidato: se omiten identidad, correo, dirección, coordenadas, distancia exacta, cupos y fotos. Los textos de eventos sí pueden contener información introducida por usuarios: no introducir datos sensibles.

El Worker valida el contrato y exige ordenar exactamente los IDs recibidos, una vez cada uno, con motivo no vacío, sin controles y ≤180 caracteres. Android repite la validación, resuelve eventos por ID y revalida sesión/contexto/elegibilidad al recibir el resultado y al mostrar/abrir tarjetas. Si el contexto cambió durante la consulta, se descarta la clasificación remota y se calcula la local actual. El Worker no consulta Firestore y no certifica por sí mismo existencia/estado de candidatos arbitrarios enviados por un cliente manipulado; la app usa sus repositorios reales. No devuelve eventos nuevos ni tiene herramientas para crear/modificar datos.

`RecommendationFeed.source` diferencia `REMOTE` (incluida caché de una respuesta remota validada) y `LOCAL`. Las dos pantallas muestran la procedencia; no presentar el respaldo determinista como IA. Los datos de eventos/participación vienen de los repositorios Firebase vinculados por Hilt, no de implementaciones en memoria.

## Consumo y actualización

- Se eliminaron los dos refrescos automáticos de pantalla al componer: abrir la lista no solicita una segunda clasificación.
- `changes` proyecta solo sesión/rol, intereses, historial propio y candidatos relevantes antes de `distinctUntilChanged`. Nombre, puntos, foto, comentarios o votos ajenos que no cambien elegibilidad no disparan una clasificación.
- ViewModel de producción agrupa cambios durante 750 ms y retira inmediatamente datos que ya no son visibles para la sesión.
- Caché de una respuesta por usuario/contexto: remota válida 5 minutos; respaldo por fallo 60 segundos. Solo en memoria; cada acceso revalida elegibilidad.
- Como máximo un intento remoto por usuario cada 60 segundos desde este repositorio, incluso si cambia el contexto. Durante espera usa clasificación local. Sin candidatos o con rol Moderador no consulta IA.
- «Actualizar recomendaciones» respeta caché y espera; no fuerza consumo ilimitado. La caducidad de eventos sigue provocando revalidación.
- Ambas rutas del Worker comparten límites de cuenta y throttle por instancia. No se promete una reserva de cuota ni servicio gratuito ilimitado.

## Verificación en Pixel 9a

El usuario reportó desplegada la revisión `ai-dual-2026-11-11-2`, con etiquetas remota y local visibles en Pixel. Estos cambios de galerías/QR no requieren redespliegue; recompilar Android conservando `ai.baseUrl`. Ver diagnóstico en `worker/README.md`. Los 429/503 intermitentes requieren datos de la misma petición; no se atribuye una causa sin ellos.

`RecommendationNavigationSmokeTest` pasó con componentes reales y API simulada contada: vista previa → Ver todos conserva etiqueta remota y **una llamada**. No prueba el número de POST en producción. El subtítulo es neutral («Según tus preferencias y actividad»); la etiqueta de procedencia sigue distinguiendo IA remota/caché y respaldo local. Las tarjetas de la fila usan flechas de fotos; la lista completa permite swipe, sin modificar la clasificación.

1. Con usuario real y candidatos elegibles, abrir Inicio. Network Inspector debe mostrar **POST `/recommend-events` → 200** con `items`; después aparece «Ordenados con IA remota». Verificar que IDs y orden coincidan con la respuesta.
2. Abrir «Recomendados para ti»: misma fuente/orden y sin segundo POST solo por navegar. `Eventos Destacados` conserva orden por votos.
3. Para un respaldo real sin cambiar servicios: con feed cargado, esperar más de cinco minutos para que expire su caché, desactivar red y pulsar «Actualizar recomendaciones». Si los repositorios conservan sesión/datos en caché, debe aparecer la clasificación local y su etiqueta. No confundir una respuesta remota aún cacheada con una nueva petición remota. Si no hay datos offline, el estado vacío no prueba clasificación local; usar el test automatizado de fallo como evidencia y repetir con datos disponibles.
4. Volver a habilitar red, esperar la ventana de 60 s y actualizar. Una respuesta válida debe recuperar la etiqueta remota.
5. Cambio de sesión/inscripción/cupo: comprobar retirada de eventos no elegibles. Las pruebas automatizadas modifican solo repositorios en memoria; cualquier prueba equivalente en Firestore requiere datos/cuentas autorizados.

Pendiente: medir POST reales al navegar, estabilidad/calidad de motivos en ambos idiomas, recorrido completo autenticado y APK release. El éxito remoto reportado por el usuario no sustituye estas verificaciones. No hay embeddings ni entrenamiento propio.
