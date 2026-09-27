# AKJ Events — Paso 1

Primera versión de la aplicación Android para descubrir eventos comunitarios. Parte del proyecto de clase, con su organización `core/`, `domain/`, `data/` y `features/`, una única `MainActivity`, Jetpack Compose, Material 3, `ViewModel`, `StateFlow` y Navigation Compose.

## Abrir y probar en Android Studio

1. Descomprime el proyecto y, en Android Studio, selecciona **Open** y abre la carpeta `AKJEvents-Paso1` (la que contiene `settings.gradle.kts`).
2. Espera a que termine **Gradle Sync**. El proyecto base usa JDK 17, Gradle 9.6 y Android SDK 37. Instala los componentes que Android Studio te indique si hacen falta.
3. Selecciona un emulador o dispositivo con Android 9 o superior y pulsa **Run 'app'**.
4. Prueba las cinco categorías y entra en el detalle de un evento. Al volver a **Todos**, aparecen cinco eventos. Hay dos eventos adicionales en los datos de muestra, `PENDING` y `REJECTED`, que no deben mostrarse en el feed público.

## Qué funciona en este paso

- Una lista de eventos verificados, filtro por categoría y navegación al detalle.
- Modelo de evento con fechas, cupo opcional, coordenadas, al menos una imagen, estado y motivo de rechazo.
- Modelos de usuario con roles `USER` y `MODERATOR` y los cuatro niveles del enunciado.
- Colores principales del prototipo AKJ Events. Las fotos provienen de URLs de ejemplo; hay una imagen local de respaldo si no cargan.

Los eventos se leen de `data/demo/SampleEvents.kt` y solo viven como datos de muestra. Aún no hay cuentas reales, base de datos, mapa, publicación, asistencia, comentarios, votos, notificaciones, estadísticas, reputación, verificación, QR ni clasificación con IA. No se envían datos a ningún servicio externo en este paso, salvo la carga de las imágenes de ejemplo.

## Dirección para las siguientes etapas

1. Configurar el repositorio GitHub del equipo y un servicio para autenticación, datos e imágenes. Supabase es una opción coherente para Auth, PostgreSQL y Storage; la elección puede adaptarse si el equipo ya usa Firebase.
2. Conectar registro, inicio de sesión y recuperación por correo. Crear perfiles normales y precargar moderadores desde un entorno administrativo, con permisos aplicados en la base de datos.
3. Crear, editar y eliminar eventos con imagen externa y punto elegido en el mapa. Publicarlos primero en `PENDING`; solo `VERIFIED` aparece públicamente. La moderación debe exigir motivo al rechazar y permitir finalizar.
4. Añadir asistencia, intereses, comentarios y notificaciones almacenadas; después estadísticas, puntos, niveles e insignias.
5. Implementar el check-in por QR verificando en el servidor el evento, el usuario y la validez del código. La sugerencia de categoría con IA consultará título y descripción mediante un servicio seguro y siempre permitirá elegir manualmente.

El archivo ZIP del proyecto de clase permanece como referencia; los ejemplos de reportes y el login simulado fueron retirados de esta copia para iniciar AKJ Events sin confundirlos con funcionalidades terminadas.
