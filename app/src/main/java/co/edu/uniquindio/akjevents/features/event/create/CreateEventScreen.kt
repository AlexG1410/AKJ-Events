package co.edu.uniquindio.akjevents.features.event.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NaturePeople
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.component.AppBottomBar
import co.edu.uniquindio.akjevents.core.component.BottomDestination
import co.edu.uniquindio.akjevents.core.component.FormFieldStyle
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.PrimaryButton
import co.edu.uniquindio.akjevents.core.component.RequestResultEffect
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnPrimaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.OutlineColor
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerHigh
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerHighest
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.TertiaryFixed
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.EventFormat
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

private const val COMING_SOON = "Disponible próximamente"

private val OnSecondaryFixed = Color(0xFF002114)

// Campos del mockup 07: fondo surface-container-low, sin borde hasta recibir el foco
private val CreateFieldStyle = FormFieldStyle(
    height = 48.dp,
    shape = RoundedCornerShape(8.dp),
    containerColor = SurfaceContainerLow,
    focusedContainerColor = SurfaceContainer,
    borderColor = Color.Transparent,
    focusedBorderColor = Terracotta,
    textStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, color = WarmText),
    placeholderColor = OutlineColor,
    iconColor = Terracotta,
    iconSize = 20.dp,
    iconStartPadding = 12.dp,
    textStartPadding = 16.dp
)

private val PlaceFieldStyle = CreateFieldStyle.copy(textStartPadding = 40.dp)

private val CapacityFieldStyle = CreateFieldStyle.copy(
    textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, color = WarmText)
)

/** Íconos de las categorías tal como aparecen en el mockup 07. */
private val EventCategory.formIcon: ImageVector
    get() = when (this) {
        EventCategory.SPORTS -> Icons.Outlined.SportsSoccer
        EventCategory.CULTURE -> Icons.Outlined.TheaterComedy
        EventCategory.ACADEMIC -> Icons.Outlined.School
        EventCategory.VOLUNTEERING -> Icons.Outlined.NaturePeople
        EventCategory.SOCIAL -> Icons.Outlined.Groups
    }

/** Fecha y hora que se están eligiendo con los diálogos de Material 3. */
private enum class DateTimeField { START, END }

@Composable
fun CreateEventScreen(
    onBack: () -> Unit,
    onEventSubmitted: () -> Unit,
    viewModel: CreateEventViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val showComingSoon: () -> Unit = {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(COMING_SOON)
        }
    }

    // Primero se elige la fecha y luego la hora
    var datePickerField by remember { mutableStateOf<DateTimeField?>(null) }
    var timePickerField by remember { mutableStateOf<DateTimeField?>(null) }
    var pickedDate by remember { mutableStateOf<LocalDate?>(null) }

    RequestResultEffect(
        result = state.result,
        snackbarHostState = snackbarHostState,
        onFailureShown = viewModel::onFailureShown,
        onSuccess = onEventSubmitted
    )

    Scaffold(
        containerColor = WarmBackground,
        topBar = { CreateEventTopBar(onBack = onBack, onDraft = showComingSoon, onHelp = showComingSoon) },
        bottomBar = {
            AppBottomBar(
                selected = BottomDestination.CREATE,
                hasUnreadAlerts = true,
                onSelect = {
                    when (it) {
                        BottomDestination.HOME -> onBack()
                        BottomDestination.CREATE -> Unit
                        else -> showComingSoon()
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // El teclado tapa la barra inferior: solo se suma lo que sobresale de ella
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            StepIndicator(isInfoComplete = state.isInfoComplete, isScheduleComplete = state.isScheduleComplete)

            BasicInfoSection(state = state, viewModel = viewModel)

            ScheduleSection(
                state = state,
                onPlaceChange = viewModel::onPlaceChange,
                onPickStart = { datePickerField = DateTimeField.START },
                onPickEnd = { datePickerField = DateTimeField.END }
            )

            CapacitySection(
                limitCapacity = state.limitCapacity,
                capacity = state.capacity,
                onLimitChange = viewModel::onLimitCapacityChange,
                onCapacityChange = viewModel::onCapacityChange,
                onDecrease = viewModel::decreaseCapacity,
                onIncrease = viewModel::increaseCapacity
            )

            ModerationNotice()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                PrimaryButton(
                    text = "Enviar para verificación",
                    onClick = viewModel::submit,
                    trailingIcon = Icons.AutoMirrored.Outlined.Send,
                    isLoading = state.isBusy,
                    loadingText = "Enviando...",
                    containerColor = Terracotta,
                    shape = CircleShape,
                    modifier = Modifier.shadow(8.dp, CircleShape, ambientColor = Terracotta, spotColor = Terracotta)
                )
                Surface(
                    onClick = showComingSoon,
                    shape = CircleShape,
                    color = SurfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Visibility, contentDescription = null, tint = WarmText, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Vista previa del evento", color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    datePickerField?.let { field ->
        val current = if (field == DateTimeField.START) state.startsAt else state.endsAt
        // El fin no puede ser antes del día de inicio
        val minDate = if (field == DateTimeField.END) state.startsAt?.toLocalDate() ?: LocalDate.now() else LocalDate.now()
        EventDatePickerDialog(
            initialDate = current?.toLocalDate() ?: minDate,
            minDate = minDate,
            onDismiss = { datePickerField = null },
            onConfirm = { date ->
                pickedDate = date
                datePickerField = null
                timePickerField = field
            }
        )
    }

    timePickerField?.let { field ->
        val current = if (field == DateTimeField.START) state.startsAt else state.endsAt
        // Por defecto el fin propone dos horas después del inicio
        val suggested = current?.toLocalTime()
            ?: state.startsAt?.takeIf { field == DateTimeField.END }?.toLocalTime()?.plusHours(2)
            ?: LocalTime.of(9, 0)
        EventTimePickerDialog(
            title = if (field == DateTimeField.START) "Hora de inicio" else "Hora de fin",
            initialTime = suggested,
            onDismiss = { timePickerField = null },
            onConfirm = { time ->
                val date = pickedDate ?: LocalDate.now()
                val dateTime = LocalDateTime.of(date, time)
                if (field == DateTimeField.START) viewModel.onStartsAtChange(dateTime) else viewModel.onEndsAtChange(dateTime)
                timePickerField = null
            }
        )
    }
}

@Composable
private fun CreateEventTopBar(onBack: () -> Unit, onDraft: () -> Unit, onHelp: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WarmBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(onClick = onBack, shape = CircleShape, color = SurfaceContainer, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver a la pantalla anterior",
                    tint = WarmText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Crear Evento", color = WarmText, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold)
            Text("Publica para la comunidad local", color = MutedText, fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(onClick = onDraft, shape = CircleShape, color = SurfaceContainer) {
            Row(
                modifier = Modifier.height(36.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Outlined.Save, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
                Text("Borrador", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        IconButton(onClick = onHelp, modifier = Modifier.size(36.dp)) {
            Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = "Guía y ayuda", tint = MutedText, modifier = Modifier.size(18.dp))
        }
    }
}

/** Tarjeta blanca de cada sección del formulario. */
@Composable
private fun FormCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { content() }
}

@Composable
private fun SectionHeader(icon: ImageVector, iconContainer: Color, iconTint: Color, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.size(32.dp).background(iconContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(title, color = WarmText, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MutedText, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, color = MutedText, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, modifier = modifier)
}

private enum class StepStatus { DONE, ACTIVE, PENDING }

/** Indicador de 3 pasos: avanza a medida que se completan la información y la fecha. */
@Composable
private fun StepIndicator(isInfoComplete: Boolean, isScheduleComplete: Boolean) {
    val doneCount = when {
        isInfoComplete && isScheduleComplete -> 2
        isInfoComplete -> 1
        else -> 0
    }
    fun status(index: Int) = when {
        index < doneCount -> StepStatus.DONE
        index == doneCount -> StepStatus.ACTIVE
        else -> StepStatus.PENDING
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Barra conectora detrás de los círculos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, end = 28.dp, top = 15.dp)
                .height(3.dp)
                .background(SurfaceContainer)
        ) {
            if (doneCount > 0) {
                Box(Modifier.weight(doneCount.toFloat()).fillMaxHeight().background(Sage))
            }
            if (doneCount < 2) {
                Spacer(Modifier.weight((2 - doneCount).toFloat()))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Step(number = 1, label = "Info", icon = Icons.Outlined.EditNote, status = status(0))
            Step(number = 2, label = "Fecha", icon = Icons.Outlined.Event, status = status(1))
            Step(number = 3, label = "Cupos", icon = Icons.Outlined.PeopleAlt, status = status(2))
        }
    }
}

@Composable
private fun Step(number: Int, label: String, icon: ImageVector, status: StepStatus) {
    Column(
        modifier = Modifier.background(Color.White).padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val circle = Modifier.size(32.dp)
        when (status) {
            StepStatus.DONE -> Box(circle.background(Sage, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Done, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            StepStatus.ACTIVE -> Box(
                circle
                    .background(Terracotta, CircleShape)
                    .border(4.dp, PrimaryFixed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            StepStatus.PENDING -> Box(circle.background(SurfaceContainer, CircleShape), contentAlignment = Alignment.Center) {
                Text("$number", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Text(
            "$number. $label",
            color = when (status) {
                StepStatus.DONE -> Sage
                StepStatus.ACTIVE -> Terracotta
                StepStatus.PENDING -> MutedText
            },
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = if (status == StepStatus.ACTIVE) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BasicInfoSection(state: CreateEventUiState, viewModel: CreateEventViewModel) {
    FormCard {
        SectionHeader(
            icon = Icons.Outlined.EditNote,
            iconContainer = PrimaryFixed,
            iconTint = OnPrimaryFixedVariant,
            title = "Información Básica",
            subtitle = "Define el tema y propósito del encuentro"
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FieldLabel("Título del evento *")
            FormTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = "Escribe un título claro y llamativo",
                leadingIcon = null,
                style = CreateFieldStyle,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                trailingContent = if (state.title.isNotBlank()) {
                    {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Sage,
                            modifier = Modifier.padding(end = 8.dp).size(20.dp)
                        )
                    }
                } else null
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel("Categoría comunitaria *")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EventCategory.entries.forEach { category ->
                    CategoryOption(
                        category = category,
                        isSelected = category == state.category,
                        onClick = { viewModel.selectCategory(category) }
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                FieldLabel("Descripción del evento *", modifier = Modifier.weight(1f))
                Text(
                    "${state.description.length} / ${CreateEventViewModel.MAX_DESCRIPTION_LENGTH}",
                    color = OutlineColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            DescriptionField(value = state.description, onValueChange = viewModel::onDescriptionChange)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Sage, modifier = Modifier.size(14.dp))
                Text(
                    "Consejo: Menciona qué deben llevar y el nivel de experiencia requerido.",
                    color = Sage,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        EventImagePicker(imageUrl = state.imageUrl, onChangeImage = viewModel::changeImage)
    }
}

@Composable
private fun CategoryOption(category: EventCategory, isSelected: Boolean, onClick: () -> Unit) {
    val contentColor = if (isSelected) Color.White else WarmText
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) Sage else SurfaceContainer,
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.height(36.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(category.formIcon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Text(category.label, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            if (isSelected) {
                Icon(Icons.Outlined.Done, contentDescription = "Seleccionada", tint = contentColor, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/** Área de texto de varias líneas para la descripción. */
@Composable
private fun DescriptionField(value: String, onValueChange: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(8.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
        textStyle = CreateFieldStyle.textStyle,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        minLines = 4,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(Terracotta),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isFocused) SurfaceContainer else SurfaceContainerLow, shape)
                    .border(1.dp, if (isFocused) Terracotta else Color.Transparent, shape)
                    .padding(12.dp)
            ) {
                if (value.isEmpty()) {
                    Text("Explica qué aprenderán o experimentarán las personas...", style = CreateFieldStyle.textStyle.copy(color = OutlineColor))
                }
                innerTextField()
            }
        }
    )
}

/** Imagen temporal aleatoria mientras no exista la carga de imágenes. */
@Composable
private fun EventImagePicker(imageUrl: String, onChangeImage: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FieldLabel("Fotografía del evento *", modifier = Modifier.weight(1f))
            Text("Imagen temporal", color = Terracotta, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(144.dp)
                    .shadow(1.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen del evento",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.event_placeholder),
                    error = painterResource(R.drawable.event_placeholder),
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.5f)))
                )
                Text(
                    "Foto Principal",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Terracotta.copy(alpha = 0.8f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Surface(
                onClick = onChangeImage,
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainerLow,
                modifier = Modifier.weight(1f).height(144.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).background(SecondaryFixed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Autorenew, contentDescription = null, tint = OnSecondaryFixedVariant, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Cambiar imagen", color = WarmText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Aleatoria", color = OutlineColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ScheduleSection(
    state: CreateEventUiState,
    onPlaceChange: (String) -> Unit,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit
) {
    FormCard {
        SectionHeader(
            icon = Icons.Outlined.Place,
            iconContainer = SecondaryFixed,
            iconTint = OnSecondaryFixedVariant,
            title = "Fecha, Hora y Espacio",
            subtitle = "Cuándo y dónde se reunirán"
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val startsAt = state.startsAt
            val endsAt = state.endsAt
            DateTimeRow(
                icon = Icons.Outlined.Schedule,
                iconTint = Terracotta,
                label = "Comienza *",
                date = startsAt?.let(EventFormat::weekdayFullDate),
                time = startsAt?.let(EventFormat::timeOfDay),
                timeColor = Sage,
                onChange = onPickStart
            )
            DateTimeRow(
                icon = Icons.Outlined.EventAvailable,
                iconTint = MutedText,
                label = "Finaliza *",
                date = endsAt?.let(EventFormat::weekdayFullDate),
                time = endsAt?.let {
                    if (startsAt != null && it.isAfter(startsAt)) EventFormat.timeWithDuration(startsAt, it) else EventFormat.timeOfDay(it)
                },
                timeColor = MutedText,
                onChange = onPickEnd
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel("Lugar o punto de encuentro *")
            FormTextField(
                value = state.place,
                onValueChange = onPlaceChange,
                placeholder = "Ej: Parque de la Vida, Av. Bolívar, Armenia",
                leadingIcon = Icons.Outlined.LocationOn,
                style = PlaceFieldStyle,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done)
            )
        }
    }
}

@Composable
private fun DateTimeRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    date: String?,
    time: String?,
    timeColor: Color,
    onChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(40.dp).background(SurfaceContainer, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), color = MutedText, fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            Text(date ?: "Sin definir", color = if (date != null) WarmText else OutlineColor, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(time ?: "Elige fecha y hora", color = if (time != null) timeColor else OutlineColor, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(onClick = onChange, shape = CircleShape, color = SurfaceContainer) {
            Text(
                if (date != null) "Cambiar" else "Elegir",
                color = WarmText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun CapacitySection(
    limitCapacity: Boolean,
    capacity: String,
    onLimitChange: (Boolean) -> Unit,
    onCapacityChange: (String) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    FormCard {
        SectionHeader(
            icon = Icons.Outlined.PeopleAlt,
            iconContainer = TertiaryFixed,
            iconTint = Color(0xFF241A00),
            title = "Participación y Capacidad",
            subtitle = "Regula el acceso y la convivencia"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerLow, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text("Limitar cupo de asistentes", color = WarmText, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
                Text("Cierra inscripciones al completarse", color = MutedText, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Switch(
                checked = limitCapacity,
                onCheckedChange = onLimitChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Terracotta,
                    checkedThumbColor = Color.White,
                    checkedBorderColor = Terracotta,
                    uncheckedTrackColor = SurfaceContainerHighest
                )
            )
        }

        AnimatedVisibility(visible = limitCapacity) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FieldLabel("Cupo máximo disponible *")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormTextField(
                        value = capacity,
                        onValueChange = onCapacityChange,
                        placeholder = "0",
                        leadingIcon = null,
                        style = CapacityFieldStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        trailingContent = {
                            Text("personas", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(end = 12.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        StepperButton(text = "-", description = "Disminuir cupo", onClick = onDecrease)
                        StepperButton(text = "+", description = "Aumentar cupo", onClick = onIncrease)
                    }
                }
                Text(
                    "Cuando se llene el cupo, nadie más podrá confirmar asistencia.",
                    color = OutlineColor,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StepperButton(text: String, description: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = SurfaceContainer,
        modifier = Modifier.size(40.dp).semantics { contentDescription = description }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = WarmText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ModerationNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .background(SecondaryFixed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(40.dp).background(Sage, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Tu evento será revisado por un moderador", color = OnSecondaryFixed, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                "Garantizamos que todos los encuentros en AKJ Events cumplan las normas comunitarias, sean inclusivos " +
                    "y ofrezcan seguridad a los asistentes. Será público cuando un moderador lo apruebe.",
                color = OnSecondaryFixedVariant,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDatePickerDialog(
    initialDate: LocalDate,
    minDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    // El DatePicker trabaja con milisegundos en UTC
    val minMillis = minDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val selectableDates = remember(minMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= minMillis
            override fun isSelectableYear(year: Int) = year >= minDate.year
        }
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = maxOf(initialDate, minDate).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = selectableDates
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
                enabled = datePickerState.selectedDateMillis != null
            ) { Text("Siguiente") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        colors = DatePickerDefaults.colors(containerColor = SurfaceContainerLow)
    ) {
        DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(containerColor = SurfaceContainerLow))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTimePickerDialog(
    title: String,
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(initialHour = initialTime.hour, initialMinute = initialTime.minute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = SurfaceContainer,
                    timeSelectorSelectedContainerColor = PrimaryFixed,
                    timeSelectorSelectedContentColor = OnPrimaryFixedVariant,
                    timeSelectorUnselectedContainerColor = SurfaceContainer,
                    periodSelectorSelectedContainerColor = PrimaryFixed,
                    periodSelectorSelectedContentColor = OnPrimaryFixedVariant
                )
            )
        },
        containerColor = SurfaceContainerLow,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute)) }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
