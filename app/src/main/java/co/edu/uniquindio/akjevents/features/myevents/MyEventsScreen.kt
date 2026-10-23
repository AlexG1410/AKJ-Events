package co.edu.uniquindio.akjevents.features.myevents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.core.component.ConfirmDialog
import co.edu.uniquindio.akjevents.core.component.ContentCard
import co.edu.uniquindio.akjevents.core.component.EmptyState
import co.edu.uniquindio.akjevents.core.component.EventImage
import co.edu.uniquindio.akjevents.core.component.FilterPill
import co.edu.uniquindio.akjevents.core.component.MessageEffect
import co.edu.uniquindio.akjevents.core.component.QrCode
import co.edu.uniquindio.akjevents.core.component.ScreenHeader
import co.edu.uniquindio.akjevents.core.component.StatusBadge
import co.edu.uniquindio.akjevents.core.component.UserAvatar
import co.edu.uniquindio.akjevents.core.theme.ErrorColor
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.OnTertiaryContainer
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerHigh
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.TertiaryColor
import co.edu.uniquindio.akjevents.core.theme.TertiaryFixed
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.EventFormat
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints

/** Diálogos que se abren desde las tarjetas de evento. */
private sealed interface MyEventDialog {
    data class Qr(val event: CommunityEvent) : MyEventDialog
    data class Attendees(val event: CommunityEvent) : MyEventDialog
    data class Notice(val event: CommunityEvent) : MyEventDialog
    data class Delete(val event: CommunityEvent) : MyEventDialog
}

@Composable
fun MyEventsScreen(
    onBack: () -> Unit,
    onCreateEvent: () -> Unit,
    onEditEvent: (String) -> Unit,
    onOpenEvent: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: MyEventsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var dialog by remember { mutableStateOf<MyEventDialog?>(null) }
    // Evento cuyo "¿Estás seguro de finalizar?" está abierto
    var confirmingFinishId by rememberSaveable { mutableStateOf<String?>(null) }

    MessageEffect(state.userMessage, snackbarHostState, viewModel::onMessageShown)

    Scaffold(
        containerColor = WarmBackground,
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ScreenHeader(title = "Mis Eventos", subtitle = "Gestión de actividades comunitarias", onBack = onBack) {
                    Surface(onClick = onCreateEvent, shape = RoundedCornerShape(12.dp), color = Terracotta) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("Crear", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            item { SummaryCard(state) }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MyEventsTab.entries) { tab ->
                        FilterPill(
                            text = tab.label,
                            count = state.count(tab),
                            isSelected = state.tab == tab,
                            onClick = { viewModel.selectTab(tab) }
                        )
                    }
                }
            }

            if (state.events.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.AutoMirrored.Outlined.EventNote,
                        title = if (state.tab == MyEventsTab.ALL) "Aún no has creado eventos" else "No hay eventos en esta pestaña",
                        message = "Propón un evento para tu comunidad; un moderador lo revisará antes de publicarlo."
                    )
                }
            }

            items(state.events, key = { it.id }) { event ->
                val attendees = state.attendees[event.id].orEmpty()
                MyEventCard(
                    event = event,
                    checkIns = attendees.count { it.checkedIn },
                    isConfirmingFinish = confirmingFinishId == event.id,
                    onOpen = { onOpenEvent(event.id) },
                    onEdit = { onEditEvent(event.id) },
                    onDelete = { dialog = MyEventDialog.Delete(event) },
                    onShowQr = { dialog = MyEventDialog.Qr(event) },
                    onShowAttendees = { dialog = MyEventDialog.Attendees(event) },
                    onSendNotice = { dialog = MyEventDialog.Notice(event) },
                    onAskFinish = { confirmingFinishId = event.id },
                    onCancelFinish = { confirmingFinishId = null },
                    onConfirmFinish = {
                        confirmingFinishId = null
                        viewModel.finishEvent(event.id)
                    }
                )
            }

            item { ProposalTip(onCreate = onCreateEvent) }
        }
    }

    when (val current = dialog) {
        is MyEventDialog.Qr -> QrDialog(event = current.event, onDismiss = { dialog = null })
        is MyEventDialog.Attendees -> AttendeesDialog(
            event = current.event,
            attendees = state.attendees[current.event.id].orEmpty(),
            onDismiss = { dialog = null }
        )
        is MyEventDialog.Notice -> NoticeDialog(
            event = current.event,
            onSend = { message ->
                viewModel.sendNotice(current.event.id, message)
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        is MyEventDialog.Delete -> ConfirmDialog(
            title = "¿Eliminar este evento?",
            message = "«${current.event.title}» se borrará junto con sus comentarios e inscripciones. Esta acción no se puede deshacer.",
            confirmText = "Eliminar",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteEvent(current.event.id)
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        null -> Unit
    }
}

@Composable
private fun SummaryCard(state: MyEventsUiState) {
    val user = state.user ?: return
    ContentCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UserAvatar(user, size = 48.dp, containerColor = SecondaryFixed, contentColor = OnSecondaryFixedVariant)
            Column(modifier = Modifier.weight(1f)) {
                Text("${user.level.label} • Nivel ${user.level.number}", color = Sage, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${state.count(MyEventsTab.ALL)} eventos gestionados • ${user.city.substringBefore(",")}",
                    color = WarmText,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            state.approvalRate?.let { rate ->
                Column(horizontalAlignment = Alignment.End) {
                    Text("$rate%", color = TertiaryColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Aprobación", color = MutedText, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun MyEventCard(
    event: CommunityEvent,
    checkIns: Int,
    isConfirmingFinish: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowQr: () -> Unit,
    onShowAttendees: () -> Unit,
    onSendNotice: () -> Unit,
    onAskFinish: () -> Unit,
    onCancelFinish: () -> Unit,
    onConfirmFinish: () -> Unit
) {
    ContentCard(spacing = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(event.status)
            Text(
                "  ${EventFormat.relative(event.createdAt)}",
                color = MutedText,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
            EventImage(event, Modifier.size(76.dp).clip(RoundedCornerShape(12.dp)))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(event.title, color = WarmText, fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Terracotta, modifier = Modifier.size(14.dp))
                    Text(
                        " ${event.address.substringBefore(",")} • ${EventFormat.shortDateTime(event.startsAt)}",
                        color = MutedText,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Groups, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                    Text(
                        " ${event.attendanceCount} inscritos" + (event.capacity?.let { " de $it" } ?: " • Cupo abierto"),
                        color = MutedText,
                        fontSize = 12.sp
                    )
                }
            }
        }

        when (event.status) {
            EventStatus.PENDING -> {
                InfoBox(
                    icon = Icons.Outlined.Info,
                    text = "Enviado ${EventFormat.relative(event.createdAt).lowercase()}. Un moderador está revisando que el evento sea real y apropiado para la comunidad.",
                    container = SurfaceContainerLow,
                    content = MutedText
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SecondaryAction("Editar", Icons.Outlined.Edit, onEdit, Modifier.weight(1f))
                    SecondaryAction("Detalles", Icons.Outlined.Visibility, onOpen, Modifier.weight(1f))
                    DeleteButton(onDelete)
                }
            }
            EventStatus.VERIFIED -> {
                Surface(onClick = onShowQr, shape = RoundedCornerShape(14.dp), color = Terracotta, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.QrCode2, contentDescription = null, tint = Color.White)
                        Text("  Mostrar QR de Asistencia", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryAction("Gestionar inscritos", Icons.Outlined.Groups, onShowAttendees, Modifier.weight(1f))
                    SecondaryAction("Enviar aviso", Icons.Outlined.Campaign, onSendNotice, Modifier.weight(1f))
                }
                if (isConfirmingFinish) {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(SurfaceContainer, RoundedCornerShape(12.dp)).padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = null, tint = Terracotta, modifier = Modifier.size(20.dp))
                            Column {
                                Text("¿Estás seguro de que deseas finalizar este evento?", color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Esta acción cerrará las confirmaciones de asistencia.", color = MutedText, fontSize = 12.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = onCancelFinish) { Text("Cancelar", color = MutedText) }
                            Surface(onClick = onConfirmFinish, shape = RoundedCornerShape(10.dp), color = Terracotta) {
                                Text("Confirmar", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                            }
                        }
                    }
                } else {
                    Surface(
                        onClick = onAskFinish,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, SurfaceContainerHigh),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Sage, modifier = Modifier.size(18.dp))
                            Text("  Marcar como finalizado", color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SecondaryAction("Ver evento", Icons.Outlined.Visibility, onOpen, Modifier.weight(1f))
                    SecondaryAction("Editar", Icons.Outlined.Edit, onEdit, Modifier.weight(1f))
                    DeleteButton(onDelete)
                }
            }
            EventStatus.REJECTED -> {
                Column(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFFFDAD6).copy(alpha = 0.6f), RoundedCornerShape(12.dp)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = ErrorColor, modifier = Modifier.size(20.dp))
                        Text("Motivo del rechazo", color = ErrorColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(event.rejectionReason.orEmpty(), color = WarmText, fontSize = 13.sp, lineHeight = 18.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(onClick = onEdit, shape = RoundedCornerShape(12.dp), color = Terracotta, modifier = Modifier.weight(1f).height(44.dp)) {
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("  Modificar y reenviar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    DeleteButton(onDelete)
                }
            }
            EventStatus.FINISHED -> {
                Row(
                    modifier = Modifier.fillMaxWidth().background(SecondaryFixed.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Sage)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Finalizado con éxito", color = OnSecondaryFixedVariant, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "$checkIns check-ins con QR • +${ReputationPoints.EVENT_FINISHED} pts de reputación",
                            color = OnSecondaryFixedVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                SecondaryAction("Ver evento y comentarios", Icons.AutoMirrored.Outlined.ArrowForward, onOpen, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun InfoBox(icon: ImageVector, text: String, container: Color, content: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().background(container, RoundedCornerShape(12.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Text(text, color = content, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun SecondaryAction(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = SurfaceContainer, modifier = modifier.heightIn(min = 44.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = WarmText, modifier = Modifier.size(18.dp))
            Text(" $text", color = WarmText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun DeleteButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp).background(Color(0xFFFFDAD6).copy(alpha = 0.5f), CircleShape)) {
        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Eliminar evento", tint = ErrorColor)
    }
}

@Composable
private fun ProposalTip(onCreate: () -> Unit) {
    Surface(onClick = onCreate, shape = RoundedCornerShape(16.dp), color = TertiaryFixed.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = OnTertiaryContainer)
            Text(
                "¿Deseas proponer otro evento? Los moderadores suelen revisarlo en menos de 48 horas.",
                color = OnTertiaryContainer,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = OnTertiaryContainer)
        }
    }
}

@Composable
private fun QrDialog(event: CommunityEvent, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("QR de asistencia", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(event.title, color = MutedText, fontSize = 14.sp, textAlign = TextAlign.Center)
                QrCode(content = event.checkInCode, modifier = Modifier.size(220.dp))
                Text(event.checkInCode, color = WarmText, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(
                    "Los asistentes escanean este código (o lo escriben) en Check-in para registrar su asistencia y ganar puntos.",
                    color = MutedText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun AttendeesDialog(event: CommunityEvent, attendees: List<AttendeeItem>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Inscritos", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "${event.attendanceCount} personas confirmadas en total. Con cuenta en AKJ Events: ${attendees.size} • " +
                        "check-in: ${attendees.count { it.checkedIn }}.",
                    color = MutedText,
                    fontSize = 13.sp
                )
                HorizontalDivider(color = SurfaceContainerHigh)
                if (attendees.isEmpty()) {
                    Text("Aún no hay personas inscritas con cuenta.", color = MutedText, fontSize = 13.sp)
                }
                attendees.forEach { attendee ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (attendee.user != null) UserAvatar(attendee.user, size = 32.dp) else Box(Modifier.size(32.dp).background(SurfaceContainerHigh, CircleShape))
                        Text(attendee.user?.name ?: "Cuenta eliminada", color = WarmText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text(
                            if (attendee.checkedIn) "Asistió" else "Confirmado",
                            color = if (attendee.checkedIn) Sage else MutedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun NoticeDialog(event: CommunityEvent, onSend: (String) -> Unit, onDismiss: () -> Unit) {
    var message by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Enviar aviso", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Las personas inscritas en «${event.title}» lo verán en sus notificaciones.", color = MutedText, fontSize = 13.sp)
                TextField(
                    value = message,
                    onValueChange = { message = it.take(300) },
                    placeholder = { Text("Ej: El punto de encuentro cambia a la entrada norte") },
                    minLines = 3,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow,
                        focusedIndicatorColor = Terracotta
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSend(message) }, enabled = message.isNotBlank()) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Enviar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
