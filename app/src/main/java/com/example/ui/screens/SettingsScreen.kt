package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Prompt
import com.example.data.model.SortOrder
import com.example.data.model.UserAccount
import com.example.ui.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userAccount: UserAccount?,
    isSigningIn: Boolean,
    defaultCategory: String,
    defaultSortOrder: SortOrder,
    confirmDelete: Boolean,
    autoCopyOnSave: Boolean,
    previewLines: Int,
    googleClientId: String,
    prompts: List<Prompt>,
    allCategories: List<String>,
    onBack: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignOutGoogle: () -> Unit,
    onDefaultCategoryChange: (String) -> Unit,
    onDefaultSortOrderChange: (SortOrder) -> Unit,
    onConfirmDeleteChange: (Boolean) -> Unit,
    onAutoCopyOnSaveChange: (Boolean) -> Unit,
    onPreviewLinesChange: (Int) -> Unit,
    onGoogleClientIdChange: (String) -> Unit,
    onExportPrompts: () -> Unit,
    onClearAllPrompts: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showPreviewLinesDialog by remember { mutableStateOf(false) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var showClientIdDialog by remember { mutableStateOf(false) }
    var clientIdInput by remember { mutableStateOf(googleClientId) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                            contentDescription = "Back"
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ==========================================
            // 1. ACCOUNT & GOOGLE SIGN-IN SECTION
            // ==========================================
            SettingsSectionHeader(title = "ACCOUNT")

            if (userAccount != null) {
                // Signed In State
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("google_account_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar circle with initial
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userAccount.displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                        contentDescription = "Google",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Google Account",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = userAccount.displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = userAccount.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Signed in since: ${DateFormatter.formatShort(userAccount.signedInAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedButton(
                                onClick = onSignOutGoogle,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sign Out")
                            }
                        }
                    }
                }
            } else {
                // Not Signed In State
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("google_login_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Google Sign-In",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Sign in to connect your Google account to Prompts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Prominent Google Sign-in Button
                        Button(
                            onClick = onSignInWithGoogle,
                            enabled = !isSigningIn,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .testTag("google_sign_in_button")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Connecting to Google...")
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Google Web Client ID Configuration setting
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    clientIdInput = googleClientId
                                    showClientIdDialog = true
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Google Cloud Web Client ID",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = if (googleClientId.isNotBlank()) "Configured" else "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. APP PREFERENCES SECTION
            // ==========================================
            SettingsSectionHeader(title = "APP PREFERENCES")

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // Default Category Setting
                    SettingsClickableRow(
                        icon = Icons.Default.Category,
                        title = "Default Category",
                        subtitle = "Pre-selected category when creating a prompt",
                        valueText = defaultCategory,
                        onClick = { showCategoryDialog = true },
                        testTag = "setting_default_category"
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Default Sort Order Setting
                    SettingsClickableRow(
                        icon = Icons.Default.Sort,
                        title = "Default Sorting Order",
                        subtitle = "Initial ordering of prompts in the library",
                        valueText = defaultSortOrder.label,
                        onClick = { showSortDialog = true },
                        testTag = "setting_default_sort"
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Confirm Delete Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.Warning,
                        title = "Confirm Before Deleting",
                        subtitle = "Display confirmation dialog before permanently removing prompts",
                        checked = confirmDelete,
                        onCheckedChange = onConfirmDeleteChange,
                        testTag = "confirm_delete_switch"
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Auto Copy on Save Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.ContentCopy,
                        title = "Auto-Copy on Save",
                        subtitle = "Automatically copy prompt content to clipboard when created or updated",
                        checked = autoCopyOnSave,
                        onCheckedChange = onAutoCopyOnSaveChange,
                        testTag = "auto_copy_switch"
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Card Preview Lines Setting
                    SettingsClickableRow(
                        icon = Icons.Default.Info,
                        title = "Card Preview Lines",
                        subtitle = "Lines of prompt text displayed on each list card",
                        valueText = "$previewLines lines",
                        onClick = { showPreviewLinesDialog = true },
                        testTag = "setting_preview_lines"
                    )
                }
            }

            // ==========================================
            // 3. DATA & LIBRARY MANAGEMENT SECTION
            // ==========================================
            SettingsSectionHeader(title = "DATA & LIBRARY")

            // Statistics Grid
            val totalWords = prompts.sumOf { p -> p.content.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size }
            val totalChars = prompts.sumOf { it.content.length }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Library Overview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(label = "Prompts", value = prompts.size.toString(), modifier = Modifier.weight(1f))
                        StatTile(label = "Categories", value = allCategories.size.toString(), modifier = Modifier.weight(1f))
                        StatTile(label = "Words", value = totalWords.toString(), modifier = Modifier.weight(1f))
                        StatTile(label = "Characters", value = totalChars.toString(), modifier = Modifier.weight(1f))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Export all prompts button
                    Button(
                        onClick = onExportPrompts,
                        enabled = prompts.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_prompts_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export All Prompts (Share / Backup)")
                    }

                    // Clear library button
                    OutlinedButton(
                        onClick = { showClearAllConfirmDialog = true },
                        enabled = prompts.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_all_prompts_button")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Saved Prompts")
                    }
                }
            }

            // ==========================================
            // 4. ABOUT SECTION
            // ==========================================
            SettingsSectionHeader(title = "ABOUT")

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Prompts",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "A fast, offline-first personal prompt library utility designed to save, organize, search, and copy AI prompts effortlessly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0.0 • Package: com.bilal.prompts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Database: Local SQLite (Room Persistent Storage)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // --- Dialogs ---

    // Default Category Picker Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Default Category") },
            text = {
                Column {
                    allCategories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onDefaultCategoryChange(category)
                                    showCategoryDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (defaultCategory.equals(category, ignoreCase = true)),
                                onClick = {
                                    onDefaultCategoryChange(category)
                                    showCategoryDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Default Sort Picker Dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Default Sort Order") },
            text = {
                Column {
                    SortOrder.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onDefaultSortOrderChange(option)
                                    showSortDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (defaultSortOrder == option),
                                onClick = {
                                    onDefaultSortOrderChange(option)
                                    showSortDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option.label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Preview Lines Picker Dialog
    if (showPreviewLinesDialog) {
        AlertDialog(
            onDismissRequest = { showPreviewLinesDialog = false },
            title = { Text("Card Preview Lines") },
            text = {
                Column {
                    listOf(2, 3, 5).forEach { lines ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onPreviewLinesChange(lines)
                                    showPreviewLinesDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (previewLines == lines),
                                onClick = {
                                    onPreviewLinesChange(lines)
                                    showPreviewLinesDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$lines lines ${if (lines == 2) "(Compact)" else if (lines == 3) "(Standard)" else "(Expanded)"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPreviewLinesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Clear All Prompts?") },
            text = {
                Text("This will permanently delete all ${prompts.size} saved prompts from your device. This action cannot be reversed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearAllConfirmDialog = false
                        onClearAllPrompts()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_clear_all_button")
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Google Client ID Config Dialog
    if (showClientIdDialog) {
        AlertDialog(
            onDismissRequest = { showClientIdDialog = false },
            title = { Text("Google Web Client ID") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paste your OAuth 2.0 Web Client ID from Google Cloud Console:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = clientIdInput,
                        onValueChange = { clientIdInput = it },
                        placeholder = { Text("e.g., 123456-xxx.apps.googleusercontent.com") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_id_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onGoogleClientIdChange(clientIdInput.trim())
                        showClientIdDialog = false
                    },
                    modifier = Modifier.testTag("save_client_id_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClientIdDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun SettingsClickableRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    valueText: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
