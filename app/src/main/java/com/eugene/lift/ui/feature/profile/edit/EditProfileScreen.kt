package com.eugene.lift.ui.feature.profile.edit

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.eugene.lift.R
import com.eugene.lift.ui.event.UiEvent
import com.eugene.lift.ui.util.toMessage
import java.io.File

@Composable
fun EditProfileRoute(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Consume ViewModel events (snackbar errors)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(
                    event.error.toMessage(context)
                )
            }
        }
    }

    // --- Camera capture state ---
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraImageUri
        if (success && uri != null) {
            viewModel.uploadAvatar(uri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val tempFile = File(context.cacheDir, "camera").also { it.mkdirs() }
                .let { File(it, "temp_avatar.jpg") }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // --- Gallery picker ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) viewModel.uploadAvatar(uri)
    }

    // --- Image source dialog ---
    var showImageSourceDialog by remember { mutableStateOf(false) }
    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onCamera = {
                showImageSourceDialog = false
                val hasCameraPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasCameraPermission) {
                    val tempFile = File(context.cacheDir, "camera").also { it.mkdirs() }
                        .let { File(it, "temp_avatar.jpg") }
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        tempFile
                    )
                    cameraImageUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onGallery = {
                showImageSourceDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    EditProfileScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onDisplayNameChange = viewModel::updateDisplayName,
        onBioChange = viewModel::updateBio,
        onColorChange = viewModel::updateAvatarColor,
        onUsernameChange = viewModel::updateUsername,
        onSuggestionClick = viewModel::applySuggestion,
        onRefreshSuggestions = viewModel::refreshSuggestions,
        onAvatarClick = { showImageSourceDialog = true },
        onSave = {
            viewModel.saveProfile()
            onNavigateBack()
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    uiState: EditProfileUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onDisplayNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onRefreshSuggestions: () -> Unit,
    onAvatarClick: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSave,
                        enabled = uiState.usernameError == null && !uiState.isUploadingAvatar
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.action_save),
                            tint = if (uiState.usernameError == null && !uiState.isUploadingAvatar)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Avatar Section ──────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarPicker(
                        avatarUrl = uiState.avatarUrl,
                        avatarColor = uiState.avatarColor,
                        displayName = uiState.displayName,
                        isUploading = uiState.isUploadingAvatar,
                        onClick = onAvatarClick
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "@${uiState.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Avatar Color Picker ─────────────────────────────────────────
            item {
                Text(
                    text = stringResource(R.string.profile_avatar_color),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AVATAR_COLORS.forEach { color ->
                        val isSelected = color == uiState.avatarColor
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color.toColorInt()))
                                .then(
                                    if (isSelected) Modifier.border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    ) else Modifier
                                )
                                .clickable { onColorChange(color) }
                        )
                    }
                }
            }

            // ── Username Field with Suggestions ────────────────────────────
            item {
                UsernameSection(
                    username = uiState.username,
                    usernameError = uiState.usernameError,
                    suggestions = uiState.usernameSuggestions,
                    onUsernameChange = onUsernameChange,
                    onSuggestionClick = onSuggestionClick,
                    onRefreshSuggestions = onRefreshSuggestions
                )
            }

            // ── Display Name ────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.displayName,
                    onValueChange = onDisplayNameChange,
                    label = { Text(stringResource(R.string.profile_display_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Bio ─────────────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.bio,
                    onValueChange = onBioChange,
                    label = { Text(stringResource(R.string.profile_bio)) },
                    placeholder = { Text(stringResource(R.string.profile_bio_hint)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun AvatarPicker(
    avatarUrl: String?,
    avatarColor: String,
    displayName: String,
    isUploading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // The outermost box dictates total size and handles clicks.
    // It is NOT clipped so the camera icon can bleed over the edge.
    Box(
        modifier = modifier
            .size(120.dp)
            .clickable(enabled = !isUploading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Inner box handles the circular avatar and its background color
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    color = runCatching { Color(avatarColor.toColorInt()) }.getOrDefault(Color(0xFF6200EE))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null && !isUploading) {
                // Coil async image for locally-stored avatar
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!isUploading) {
                Text(
                    text = displayName.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            }
        }

        // Camera icon overlay positioned on the bottom edge, unclipped
        if (!isUploading) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape), // adding border to pop against the image
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = stringResource(R.string.cd_change_avatar),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UsernameSection(
    username: String,
    usernameError: String?,
    suggestions: List<String>,
    onUsernameChange: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onRefreshSuggestions: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text(stringResource(R.string.profile_username_label)) },
            prefix = { Text(stringResource(R.string.profile_username_prefix)) },
            singleLine = true,
            isError = usernameError != null,
            supportingText = {
                if (usernameError != null) {
                    Text(usernameError, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.profile_username_suggestions),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(
                onClick = onRefreshSuggestions,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.cd_refresh_suggestions),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                SuggestionChip(
                    onClick = { onSuggestionClick(suggestion) },
                    label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }
    }
}

@Composable
private fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_change_photo_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Camera option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onCamera)
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(stringResource(R.string.profile_take_photo), style = MaterialTheme.typography.bodyLarge)
                }
                // Gallery option — using material icon alias
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onGallery)
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(stringResource(R.string.profile_choose_from_gallery), style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.component_cancel)) }
        }
    )
}

// ── Constants ────────────────────────────────────────────────────────────────

private val AVATAR_COLORS = listOf(
    "#F44336", "#E91E63", "#9C27B0", "#673AB7",
    "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
    "#009688", "#4CAF50", "#8BC34A", "#FF9800",
    "#FF5722", "#795548", "#607D8B"
)
