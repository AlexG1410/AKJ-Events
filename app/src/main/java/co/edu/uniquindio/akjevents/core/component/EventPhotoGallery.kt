package co.edu.uniquindio.akjevents.core.component

import android.animation.ValueAnimator
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PhotoGalleryMode { DETAIL, HORIZONTAL_ROW, LIST, COMPACT }

/** Las URL permanecen en su orden original. El estado nunca se reutiliza para otro evento. */
@Composable
fun EventPhotoGallery(event: CommunityEvent, modifier: Modifier = Modifier, mode: PhotoGalleryMode = PhotoGalleryMode.HORIZONTAL_ROW, imageOverlay: @Composable BoxScope.() -> Unit = {}) {
    key(event.id) { PhotoGallery(event.id, event.title, event.imageUrls, modifier, mode, imageOverlay) }
}

@Composable
internal fun PhotoGallery(id: String, title: String, images: List<String>, modifier: Modifier, mode: PhotoGalleryMode, imageOverlay: @Composable BoxScope.() -> Unit = {}) {
    val count = images.size.coerceAtLeast(1)
    val pager = rememberPagerState(pageCount = { count })
    val scope = rememberCoroutineScope()
    var paused by rememberSaveable(id) { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    var gestureVersion by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateFlow.collectAsState()
    val resumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    val motionAllowed = rememberGalleryMotionAllowed()
    val position = stringResource(R.string.gallery_position, pager.currentPage + 1, count)
    val next = stringResource(R.string.gallery_next)
    val previous = stringResource(R.string.gallery_previous)
    val automatic = mode == PhotoGalleryMode.DETAIL && count > 1

    LaunchedEffect(id, count) { if (pager.currentPage >= count) pager.scrollToPage(0) }
    LaunchedEffect(automatic, paused, resumed, visible, motionAllowed, gestureVersion) {
        if (!automatic || paused || !resumed || !visible || !motionAllowed) return@LaunchedEffect
        while (isActive) {
            snapshotFlow { !pressed && !pager.isScrollInProgress }.first { it }
            delay(5_000)
            if (!pressed && !pager.isScrollInProgress) {
                pager.animateScrollToPage((pager.currentPage + 1) % count, animationSpec = tween(450))
            }
        }
    }
    fun advance(delta: Int) {
        gestureVersion++
        scope.launch {
            val target = (pager.currentPage + delta + count) % count
            if (motionAllowed) pager.animateScrollToPage(target, animationSpec = tween(350)) else pager.scrollToPage(target)
        }
    }
    Box(modifier
        .testTag("event-gallery-$id")
        .semantics { stateDescription = position }
        .onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            visible = bounds.width > 0 && bounds.height > 0
        }
        .pointerInput(id) {
            // Observa sin consumir: la fila de eventos conserva su gesto horizontal.
            try {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent(PointerEventPass.Initial).changes.any { it.pressed }
                        if (pressed != down) { pressed = down; gestureVersion++ }
                    }
                }
            } finally { pressed = false }
        }) {
        HorizontalPager(
            state = pager,
            userScrollEnabled = count > 1 && (mode == PhotoGalleryMode.DETAIL || mode == PhotoGalleryMode.LIST),
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = images.getOrNull(page),
                contentDescription = stringResource(R.string.gallery_image_description, title, page + 1, count),
                placeholder = painterResource(R.drawable.event_placeholder), error = painterResource(R.drawable.event_placeholder),
                fallback = painterResource(R.drawable.event_placeholder),
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
        }
        imageOverlay()
        if (count > 1) {
            if (mode == PhotoGalleryMode.COMPACT) {
                IconButton(onClick = { advance(1) }, modifier = Modifier.align(Alignment.TopEnd).size(48.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)) {
                    Text("${pager.currentPage + 1}/$count", color = Color.White, fontSize = 12.sp,
                        modifier = Modifier.semantics { stateDescription = "$next. $position" })
                }
            } else {
                IconButton(onClick = { advance(-1) }, modifier = Modifier.align(Alignment.CenterStart).background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, previous, tint = Color.White)
                }
                IconButton(onClick = { advance(1) }, modifier = Modifier.align(Alignment.CenterEnd).background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, next, tint = Color.White)
                }
                Row(Modifier.align(Alignment.BottomEnd).padding(6.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(position, color = Color.White, fontSize = 11.sp)
                    if (automatic) {
                        IconButton(onClick = { paused = !paused; gestureVersion++ }, enabled = motionAllowed) {
                            Icon(if (paused || !motionAllowed) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                stringResource(if (paused || !motionAllowed) R.string.gallery_resume else R.string.gallery_pause), tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/** Respeta «Quitar animaciones»/escala 0 y vuelve a comprobar al regresar de Ajustes. */
@Composable
private fun rememberGalleryMotionAllowed(): Boolean {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var enabled by remember { mutableStateOf(ValueAnimator.areAnimatorsEnabled()) }
    DisposableEffect(context, lifecycle) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { enabled = ValueAnimator.areAnimatorsEnabled() }
        }
        val lifecycleObserver = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) enabled = ValueAnimator.areAnimatorsEnabled() }
        context.contentResolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        lifecycle.addObserver(lifecycleObserver)
        onDispose { context.contentResolver.unregisterContentObserver(observer); lifecycle.removeObserver(lifecycleObserver) }
    }
    return enabled
}
