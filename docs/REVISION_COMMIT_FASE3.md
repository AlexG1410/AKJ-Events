# Selección sugerida para compartir fase 3

> Referencia histórica del bloque IA/QR/galerías, ya contenido en `000ec9d` al comenzar el cierre debug. El PR #7 está en borrador según el equipo. Para los **cambios locales posteriores**, revisar `git status` y el listado de `CIERRE_DEBUG_FASE3.md`; no volver a seleccionar archivos antiguos como si siguieran sin seguimiento. Esta etapa no ejecuta commit/push/merge ni despliegues.

Rama: `fase3-ia-categorias`. Esta lista no ejecuta staging, commit ni push. Revisar el diff de todos los archivos, incluidos los nuevos (sin seguimiento); no seleccionar solo los cambios visuales del 15/11 porque dependen del trabajo previo de IA/QR/galerías.

## Seleccionar conjuntamente

Los siguientes grupos corresponden a los cambios locales vistos el 15/11; las carpetas indican archivos fuente/pruebas, no sus salidas de compilación.

- **Configuración y dependencias:** `.gitignore`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `app/src/main/AndroidManifest.xml`. Conservar AGP 9.3.0; Foundation/ZXing Embedded y generación de `AI_BASE_URL` son parte del conjunto.
- **IA Android previa:** todos los nuevos archivos de `data/remote/ai/`, `RemoteCategorySuggestionRepository.kt`, `RemoteRecommendationRanker.kt`, `di/AiModule.kt`, `domain/repository/CategorySuggestionRepository.kt`, `domain/usecase/event/SuggestEventCategoryUseCase.kt`; modificaciones de `LocalRecommendationRepository.kt`, modelo/contrato `Recommendation`, pantallas/ViewModels de creación y recomendaciones, y `di/RepositoryModule.kt`.
- **QR/consistencia previos:** `domain/repository/CheckInRepository.kt`, `data/repository/InMemoryCheckInRepository.kt`, `data/repository/remote/FirestoreCheckInRepository.kt`, `CheckInUseCase.kt`, `features/checkin/` (incluido `QrScanner.kt`), actualización de `FirebaseUserRepository.kt` que evita sobrescribir reputación al editar perfil.
- **Galerías y corrección actual:** `core/component/EventPhotoGallery.kt`, `RecommendedEventCard.kt`, `features/home/HomeScreen.kt`, `features/event/detail/EventDetailScreen.kt` y recursos completos `res/values/strings.xml`/`res/values-es/strings.xml`. No seleccionar solamente dos líneas de recursos: también contienen textos de IA/galerías/QR necesarios.
- **Pruebas Android:** todos los archivos modificados/nuevos en `app/src/test/` y `app/src/androidTest/` que muestra `git status`. Incluyen `TestUseCases.kt`, pruebas IA, QR, galerías, navegación/procedencia, y nuevos `AttendanceBarSmokeTest.kt`/ajuste de `EventDetailViewModelTest.kt`.
- **Worker IA compartido:** `worker/src/index.js`, `worker/test/worker.test.js`, `worker/test/runtime.test.js`, `worker/package.json`, `worker/package-lock.json`, `worker/wrangler.toml`, `worker/README.md`. El archivo TOML contiene identificador público del proyecto, no clave OpenRouter. Compartir el código no requiere redesplegarlo para estos ajustes visuales.
- **Pruebas Firestore reproducibles:** `firebase.checkin-test.json`, `firebase-tests/checkin.test.js`, `firebase-tests/package.json`, `firebase-tests/package-lock.json`. Reglas existentes sin cambios. Son tooling local, no requisito de ejecución del APK.
- **Documentación:** `AGENTS.md`, `README.md`, `features/recommendations/README.md` (ruta bajo el paquete Kotlin) y los documentos de `docs/`, incluido este listado. Revisar que la evidencia manual permanezca atribuida al usuario y los pendientes no se conviertan en éxitos.

Todos los paquetes Kotlin indicados son relativos a `app/src/main/java/co/edu/uniquindio/akjevents/`, salvo recursos/pruebas. Si se divide en varios commits, mantenerlos juntos en la rama y comprobar compilación final; no compartir un commit parcial que omita archivos nuevos referenciados por Hilt o las pantallas.

## Dejar fuera

- `local.properties` (SDK, Mapbox, configuración Cloudinary y URL IA), archivos `.env*`, `worker/.dev.vars*`, claves OpenRouter, tokens Firebase, cuentas de servicio y claves privadas/keystores de firma. No forzar `git add -f` sobre archivos ignorados.
- `node_modules/` de Worker/Firebase/seed, `.wrangler/`, `.firebase/`, logs `*-debug.log`, `.gradle/`, `build/`, APK/reportes generados y configuración personal `.idea/`.
- Capturas con datos personales, volcados de Firestore y exportaciones del emulador no revisadas.

`app/google-services.json` **ya está versionado y no aparece modificado**: no se propone cambiarlo ni copiar credenciales de servidor ahí. Es configuración cliente Firebase, no una cuenta de servicio; su presencia no reemplaza restricciones/reglas de seguridad. Revisar aparte cualquier política del equipo sobre su distribución.

## Para que el equipo pruebe

- Compartir las propiedades locales por canal acordado, nunca dentro del commit: `MAPBOX_ACCESS_TOKEN` público, `cloudinary.cloudName`, `cloudinary.uploadPreset` y `ai.baseUrl` HTTPS pública del Worker. La clave OpenRouter permanece exclusivamente en el secreto remoto del Worker, no en Android.
- Sin URL IA válida, la clasificación puede usar respaldo local y no demuestra IA remota. Mapas/subidas dependen de su configuración; compilar no certifica esos servicios.
- Compilar/instalar el APK nuevo para ver el subtítulo y el bloqueo visual de cancelación. No exige nuevas reglas/índices, facturación ni despliegue Worker.
- Antes de confirmar: revisar `git diff`, archivos nuevos y selección preparada por el usuario; ejecutar `git diff --check` y, tras seleccionar, `git diff --cached --check`.

Pendientes no resueltos: endurecer reglas Firestore, robustecer eliminación de cuenta, saldo/historial del reintento manual y misma cuenta en otro dispositivo; release y App Distribution.
