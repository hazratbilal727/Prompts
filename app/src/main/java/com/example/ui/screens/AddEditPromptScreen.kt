package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Prompt
import com.example.ui.components.CategoryFilterChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPromptScreen(
    promptToEdit: Prompt?,
    allCategories: List<String>,
    defaultCategory: String = "General",
    onBack: () -> Unit,
    onSave: (name: String, content: String, category: String, promptId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val isEditing = promptToEdit != null

    var name by remember { mutableStateOf(promptToEdit?.name ?: "") }
    var content by remember { mutableStateOf(promptToEdit?.content ?: "") }
    var selectedCategory by remember { mutableStateOf(promptToEdit?.category ?: defaultCategory) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var contentError by remember { mutableStateOf<String?>(null) }

    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryInput by remember { mutableStateOf("") }

    // If editing changes externally
    LaunchedEffect(promptToEdit) {
        promptToEdit?.let {
            name = it.name
            content = it.content
            selectedCategory = it.category
        }
    }

    fun handleSave() {
        val trimmedName = name.trim()
        val trimmedContent = content.trim()

        var hasError = false
        if (trimmedName.isEmpty()) {
            nameError = "Prompt name is required"
            hasError = true
        } else {
            nameError = null
        }

        if (trimmedContent.isEmpty()) {
            contentError = "Prompt content cannot be empty"
            hasError = true
        } else {
            contentError = null
        }

        if (!hasError) {
            onSave(trimmedName, trimmedContent, selectedCategory, promptToEdit?.id)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Prompt" else "New Prompt",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { handleSave() },
                        modifier = Modifier.testTag("save_prompt_top_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Prompt Name Field
            Column {
                Text(
                    text = "Prompt Name *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    placeholder = { Text("e.g., Senior Python Code Reviewer") },
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) {
                            Text(text = nameError!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(text = "Give this prompt a recognizable, descriptive title")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_name_input")
                )
            }

            // Category Selection
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category *",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Selected: $selectedCategory",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable category chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    allCategories.forEach { category ->
                        val isSelected = selectedCategory.equals(category, ignoreCase = true)
                        CategoryFilterChip(
                            category = category,
                            isSelected = isSelected,
                            onClick = { selectedCategory = category },
                            modifier = Modifier.testTag("form_category_chip_$category")
                        )
                    }

                    // Add Custom Category Chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                            .clickable { showCustomCategoryDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("add_custom_category_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Prompt Content Multiline Area
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Prompt Content *",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    val wordCount = content.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
                    Text(
                        text = "$wordCount words • ${content.length} chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        if (contentError != null) contentError = null
                    },
                    placeholder = {
                        Text(
                            "Enter or paste the complete prompt here...\n\nInclude any instructions, constraints, context, and placeholders like [Topic] or [Code].",
                            lineHeight = 22.sp
                        )
                    },
                    isError = contentError != null,
                    supportingText = {
                        if (contentError != null) {
                            Text(text = contentError!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(text = "This is the full text copied to your clipboard")
                        }
                    },
                    minLines = 8,
                    maxLines = 24,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 22.sp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_content_input")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Save Button
            Button(
                onClick = { handleSave() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_prompt_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Update Prompt" else "Save Prompt",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Custom Category Input Dialog
    if (showCustomCategoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showCustomCategoryDialog = false
                customCategoryInput = ""
            },
            title = { Text("New Category") },
            text = {
                Column {
                    Text("Enter a name for your custom category:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customCategoryInput,
                        onValueChange = { customCategoryInput = it },
                        singleLine = true,
                        placeholder = { Text("e.g., SEO, Prompts 101") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_category_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = customCategoryInput.trim()
                        if (trimmed.isNotEmpty()) {
                            selectedCategory = trimmed
                        }
                        showCustomCategoryDialog = false
                        customCategoryInput = ""
                    },
                    modifier = Modifier.testTag("confirm_custom_category_button")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCustomCategoryDialog = false
                        customCategoryInput = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
