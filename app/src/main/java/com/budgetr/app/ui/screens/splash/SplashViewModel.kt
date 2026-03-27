package com.budgetr.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.budgetr.app.util.AuthManager
import com.budgetr.app.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val prefs: PreferencesManager
) : ViewModel() {

    fun isSignedIn(): Boolean = authManager.isSignedIn()
    fun hasSpreadsheet(): Boolean = prefs.hasSpreadsheet()
}
