package co.edu.uniquindio.akjevents.features.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.theme.BrandBorder
import co.edu.uniquindio.akjevents.core.theme.BrandDark
import co.edu.uniquindio.akjevents.core.theme.BrandMuted
import co.edu.uniquindio.akjevents.core.theme.BrandOnPastelYellow
import co.edu.uniquindio.akjevents.core.theme.BrandPastelYellow
import co.edu.uniquindio.akjevents.core.theme.BrandSage
import co.edu.uniquindio.akjevents.core.theme.BrandSageLight
import co.edu.uniquindio.akjevents.core.theme.BrandTerracotta
import co.edu.uniquindio.akjevents.core.theme.BrandTerracottaLight
import co.edu.uniquindio.akjevents.core.theme.WarmBackground

private val VersionText = Color(0xFFA39A94)
private val SeparatorDot = Color(0xFFD4D4D4)

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .drawBehind { drawAmbientBackground() }
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .offset(y = (-24).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SplashLogo()
            Spacer(Modifier.height(24.dp))

            Text(
                "COMUNIDAD EN MOVIMIENTO",
                color = BrandTerracotta,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "AKJ Events",
                color = BrandDark,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.75).sp
            )

            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SloganDot()
                Text(
                    "“Conecta tu comunidad”",
                    color = BrandSage,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                SloganDot()
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Descubre, crea y participa en iniciativas, cultura y deporte cerca de ti.",
                color = BrandMuted,
                fontSize = 12.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 240.dp)
            )

            Spacer(Modifier.height(40.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = BrandTerracotta,
                trackColor = BrandTerracotta.copy(alpha = 0.2f),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Cargando eventos cercanos...",
                color = BrandMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )
        }

        SplashFooter(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SplashLogo() {
    Box(contentAlignment = Alignment.Center) {
        // Resplandor terracota detrás del logo
        Box(
            Modifier
                .size(160.dp)
                .background(
                    Brush.radialGradient(
                        listOf(BrandTerracotta.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(128.dp)
                .shadow(4.dp, CircleShape)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                .border(1.dp, BrandBorder, CircleShape)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo AKJ Events",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = 12.dp)
                .shadow(1.dp, CircleShape)
                .background(BrandPastelYellow, CircleShape)
                .border(1.dp, Color(0x99FDE68A), CircleShape)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Rounded.Groups, contentDescription = null, tint = BrandOnPastelYellow, modifier = Modifier.size(12.dp))
            Text("Quindío", color = BrandOnPastelYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SloganDot() {
    Box(Modifier.size(6.dp).background(BrandSage, CircleShape))
}

@Composable
private fun SplashFooter(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.7f), CircleShape)
                .border(1.dp, BrandBorder, CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeaturePill(Icons.Rounded.Verified, "Eventos Verificados", BrandSage)
            Text("•", color = SeparatorDot, fontSize = 11.sp)
            FeaturePill(Icons.Rounded.QrCodeScanner, "Check-in QR", BrandTerracotta)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Nodo Armenia • Quindío", color = BrandDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("Versión $versionName • Material 3", color = VersionText, fontSize = 10.sp)
        }
    }
}

@Composable
private fun FeaturePill(icon: ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

/** Manchas de color difuminadas y patrón de puntos del fondo del mockup. */
private fun DrawScope.drawAmbientBackground() {
    val spacing = 40.dp.toPx()
    val dotRadius = 2.dp.toPx()
    val dotColor = BrandTerracotta.copy(alpha = 0.04f)
    var y = spacing / 2
    while (y < size.height) {
        var x = spacing / 2
        while (x < size.width) {
            drawCircle(dotColor, dotRadius, Offset(x, y))
            x += spacing
        }
        y += spacing
    }

    val glowRadius = 200.dp.toPx()
    val topRight = Offset(size.width - 64.dp.toPx(), 64.dp.toPx())
    drawCircle(
        Brush.radialGradient(listOf(BrandTerracottaLight.copy(alpha = 0.7f), Color.Transparent), topRight, glowRadius),
        glowRadius,
        topRight
    )
    val bottomLeft = Offset(80.dp.toPx(), size.height - 48.dp.toPx())
    drawCircle(
        Brush.radialGradient(listOf(BrandSageLight.copy(alpha = 0.8f), Color.Transparent), bottomLeft, glowRadius),
        glowRadius,
        bottomLeft
    )
}
