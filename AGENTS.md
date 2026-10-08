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

El repositorio actual incluye los mockups en `mockups/`. Las versiones de texto de las guías **04–08** y el proyecto base `proyecto-guía-moviles-20262-main.zip` no están guardados en este repositorio; si una tarea los requiere, localice su ruta real en el equipo o solicítela antes de atribuirles reglas. El proyecto base usa `demoapp` y reportes; aproveche su estructura sin copiar su funcionalidad ni reemplazar avances de AKJ Events.

**Cuando llegue una nueva guía:** añada una fila con número, enlace o archivo y tema; revise qué cambia para el código, la estructura o la navegación; actualice las reglas correspondientes. No dé por aplicada una guía que todavía no se haya entregado.

## 3. Organización del proyecto y del código

El proyecto base usa una sola `MainActivity` que aloja Compose y separa responsabilidades. Tome esta distribución como referencia y compruebe los paquetes existentes antes de crear otros:

```text
app/src/main/java/co/edu/uniquindio/akjevents/
├── MainActivity.kt
├── core/
│   ├── component/       # composables reutilizables
│   ├── theme/           # color, tipografía y tema
│   └── util/            # utilidades compartidas
├── navigation/          # destinos tipados y grafo principal
├── domain/
│   ├── model/           # entidades, categorías, roles y estados
│   └── repository/      # contratos de repositorios
├── data/
│   ├── demo/            # datos y credenciales locales de demostración
│   └── repository/      # implementaciones de repositorios
└── features/            # pantallas y ViewModels por funcionalidad
```

- Conserve el código existente si cumple los requisitos y funciona. Evite componentes duplicados o mover paquetes por una preferencia personal.
- Coloque la presentación en `*Screen.kt`. Para formularios y operaciones con estado, use `*ViewModel.kt` y `UiState` siguiendo las guías; la pantalla observa el estado y envía acciones.
- Ponga entidades compartidas en `domain/model`; deje las fuentes de datos e integraciones externas en `data` cuando se implementen. No concentre interfaz, persistencia y reglas de negocio en un solo composable.
- Coloque en `core/component` únicamente componentes que se reutilicen; los específicos van en su `feature`. Respete la configuración existente de Gradle y el catálogo de versiones al añadir dependencias.

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

Este archivo explica **cómo** trabajar en AKJ Events; la tarea y rúbrica actuales determinan **qué** construir ahora.
