package com.example.data.repository

import com.example.data.local.PromptDao
import com.example.data.model.Prompt
import kotlinx.coroutines.flow.Flow

interface PromptRepository {
    val allPrompts: Flow<List<Prompt>>
    fun getPromptById(id: Long): Flow<Prompt?>
    suspend fun getPromptByIdOnce(id: Long): Prompt?
    suspend fun insertPrompt(name: String, content: String, category: String): Long
    suspend fun updatePrompt(id: Long, name: String, content: String, category: String)
    suspend fun deletePrompt(prompt: Prompt)
    suspend fun deletePromptById(id: Long)
    suspend fun deleteAllPrompts()
}

class PromptRepositoryImpl(
    private val promptDao: PromptDao
) : PromptRepository {

    override val allPrompts: Flow<List<Prompt>> = promptDao.getAllPrompts()

    override fun getPromptById(id: Long): Flow<Prompt?> = promptDao.getPromptById(id)

    override suspend fun getPromptByIdOnce(id: Long): Prompt? = promptDao.getPromptByIdOnce(id)

    override suspend fun insertPrompt(name: String, content: String, category: String): Long {
        val now = System.currentTimeMillis()
        val prompt = Prompt(
            name = name.trim(),
            content = content.trim(),
            category = category.trim(),
            createdAt = now,
            updatedAt = now
        )
        return promptDao.insertPrompt(prompt)
    }

    override suspend fun updatePrompt(id: Long, name: String, content: String, category: String) {
        val existing = promptDao.getPromptByIdOnce(id)
        val createdAt = existing?.createdAt ?: System.currentTimeMillis()
        val updated = Prompt(
            id = id,
            name = name.trim(),
            content = content.trim(),
            category = category.trim(),
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis()
        )
        promptDao.updatePrompt(updated)
    }

    override suspend fun deletePrompt(prompt: Prompt) {
        promptDao.deletePrompt(prompt)
    }

    override suspend fun deletePromptById(id: Long) {
        promptDao.deletePromptById(id)
    }

    override suspend fun deleteAllPrompts() {
        promptDao.deleteAllPrompts()
    }
}
