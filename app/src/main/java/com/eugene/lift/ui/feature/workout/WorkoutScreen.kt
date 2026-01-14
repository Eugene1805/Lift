package com.eugene.lift.ui.feature.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.lift.R
import com.eugene.lift.domain.model.WorkoutTemplate

@Composable
fun WorkoutRoute(
    onNavigateToEdit: (String?) -> Unit,
    onTemplateClick: (String) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    WorkoutScreen(
        templates = templates,
        selectedTab = selectedTab,
        onTabSelected = viewModel::onTabSelected,
        onCreateClick = { onNavigateToEdit(null) }, // Null ID = Crear
        onEditClick = { onNavigateToEdit(it.id) },
        onTemplateClick = { onTemplateClick(it.id) },
        onArchiveClick = viewModel::archiveTemplate,
        onDeleteClick = { viewModel.deleteTemplate(it.id) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    templates: List<WorkoutTemplate>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onCreateClick: () -> Unit,
    onEditClick: (WorkoutTemplate) -> Unit,
    onTemplateClick: (WorkoutTemplate) -> Unit,
    onArchiveClick: (WorkoutTemplate) -> Unit,
    onDeleteClick: (WorkoutTemplate) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text(stringResource(R.string.title_workout)) })
                TabRow(selectedTabIndex = selectedTab) {
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
        },
        floatingActionButton = {
            if (selectedTab == 0) { // Solo permitimos crear en la pestaña de activas
                FloatingActionButton(onClick = onCreateClick) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        if (templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.empty_routines), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates, key = { it.id }) { template ->
                    TemplateItemCard(
                        template = template,
                        onClick = { onTemplateClick(template) },
                        onEdit = { onEditClick(template) },
                        onArchive = { onArchiveClick(template) },
                        onDelete = { onDeleteClick(template) }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateItemCard(
    template: WorkoutTemplate,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(onClick = onClick) {
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
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        )
    }
}