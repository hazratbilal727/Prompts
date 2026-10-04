package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Prompt
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY updatedAt DESC")
    fun getAllPrompts(): Flow<List<Prompt>>

    @Query("SELECT * FROM prompts WHERE id = :id")
    fun getPromptById(id: Long): Flow<Prompt?>

    @Query("SELECT * FROM prompts WHERE id = :id")
    suspend fun getPromptByIdOnce(id: Long): Prompt?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: Prompt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prompts: List<Prompt>)

    @Update
    suspend fun updatePrompt(prompt: Prompt)

    @Delete
    suspend fun deletePrompt(prompt: Prompt)

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun deletePromptById(id: Long)

    @Query("DELETE FROM prompts")
    suspend fun deleteAllPrompts()

    @Query("SELECT COUNT(*) FROM prompts")
    suspend fun getPromptCount(): Int
}
