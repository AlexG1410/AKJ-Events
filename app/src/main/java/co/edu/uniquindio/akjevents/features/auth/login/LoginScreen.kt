package co.edu.uniquindio.akjevents.features.auth.login

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.component.FormFieldError
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.PasswordFormTextField
import co.edu.uniquindio.akjevents.core.component.PrimaryButton
import co.edu.uniquindio.akjevents.core.component.RequestResultEffect
import co.edu.uniquindio.akjevents.core.theme.BrandBorder
import co.edu.uniquindio.akjevents.core.theme.BrandDark
import co.edu.uniquindio.akjevents.core.theme.BrandMuted
import co.edu.uniquindio.akjevents.core.theme.BrandSage
import co.edu.uniquindio.akjevents.core.theme.BrandSageLight
import co.edu.uniquindio.akjevents.core.theme.BrandTerracotta
import co.edu.uniquindio.akjevents.core.theme.WarmBackground

@Composable
fun LoginScreen(
    onLoginSuccess: (isModerator: Boolean) -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    RequestResultEffect(
        result = state.result,
        snackbarHostState = snackbarHostState,
        onFailureShown = viewModel::onFailureShown,
        onSuccess = { onLoginSuccess(state.isModerator) }
    )

    Scaffold(
        containerColor = WarmBackground,
        // Incluye el teclado para que el contenido y el Snackbar queden por encima de él
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    // Ocupa al menos toda la pantalla para que el pie quede abajo
                    .heightIn(min = maxHeight)
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)
            ) {
                LoginHeader()

                Text(
                    stringResource(R.string.login_title),
                    color = BrandDark,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.6).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.login_subtitle),
                    color = BrandMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FieldLabel(stringResource(R.string.field_email))
                            Text(stringResource(R.string.login_email_example), color = BrandMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                        FormTextField(
                            value = state.email,
                            onValueChange = viewModel::onEmailChange,
                            placeholder = stringResource(R.string.login_email_placeholder),
                            leadingIcon = Icons.Outlined.Mail,
                            isError = state.emailError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            trailingContent = if (state.isEmailValid) {
                                {
                                    Icon(
                                        Icons.Outlined.CheckCircle,
                                        contentDescription = stringResource(R.string.login_valid_email),
                                        tint = BrandSage,
                                        modifier = Modifier.padding(end = 10.dp).size(18.dp)
                                    )
                                }
                            } else null
                        )
                        FormFieldError(state.emailError?.asString())
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FieldLabel(stringResource(R.string.field_password))
                            Text(
                                stringResource(R.string.login_forgot_password),
                                color = BrandTerracotta,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(onClick = onForgotPassword)
                            )
                        }
                        PasswordFormTextField(
                            value = state.password,
                            onValueChange = viewModel::onPasswordChange,
                            placeholder = stringResource(R.string.login_password_placeholder),
                            leadingIcon = Icons.Outlined.Lock,
                            isPasswordVisible = state.isPasswordVisible,
                            onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                            isError = state.passwordError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.login()
                            })
                        )
                        FormFieldError(state.passwordError?.asString())
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .toggleable(
                                value = state.rememberDevice,
                                role = Role.Checkbox,
                                onValueChange = viewModel::onRememberDeviceChange
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = state.rememberDevice,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = BrandTerracotta, uncheckedColor = BrandMuted)
                        )
                        Text(stringResource(R.string.login_remember_device), color = BrandDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    PrimaryButton(
                        text = stringResource(R.string.login_button),
                        trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
                        enabled = state.isFormValid,
                        isLoading = state.isBusy,
                        loadingText = stringResource(R.string.login_loading),
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login()
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    TrustNote()
                }

                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = BrandBorder)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.login_no_account), color = BrandMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        stringResource(R.string.login_create_account),
                        color = BrandTerracotta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable(onClick = onCreateAccount)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, BrandBorder),
            shadowElevation = 1.dp
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = stringResource(R.string.app_logo_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier.padding(6.dp)
            )
        }

        val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
            label = "pulseAlpha"
        )
        Row(
            modifier = Modifier
                .background(BrandSageLight, CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(Modifier.size(8.dp).alpha(pulse).background(BrandSage, CircleShape))
            Text(stringResource(R.string.login_active_community), color = BrandSage, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, color = BrandDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun TrustNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandSageLight.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .border(1.dp, BrandSage.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Outlined.Shield, contentDescription = null, tint = BrandSage, modifier = Modifier.padding(top = 2.dp).size(18.dp))
        val privacyNotice = stringResource(R.string.login_privacy_notice)
        val appName = stringResource(R.string.app_name)
        Text(
            buildAnnotatedString {
                append(privacyNotice)
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(appName) }
                append(".")
            },
            color = BrandDark,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
