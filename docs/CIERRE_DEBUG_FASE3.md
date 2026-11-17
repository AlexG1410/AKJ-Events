# Cierre funcional en debug — Fase 3

Revisión local: 15/11/2026. Base `fase3-ia-categorias`, commit `000ec9d`; PR #7 borrador según el equipo (no se modificó ni se consultó el PR). Se conservó el cambio preexistente de `CategorySuggestionRepositoryTest.kt`. Esta etapa no incluye R8, firma, release ni App Distribution.

## Contraste de requisitos y proveedores reales

Fuente: [PF oficial, temática 5 y fase 3](https://caflorezvi.github.io/guias-apps-moviles/0.proyecto-final.html), reconsultada. No hay rúbrica privada/puntajes disponibles. Código: pantalla → ViewModel → caso de uso → contrato → implementación, Hilt y rutas tipadas conservados.

## Resultados ejecutados

| Comprobación | Resultado |
|---|---|
| Comando solicitado JBR 25, `--rerun-tasks`, dos clases HTTP | 7 categorías + 9 ranker, sin fallos, HttpServer/Retrofit conservados |
| `:app:assembleDebug` | Correcto |
| `:app:testDebugUnitTest` | **200 aprobadas**, 0 fallos/errores |
| `:app:lintDebug` | **0 errores, 62 advertencias, 1 sugerencia**, sin baseline/supresiones |
| `npm test` en Worker | **42 aprobadas**, ningún LLM real |
| `npm test` en firebase-tests | **16 aprobadas** (12 permisos/protocolo, 4 eliminación) con Auth/Firestore demo |
| `npm run test:android` | **3 aprobadas** Pixel_9a: repositorios Android reales en Auth/Firestore locales y cancelar diálogo |
| Regresiones Compose seleccionadas | **17 aprobadas** Pixel_9a: 8 galerías, 2 barra, 1 cámara, 1 navegación recomendaciones, 3 categoría, 2 procedencia |
| Diff | `git diff --check` sin errores |

Las 3 nuevas + 17 regresiones son **20 instrumentadas en dos ejecuciones**, no 20 recorridos reales de servicios. El primer intento HTTP falló durante la edición por firma transitoria del test de eliminación, no por HttpServer; se repitió exactamente el comando y pasó. La primera instrumentación detectó crash `em/sp` del campo flotante nuevo y fallo de conectividad/instancias del fixture: se corrigieron con campo sin etiqueta flotante, `adb reverse` y claves demo únicas; repetición 3/3 correcta.

**Advertencias, separadas de fallos:** permanecen avisos de destino Compose de Mapbox, regla Compose de tests deprecada, opción AGP experimental y deprecaciones Node `punycode`/`url.parse`. Lint destaca tipografía/plurales/recursos sin uso y dependencias, no hay errores. Audit npm actual: Worker **0**, tooling emuladores **15 moderadas, 0 altas/críticas**; solo dependencias de desarrollo, sin fixes forzados. Los logs PERMISSION_DENIED en casos negativos verifican denegaciones esperadas. No hubo consultas/escrituras de producción, despliegues ni cambios de release.

| Área | Implementación real comprobada en código | Límite/evidencia pendiente |
|---|---|---|
| Autenticación y roles | Firebase Authentication, correo/contraseña, recuperación con `sendPasswordResetEmail`, idioma de app; perfiles/rol en Firestore, restauración con DataStore | Recuperación por correo y nuevo inicio de sesión aprobados en el recorrido manual de usuario y moderador. |
| Persistencia | Cloud Firestore y listeners de perfiles/eventos/participación/notificaciones; notificaciones internas, no FCM |Verificación, rechazo con motivo, finalización y persistencia entre dispositivos aprobados en el recorrido manual |
| Mapas | Mapbox 11.22.0 `-ndk27`, permiso aproximado/preciso opcional, coordenadas guardadas | `Geocoder` Android para dirección; búsqueda `SamplePlaces` sigue local y filtro ciudad aproximado; GPS/retorno/Android 9–12 pendientes |
| Fotos | Cloudinary unsigned vía Retrofit 3.0, JPEG 85/1600 px, máximo 3; Coil 3.6.3 para mostrar | Sin subidas reales ahora. No borrado remoto ni atomicidad Cloudinary/Firestore; restringir preset |
| IA categoría | Worker Cloudflare → OpenRouter `openrouter/free`, ID token Firebase, aceptación explícita/manual | Éxito real reportado por equipo; no LLM real en tests; emulador Auth no sirve para Worker real (firma inválida) |
| IA recomendaciones | Mismo Worker, ranking validado, caché y respaldo local distinguido; Destacados por votos | Éxito remoto/local reportado; POST reales por navegación y 429/503 pendientes |
| Idiomas | Inglés predeterminado y español, `stringResource`/`ResourceProvider`, prueba de equivalencia | Recorrido UI/correos/TalkBack en ambos idiomas pendiente |
| Usuario/moderador | Crear pendiente, editar, finalizar, verificar/rechazar con motivo, comentarios/avisos; detalle por ID | Recorrido completo ambos roles y persistencia del rechazo fuera del feed |
| QR adicional | ZXing core 3.5.3 + Embedded 4.3.0 offline, cámara/manual, evento esperado, transacción asistencia/saldo/historial | Moto: lectura y mensaje de repetición reportados; Reintento con la misma cuenta en dos dispositivos aprobado en el recorrido manual, con un solo premio y una sola entrada en el historial |
| Eliminar cuenta | Reautenticación obligatoria y limpieza/reintento descritos abajo | No es una transacción Auth+Firestore; fallos extremos requieren asistencia de servidor |

Herramientas: Android Studio/JBR **25.0.2**, AGP **9.3.0**, Gradle del wrapper, Kotlin/Compose BOM del catálogo, Material 3, Foundation Pager, Hilt, coroutines, Navigation Compose, Retrofit/Gson, Coil; Firebase CLI y SDK/rules-unit-testing para emuladores, Node test runner y Miniflare/workerd para Worker. Mockups existentes de Stitch; asistencia de código/revisión con OpenCode. No se infiere uso de otras herramientas de diseño ni contribución individual sin evidencia del equipo. Sin nuevos servicios pagos ni facturación.

## Eliminación: orden y recuperación

1. Diálogo pide **contraseña actual** en memoria (`remember`, no `rememberSaveable`, SavedState ni logs). Cancelar no llama al caso de uso; campo vacío no habilita confirmar.
2. `DeleteAccountUseCase` exige `UserRepository.reauthenticate(uid, password)` antes de **cualquier escritura**. El repositorio Firebase usa el email de Auth, no el formulario editable. Credencial incorrecta/sesión cambiada/identidad cancelada: no borra perfil, puntos, historial, eventos ni participación.
3. Tras verificar, marca `users/{uid}.deletionPending=true`. Las reglas candidatas exigen `auth_time` reciente (<5 min) para esa marca. Permite eliminar participaciones históricas durante la limpieza, no cancelar un check-in en el flujo ordinario.
4. Limpieza de eventos/comentarios/participación y contadores en batch; después notificaciones, después historial en lotes de 400, perfil y Auth al final. Un error detiene pasos posteriores. El error visible avisa que un borrado ya iniciado puede ser parcial. No se guardan contraseñas para reanudar automáticamente.
5. Si falla limpieza: perfil/Auth siguen disponibles con marca; reabrir Editar perfil, introducir contraseña de la **misma cuenta** y reintentar. Consultas solo encuentran participación restante, por lo que un batch ya confirmado no vuelve a descontar sus contadores. No restaurar datos ya eliminados ni volver a conceder premios.
6. Si falla Auth final después de borrar perfil: se intenta restaurar la copia del perfil con la marca (sin cerrar sesión), para permitir el mismo reintento. Se evita navegar por el perfil nulo transitorio. Fallo final inyectado y recuperación se prueban con SDK JS local; no se simula que Firebase real falle así de forma determinista.

**Límites no resueltos:** cierre del proceso entre perfil/Auth, caída de red también durante compensación, respuesta Auth ambigua, sesión invalidada/cambiada o datos creados concurrentemente pueden dejar restos o perfil ausente. Sin perfil, login puede recrear uno básico: no es recuperación de reputación borrada; requiere revisión asistida de un responsable, no reintentos ciegos. Batches grandes de eventos/notificaciones siguen sujetos al límite de escrituras y accesos de reglas; no se certifican cuentas de volumen arbitrario. La eliminación puede requerir operación de servidor durable/idempotente con etapas y reanudación para cerrar estos casos. Cloudinary no se limpia automáticamente.

## Reglas: cambios locales y bloqueo de seguridad

`firestore.rules` es una **propuesta probada solo localmente**, no desplegada. Se valida correspondencia de transacciones de participación/contadores, cupo/evento público/no propio, check-in monotónico, cancelación solo previa (salvo eliminación de cuenta/evento), permisos de moderación acotados y perfil personal sin escalada de rol. Nuevas recompensas QR incluyen `eventId` en `PointsEntryDto` y reglas comprueban ID determinista, transición de asistencia y delta +15; historial QR no se sobreescribe ni borra desde el uso ordinario.

**Cambio de contrato aditivo:** `pointsHistory` nuevo QR guarda `eventId`; documentos antiguos siguen legibles sin migración y no se vuelven a premiar. Un cliente viejo sin el campo no podrá crear un nuevo premio QR con las reglas nuevas. Coordinar actualización de clientes/revisión de reglas en la siguiente decisión de despliegue; no desplegarlas ahora. Android nuevo sigue compatible con las reglas antiguas, pero no obtiene sus protecciones nuevas en producción.

**No cerrado:** `users.points/badges`, creación de perfiles con reputación y movimientos no QR siguen confiando en clientes autenticados. Una prueba local demuestra que un tercero puede modificar reputación ajena/forjar movimiento genérico. No se bloquea a ciegas porque creación, comentario, votos y moderación aún conceden puntos desde Android. Hace falta mover a servidor confiable la premiación completa (incluidas insignias, verificación y compensaciones de votos), con operaciones idempotentes y vínculo acción→historial→saldo; después denegar escrituras cliente sobre esos campos y probar todos los flujos. No añadir Admin SDK, clave privada ni secreto OpenRouter al APK. Notificaciones también requieren mayor validación contra abuso. Esto impide certificar publicación pública segura, aunque debug compile.

## Validación manual en debug

Kevyn Ramírez González reportó que completó satisfactoriamente el recorrido manual de recuperación de contraseña, publicación con foto nueva y ubicación, sugerencia remota de categoría, moderación y persistencia entre dispositivos, interfaz en español e inglés y reintento del QR con la misma cuenta.

El resultado corresponde a la compilación debug probada por el equipo.
El recorrido reproducible y los resultados esperados se conservan abajo.

| Prueba | Dispositivo/cuenta y pasos | Resultado esperado / evidencia a anotar |
|---|---|---|
| Recuperación por correo | A, U y después M: salir → Olvidé contraseña → correo → abrir enlace → contraseña nueva → login | Correo recibido y enlace completo para ambos roles; clave vieja rechazada, nueva aceptada. Auth Emulator muestra enlace local pero **no prueba entrega de correo** |
| Foto nueva + mapa | A/U: Crear → foto nueva cámara/galería → mapa/manual o GPS → confirmar ubicación → publicar | Foto Cloudinary HTTPS y coordenadas persisten al reabrir; nuevo evento pendiente, no público. Negar cámara/ubicación conserva galería/selección manual |
| Categoría remota | A/U en entorno IA de pruebas con token Firebase firmado: título/descripción suficiente → esperar sugerencia → aceptar; repetir eligiendo manual | Categoría no cambia sin aceptación; petición 200/etiqueta remota, manual no sobrescrita. Tokens del emulador **no** verifican IA remota |
| Moderar + persistencia | B/M: abrir pendiente creado por U → verificar; A/P abre feed/detalle. Repetir rechazo con motivo y finalizar; reiniciar apps | Aprobado visible en B/A con misma foto/mapa; rechazo con motivo ausente en feed público y aviso a U; estado persiste en segundo dispositivo |
| Español/inglés | A/U en español; cambiar idioma app a inglés y recorrer login, crear, errores, detalle, QR, perfil; B/M panel | Claves completas y acciones traducidas, sin cambiar contenido escrito por usuarios. Correos respetan idioma/plantilla configurada |
| Reintento QR misma cuenta | A/P: anotar saldo S e historial H → escanear evento ajeno con cupo → anotar S+15 y una entrada; repetir → iniciar **misma P en B** y repetir | Primer premio único; ambos reintentos «ya registrado», saldo **S+15**, mismo movimiento/ID y sin entrada adicional. No usar 117/146 como antes/después |
| Cancelar antes/después | A/P: confirmar inscripción → cancelar antes QR; reinscribir y QR → detalle | Antes libera cupo; después no ofrece cancelar, estado QR y saldo/historial conservados |
| Eliminar cuenta | A/U desechable **solo local/sandbox**: cancelar diálogo; clave incorrecta; correcta; simular fallo y reintentar | Cancelar/incorrecta intactos; éxito cierra sesión y datos eliminados; fallo parcial explica reintento misma cuenta sin doble descuento |

También pendientes de regresión manual: GPS aproximado/preciso/denegado y retorno de mapa en registro/perfil/eventos; errores de imagen/red, cupo concurrente, QR inválido/de otro evento; galerías fuera del detalle, pausa/segundo plano/movimiento reducido, comentarios/notificaciones leídas/no leídas, filtros con sus limitaciones documentadas y dos idiomas/TalkBack.

## Estado al cierre de debug

- Ejecutar y registrar los recorridos externos pendientes en entorno autorizado, especialmente correo, nueva foto, IA, moderación/persistencia y saldo/historial QR entre dispositivos.(completado)
- Revisar la propuesta de reglas y resolver/aceptar formalmente el bloqueo de reputación cliente; sin ello no declarar seguridad de servidor completa.
- Verificar límites y recuperación de eliminación; para robustez frente a cierres/red/sesiones, completar coordinación durable de servidor.
- Repetir debug, pruebas y Lint tras cualquier cambio; no extrapolar éxito de componentes a integración completa.
- Release, R8, firma y App Distribution pendientes.

## Archivos de esta pasada

Paquete Kotlin base: `app/src/main/java/co/edu/uniquindio/akjevents/`.

- Eliminación: `domain/repository/UserRepository.kt`, `domain/usecase/user/DeleteAccountUseCase.kt`, `data/repository/InMemoryUserRepository.kt`, `data/repository/remote/FirebaseUserRepository.kt`, `features/profile/edit/EditProfileScreen.kt`, `EditProfileViewModel.kt` y recursos `app/src/main/res/values{,-es}/strings.xml`.
- Reglas/QR: `firestore.rules`, `data/remote/user/PointsEntryDto.kt`, `data/repository/remote/FirestoreCheckInRepository.kt`.
- Pruebas: `EditProfileViewModelTest.kt`, nuevo `DeleteAccountUseCaseTest.kt` bajo `app/src/test/`; nuevos `AccountDeletionEmulatorTest.kt`, `DeleteAccountDialogSmokeTest.kt` bajo `app/src/androidTest/`.
- Emuladores: `firebase.checkin-test.json`, `firebase-tests/package.json`, `checkin.test.js`, nuevos `deletion.test.js` y `run-android-tests.ps1`.
- Documentación: `README.md`, `AGENTS.md`, `worker/README.md`, `docs/FASE3_AUDITORIA.md`, `docs/PRUEBAS_GALERIAS_QR.md`, nota histórica en `docs/REVISION_COMMIT_FASE3.md` y este documento.
- `.gitignore`: excluye caché generada `.kotlin/`, sin borrar archivos.
- **Previo/conservado:** `app/src/test/java/co/edu/uniquindio/akjevents/CategorySuggestionRepositoryTest.kt` ya estaba modificado al comenzar; no se modificó por el agente ni se quitó su HTTP loopback. `RemoteRecommendationRankerTest.kt` tampoco necesitó cambios.

No incluir caches, APK, reportes, logs, `.env`/`.dev.vars`, `local.properties`, cuentas de servicio ni keystores. Sin staging/commit/push/merge, despliegue o pruebas contra producción.
