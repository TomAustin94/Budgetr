package com.budgetr.app.util

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Named

class PreferencesManager @Inject constructor(
    @Named("encrypted") private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_SPREADSHEET_ID = "spreadsheet_id"
        private const val KEY_SPREADSHEET_NAME = "spreadsheet_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHOTO = "user_photo"
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun setAccessToken(token: String?) = prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()

    fun getSpreadsheetId(): String? = prefs.getString(KEY_SPREADSHEET_ID, null)
    fun setSpreadsheetId(id: String?) = prefs.edit().putString(KEY_SPREADSHEET_ID, id).apply()

    fun getSpreadsheetName(): String? = prefs.getString(KEY_SPREADSHEET_NAME, null)
    fun setSpreadsheetName(name: String?) = prefs.edit().putString(KEY_SPREADSHEET_NAME, name).apply()

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun setUserEmail(email: String?) = prefs.edit().putString(KEY_USER_EMAIL, email).apply()

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun setUserName(name: String?) = prefs.edit().putString(KEY_USER_NAME, name).apply()

    fun getUserPhoto(): String? = prefs.getString(KEY_USER_PHOTO, null)
    fun setUserPhoto(url: String?) = prefs.edit().putString(KEY_USER_PHOTO, url).apply()

    fun clearAll() = prefs.edit().clear().apply()
}
