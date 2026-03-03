package com.eugene.lift.ui.feature.workout

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import com.eugene.lift.ui.dragdrop.DragContainer
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.model.Folder
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.ui.feature.workout.components.CreateFolderDialog
import com.eugene.lift.ui.feature.workout.components.FolderChip
import com.eugene.lift.ui.feature.workout.components.FolderRow
import com.eugene.lift.ui.feature.workout.components.MoveToFolderDialog
import kotlinx.coroutines.flow.collectLatest


@Composable
fun WorkoutRoute(
    onNavigateToEdit: (String?) -> Unit,
    onTemplateClick: (String) -> Unit,
    onStartWorkoutClick: (String) -> Unit,
    onStartEmptyClick: (String?) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val onEventUpdated by rememberUpdatedState(newValue = { event: WorkoutUiEvent ->
        when (event) {
            WorkoutUiEvent.AddTemplateClicked -> onNavigateToEdit(null)
            is WorkoutUiEvent.TemplateEditClicked -> onNavigateToEdit(event.templateId)
            is WorkoutUiEvent.TemplateClicked -> onTemplateClick(event.templateId)
            is WorkoutUiEvent.TemplateStartClicked -> onStartWorkoutClick(event.templateId)
            WorkoutUiEvent.StartEmptyClicked -> onStartEmptyClick(null)
            is WorkoutUiEvent.TemplateShared -> {
                val template = uiState.templates.find { it.id == event.templateId } ?: return@rememberUpdatedState
                val shareText = context.getString(
                    R.string.share_routine_text,
                    template.name,
                    template.exercises.size
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, null))
            }
            else -> viewModel.onEvent(event)
        }
    })

    WorkoutScreen(
        uiState = uiState,
        onEvent = onEventUpdated
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    uiState: WorkoutUiState,
    onEvent: (WorkoutUiEvent) -> Unit
) {
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var templateToMove by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var folderToDelete by remember { mutableStateOf<com.eugene.lift.domain.model.Folder?>(null) }
    var templateToDelete by remember { mutableStateOf<WorkoutTemplate?>(null) }

    val pagerState = rememberPagerState(initialPage = uiState.selectedTab, pageCount = { 2 })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            if (page != uiState.selectedTab) {
                onEvent(WorkoutUiEvent.TabSelected(page))
            }
        }
    }

    DragContainer(dragState = uiState.dragState) {
        WorkoutContent(
            uiState = uiState,
            pagerState = pagerState,
            onEvent = onEvent,
            onCreateFolderClick = { showCreateFolderDialog = true },
            onMoveRequest = { templateToMove = it },
            onFolderDeleteRequest = { folderId -> folderToDelete = uiState.folders.find { it.id == folderId } },
            onTemplateDeleteRequest = { templateToDelete = it }
        )
    }

    val overlayState = remember(showCreateFolderDialog, templateToMove, pagerState.currentPage) {
        WorkoutOverlayState(
            showCreateFolderDialog = showCreateFolderDialog,
            templateToMove = templateToMove,
            showFab = pagerState.currentPage == 0
        )
    }

    WorkoutOverlays(
        overlayState = overlayState,
        folders = uiState.folders,
        onAction = { action ->
            when (action) {
                WorkoutOverlayAction.AddTemplate -> onEvent(WorkoutUiEvent.AddTemplateClicked)
                WorkoutOverlayAction.DismissCreateFolder -> showCreateFolderDialog = false
                is WorkoutOverlayAction.CreateFolder -> {
                    onEvent(WorkoutUiEvent.FolderCreated(action.name, action.color))
                    showCreateFolderDialog = false
                }
                WorkoutOverlayAction.ClearMove -> templateToMove = null
                is WorkoutOverlayAction.SelectFolder -> {
                    templateToMove?.let { template -> onEvent(WorkoutUiEvent.TemplateMoved(template.id, action.folderId)) }
                    templateToMove = null
                }
                WorkoutOverlayAction.ShowCreateFolder -> showCreateFolderDialog = true
            }
        }
    )

    if (folderToDelete != null) {
        val folder = folderToDelete!!
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(WorkoutUiEvent.FolderDeleted(folder.id))
                    folderToDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) { Text(stringResource(R.string.folder_cancel)) }
            },
            title = { Text(stringResource(R.string.folder_delete_title, folder.name)) },
            text = { Text(stringResource(R.string.folder_delete_message)) }
        )
    }

    if (templateToDelete != null) {
        val template = templateToDelete!!
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(WorkoutUiEvent.TemplateDeleted(template.id))
                        templateToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToDelete = null }) {
                    Text(stringResource(R.string.folder_cancel))
                }
            },
            title = { Text(stringResource(R.string.template_delete_title)) },
            text = { Text(stringResource(R.string.template_delete_message)) }
        )
    }
}

@Composable
private fun WorkoutContent(
    uiState: WorkoutUiState,
    pagerState: PagerState,
    onEvent: (WorkoutUiEvent) -> Unit,
    onCreateFolderClick: () -> Unit,
    onMoveRequest: (WorkoutTemplate) -> Unit,
    onFolderDeleteRequest: (String) -> Unit,
    onTemplateDeleteRequest: (WorkoutTemplate) -> Unit
) {
    LaunchedEffect(uiState.selectedTab) {
        if (pagerState.currentPage != uiState.selectedTab) {
            pagerState.animateScrollToPage(uiState.selectedTab)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WorkoutTopBar(
            selectedTab = pagerState.currentPage,
            onTabSelected = { index ->
                if (index != pagerState.currentPage) {
                    onEvent(WorkoutUiEvent.TabSelected(index))
                }
            },
            onToggleReorderMode = { onEvent(WorkoutUiEvent.ToggleReorderMode) }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (uiState.isLoading) {
                WorkoutSkeletonList(modifier = Modifier.fillMaxSize())
            } else {
                val pageTemplates = remember(uiState.templates, page) {
                    if (page == 0) uiState.templates.filter { !it.isArchived } else uiState.templates.filter { it.isArchived }
                }

                WorkoutTemplatesSection(
                    selectedPage = page,
                    currentTemplates = pageTemplates,
                    uiState = uiState,
                    onEvent = onEvent,
                    onCreateFolderClick = onCreateFolderClick,
                    onMoveRequest = onMoveRequest,
                    onFolderDeleteRequest = onFolderDeleteRequest,
                    onTemplateDeleteRequest = onTemplateDeleteRequest
                )
            }
        }
    }
}

@Composable
private fun WorkoutOverlays(
    overlayState: WorkoutOverlayState,
    folders: List<Folder>,
    onAction: (WorkoutOverlayAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (overlayState.showFab) {
            FloatingActionButton(
                onClick = { onAction(WorkoutOverlayAction.AddTemplate) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        if (overlayState.showCreateFolderDialog) {
            CreateFolderDialog(
                onDismiss = { onAction(WorkoutOverlayAction.DismissCreateFolder) },
                onCreate = { name, color ->
                    onAction(WorkoutOverlayAction.CreateFolder(name, color))
                }
            )
        }

        if (overlayState.templateToMove != null) {
            MoveToFolderDialog(
                folders = folders,
                onDismiss = { onAction(WorkoutOverlayAction.ClearMove) },
                onSelectFolder = { folderId -> onAction(WorkoutOverlayAction.SelectFolder(folderId)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutTopBar(selectedTab: Int, onTabSelected: (Int) -> Unit, onToggleReorderMode: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.title_workout)) },
        actions = {
            IconButton(onClick = onToggleReorderMode) {
                Icon(Icons.Default.Menu, contentDescription = null)
            }
        },
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
            onClick = { onTabSelected(0) },
            text = { Text(stringResource(R.string.tab_my_routines)) }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = { Text(stringResource(R.string.tab_archived)) }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun WorkoutTemplatesSection(
    selectedPage: Int,
    currentTemplates: List<WorkoutTemplate>,
    uiState: WorkoutUiState,
    onEvent: (WorkoutUiEvent) -> Unit,
    onCreateFolderClick: () -> Unit,
    onMoveRequest: (WorkoutTemplate) -> Unit,
    onFolderDeleteRequest: (String) -> Unit,
    onTemplateDeleteRequest: (WorkoutTemplate) -> Unit
) {
    // Track which template is being dragged to a folder
    var draggingTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }
    
    val lazyListState = rememberLazyListState()
    val dragDropState = com.eugene.lift.ui.util.rememberDragDropState(lazyListState)

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (selectedPage == 0) {
            item {
                FolderRow(
                    folders = uiState.folders,
                    currentFolderId = uiState.currentFolderId,
                    onFolderClick = { onEvent(WorkoutUiEvent.FolderSelected(it)) },
                    onBackToRoot = { onEvent(WorkoutUiEvent.FolderSelected(null)) },
                    onCreateFolderClick = onCreateFolderClick,
                    onDeleteFolder = onFolderDeleteRequest
                )
            }
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    QuickStartCard(onClick = { onEvent(WorkoutUiEvent.StartEmptyClicked) })
                }
            }
            if (currentTemplates.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }

        if (currentTemplates.isNotEmpty()) {
            itemsIndexed(currentTemplates, key = { _, t -> t.id }) { index, template ->
                val isReorderDragging = dragDropState.draggingItemIndex == index
                val isFolderDragging = draggingTemplate?.id == template.id

                Box(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp)
                ) {
                    if (uiState.reorderState.isReorderMode) {
                        val alpha by androidx.compose.animation.core.animateFloatAsState(
                            if (isReorderDragging) 0.4f else 1f, label = "drop_alpha"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(alpha)
                        ) {
                            CompactTemplateRow(
                                templateName = template.name,
                                dragHandle = {
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = stringResource(R.string.exercise_drag_handle),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .padding(8.dp)
                                            .pointerInput(template.id) {
                                                detectDragGestures(
                                                    onDragStart = { offset ->
                                                        dragDropState.onDragStart(index, offset.y)
                                                    },
                                                    onDrag = { _, dragAmount ->
                                                        dragDropState.onDragBy(dragAmount.y)
                                                    },
                                                    onDragEnd = {
                                                        dragDropState.onDragEnd { from, to ->
                                                            onEvent(WorkoutUiEvent.TemplatesReordered(from, to, selectedPage == 1))
                                                        }
                                                    },
                                                    onDragCancel = { dragDropState.onDragCancelled() }
                                                )
                                            }
                                    )
                                }
                            )
                        }
                    } else {
                        Box {
                            TemplateItemCard(
                                template = template,
                                isDragging = isFolderDragging,
                                onEvent = onEvent,
                                onMoveRequest = { onMoveRequest(template) },
                                onDeleteRequest = { onTemplateDeleteRequest(template) }
                            )
                        }
                    }
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
                        text = if (selectedPage == 0) stringResource(R.string.empty_routines)
                        else stringResource(R.string.empty_archived),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateItemCard(
    template: WorkoutTemplate,
    isDragging: Boolean = false,
    onEvent: (WorkoutUiEvent) -> Unit,
    onMoveRequest: () -> Unit,
    onDeleteRequest: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val cardAlpha = if (isDragging) 0.5f else 1f

    Card(
        onClick = { onEvent(WorkoutUiEvent.TemplateClicked(template.id)) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            if (isDragging) 2.dp else 1.dp,
            if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
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
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.template_actions_menu)
                            )
                        }
                        TemplateActionsMenu(
                            showMenu = showMenu,
                            onShowMenuChange = { showMenu = it },
                            template = template,
                            onMoveRequest = onMoveRequest,
                            onDeleteRequest = onDeleteRequest,
                            onEvent = onEvent
                        )
                    }
                }
            )
        }

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

        Button(
            onClick = { onEvent(WorkoutUiEvent.TemplateStartClicked(template.id)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(R.string.template_detail_start_routine))
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
    // Infinite shimmer animation
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceContainerLow,
        MaterialTheme.colorScheme.surfaceContainerHigh,
        MaterialTheme.colorScheme.surfaceContainerLow
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val brush = Brush.horizontalGradient(
        colors = shimmerColors,
        startX = translateAnim - 300f,
        endX = translateAnim + 300f
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick-start card skeleton
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(brush)
            )
        }
        item { Spacer(Modifier.height(4.dp)) }
        // Template card skeletons
        items(5) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Title placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .height(18.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(brush)
                            )
                            Spacer(Modifier.height(6.dp))
                            // Subtitle placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.35f)
                                    .height(13.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(brush)
                            )
                        }
                        // Icon button placeholder
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(brush)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    // Start button placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(brush)
                    )
                }
            }
        }
    }
}


@Composable
fun TemplateActionsMenu(
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    template: WorkoutTemplate,
    onMoveRequest: () -> Unit,
    onDeleteRequest: () -> Unit = {},
    onEvent: (WorkoutUiEvent) -> Unit
) {
    DropdownMenu(expanded = showMenu, onDismissRequest = { onShowMenuChange(false) }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_edit)) },
            onClick = { onShowMenuChange(false); onEvent(WorkoutUiEvent.TemplateEditClicked(template.id)) },
            leadingIcon = { Icon(Icons.Default.Edit, null) }
        )
        DropdownMenuItem(
            text = { Text(stringResource(if (template.isArchived) R.string.action_unarchive else R.string.action_archive)) },
            onClick = { onShowMenuChange(false); onEvent(WorkoutUiEvent.TemplateArchiveToggled(template.id, !template.isArchived)) },
            leadingIcon = { Icon(if(template.isArchived) Icons.Default.Unarchive else Icons.Default.Archive, null) }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_move_to_folder)) },
            onClick = { onShowMenuChange(false); onMoveRequest() },
            leadingIcon = { Icon(Icons.Default.Folder, null) }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
            onClick = { onShowMenuChange(false); onDeleteRequest() },
            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_duplicate)) },
            onClick = { onShowMenuChange(false); onEvent(WorkoutUiEvent.TemplateDuplicated(template.id)) },
            leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_share)) },
            onClick = {
                onShowMenuChange(false)
                onEvent(WorkoutUiEvent.TemplateShared(template.id))
            },
            leadingIcon = { Icon(Icons.Default.Share, null) }
        )
    }
}

private data class WorkoutOverlayState(
    val showCreateFolderDialog: Boolean,
    val templateToMove: WorkoutTemplate?,
    val showFab: Boolean
)

private sealed interface WorkoutOverlayAction {
    data object AddTemplate : WorkoutOverlayAction
    data object ShowCreateFolder : WorkoutOverlayAction
    data object DismissCreateFolder : WorkoutOverlayAction
    data class CreateFolder(val name: String, val color: String) : WorkoutOverlayAction
    data object ClearMove : WorkoutOverlayAction
    data class SelectFolder(val folderId: String?) : WorkoutOverlayAction
}

@Composable
fun CompactTemplateRow(
    templateName: String,
    dragHandle: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dragHandle()
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = templateName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
