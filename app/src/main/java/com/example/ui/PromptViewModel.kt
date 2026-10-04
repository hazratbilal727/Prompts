package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserSettingsManager
import com.example.data.model.DEFAULT_CATEGORIES
import com.example.data.model.Prompt
import com.example.data.model.SortOrder
import com.example.data.model.UserAccount
import com.example.data.repository.PromptRepository
import com.example.util.GoogleAuthHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    data object PromptList : Screen
    data class PromptDetail(val promptId: Long) : Screen
    data class AddEditPrompt(val promptId: Long? = null) : Screen
    data object Settings : Screen
}

class PromptViewModel(
    private val repository: PromptRepository,
    private val userSettingsManager: UserSettingsManager? = null,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope = coroutineScope ?: viewModelScope

    // Navigation state stack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.PromptList))
    private val _currentScreen = MutableStateFlow<Screen>(Screen.PromptList)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected category filter (null = "All")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Current sorting order (initialized from settings if present)
    private val _sortOrder = MutableStateFlow(
        userSettingsManager?.defaultSortOrder?.value ?: SortOrder.RECENTLY_UPDATED
    )
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Copy & feedback notification message
    private val _feedbackMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val feedbackMessage: SharedFlow<String> = _feedbackMessage.asSharedFlow()

    // Google Sign-In loading state
    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    // Settings observable flows
    val userAccount: StateFlow<UserAccount?> =
        userSettingsManager?.userAccount ?: MutableStateFlow(null)

    val defaultCategory: StateFlow<String> =
        userSettingsManager?.defaultCategory ?: MutableStateFlow("General")

    val confirmDelete: StateFlow<Boolean> =
        userSettingsManager?.confirmDelete ?: MutableStateFlow(true)

    val autoCopyOnSave: StateFlow<Boolean> =
        userSettingsManager?.autoCopyOnSave ?: MutableStateFlow(false)

    val previewLines: StateFlow<Int> =
        userSettingsManager?.previewLines ?: MutableStateFlow(3)

    val googleClientId: StateFlow<String> =
        userSettingsManager?.googleClientId ?: MutableStateFlow("")

    // All raw prompts from database
    val allPrompts: StateFlow<List<Prompt>> = repository.allPrompts
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    // All available categories (defaults + any user added)
    val allCategories: StateFlow<List<String>> = allPrompts
        .combine(MutableStateFlow(DEFAULT_CATEGORIES)) { prompts, defaults ->
            val set = linkedSetOf<String>()
            set.addAll(defaults)
            prompts.forEach { set.add(it.category) }
            set.toList()
        }
        .stateIn(scope, SharingStarted.Eagerly, DEFAULT_CATEGORIES)

    // Filtered, searched, and sorted prompts list
    val filteredPrompts: StateFlow<List<Prompt>> = combine(
        repository.allPrompts,
        _searchQuery,
        _selectedCategory,
        _sortOrder
    ) { prompts, query, category, sort ->
        val trimmedQuery = query.trim().lowercase()

        val matchingPrompts = prompts.filter { prompt ->
            val matchesQuery = if (trimmedQuery.isEmpty()) {
                true
            } else {
                prompt.name.lowercase().contains(trimmedQuery) ||
                prompt.content.lowercase().contains(trimmedQuery) ||
                prompt.category.lowercase().contains(trimmedQuery)
            }

            val matchesCategory = if (category == null || category.isEmpty() || category.equals("All", ignoreCase = true)) {
                true
            } else {
                prompt.category.equals(category, ignoreCase = true)
            }

            matchesQuery && matchesCategory
        }

        when (sort) {
            SortOrder.RECENTLY_UPDATED -> matchingPrompts.sortedByDescending { it.updatedAt }
            SortOrder.RECENTLY_CREATED -> matchingPrompts.sortedByDescending { it.createdAt }
            SortOrder.ALPHABETICAL_ASC -> matchingPrompts.sortedBy { it.name.lowercase() }
            SortOrder.ALPHABETICAL_DESC -> matchingPrompts.sortedByDescending { it.name.lowercase() }
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // Currently observed prompt in Detail screen
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentDetailPrompt: StateFlow<Prompt?> = _currentScreen.flatMapLatest { screen ->
        if (screen is Screen.PromptDetail) {
            repository.getPromptById(screen.promptId)
        } else {
            flowOf(null)
        }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    // Search & Filter controls
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    // Navigation
    fun navigateTo(screen: Screen) {
        val updated = _screenStack.value + screen
        _screenStack.value = updated
        _currentScreen.value = screen
    }

    fun popBack(): Boolean {
        val current = _screenStack.value
        return if (current.size > 1) {
            val updated = current.dropLast(1)
            _screenStack.value = updated
            _currentScreen.value = updated.last()
            true
        } else {
            false
        }
    }

    fun navigateToDetail(promptId: Long) {
        navigateTo(Screen.PromptDetail(promptId))
    }

    fun navigateToAddPrompt() {
        navigateTo(Screen.AddEditPrompt(null))
    }

    fun navigateToEditPrompt(promptId: Long) {
        navigateTo(Screen.AddEditPrompt(promptId))
    }

    fun navigateToSettings() {
        navigateTo(Screen.Settings)
    }

    // CRUD operations
    fun savePrompt(
        context: Context,
        name: String,
        content: String,
        category: String,
        promptId: Long? = null,
        onComplete: (Long) -> Unit
    ) {
        scope.launch {
            val trimmedName = name.trim()
            val trimmedContent = content.trim()
            val trimmedCategory = category.trim().ifEmpty { defaultCategory.value }

            val savedId = if (promptId != null && promptId > 0) {
                repository.updatePrompt(promptId, trimmedName, trimmedContent, trimmedCategory)
                _feedbackMessage.tryEmit("Prompt updated")
                promptId
            } else {
                val newId = repository.insertPrompt(trimmedName, trimmedContent, trimmedCategory)
                _feedbackMessage.tryEmit("Prompt saved")
                newId
            }

            // Auto-copy to clipboard if enabled in settings
            if (autoCopyOnSave.value) {
                copyPromptToClipboard(context, Prompt(
                    id = savedId,
                    name = trimmedName,
                    content = trimmedContent,
                    category = trimmedCategory
                ))
            }

            onComplete(savedId)
        }
    }

    fun deletePrompt(promptId: Long, onComplete: () -> Unit) {
        scope.launch {
            repository.deletePromptById(promptId)
            _feedbackMessage.tryEmit("Prompt deleted")
            // If currently viewing detail or edit screen of this prompt, pop back
            val current = _screenStack.value.lastOrNull()
            if ((current is Screen.PromptDetail && current.promptId == promptId) ||
                (current is Screen.AddEditPrompt && current.promptId == promptId)
            ) {
                popBack()
            }
            onComplete()
        }
    }

    fun clearAllPrompts(onComplete: () -> Unit) {
        scope.launch {
            repository.deleteAllPrompts()
            _feedbackMessage.tryEmit("All prompts deleted")
            // If currently in detail or edit, pop to list
            _screenStack.value = listOf(Screen.PromptList)
            _currentScreen.value = Screen.PromptList
            onComplete()
        }
    }

    fun copyPromptToClipboard(context: Context, prompt: Prompt) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Prompt Content", prompt.content)
        clipboard.setPrimaryClip(clip)
        _feedbackMessage.tryEmit("Prompt copied")
    }

    fun exportPromptsToText(prompts: List<Prompt>): String {
        val sb = StringBuilder()
        sb.append("# My Saved Prompts (${prompts.size})\n\n")
        prompts.forEachIndexed { index, prompt ->
            sb.append("## ${index + 1}. ${prompt.name}\n")
            sb.append("**Category:** ${prompt.category}\n\n")
            sb.append("```\n")
            sb.append(prompt.content)
            sb.append("\n```\n\n---\n\n")
        }
        return sb.toString()
    }

    fun sharePrompts(context: Context, prompts: List<Prompt>) {
        val exportText = exportPromptsToText(prompts)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, exportText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Prompts")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    // Google Sign-In Integration
    fun signInWithGoogle(context: Context) {
        scope.launch {
            _isSigningIn.value = true
            val helper = GoogleAuthHelper(context)
            val result = helper.signInWithGoogle(googleClientId.value)
            _isSigningIn.value = false
            result.onSuccess { account ->
                userSettingsManager?.saveUserAccount(account)
                _feedbackMessage.tryEmit("Signed in as ${account.displayName}")
            }.onFailure { error ->
                _feedbackMessage.tryEmit(error.message ?: "Google Sign-In failed")
            }
        }
    }

    fun signOutGoogle(context: Context) {
        scope.launch {
            val helper = GoogleAuthHelper(context)
            helper.signOut()
            userSettingsManager?.clearUserAccount()
            _feedbackMessage.tryEmit("Signed out from Google")
        }
    }

    // Settings modifiers
    fun setDefaultCategory(category: String) {
        userSettingsManager?.setDefaultCategory(category)
        _feedbackMessage.tryEmit("Default category updated: $category")
    }

    fun setDefaultSortOrder(sortOrder: SortOrder) {
        userSettingsManager?.setDefaultSortOrder(sortOrder)
        _sortOrder.value = sortOrder
        _feedbackMessage.tryEmit("Default sort: ${sortOrder.label}")
    }

    fun setConfirmDelete(enabled: Boolean) {
        userSettingsManager?.setConfirmDelete(enabled)
    }

    fun setAutoCopyOnSave(enabled: Boolean) {
        userSettingsManager?.setAutoCopyOnSave(enabled)
    }

    fun setPreviewLines(lines: Int) {
        userSettingsManager?.setPreviewLines(lines)
    }

    fun setGoogleClientId(clientId: String) {
        userSettingsManager?.setGoogleClientId(clientId)
        _feedbackMessage.tryEmit("Google Web Client ID updated")
    }

    class Factory(
        private val repository: PromptRepository,
        private val userSettingsManager: UserSettingsManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PromptViewModel::class.java)) {
                return PromptViewModel(repository, userSettingsManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
