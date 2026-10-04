package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.UserSettingsManager
import com.example.data.model.Prompt
import com.example.data.model.SortOrder
import com.example.data.model.UserAccount
import com.example.data.repository.PromptRepository
import com.example.ui.PromptViewModel
import com.example.ui.Screen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * In-memory Fake repository for fast, deterministic unit & CUJ testing.
 */
class FakePromptRepository : PromptRepository {
    private val promptsFlow = MutableStateFlow<List<Prompt>>(emptyList())
    private var nextId = 1L

    override val allPrompts: Flow<List<Prompt>> = promptsFlow.asStateFlow()

    override fun getPromptById(id: Long): Flow<Prompt?> {
        return promptsFlow.map { list -> list.find { it.id == id } }
    }

    override suspend fun getPromptByIdOnce(id: Long): Prompt? {
        return promptsFlow.value.find { it.id == id }
    }

    override suspend fun insertPrompt(name: String, content: String, category: String): Long {
        val id = nextId++
        val now = System.currentTimeMillis()
        val newPrompt = Prompt(
            id = id,
            name = name,
            content = content,
            category = category,
            createdAt = now,
            updatedAt = now
        )
        promptsFlow.value = promptsFlow.value + newPrompt
        return id
    }

    override suspend fun updatePrompt(id: Long, name: String, content: String, category: String) {
        val current = promptsFlow.value
        val existing = current.find { it.id == id }
        val createdAt = existing?.createdAt ?: System.currentTimeMillis()
        val updated = Prompt(
            id = id,
            name = name,
            content = content,
            category = category,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis()
        )
        promptsFlow.value = current.map { if (it.id == id) updated else it }
    }

    override suspend fun deletePrompt(prompt: Prompt) {
        deletePromptById(prompt.id)
    }

    override suspend fun deletePromptById(id: Long) {
        promptsFlow.value = promptsFlow.value.filter { it.id != id }
    }

    override suspend fun deleteAllPrompts() {
        promptsFlow.value = emptyList()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var fakeRepository: FakePromptRepository
    private lateinit var settingsManager: UserSettingsManager
    private lateinit var viewModel: PromptViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        fakeRepository = FakePromptRepository()
        settingsManager = UserSettingsManager(context)
        settingsManager.clearUserAccount()
        viewModel = PromptViewModel(fakeRepository, settingsManager)
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Prompts", appName)
    }

    @Test
    fun `repository insert and query test`() = runTest {
        val id = fakeRepository.insertPrompt("Test Prompt", "This is test prompt content", "Coding")
        assertTrue(id > 0)

        val retrieved = fakeRepository.getPromptByIdOnce(id)
        assertNotNull(retrieved)
        assertEquals("Test Prompt", retrieved?.name)
        assertEquals("Coding", retrieved?.category)
        assertEquals("This is test prompt content", retrieved?.content)
    }

    @Test
    fun `repository update and delete test`() = runTest {
        val id = fakeRepository.insertPrompt("Original Name", "Original Content", "Writing")

        fakeRepository.updatePrompt(id, "Updated Name", "Updated Content", "Marketing")

        val fetched = fakeRepository.getPromptByIdOnce(id)
        assertEquals("Updated Name", fetched?.name)
        assertEquals("Marketing", fetched?.category)

        fakeRepository.deletePromptById(id)
        val deleted = fakeRepository.getPromptByIdOnce(id)
        assertNull(deleted)
    }

    @Test
    fun `viewModel search and category filter test`() = runTest {
        val vm = PromptViewModel(fakeRepository, settingsManager, backgroundScope)
        fakeRepository.insertPrompt("Python Clean Code", "Write clean python code", "Coding")
        fakeRepository.insertPrompt("Blog Post Hook", "Write a marketing hook", "Marketing")

        val all = vm.allPrompts.first { it.size == 2 }
        assertEquals(2, all.size)

        // Search for 'python'
        vm.updateSearchQuery("python")
        val searchResults = vm.filteredPrompts.first { list ->
            list.any { it.name.contains("Python") }
        }
        assertEquals(1, searchResults.size)
        assertEquals("Python Clean Code", searchResults[0].name)

        // Clear search
        vm.clearSearchQuery()
        val allAfterClear = vm.filteredPrompts.first { it.size == 2 }
        assertEquals(2, allAfterClear.size)

        // Filter by Marketing category
        vm.selectCategory("Marketing")
        val categoryResults = vm.filteredPrompts.first { list ->
            list.size == 1 && list[0].category == "Marketing"
        }
        assertEquals(1, categoryResults.size)
        assertEquals("Blog Post Hook", categoryResults[0].name)

        // Reset category filter
        vm.selectCategory(null)
        val resetResults = vm.filteredPrompts.first { it.size == 2 }
        assertEquals(2, resetResults.size)
    }

    @Test
    fun `sorting order test`() = runTest {
        val vm = PromptViewModel(fakeRepository, settingsManager, backgroundScope)
        fakeRepository.insertPrompt("Beta Prompt", "Content 1", "General")
        fakeRepository.insertPrompt("Alpha Prompt", "Content 2", "General")

        vm.setSortOrder(SortOrder.ALPHABETICAL_ASC)
        val asc = vm.filteredPrompts.first { it.size == 2 && it[0].name == "Alpha Prompt" }
        assertEquals("Alpha Prompt", asc[0].name)
        assertEquals("Beta Prompt", asc[1].name)

        vm.setSortOrder(SortOrder.ALPHABETICAL_DESC)
        val desc = vm.filteredPrompts.first { it.size == 2 && it[0].name == "Beta Prompt" }
        assertEquals("Beta Prompt", desc[0].name)
        assertEquals("Alpha Prompt", desc[1].name)
    }

    @Test
    fun `navigation stack test`() {
        assertEquals(Screen.PromptList, viewModel.currentScreen.value)

        viewModel.navigateToDetail(101L)
        assertEquals(Screen.PromptDetail(101L), viewModel.currentScreen.value)

        viewModel.navigateToEditPrompt(101L)
        assertEquals(Screen.AddEditPrompt(101L), viewModel.currentScreen.value)

        assertTrue(viewModel.popBack())
        assertEquals(Screen.PromptDetail(101L), viewModel.currentScreen.value)

        viewModel.navigateToSettings()
        assertEquals(Screen.Settings, viewModel.currentScreen.value)

        assertTrue(viewModel.popBack())
        assertEquals(Screen.PromptDetail(101L), viewModel.currentScreen.value)

        assertTrue(viewModel.popBack())
        assertEquals(Screen.PromptList, viewModel.currentScreen.value)
    }

    @Test
    fun `user account settings test`() {
        assertNull(settingsManager.userAccount.value)

        val account = UserAccount(
            id = "google_12345",
            email = "user@example.com",
            displayName = "Test User"
        )
        settingsManager.saveUserAccount(account)

        val loaded = settingsManager.userAccount.value
        assertNotNull(loaded)
        assertEquals("google_12345", loaded?.id)
        assertEquals("user@example.com", loaded?.email)
        assertEquals("Test User", loaded?.displayName)

        settingsManager.clearUserAccount()
        assertNull(settingsManager.userAccount.value)
    }

    @Test
    fun `clear all prompts test`() = runTest {
        val vm = PromptViewModel(fakeRepository, settingsManager, backgroundScope)
        fakeRepository.insertPrompt("Prompt 1", "Content 1", "General")
        fakeRepository.insertPrompt("Prompt 2", "Content 2", "Coding")

        val initial = vm.allPrompts.first { it.size == 2 }
        assertEquals(2, initial.size)

        vm.clearAllPrompts {}

        val afterClear = vm.allPrompts.first { it.isEmpty() }
        assertTrue(afterClear.isEmpty())
    }

    @Test
    fun `export prompts formatting test`() {
        val prompts = listOf(
            Prompt(id = 1L, name = "Reviewer", content = "Review code carefully", category = "Coding"),
            Prompt(id = 2L, name = "Writer", content = "Write clean prose", category = "Writing")
        )
        val text = viewModel.exportPromptsToText(prompts)
        assertTrue(text.contains("My Saved Prompts (2)"))
        assertTrue(text.contains("1. Reviewer"))
        assertTrue(text.contains("Review code carefully"))
        assertTrue(text.contains("2. Writer"))
    }
}
