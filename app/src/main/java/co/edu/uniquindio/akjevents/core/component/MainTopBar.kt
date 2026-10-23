package co.edu.uniquindio.akjevents.core.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnPrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.domain.model.User

/** Barra superior de las pantallas principales: logo, "AKJ EVENTS", título, acciones y avatar. */
@Composable
fun MainTopBar(
    title: String,
    modifier: Modifier = Modifier,
    user: User? = null,
    onAvatarClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = WarmBackground.copy(alpha = 0.95f), shadowElevation = 1.dp, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo AKJ Events",
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("AKJ EVENTS", color = Terracotta, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(
                    title,
                    color = WarmText,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            actions()
            if (user != null) {
                UserAvatar(
                    user = user,
                    size = 32.dp,
                    onClick = onAvatarClick,
                    modifier = Modifier.shadow(1.dp, CircleShape)
                )
            }
        }
    }
}

/** Encabezado de una pantalla secundaria: botón atrás, antetítulo opcional, título y acciones. */
@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    kicker: String? = null,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RoundIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Volver", onClick = onBack)
        Column(modifier = Modifier.weight(1f)) {
            kicker?.let {
                Text(it.uppercase(), color = Terracotta, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Text(title, color = WarmText, fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold)
            subtitle?.let { Text(it, color = MutedText, fontSize = 12.sp, lineHeight = 16.sp) }
        }
        actions()
    }
}

/** Botón redondo con ícono, como los de las barras de los mockups. */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    containerColor: Color = SurfaceContainer,
    tint: Color = WarmText
) {
    Surface(onClick = onClick, shape = CircleShape, color = containerColor, modifier = modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size / 2))
        }
    }
}

/** Foto de perfil o, mientras no haya carga de imágenes, las iniciales del usuario. */
@Composable
fun UserAvatar(
    user: User,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = PrimaryFixed,
    contentColor: Color = OnPrimaryFixed
) {
    val content: @Composable () -> Unit = {
        Box(
            modifier = Modifier.size(size).clip(CircleShape).background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            if (user.profilePictureUrl != null) {
                AsyncImage(model = user.profilePictureUrl, contentDescription = "Foto de ${user.name}", modifier = Modifier.size(size))
            } else {
                Text(user.initials, color = contentColor, fontSize = (size.value * 0.38f).sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (onClick != null) {
        Surface(onClick = onClick, shape = CircleShape, modifier = modifier.size(size), content = content)
    } else {
        Box(modifier = modifier.size(size)) { content() }
    }
}
