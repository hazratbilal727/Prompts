package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.SortOrder
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _userAccount = MutableStateFlow<UserAccount?>(loadUserAccount())
    val userAccount: StateFlow<UserAccount?> = _userAccount.asStateFlow()

    private val _defaultCategory = MutableStateFlow(
        prefs.getString(KEY_DEFAULT_CATEGORY, "General") ?: "General"
    )
    val defaultCategory: StateFlow<String> = _defaultCategory.asStateFlow()

    private val _defaultSortOrder = MutableStateFlow(
        try {
            val name = prefs.getString(KEY_DEFAULT_SORT_ORDER, SortOrder.RECENTLY_UPDATED.name)
            SortOrder.valueOf(name ?: SortOrder.RECENTLY_UPDATED.name)
        } catch (_: Exception) {
            SortOrder.RECENTLY_UPDATED
        }
    )
    val defaultSortOrder: StateFlow<SortOrder> = _defaultSortOrder.asStateFlow()

    private val _confirmDelete = MutableStateFlow(
        prefs.getBoolean(KEY_CONFIRM_DELETE, true)
    )
    val confirmDelete: StateFlow<Boolean> = _confirmDelete.asStateFlow()

    private val _autoCopyOnSave = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_COPY_ON_SAVE, false)
    )
    val autoCopyOnSave: StateFlow<Boolean> = _autoCopyOnSave.asStateFlow()

    private val _previewLines = MutableStateFlow(
        prefs.getInt(KEY_PREVIEW_LINES, 3)
    )
    val previewLines: StateFlow<Int> = _previewLines.asStateFlow()

    private val _googleClientId = MutableStateFlow(
        prefs.getString(KEY_GOOGLE_CLIENT_ID, "") ?: ""
    )
    val googleClientId: StateFlow<String> = _googleClientId.asStateFlow()

    private fun loadUserAccount(): UserAccount? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val name = prefs.getString(KEY_USER_NAME, "Google User") ?: "Google User"
        val pic = prefs.getString(KEY_USER_PIC, null)
        val token = prefs.getString(KEY_USER_TOKEN, null)
        val signedInAt = prefs.getLong(KEY_USER_SIGNED_IN_AT, System.currentTimeMillis())

        return UserAccount(
            id = id,
            email = email,
            displayName = name,
            profilePictureUrl = pic,
            idToken = token,
            signedInAt = signedInAt
        )
    }

    fun saveUserAccount(account: UserAccount) {
        prefs.edit()
            .putString(KEY_USER_ID, account.id)
            .putString(KEY_USER_EMAIL, account.email)
            .putString(KEY_USER_NAME, account.displayName)
            .putString(KEY_USER_PIC, account.profilePictureUrl)
            .putString(KEY_USER_TOKEN, account.idToken)
            .putLong(KEY_USER_SIGNED_IN_AT, account.signedInAt)
            .apply()
        _userAccount.value = account
    }

    fun clearUserAccount() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_PIC)
            .remove(KEY_USER_TOKEN)
            .remove(KEY_USER_SIGNED_IN_AT)
            .apply()
        _userAccount.value = null
    }

    fun setDefaultCategory(category: String) {
        prefs.edit().putString(KEY_DEFAULT_CATEGORY, category).apply()
        _defaultCategory.value = category
    }

    fun setDefaultSortOrder(sortOrder: SortOrder) {
        prefs.edit().putString(KEY_DEFAULT_SORT_ORDER, sortOrder.name).apply()
        _defaultSortOrder.value = sortOrder
    }

    fun setConfirmDelete(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONFIRM_DELETE, enabled).apply()
        _confirmDelete.value = enabled
    }

    fun setAutoCopyOnSave(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_COPY_ON_SAVE, enabled).apply()
        _autoCopyOnSave.value = enabled
    }

    fun setPreviewLines(lines: Int) {
        val clamped = lines.coerceIn(1, 10)
        prefs.edit().putInt(KEY_PREVIEW_LINES, clamped).apply()
        _previewLines.value = clamped
    }

    fun setGoogleClientId(clientId: String) {
        prefs.edit().putString(KEY_GOOGLE_CLIENT_ID, clientId.trim()).apply()
        _googleClientId.value = clientId.trim()
    }

    companion object {
        private const val PREFS_NAME = "prompts_user_settings"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PIC = "user_pic"
        private const val KEY_USER_TOKEN = "user_token"
        private const val KEY_USER_SIGNED_IN_AT = "user_signed_in_at"

        private const val KEY_DEFAULT_CATEGORY = "default_category"
        private const val KEY_DEFAULT_SORT_ORDER = "default_sort_order"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_AUTO_COPY_ON_SAVE = "auto_copy_on_save"
        private const val KEY_PREVIEW_LINES = "preview_lines"
        private const val KEY_GOOGLE_CLIENT_ID = "google_client_id"
    }
}
