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
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NaturePeople
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.VerifiedUser
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.component.EmptyState
import co.edu.uniquindio.akjevents.core.component.FormFieldStyle
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.ImagePickerSheet
import co.edu.uniquindio.akjevents.core.component.PrimaryButton
import co.edu.uniquindio.akjevents.core.component.RequestResultEffect
import co.edu.uniquindio.akjevents.core.component.rememberImagePicker
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
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.core.util.labelRes
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import coil3.compose.AsyncImage
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Locale
import kotlinx.coroutines.launch

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

/**
 * Formulario de los mockups 07 (crear) y 08 (editar, cuando la ruta trae el id del evento).
 * La ubicación se elige en el mapa con [onPickLocation] y regresa en [pickedLocation].
 */
@Composable
fun CreateEventScreen(
    onBack: () -> Unit,
    onEventSubmitted: () -> Unit,
    onPickLocation: (PickedLocation?) -> Unit,
    pickedLocation: PickedLocation?,
    onPickedLocationConsumed: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showHelp by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Cámara o galería (guía 15); si se niega el permiso de cámara, la galería sigue disponible
    val imagePicker = rememberImagePicker(
        onImagePicked = { viewModel.addImage(it.toString()) },
        onCameraDenied = {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.camera_permission_required)) }
        }
    )

    // Primero se elige la fecha y luego la hora
    var datePickerField by remember { mutableStateOf<DateTimeField?>(null) }
    var timePickerField by remember { mutableStateOf<DateTimeField?>(null) }
    var pickedDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(pickedLocation) {
        if (pickedLocation != null) {
            viewModel.onLocationPicked(pickedLocation)
            onPickedLocationConsumed()
        }
    }

    RequestResultEffect(
        result = state.result,
        snackbarHostState = snackbarHostState,
        onFailureShown = viewModel::onFailureShown,
        onSuccess = onEventSubmitted
    )

    Scaffold(
        containerColor = WarmBackground,
        topBar = { CreateEventTopBar(isEditing = state.isEditing, onBack = onBack, onHelp = { showHelp = true }) },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        if (!state.canSubmit) {
            EmptyState(
                icon = Icons.Outlined.EditOff,
                title = stringResource(R.string.create_cannot_edit_title),
                message = stringResource(R.string.create_cannot_edit_message),
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }
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

            BasicInfoSection(state = state, viewModel = viewModel, onAddImage = imagePicker::open)

            ScheduleSection(
                state = state,
                onPlaceChange = viewModel::onPlaceChange,
                onPickStart = { datePickerField = DateTimeField.START },
                onPickEnd = { datePickerField = DateTimeField.END },
                onPickLocation = {
                    onPickLocation(state.location?.let { PickedLocation(state.place, it) })
                }
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
                    text = stringResource(if (state.isEditing) R.string.create_save_changes else R.string.create_submit),
                    onClick = viewModel::submit,
                    leadingIcon = if (state.isEditing) Icons.Outlined.Save else null,
                    trailingIcon = if (state.isEditing) null else Icons.AutoMirrored.Outlined.Send,
                    isLoading = state.isBusy,
                    loadingText = stringResource(if (state.isEditing) R.string.create_saving else R.string.recover_loading),
                    containerColor = Terracotta,
                    shape = CircleShape,
                    modifier = Modifier.shadow(8.dp, CircleShape, ambientColor = Terracotta, spotColor = Terracotta)
                )
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = SurfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Close, contentDescription = null, tint = WarmText, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(if (state.isEditing) R.string.create_cancel_edit else R.string.action_cancel), color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    ImagePickerSheet(imagePicker)

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
            title = stringResource(if (field == DateTimeField.START) R.string.create_start_time else R.string.create_end_time),
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

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            containerColor = SurfaceContainerLow,
            title = { Text(stringResource(R.string.create_help_title), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    stringResource(R.string.create_help_message),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text(stringResource(R.string.action_got_it)) } }
        )
    }
}

@Composable
private fun CreateEventTopBar(isEditing: Boolean, onBack: () -> Unit, onHelp: () -> Unit) {
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
                    contentDescription = stringResource(R.string.recover_back),
                    tint = WarmText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            if (isEditing) {
                Text(stringResource(R.string.create_editing), color = Terracotta, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Text(
                stringResource(if (isEditing) R.string.create_edit_title else R.string.create_title),
                color = WarmText,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(if (isEditing) R.string.create_edit_subtitle else R.string.create_subtitle),
                color = MutedText,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        IconButton(onClick = onHelp, modifier = Modifier.size(36.dp)) {
            Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = stringResource(R.string.create_help), tint = MutedText, modifier = Modifier.size(18.dp))
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
            Step(number = 1, label = stringResource(R.string.create_step_info), icon = Icons.Outlined.EditNote, status = status(0))
            Step(number = 2, label = stringResource(R.string.create_step_date), icon = Icons.Outlined.Event, status = status(1))
            Step(number = 3, label = stringResource(R.string.create_step_capacity), icon = Icons.Outlined.PeopleAlt, status = status(2))
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
            stringResource(R.string.create_step_label, number, label),
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
private fun BasicInfoSection(state: CreateEventUiState, viewModel: CreateEventViewModel, onAddImage: () -> Unit) {
    FormCard {
        SectionHeader(
            icon = Icons.Outlined.EditNote,
            iconContainer = PrimaryFixed,
            iconTint = OnPrimaryFixedVariant,
            title = stringResource(R.string.create_basic_info),
            subtitle = stringResource(R.string.create_basic_info_subtitle)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FieldLabel(stringResource(R.string.create_field_title))
            FormTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = stringResource(R.string.create_title_placeholder),
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
            FieldLabel(stringResource(R.string.create_field_category))
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
                FieldLabel(stringResource(R.string.create_field_description), modifier = Modifier.weight(1f))
                Text(
                    stringResource(R.string.create_character_count, state.description.length, CreateEventViewModel.MAX_DESCRIPTION_LENGTH),
                    color = OutlineColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            DescriptionField(value = state.description, onValueChange = viewModel::onDescriptionChange)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Sage, modifier = Modifier.size(14.dp))
                Text(
                    stringResource(R.string.create_description_tip),
                    color = Sage,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        EventImagePicker(images = state.images, canAdd = state.canAddImage, onAdd = onAddImage, onRemove = viewModel::removeImage)
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
            Text(stringResource(category.labelRes), color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            if (isSelected) {
                Icon(Icons.Outlined.Done, contentDescription = stringResource(R.string.create_selected), tint = contentColor, modifier = Modifier.size(14.dp))
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
                    Text(stringResource(R.string.create_description_placeholder), style = CreateFieldStyle.textStyle.copy(color = OutlineColor))
                }
                innerTextField()
            }
        }
    )
}

/**
 * Fotos del evento (mockup 07): la principal a la izquierda y, a la derecha, las demás y el botón para
 * añadir otra (máximo [CreateEventViewModel.MAX_IMAGES]). Se toman con la cámara o se eligen de la galería.
 */
@Composable
private fun EventImagePicker(images: List<String>, canAdd: Boolean, onAdd: () -> Unit, onRemove: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FieldLabel(stringResource(R.string.create_field_photo), modifier = Modifier.weight(1f))
            Text(stringResource(R.string.create_min_one_image), color = Terracotta, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        if (images.isEmpty()) {
            AddPhotoTile(count = 0, onClick = onAdd, modifier = Modifier.fillMaxWidth().height(144.dp))
            return@Column
        }
        Row(modifier = Modifier.height(144.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EventPhoto(
                image = images.first(),
                index = 0,
                onRemove = { onRemove(0) },
                isMain = true,
                modifier = Modifier.weight(2f).fillMaxHeight()
            )
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                images.drop(1).forEachIndexed { offset, image ->
                    val index = offset + 1
                    EventPhoto(image = image, index = index, onRemove = { onRemove(index) }, modifier = Modifier.fillMaxWidth().weight(1f))
                }
                if (canAdd) {
                    AddPhotoTile(count = images.size, onClick = onAdd, modifier = Modifier.fillMaxWidth().weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EventPhoto(image: String, index: Int, onRemove: () -> Unit, modifier: Modifier = Modifier, isMain: Boolean = false) {
    Box(modifier = modifier.shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp))) {
        // Coil carga igual una URL ya publicada o el URI local de la cámara o la galería
        AsyncImage(
            model = image,
            contentDescription = stringResource(R.string.create_photo_number, index + 1),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.event_placeholder),
            error = painterResource(R.drawable.event_placeholder),
            modifier = Modifier.fillMaxSize()
        )
        if (isMain) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.5f)))
            )
            Text(
                stringResource(R.string.create_main_photo),
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
            onClick = onRemove,
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.55f),
            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.create_remove_photo), tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AddPhotoTile(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = SurfaceContainerLow, modifier = modifier) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(SecondaryFixed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, tint = OnSecondaryFixedVariant, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.create_add_photo), color = WarmText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.create_photos_count, count, CreateEventViewModel.MAX_IMAGES),
                color = OutlineColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ScheduleSection(
    state: CreateEventUiState,
    onPlaceChange: (String) -> Unit,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit,
    onPickLocation: () -> Unit
) {
    FormCard {
        SectionHeader(
            icon = Icons.Outlined.Place,
            iconContainer = SecondaryFixed,
            iconTint = OnSecondaryFixedVariant,
            title = stringResource(R.string.create_schedule),
            subtitle = stringResource(R.string.create_schedule_subtitle)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val startsAt = state.startsAt
            val endsAt = state.endsAt
            DateTimeRow(
                icon = Icons.Outlined.Schedule,
                iconTint = Terracotta,
                label = stringResource(R.string.create_starts),
                date = startsAt?.let { EventFormat.weekdayFullDate(it) },
                time = startsAt?.let(EventFormat::timeOfDay),
                timeColor = Sage,
                onChange = onPickStart
            )
            DateTimeRow(
                icon = Icons.Outlined.EventAvailable,
                iconTint = MutedText,
                label = stringResource(R.string.create_ends),
                date = endsAt?.let { EventFormat.weekdayFullDate(it) },
                time = endsAt?.let {
                    if (startsAt != null && it.isAfter(startsAt)) EventFormat.timeWithDuration(startsAt, it) else EventFormat.timeOfDay(it)
                },
                timeColor = MutedText,
                onChange = onPickEnd
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel(stringResource(R.string.create_field_place))
            FormTextField(
                value = state.place,
                onValueChange = onPlaceChange,
                placeholder = stringResource(R.string.create_place_placeholder),
                leadingIcon = Icons.Outlined.LocationOn,
                style = PlaceFieldStyle,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done)
            )
        }

        LocationPickerRow(location = state.location, onPick = onPickLocation)
    }
}

/** Fila que abre el mapa para elegir el punto exacto del evento (mockups 08 y 09). */
@Composable
private fun LocationPickerRow(location: Location?, onPick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel(stringResource(R.string.create_field_map))
        Surface(
            onClick = onPick,
            shape = RoundedCornerShape(8.dp),
            color = if (location != null) SecondaryFixed.copy(alpha = 0.6f) else SurfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(if (location != null) Sage else Terracotta, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(if (location != null) R.string.create_coordinates_verified else R.string.create_choose_on_map),
                        color = if (location != null) OnSecondaryFixedVariant else WarmText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        location?.let { stringResource(R.string.detail_coordinates, "%.4f".format(Locale.US, it.latitude), "%.4f".format(Locale.US, it.longitude)) }
                            ?: stringResource(R.string.create_mark_point),
                        color = MutedText,
                        fontSize = 12.sp
                    )
                }
                Text(
                    stringResource(if (location != null) R.string.create_adjust_pin else R.string.create_open_map),
                    color = Terracotta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
            Text(date ?: stringResource(R.string.create_not_set), color = if (date != null) WarmText else OutlineColor, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(time ?: stringResource(R.string.create_choose_date_time), color = if (time != null) timeColor else OutlineColor, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(onClick = onChange, shape = CircleShape, color = SurfaceContainer) {
            Text(
                stringResource(if (date != null) R.string.home_change else R.string.create_choose),
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
            title = stringResource(R.string.create_capacity_section),
            subtitle = stringResource(R.string.create_capacity_subtitle)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerLow, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(stringResource(R.string.create_limit_capacity), color = WarmText, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.create_limit_capacity_hint), color = MutedText, fontSize = 12.sp, lineHeight = 16.sp)
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
                FieldLabel(stringResource(R.string.create_field_capacity))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormTextField(
                        value = capacity,
                        onValueChange = onCapacityChange,
                        placeholder = "0",
                        leadingIcon = null,
                        style = CapacityFieldStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        trailingContent = {
                            Text(stringResource(R.string.create_people), color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(end = 12.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        StepperButton(text = "-", description = stringResource(R.string.create_decrease_capacity), onClick = onDecrease)
                        StepperButton(text = "+", description = stringResource(R.string.create_increase_capacity), onClick = onIncrease)
                    }
                }
                Text(
                    stringResource(R.string.create_capacity_full_hint),
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
            Text(stringResource(R.string.create_review_title), color = OnSecondaryFixed, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.create_review_message),
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
            ) { Text(stringResource(R.string.action_next)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
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
            TextButton(onClick = { onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute)) }) { Text(stringResource(R.string.action_accept)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
