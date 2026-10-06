package co.edu.uniquindio.akjevents.features.auth.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.component.FormFieldStyles
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.PasswordFormTextField
import co.edu.uniquindio.akjevents.core.component.PrimaryButton
import co.edu.uniquindio.akjevents.core.component.RequestResultEffect
import co.edu.uniquindio.akjevents.core.theme.BrandBorder
import co.edu.uniquindio.akjevents.core.theme.BrandDark
import co.edu.uniquindio.akjevents.core.theme.BrandMuted
import co.edu.uniquindio.akjevents.core.theme.BrandOnPastelYellow
import co.edu.uniquindio.akjevents.core.theme.BrandPastelYellow
import co.edu.uniquindio.akjevents.core.theme.BrandSage
import co.edu.uniquindio.akjevents.core.theme.BrandTerracotta
import co.edu.uniquindio.akjevents.core.theme.Honey
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.domain.model.EventCategory

// Los campos del registro son un poco más bajos que los del login (py-2.5 en el mockup)
private val RegisterFieldStyle = FormFieldStyles.Brand.copy(height = 40.dp)

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Mientras se escribe la contraseña, mantiene visible el campo junto con su indicador de seguridad
    val passwordRequester = remember { BringIntoViewRequester() }
    var isPasswordFocused by remember { mutableStateOf(false) }
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    LaunchedEffect(isPasswordFocused) {
        if (isPasswordFocused) {
            // Se repite cuando el teclado termina de abrirse o el indicador aparece/desaparece
            snapshotFlow { imeInsets.getBottom(density) to (state.passwordStrength == PasswordStrength.NONE) }
                .collectLatest {
                    // El cambio aún no está medido: se esperan dos frames para usar el layout ya actualizado
                    withFrameNanos {}
                    withFrameNanos {}
                    passwordRequester.bringIntoView()
                }
        }
    }

    RequestResultEffect(
        result = state.result,
        snackbarHostState = snackbarHostState,
        onFailureShown = viewModel::onFailureShown,
        onSuccess = onRegisterSuccess
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
                    .heightIn(min = maxHeight)
                    .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 24.dp)
            ) {
                RegisterTopBar(onBack = onBack)

                Spacer(Modifier.height(8.dp))
                Text(
                    "Crea tu cuenta",
                    color = BrandDark,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.6).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Únete para organizar y participar con tu comunidad local.",
                    color = BrandMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(16.dp))
                StartingLevelBadge(levelLabel = state.startingLevel.label)
                Spacer(Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    LabeledField("Nombre completo *") {
                        FormTextField(
                            value = state.fullName,
                            onValueChange = viewModel::onFullNameChange,
                            placeholder = "Tu nombre y apellido",
                            leadingIcon = Icons.Outlined.Person,
                            style = RegisterFieldStyle,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    LabeledField("Correo electrónico *") {
                        FormTextField(
                            value = state.email,
                            onValueChange = viewModel::onEmailChange,
                            placeholder = "correo@ejemplo.com",
                            leadingIcon = Icons.Outlined.Mail,
                            style = RegisterFieldStyle,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                        )
                    }

                    LabeledField("Ciudad o municipio *") {
                        CityDropdown(
                            selectedCity = state.city,
                            cities = viewModel.cities,
                            onCitySelected = viewModel::onCityChange
                        )
                    }

                    LabeledField(
                        label = "Contraseña segura *",
                        modifier = Modifier.bringIntoViewRequester(passwordRequester)
                    ) {
                        PasswordFormTextField(
                            modifier = Modifier.onFocusChanged { isPasswordFocused = it.hasFocus },
                            value = state.password,
                            onValueChange = viewModel::onPasswordChange,
                            placeholder = "Mínimo 8 caracteres",
                            leadingIcon = Icons.Outlined.Lock,
                            isPasswordVisible = state.isPasswordVisible,
                            onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                            style = RegisterFieldStyle,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                        )
                        if (state.passwordStrength != PasswordStrength.NONE) {
                            PasswordStrengthIndicator(state.passwordStrength)
                        }
                    }

                    InterestsSection(
                        selected = state.interests,
                        onToggle = viewModel::toggleInterest
                    )

                    RulesAgreement(
                        accepted = state.acceptedRules,
                        onAcceptedChange = viewModel::onAcceptedRulesChange
                    )

                    PrimaryButton(
                        text = "Crear cuenta y comenzar",
                        leadingIcon = Icons.Outlined.HowToReg,
                        isLoading = state.isBusy,
                        loadingText = "Creando cuenta...",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.register()
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = BrandBorder)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("¿Ya tienes cuenta comunitaria?", color = BrandMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "Iniciar sesión",
                        color = BrandTerracotta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable(onClick = onBack)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterTopBar(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Surface(
            onClick = onBack,
            modifier = Modifier.size(36.dp).align(Alignment.CenterStart),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, BrandBorder),
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = BrandDark, modifier = Modifier.size(18.dp))
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .shadow(1.dp, CircleShape)
                .background(Color.White, CircleShape)
                .border(1.dp, BrandBorder, CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(16.dp)
            )
            Text("AKJ Events", color = BrandTerracotta, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StartingLevelBadge(levelLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandPastelYellow.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xCCFDE68A), RoundedCornerShape(16.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .shadow(1.dp, RoundedCornerShape(12.dp))
                .background(BrandPastelYellow, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.MilitaryTech, contentDescription = null, tint = BrandOnPastelYellow, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(
                "Inicia como: $levelLabel (Nivel 1)",
                color = BrandOnPastelYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Gana +10 pts de bienvenida al completar tu registro hoy.",
                color = BrandMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun LabeledField(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = BrandDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityDropdown(
    selectedCity: String,
    cities: List<String>,
    onCitySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        FormTextField(
            value = selectedCity,
            onValueChange = {},
            readOnly = true,
            placeholder = "Selecciona tu ciudad o municipio",
            leadingIcon = Icons.Outlined.LocationOn,
            leadingIconTint = BrandTerracotta,
            style = RegisterFieldStyle,
            trailingContent = {
                Icon(
                    Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = BrandMuted,
                    modifier = Modifier.padding(end = 8.dp).size(18.dp)
                )
            },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White
        ) {
            cities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city, fontSize = 13.sp, color = BrandDark) },
                    onClick = {
                        onCitySelected(city)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val color = when (strength) {
        PasswordStrength.STRONG -> BrandSage
        PasswordStrength.MEDIUM -> Honey
        else -> BrandTerracotta
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(3) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (index < strength.filledBars) color else BrandBorder)
            )
        }
        Text(strength.label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private val EventCategory.icon: ImageVector
    get() = when (this) {
        EventCategory.SPORTS -> Icons.Outlined.SportsSoccer
        EventCategory.CULTURE -> Icons.Outlined.Palette
        EventCategory.ACADEMIC -> Icons.Outlined.School
        EventCategory.VOLUNTEERING -> Icons.Outlined.VolunteerActivism
        EventCategory.SOCIAL -> Icons.Outlined.Forum
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestsSection(selected: Set<EventCategory>, onToggle: (EventCategory) -> Unit) {
    Column(
        modifier = Modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Intereses comunitarios", color = BrandDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (selected.isNotEmpty()) {
                Text(
                    if (selected.size == 1) "1 seleccionado" else "${selected.size} seleccionados",
                    color = BrandSage,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EventCategory.entries.forEach { category ->
                InterestChip(
                    category = category,
                    isSelected = category in selected,
                    onClick = { onToggle(category) }
                )
            }
        }
    }
}

@Composable
private fun InterestChip(category: EventCategory, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        onClick = onClick,
        shape = shape,
        color = if (isSelected) BrandTerracotta else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, BrandBorder),
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                category.icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else BrandMuted,
                modifier = Modifier.size(16.dp)
            )
            Text(
                category.label,
                color = if (isSelected) Color.White else BrandDark,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RulesAgreement(accepted: Boolean, onAcceptedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .toggleable(value = accepted, role = Role.Checkbox, onValueChange = onAcceptedChange),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = accepted,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = BrandTerracotta, uncheckedColor = BrandMuted)
        )
        Text(
            buildAnnotatedString {
                append("Acepto las ")
                withStyle(
                    SpanStyle(color = BrandTerracotta, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                ) { append("Normas Comunitarias") }
                append(" y la política de privacidad de Quindío.")
            },
            color = BrandMuted,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
