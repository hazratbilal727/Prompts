package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Prompt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Prompt::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prompts_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialPrompts(database.promptDao())
                    }
                }
            }
        }

        private suspend fun populateInitialPrompts(dao: PromptDao) {
            val now = System.currentTimeMillis()
            val initialPrompts = listOf(
                Prompt(
                    name = "Clean Code Reviewer",
                    category = "Coding",
                    content = """Review the following code for:
1. Architectural clean code best practices and readability
2. Edge cases, potential nullability or memory leaks
3. Performance bottlenecks and algorithmic efficiency
4. Clear recommendations with refactored code snippets

Code to review:
[Paste code here]""",
                    createdAt = now - 3600000 * 24 * 2,
                    updatedAt = now - 3600000 * 12
                ),
                Prompt(
                    name = "Engaging Social Media Hook",
                    category = "Marketing",
                    content = """Generate 5 viral hook variations for a social media post about [Topic].
Each hook must:
- Hook the reader in the first 8 words
- Spark curiosity without being misleading clickbait
- Use strong active verbs and psychological triggers
- Include a recommended CTA (call to action) at the end""",
                    createdAt = now - 3600000 * 24,
                    updatedAt = now - 3600000 * 6
                ),
                Prompt(
                    name = "Technical Concept Explainer (Feynman)",
                    category = "Writing",
                    content = """Explain [Technical Topic] as if I am an inquisitive 12-year-old.
Follow these steps:
1. Use an intuitive real-world physical analogy
2. Break down the core mechanism into 3 simple milestones
3. Highlight common misconceptions and clarify them simply
4. Conclude with a memorable 1-sentence TL;DR summary""",
                    createdAt = now - 3600000 * 8,
                    updatedAt = now - 3600000 * 2
                )
            )
            dao.insertAll(initialPrompts)
        }
    }
}
