package com.eugene.lift.ui.feature.workout

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.model.Folder
import com.eugene.lift.domain.model.WorkoutTemplate
import kotlinx.coroutines.launch
import com.eugene.lift.ui.feature.workout.components.FolderRow
import com.eugene.lift.ui.feature.workout.components.CreateFolderDialog
import com.eugene.lift.ui.feature.workout.components.MoveToFolderDialog

@Composable
fun WorkoutRoute(
    onNavigateToEdit: (String?) -> Unit,
    onTemplateClick: (String) -> Unit,
    onStartWorkoutClick: (String) -> Unit,
    onStartEmptyClick: (String?) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val currentFolderId by viewModel.currentFolderId.collectAsStateWithLifecycle()
    WorkoutScreen(
        templates = templates,
        selectedTab = selectedTab,
        folders = folders,
        currentFolderId = currentFolderId,
        onTabSelected = viewModel::onTabSelected,
        onSelectFolder = viewModel::selectFolder,
        onCreateFolder = viewModel::createFolder,
        onMoveTemplate = viewModel::moveTemplate,
        onCreateClick = { onNavigateToEdit(null) },
        onEditClick = { onNavigateToEdit(it.id) },
        onTemplateClick = { onTemplateClick(it.id) },
        onStartWorkoutClick = { onStartWorkoutClick(it.id) },
        onArchiveClick = viewModel::archiveTemplate,
        onDeleteClick = { viewModel.deleteTemplate(it.id) },
        onDuplicateClick = { template ->
            viewModel.duplicateTemplate(template.id)
        },
        onShareClick = {
            Toast.makeText(context, context.getString(R.string.workout_routine_copied), Toast.LENGTH_SHORT).show()
        },
        onStartEmptyClick = { onStartEmptyClick(null) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    templates: List<WorkoutTemplate>?,
    selectedTab: Int,
    folders: List<Folder>,
    currentFolderId: String?,
    onSelectFolder: (String?) -> Unit,
    onCreateFolder: (String, String) -> Unit,
    onMoveTemplate: (WorkoutTemplate, String?) -> Unit,
    onTabSelected: (Int) -> Unit,
    onCreateClick: () -> Unit,
    onEditClick: (WorkoutTemplate) -> Unit,
    onTemplateClick: (WorkoutTemplate) -> Unit,
    onStartWorkoutClick: (WorkoutTemplate) -> Unit,
    onArchiveClick: (WorkoutTemplate) -> Unit,
    onDeleteClick: (WorkoutTemplate) -> Unit,
    onDuplicateClick: (WorkoutTemplate) -> Unit,
    onShareClick: () -> Unit,
    onStartEmptyClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) { onTabSelected(pagerState.currentPage) }
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var templateToMove by remember { mutableStateOf<WorkoutTemplate?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.title_workout)) },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text(stringResource(R.string.tab_my_routines)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text(stringResource(R.string.tab_archived)) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = onCreateClick) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->

        if (templates == null) {
            WorkoutSkeletonList(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->

            val currentTemplates = if (page == 0) {
                templates.filter { !it.isArchived }
            } else {
                templates.filter { it.isArchived }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (page == 0) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            FolderRow(
                                folders = folders,
                                currentFolderId = currentFolderId,
                                onFolderClick = { onSelectFolder(it) },
                                onBackToRoot = { onSelectFolder(null) }, // Null = Ir a raíz
                                onCreateFolderClick = { showCreateFolderDialog = true },
                                onDeleteFolder = { /* TODO: Implementar borrado si quieres */ }
                            )
                        }
                    }
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            QuickStartCard(onClick = onStartEmptyClick)
                        }
                    }
                    if (currentTemplates.isNotEmpty()) {
                        item {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                    }
                }

                if (currentTemplates.isNotEmpty()) {
                    items(count = currentTemplates.size, key = { currentTemplates[it].id }) { index ->
                        val template = currentTemplates[index]
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            TemplateItemCard(
                                template = template,
                                onClick = { onTemplateClick(template) },
                                onEdit = { onEditClick(template) },
                                onArchive = { onArchiveClick(template) },
                                onDelete = { onDeleteClick(template) },
                                onDuplicate = { onDuplicateClick(template) },
                                onShare = { onShareClick() },
                                onMove = { templateToMove = template },
                                onStartWorkout = { onStartWorkoutClick(template) }
                            )
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (page == 0) stringResource(R.string.empty_routines)
                                else stringResource(R.string.empty_archived),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        if (showCreateFolderDialog) {
            CreateFolderDialog(
                onDismiss = { showCreateFolderDialog = false },
                onCreate = { name, color ->
                    onCreateFolder(name, color)
                    showCreateFolderDialog = false
                }
            )
        }

        if (templateToMove != null) {
            MoveToFolderDialog(
                folders = folders,
                onDismiss = { templateToMove = null },
                onSelectFolder = { folderId ->
                    onMoveTemplate(templateToMove!!, folderId)
                    templateToMove = null
                }
            )
        }
    }
}

@Composable
fun TemplateItemCard(
    template: WorkoutTemplate,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onMove: () -> Unit,
    onStartWorkout: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(template.name, style = MaterialTheme.typography.titleMedium) },
                supportingContent = {
                    val count = template.exercises.size
                    Text(stringResource(R.string.exercise_count, count))
                },
                trailingContent = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_edit)) },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(if (template.isArchived) R.string.action_unarchive else R.string.action_archive)) },
                            onClick = { showMenu = false; onArchive() },
                            leadingIcon = { Icon(if(template.isArchived) Icons.Default.Unarchive else Icons.Default.Archive, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_move_to_folder)) },
                            onClick = { showMenu = false; onMove() },
                            leadingIcon = { Icon(Icons.Default.Folder, null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_duplicate)) },
                            onClick = { showMenu = false; onDuplicate() },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_share)) },
                            onClick = {
                                showMenu = false
                                val textToCopy = context.getString(R.string.share_routine_text, template.name, template.exercises.size)
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                onShare() // Callback por si quieres mostrar un Toast/Snackbar
                            },
                            leadingIcon = { Icon(Icons.Default.Share, null) }
                        )
                    }
                }
            }
        )

        // Exercise list with set counts
        if (template.exercises.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                template.exercises.forEach { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = exercise.exercise.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.template_sets_count, exercise.targetSets),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Start Workout Button
        androidx.compose.material3.Button(
            onClick = onStartWorkout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(R.string.template_detail_start_routine))
        }
    }
    }
}

@Composable
fun QuickStartCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.quick_start_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.quick_start_subtitle),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun WorkoutSkeletonList(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(6) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .size(height = 18.dp, width = 1.dp)
                            .fillMaxSize()
                            .padding(0.dp)
                            .fillMaxSize()
                            .padding(0.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .size(height = 14.dp, width = 1.dp)
                            .fillMaxSize()
                            .padding(0.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(height = 36.dp, width = 1.dp)
                            .fillMaxSize()
                            .padding(0.dp)
                    )
                }
            }
        }
    }
}
