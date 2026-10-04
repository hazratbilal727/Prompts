package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.local.UserSettingsManager
import com.example.data.repository.PromptRepositoryImpl
import com.example.ui.PromptViewModel
import com.example.ui.Screen
import com.example.ui.screens.AddEditPromptScreen
import com.example.ui.screens.PromptDetailScreen
import com.example.ui.screens.PromptListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PromptsTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PromptViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PromptRepositoryImpl(database.promptDao())
        val settingsManager = UserSettingsManager(applicationContext)
        PromptViewModel.Factory(repository, settingsManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PromptsTheme {
                PromptsApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PromptsApp(
    viewModel: PromptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allPrompts by viewModel.allPrompts.collectAsStateWithLifecycle()
    val filteredPrompts by viewModel.filteredPrompts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val currentDetailPrompt by viewModel.currentDetailPrompt.collectAsStateWithLifecycle()

    // Settings & Account state
    val userAccount by viewModel.userAccount.collectAsStateWithLifecycle()
    val isSigningIn by viewModel.isSigningIn.collectAsStateWithLifecycle()
    val defaultCategory by viewModel.defaultCategory.collectAsStateWithLifecycle()
    val confirmDelete by viewModel.confirmDelete.collectAsStateWithLifecycle()
    val autoCopyOnSave by viewModel.autoCopyOnSave.collectAsStateWithLifecycle()
    val previewLines by viewModel.previewLines.collectAsStateWithLifecycle()
    val googleClientId by viewModel.googleClientId.collectAsStateWithLifecycle()

    // Listen for clipboard copy, auth, or feedback events
    LaunchedEffect(Unit) {
        viewModel.feedbackMessage.collectLatest { message ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    is Screen.PromptList -> {
                        PromptListScreen(
                            prompts = filteredPrompts,
                            allPromptsCount = allPrompts.size,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            allCategories = allCategories,
                            sortOrder = sortOrder,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onClearSearch = viewModel::clearSearchQuery,
                            onCategorySelect = viewModel::selectCategory,
                            onSortOrderChange = viewModel::setSortOrder,
                            onPromptClick = { id -> viewModel.navigateToDetail(id) },
                            onAddPromptClick = { viewModel.navigateToAddPrompt() },
                            onCopyPrompt = { prompt -> viewModel.copyPromptToClipboard(context, prompt) },
                            onEditPrompt = { id -> viewModel.navigateToEditPrompt(id) },
                            onDeletePrompt = { id -> viewModel.deletePrompt(id) {} },
                            onSettingsClick = { viewModel.navigateToSettings() },
                            confirmDelete = confirmDelete,
                            previewLines = previewLines
                        )
                    }

                    is Screen.PromptDetail -> {
                        PromptDetailScreen(
                            prompt = currentDetailPrompt,
                            onBack = { viewModel.popBack() },
                            onCopy = { prompt -> viewModel.copyPromptToClipboard(context, prompt) },
                            onEdit = { id -> viewModel.navigateToEditPrompt(id) },
                            onDelete = { id -> viewModel.deletePrompt(id) {} },
                            confirmDelete = confirmDelete
                        )
                    }

                    is Screen.AddEditPrompt -> {
                        val promptToEdit = if (targetScreen.promptId != null) {
                            allPrompts.find { it.id == targetScreen.promptId }
                        } else null

                        AddEditPromptScreen(
                            promptToEdit = promptToEdit,
                            allCategories = allCategories,
                            defaultCategory = defaultCategory,
                            onBack = { viewModel.popBack() },
                            onSave = { name, content, category, promptId ->
                                viewModel.savePrompt(context, name, content, category, promptId) {
                                    viewModel.popBack()
                                }
                            }
                        )
                    }

                    is Screen.Settings -> {
                        SettingsScreen(
                            userAccount = userAccount,
                            isSigningIn = isSigningIn,
                            defaultCategory = defaultCategory,
                            defaultSortOrder = sortOrder,
                            confirmDelete = confirmDelete,
                            autoCopyOnSave = autoCopyOnSave,
                            previewLines = previewLines,
                            googleClientId = googleClientId,
                            prompts = allPrompts,
                            allCategories = allCategories,
                            onBack = { viewModel.popBack() },
                            onSignInWithGoogle = { viewModel.signInWithGoogle(context) },
                            onSignOutGoogle = { viewModel.signOutGoogle(context) },
                            onDefaultCategoryChange = { viewModel.setDefaultCategory(it) },
                            onDefaultSortOrderChange = { viewModel.setDefaultSortOrder(it) },
                            onConfirmDeleteChange = { viewModel.setConfirmDelete(it) },
                            onAutoCopyOnSaveChange = { viewModel.setAutoCopyOnSave(it) },
                            onPreviewLinesChange = { viewModel.setPreviewLines(it) },
                            onGoogleClientIdChange = { viewModel.setGoogleClientId(it) },
                            onExportPrompts = { viewModel.sharePrompts(context, allPrompts) },
                            onClearAllPrompts = { viewModel.clearAllPrompts {} }
                        )
                    }
                }
            }
        }
    }
}
