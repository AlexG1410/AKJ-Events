# AKJ Events — Fase 2

Aplicación Android para descubrir, crear y participar en eventos comunitarios del Quindío. Es parte del proyecto de clase: una única `MainActivity` con Jetpack Compose, Material 3, `ViewModel` + `StateFlow` y Navigation Compose con rutas tipadas. Las pantallas replican los mockups de la carpeta `mockups/`.

## Qué incluye esta entrega

- **Splash** con el logo y el ícono adaptativo de la app. Pasa solo al Login después de 2 segundos.
- **Login** con credenciales locales de demostración, validación de correo y contraseña, opción de mostrar la contraseña y enlaces a Registro y Recuperar contraseña.
- **Registro** con nombre, correo, ciudad, contraseña segura y su confirmación, intereses por categoría y aceptación de las Normas Comunitarias. La contraseña debe tener al menos 8 caracteres, una mayúscula y un número. Todo usuario nuevo empieza en el nivel 1 (Espectador).
- **Recuperar contraseña** con validación del correo y confirmación del envío del enlace.
- **Feed (Home)** con los eventos verificados, búsqueda por texto, filtro por categoría, marcar interés y una barra inferior. El botón **Crear** de la barra y el banner del feed abren Crear evento.
- **Detalle de evento** con imagen, fecha, lugar, organizador y cupo; permite confirmar o cancelar la asistencia y marcar interés.
- **Crear evento** con título, categoría, descripción, fecha y hora de inicio y fin, lugar, cupo opcional e imagen. El evento se guarda en estado `PENDING` (pendiente de verificación), por eso no aparece en el feed.
- Los formularios muestran los errores y las confirmaciones en un **Snackbar**. Las opciones que aún no existen (mapa, alertas, perfil, etc.) muestran "Disponible próximamente".

Al iniciar sesión, el Splash y el Login salen de la pila de navegación: el botón atrás desde el Home cierra la app en vez de volver a ellos.

## Qué está simulado

- **Sesión:** no hay backend de autenticación. El Login compara las credenciales con `DemoCredentials`, mientras Registro y Recuperar contraseña validan los datos y simulan una respuesta exitosa. No se crean cuentas ni se envían correos. La sesión es la del usuario de muestra `SampleUsers.currentUser`.
- **Datos en memoria:** los eventos viven en `InMemoryEventRepository`, que arranca con los datos de `data/demo/SampleEvents.kt`. La asistencia, los intereses y los eventos creados se comparten entre pantallas, pero se pierden al cerrar la app.
- **Imágenes:** las fotos de los eventos salen de URLs de ejemplo de picsum.photos; hay una imagen local de respaldo si no cargan. Crear evento todavía no permite subir una imagen propia.
- **Ubicación:** no hay mapa; un evento nuevo usa las coordenadas del organizador.

Solo los eventos `VERIFIED` se muestran públicamente. Los datos de muestra incluyen un evento `PENDING` y uno `REJECTED` que no deben aparecer en el feed ni en el detalle.

## Cómo ejecutar la app

1. En Android Studio, selecciona **Open** y abre la carpeta del proyecto (la que contiene `settings.gradle.kts`).
2. Espera a que termine **Gradle Sync**. El proyecto usa JDK 17, Gradle 9.6 y Android SDK 37; instala los componentes que Android Studio indique.
3. Elige un emulador o dispositivo con Android 9 (API 28) o superior y pulsa **Run 'app'**.
4. Inicia sesión con las credenciales locales de demostración:

   - **Correo:** `camilo.rodriguez@akjevents.co`
   - **Contraseña:** `AkjEvents123`

Las credenciales incorrectas muestran un mensaje de error y mantienen al usuario en el Login.

Desde la terminal, en la raíz del proyecto (`.\gradlew.bat` en PowerShell):

```
./gradlew assembleDebug        # compila el APK de depuración
./gradlew installDebug         # lo instala en el emulador o dispositivo conectado
./gradlew testDebugUnitTest    # pruebas unitarias
./gradlew lint                 # análisis de Android Lint
```

## Organización del código

Dentro de `app/src/main/java/co/edu/uniquindio/akjevents/`:

- `core/`: tema, componentes compartidos y utilidades (`RequestResult`, validación de formularios, formato de fechas).
- `domain/`: modelos (`CommunityEvent`, `User`, categorías, estados, roles y niveles) y la interfaz `EventRepository`.
- `data/`: el repositorio en memoria, los datos de muestra y las credenciales locales de demostración.
- `features/`: una carpeta por pantalla, cada una con su `Screen`, su `ViewModel` y su `UiState`.
- `navigation/`: navegación principal y rutas tipadas de la aplicación.

## Siguientes etapas

1. Conectar un servicio para autenticación, datos e imágenes (Supabase o Firebase).
2. Registro, inicio de sesión y recuperación reales, con roles `USER` y `MODERATOR` (moderadores precargados y permisos aplicados en la base de datos).
3. Editar y eliminar eventos, con imagen propia y punto elegido en el mapa. La moderación debe exigir un motivo al rechazar y permitir finalizar eventos.
4. Comentarios y notificaciones almacenadas; después estadísticas, puntos, niveles e insignias.
5. Check-in por QR verificado en el servidor y sugerencia de categoría con IA que siempre permita elegir manualmente.
