package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an AI prompt in the local database.
 */
@Entity(tableName = "prompts")
data class Prompt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val content: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Sorting options for the prompt list.
 */
enum class SortOrder(val label: String) {
    RECENTLY_UPDATED("Recently updated"),
    RECENTLY_CREATED("Recently created"),
    ALPHABETICAL_ASC("Alphabetical A–Z"),
    ALPHABETICAL_DESC("Alphabetical Z–A")
}

/**
 * Default categories provided by the app.
 */
val DEFAULT_CATEGORIES = listOf(
    "General",
    "Coding",
    "Design",
    "Writing",
    "Marketing",
    "Business",
    "Research",
    "Image Generation",
    "Development",
    "Other"
)
