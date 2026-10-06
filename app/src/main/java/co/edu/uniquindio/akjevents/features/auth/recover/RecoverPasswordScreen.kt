package co.edu.uniquindio.akjevents.features.auth.recover

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.rounded.LocalActivity
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.core.component.FormFieldStyle
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.PrimaryButton
import co.edu.uniquindio.akjevents.core.component.RequestResultEffect
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnPrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.OutlineColor
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixedDim
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText

// Este mockup usa los tokens Material 3 de DESIGN.md (primary #9C3E26), no los tonos de marca
private val RecoverFieldStyle = FormFieldStyle(
    height = 48.dp,
    shape = RoundedCornerShape(8.dp),
    containerColor = SurfaceContainerLow,
    focusedContainerColor = Color.White,
    borderColor = Color.Transparent,
    focusedBorderColor = Terracotta,
    focusedBorderWidth = 2.dp,
    textStyle = TextStyle(fontSize = 14.sp, letterSpacing = 0.25.sp, color = WarmText),
    placeholderColor = OutlineColor,
    iconColor = OutlineColor,
    iconSize = 22.dp,
    iconStartPadding = 16.dp,
    textStartPadding = 48.dp
)

@Composable
fun RecoverPasswordScreen(
    onBack: () -> Unit,
    onLinkSent: () -> Unit,
    viewModel: RecoverPasswordViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    RequestResultEffect(
        result = state.result,
        snackbarHostState = snackbarHostState,
        onFailureShown = viewModel::onFailureShown,
        onSuccess = onLinkSent
    )

    val send = {
        focusManager.clearFocus()
        viewModel.sendRecoveryLink()
    }

    Scaffold(
        containerColor = WarmBackground,
        // Incluye el teclado para que el contenido y el Snackbar queden por encima de él
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack,
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = SurfaceContainer,
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver a la pantalla anterior",
                            tint = WarmText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .shadow(1.dp, CircleShape)
                        .background(PrimaryFixed, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.LocalActivity, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
                    Text(
                        "AKJ Events",
                        color = OnPrimaryFixed,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.6.sp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KeyBadge()
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Recuperar contraseña",
                        color = WarmText,
                        fontSize = 28.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.7).sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Ingresa tu correo electrónico registrado. Te enviaremos un enlace seguro para " +
                            "restablecer tu contraseña. Funciona tanto para cuentas de usuario como de moderador.",
                        color = MutedText,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        letterSpacing = 0.25.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 320.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                buildAnnotatedString {
                                    append("Correo electrónico ")
                                    withStyle(SpanStyle(color = Terracotta)) { append("*") }
                                },
                                color = WarmText,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.1.sp
                            )
                            Text("Requerido", color = MutedText, fontSize = 11.sp, letterSpacing = 0.5.sp)
                        }
                        FormTextField(
                            value = state.email,
                            onValueChange = viewModel::onEmailChange,
                            placeholder = "ejemplo@akjevents.co",
                            leadingIcon = Icons.Outlined.Mail,
                            style = RecoverFieldStyle,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { send() })
                        )
                    }
                    PrimaryButton(
                        text = "Enviar enlace de recuperación",
                        leadingIcon = Icons.AutoMirrored.Outlined.Send,
                        isLoading = state.isBusy,
                        loadingText = "Enviando...",
                        containerColor = Terracotta,
                        shape = RoundedCornerShape(8.dp),
                        fontWeight = FontWeight.SemiBold,
                        onClick = send
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿Recordaste tu contraseña?", color = MutedText, fontSize = 14.sp, letterSpacing = 0.25.sp)
                Row(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable(onClick = onBack)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Iniciar sesión", color = Terracotta, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun KeyBadge() {
    val ping = rememberInfiniteTransition(label = "ping")
    val pingScale by ping.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart),
        label = "pingScale"
    )

    Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
        // Onda que se expande y desvanece (animate-ping del mockup)
        Box(
            Modifier
                .size(80.dp)
                .scale(pingScale)
                .alpha(0.25f * (2f - pingScale))
                .background(PrimaryFixedDim.copy(alpha = 0.4f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(4.dp, CircleShape)
                .background(PrimaryFixed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.VpnKey, contentDescription = null, tint = Terracotta, modifier = Modifier.size(36.dp))
        }
    }
}
