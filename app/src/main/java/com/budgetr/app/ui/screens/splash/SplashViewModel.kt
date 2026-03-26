package com.budgetr.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.budgetr.app.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    fun isSignedIn(): Boolean = authManager.isSignedIn()
}
