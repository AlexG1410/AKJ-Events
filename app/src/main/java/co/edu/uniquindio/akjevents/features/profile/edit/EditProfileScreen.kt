package co.edu.uniquindio.akjevents.features.profile.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.component.ConfirmDialog
import co.edu.uniquindio.akjevents.core.component.FormFieldStyle
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.ImagePickerSheet
import co.edu.uniquindio.akjevents.core.component.PrimaryButton
import co.edu.uniquindio.akjevents.core.component.RequestResultEffect
import co.edu.uniquindio.akjevents.core.component.ScreenHeader
import co.edu.uniquindio.akjevents.core.component.UserAvatar
import co.edu.uniquindio.akjevents.core.component.rememberImagePicker
import co.edu.uniquindio.akjevents.core.component.style
import co.edu.uniquindio.akjevents.core.theme.ErrorColor
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.OutlineColor
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerHigh
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.core.util.labelRes
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.User
import java.util.Locale
import kotlinx.coroutines.launch

private val ProfileFieldStyle = FormFieldStyle(
    height = 52.dp,
    shape = RoundedCornerShape(16.dp),
    containerColor = SurfaceContainerLow,
    focusedContainerColor = Color.White,
    borderColor = Color.Transparent,
    focusedBorderColor = Terracotta,
    textStyle = TextStyle(fontSize = 15.sp, color = WarmText),
    placeholderColor = OutlineColor,
    iconColor = MutedText,
    iconSize = 20.dp,
    iconStartPadding = 16.dp,
    textStartPadding = 48.dp
)

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onPickLocation: (PickedLocation) -> Unit,
    pickedLocation: PickedLocation?,
    onPickedLocationConsumed: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Cámara o galería (guía 15); si se niega el permiso de cámara, la galería sigue disponible
    val imagePicker = rememberImagePicker(
        onImagePicked = { viewModel.onPhotoPicked(it.toString()) },
        onCameraDenied = {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.camera_permission_required)) }
        }
    )

    LaunchedEffect(pickedLocation) {
        if (pickedLocation != null) {
            viewModel.onLocationPicked(pickedLocation)
            onPickedLocationConsumed()
        }
    }
    LaunchedEffect(state.isAccountDeleted) {
        if (state.isAccountDeleted) onAccountDeleted()
    }
    RequestResultEffect(
        result = state.result,
        snackbarHostState = snackbarHostState,
        onFailureShown = viewModel::onFailureShown,
        onSuccess = onSaved
    )

    val user = state.user
    Scaffold(
        containerColor = WarmBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (user == null) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenHeader(title = stringResource(R.string.edit_profile_title), kicker = stringResource(R.string.edit_profile_kicker), onBack = onBack) {
                Surface(onClick = onBack, shape = CircleShape, color = SurfaceContainer) {
                    Text(stringResource(R.string.action_cancel), color = WarmText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfilePhoto(user = user.copy(profilePictureUrl = state.photo), onChange = imagePicker::open)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = Sage, modifier = Modifier.size(18.dp))
                    Text(
                        if (user.isModerator) stringResource(R.string.edit_profile_moderator) else stringResource(R.string.edit_profile_verified, stringResource(user.level.labelRes)),
                        color = WarmText,
                        fontSize = 14.sp
                    )
                }
            }

            Field(stringResource(R.string.register_full_name)) {
                FormTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = stringResource(R.string.register_full_name_placeholder),
                    leadingIcon = Icons.Outlined.Person,
                    style = ProfileFieldStyle,
                    isError = state.nameError != null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                )
                state.nameError?.let { ErrorText(it.asString()) }
            }
            Field(stringResource(R.string.register_email)) {
                FormTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = stringResource(R.string.register_email_placeholder),
                    leadingIcon = Icons.Outlined.Mail,
                    style = ProfileFieldStyle,
                    isError = state.emailError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
                )
                state.emailError?.let { ErrorText(it.asString()) }
            }
            Field(stringResource(R.string.edit_profile_location)) {
                CityPicker(city = state.city, cities = viewModel.cities, onSelect = viewModel::onCityChange)
                MapLocationRow(
                    address = state.address,
                    location = state.location,
                    onClick = { state.location?.let { onPickLocation(PickedLocation(state.address, it)) } }
                )
            }

            if (!user.isModerator) {
                InterestsSection(selected = state.interests, onToggle = viewModel::toggleInterest)
            }

            PrimaryButton(
                text = stringResource(R.string.create_save_changes),
                onClick = viewModel::save,
                leadingIcon = Icons.Outlined.Check,
                isLoading = state.isBusy,
                loadingText = stringResource(R.string.create_saving),
                containerColor = Terracotta,
                height = 52.dp
            )

            if (!user.isModerator) {
                AccountZone(onDelete = { confirmDelete = true })
            }
        }
    }

    ImagePickerSheet(imagePicker)

    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.edit_profile_delete_title),
            message = stringResource(R.string.edit_profile_delete_message),
            confirmText = stringResource(R.string.edit_profile_delete_confirm),
            isDestructive = true,
            onConfirm = {
                confirmDelete = false
                viewModel.deleteAccount()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

/** Foto circular con el botón de cámara para cambiarla (mockup 13). */
@Composable
private fun ProfilePhoto(user: User, onChange: () -> Unit) {
    Box {
        UserAvatar(user, size = 112.dp, onClick = onChange)
        Surface(
            onClick = onChange,
            shape = CircleShape,
            color = Terracotta,
            shadowElevation = 2.dp,
            modifier = Modifier.align(Alignment.BottomEnd).size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = stringResource(R.string.edit_profile_change_photo), tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun ErrorText(text: String) = Text(text, color = ErrorColor, fontSize = 12.sp)

@Composable
private fun CityPicker(city: String, cities: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(onClick = { expanded = true }, shape = RoundedCornerShape(16.dp), color = SurfaceContainerLow, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MutedText, modifier = Modifier.size(20.dp))
                Text(city.ifBlank { stringResource(R.string.edit_profile_choose_city) }, color = WarmText, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.ExpandMore, contentDescription = stringResource(R.string.edit_profile_pick_city), tint = MutedText)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            cities.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelect(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun MapLocationRow(address: String, location: Location?, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = SecondaryFixed.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.Map, contentDescription = null, tint = OnSecondaryFixedVariant)
            Column(modifier = Modifier.weight(1f)) {
                Text(address, color = OnSecondaryFixedVariant, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(
                    location?.let {
                        stringResource(
                            R.string.edit_profile_adjust_on_map,
                            stringResource(R.string.detail_coordinates, "%.4f".format(Locale.US, it.latitude), "%.4f".format(Locale.US, it.longitude))
                        )
                    } ?: stringResource(R.string.edit_profile_select_on_map),
                    color = OnSecondaryFixedVariant,
                    fontSize = 12.sp
                )
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = OnSecondaryFixedVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestsSection(selected: Set<EventCategory>, onToggle: (EventCategory) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            Text(stringResource(R.string.edit_profile_interests), color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(pluralStringResource(R.plurals.interests_active, selected.size, selected.size), color = Sage, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EventCategory.entries.forEach { category ->
                val isSelected = category in selected
                Surface(onClick = { onToggle(category) }, shape = CircleShape, color = if (isSelected) Terracotta else SurfaceContainerHigh) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(category.style.icon, contentDescription = null, tint = if (isSelected) Color.White else WarmText, modifier = Modifier.size(18.dp))
                        Text(stringResource(category.labelRes), color = if (isSelected) Color.White else WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountZone(onDelete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceContainerHigh)
            Text(stringResource(R.string.edit_profile_account_zone), color = MutedText, fontSize = 12.sp, letterSpacing = 1.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceContainerHigh)
        }
        Column(
            modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(16.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(40.dp).background(Color(0xFFFFDAD6), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = ErrorColor)
                }
                Column {
                    Text(stringResource(R.string.edit_profile_identity), color = WarmText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.edit_profile_delete_hint),
                        color = MutedText,
                        fontSize = 13.sp
                    )
                }
            }
            Surface(onClick = onDelete, shape = RoundedCornerShape(12.dp), color = SurfaceContainer, modifier = Modifier.fillMaxWidth().height(46.dp)) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = ErrorColor, modifier = Modifier.size(18.dp))
                    Text("  " + stringResource(R.string.edit_profile_delete_account), color = ErrorColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
