package com.eugene.lift.ui.feature.workout.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eugene.lift.R
import com.eugene.lift.domain.model.Folder
import com.eugene.lift.ui.util.FolderColors
import com.eugene.lift.ui.util.toColor

@Composable
fun FolderRow(
    folders: List<Folder>,
    currentFolderId: String?,
    onFolderClick: (String) -> Unit,
    onBackToRoot: () -> Unit,
    onCreateFolderClick: () -> Unit,
    onDeleteFolder: (String) -> Unit // Opcional: Para borrar con long press
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentFolderId != null) {
            item {
                IconButton(
                    onClick = onBackToRoot,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            }
        }

        items(folders, key = { it.id }) { folder ->
            FolderChip(
                folder = folder,
                isSelected = folder.id == currentFolderId,
                onClick = { onFolderClick(folder.id) }
            )
        }

        if (currentFolderId == null) {
            item {
                AssistChip(
                    onClick = onCreateFolderClick,
                    label = { Text(stringResource(R.string.folder_new)) },
                    leadingIcon = { Icon(Icons.Default.CreateNewFolder, null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
fun FolderChip(
    folder: Folder,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = folder.color.toColor()

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = folder.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(FolderColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.folder_new_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.folder_name_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.folder_color_label), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Selector de Color Simple
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FolderColors) { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colorHex.toColor())
                                .border(
                                    width = if (selectedColor == colorHex) 2.dp else 0.dp,
                                    color = if (selectedColor == colorHex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if(name.isNotBlank()) onCreate(name, selectedColor) }) {
                Text(stringResource(R.string.folder_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.folder_cancel)) }
        }
    )
}

@Composable
fun MoveToFolderDialog(
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onSelectFolder: (String?) -> Unit // Null = Mover a raíz
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.folder_move_to_title)) },
        text = {
            LazyColumn {
                item {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.folder_root)) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Folder,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.clickable { onSelectFolder(null) }
                    )
                }
                items(folders) { folder ->
                    ListItem(
                        headlineContent = { Text(folder.name) },
                        leadingContent = { Icon(Icons.Default.Folder, null, tint = folder.color.toColor()) },
                        modifier = Modifier.clickable { onSelectFolder(folder.id) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.folder_cancel)) }
        }
    )
}