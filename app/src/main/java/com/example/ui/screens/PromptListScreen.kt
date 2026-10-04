package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Prompt
import com.example.data.model.SortOrder
import com.example.ui.components.CategoryBadge
import com.example.ui.components.CategoryFilterChip
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptListScreen(
    prompts: List<Prompt>,
    allPromptsCount: Int,
    searchQuery: String,
    selectedCategory: String?,
    allCategories: List<String>,
    sortOrder: SortOrder,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCategorySelect: (String?) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onPromptClick: (Long) -> Unit,
    onAddPromptClick: () -> Unit,
    onCopyPrompt: (Prompt) -> Unit,
    onEditPrompt: (Long) -> Unit,
    onDeletePrompt: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    confirmDelete: Boolean = true,
    previewLines: Int = 3,
    modifier: Modifier = Modifier
) {
    var showSortDialog by remember { mutableStateOf(false) }
    var promptToDelete by remember { mutableStateOf<Prompt?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prompts",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = allPromptsCount.toString(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showSortDialog = true },
                        modifier = Modifier.testTag("sort_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort Prompts",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPromptClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Prompt", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_prompt_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("search_input"),
                placeholder = {
                    Text(
                        "Search prompts, content, category...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = onClearSearch,
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Category Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "All" filter chip
                val isAllSelected = selectedCategory == null || selectedCategory.isEmpty() || selectedCategory.equals("All", ignoreCase = true)
                CategoryFilterChip(
                    category = "All",
                    isSelected = isAllSelected,
                    onClick = { onCategorySelect(null) },
                    count = allPromptsCount,
                    modifier = Modifier.testTag("category_chip_all")
                )

                allCategories.forEach { category ->
                    val isSelected = selectedCategory.equals(category, ignoreCase = true)
                    CategoryFilterChip(
                        category = category,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) onCategorySelect(null) else onCategorySelect(category)
                        },
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }

            // Active sort indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${prompts.size} ${if (prompts.size == 1) "prompt" else "prompts"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSortDialog = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Sort: ${sortOrder.label}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Prompts List or Empty States
            if (prompts.isEmpty()) {
                if (allPromptsCount == 0) {
                    // Truly empty database
                    EmptyStateGeneral(
                        onCreateClick = onAddPromptClick,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Filter or search returned empty
                    EmptyStateFiltered(
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        onClearFilters = {
                            onClearSearch()
                            onCategorySelect(null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("prompts_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(prompts, key = { it.id }) { prompt ->
                        PromptItemCard(
                            prompt = prompt,
                            previewLines = previewLines,
                            onClick = { onPromptClick(prompt.id) },
                            onCopy = { onCopyPrompt(prompt) },
                            onEdit = { onEditPrompt(prompt.id) },
                            onDelete = {
                                if (confirmDelete) {
                                    promptToDelete = prompt
                                } else {
                                    onDeletePrompt(prompt.id)
                                }
                            },
                            modifier = Modifier.testTag("prompt_item_${prompt.id}")
                        )
                    }
                }
            }
        }
    }

    // Sort order selection dialog
    if (showSortDialog) {
        Dialog(onDismissRequest = { showSortDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Sort Prompts By",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SortOrder.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSortOrderChange(option)
                                    showSortDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (sortOrder == option),
                                onClick = {
                                    onSortOrderChange(option)
                                    showSortDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (sortOrder == option) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showSortDialog = false }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    // Permanent delete confirmation dialog
    promptToDelete?.let { prompt ->
        DeleteConfirmDialog(
            promptTitle = prompt.name,
            onConfirm = {
                onDeletePrompt(prompt.id)
                promptToDelete = null
            },
            onDismiss = { promptToDelete = null }
        )
    }
}

@Composable
fun PromptItemCard(
    prompt: Prompt,
    previewLines: Int = 3,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var copiedRecently by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Category Badge + Date + More Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(category = prompt.category)

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateFormatter.formatRelative(prompt.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("more_options_button_${prompt.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                },
                                modifier = Modifier.testTag("menu_edit_prompt_${prompt.id}")
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Delete",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                modifier = Modifier.testTag("menu_delete_prompt_${prompt.id}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prompt Name / Title
            Text(
                text = prompt.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Prompt Content Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(10.dp)
            ) {
                Text(
                    text = prompt.content,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = previewLines,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Footer: Character stats + Fast Copy Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val wordCount = prompt.content.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
                Text(
                    text = "$wordCount words • ${prompt.content.length} chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                // Quick Copy Action
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onCopy()
                            copiedRecently = true
                        }
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("copy_prompt_button_${prompt.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (copiedRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy Prompt",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (copiedRecently) "Copied" else "Copy",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateGeneral(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No prompts saved yet",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your first prompt to start building your personal prompt library.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("empty_state_create_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Prompt")
        }
    }
}

@Composable
fun EmptyStateFiltered(
    searchQuery: String,
    selectedCategory: String?,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterListOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No matching prompts found",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        val hint = when {
            searchQuery.isNotEmpty() && selectedCategory != null ->
                "No results for \"$searchQuery\" in category \"$selectedCategory\"."
            searchQuery.isNotEmpty() ->
                "No results for \"$searchQuery\"."
            selectedCategory != null ->
                "No prompts saved in category \"$selectedCategory\"."
            else -> "Try adjusting your search terms or category filter."
        }

        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onClearFilters,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("clear_filters_button")
        ) {
            Icon(Icons.Default.Clear, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear Filters")
        }
    }
}
