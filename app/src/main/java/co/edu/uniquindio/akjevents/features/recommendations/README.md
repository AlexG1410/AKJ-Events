# Recomendados para ti

Implementación actual: clasificación local determinista, no generada por IA. Inicio y la pantalla completa comparten `RecommendationsViewModel` desde `AppNavigation`. Los destacados del feed conservan su orden por votos.

`LocalRecommendationRepository` toma candidatos de `publicEvents`: verificados, inicio futuro, no propios, sin asistencia confirmada y con cupo. Usa intereses declarados y las categorías, títulos y descripciones de eventos marcados con «Me interesa» o con check-in. El historial solo admite verificados y finalizados; se limita a 20 eventos. La afinidad local combina categorías, coincidencia de palabras (no similitud semántica), distancia, proporción de cupos libres y penalización de categorías repetidas.

Antes de clasificar se preseleccionan como máximo 24 candidatos. `RecommendationRequest` no incluye identidad, correo, dirección ni coordenadas exactas del usuario. Las respuestas se validan contra esos IDs: no se aceptan duplicados, IDs inventados ni explicaciones vacías o excesivas. Un error o respuesta inválida activa la clasificación local. Antes de publicar, componer y abrir el detalle se comprueba de nuevo la elegibilidad actual. Los cambios de sesión, participación y eventos actualizan la fuente compartida; al comenzar un evento recomendado también se actualiza.

## Integración futura

Implementar `RecommendationRanker` con un adaptador que llame a una **función del servidor**. Esa función custodiará la clave del proveedor, ordenará el conjunto recibido por afinidad semántica y devolverá únicamente `RankedRecommendation(eventId, reason)`. Inyectar ese adaptador en `LocalRecommendationRepository`; no es necesario cambiar las pantallas. El respaldo local y la validación de resultados permanecen en el repositorio.

Todavía no hay llamada remota, backend, claves ni embeddings. Los datos y la participación siguen en memoria. Las restricciones de candidatos también deberán aplicarse en el servidor cuando exista.
