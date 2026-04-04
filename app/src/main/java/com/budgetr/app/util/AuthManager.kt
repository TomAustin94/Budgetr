package com.budgetr.app.util

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: PreferencesManager
) {
    companion object {
        const val SHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets"
        const val DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.metadata.readonly"
        const val TOKEN_SCOPE = "oauth2:$SHEETS_SCOPE $DRIVE_SCOPE"
    }

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .build()

    private val client: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent = client.signInIntent

    fun isSignedIn(): Boolean = GoogleSignIn.getLastSignedInAccount(context) != null

    fun handleSignInResult(account: GoogleSignInAccount, accessToken: String) {
        prefs.setAccessToken(accessToken)
        prefs.setUserEmail(account.email)
        prefs.setUserName(account.displayName)
        prefs.setUserPhoto(account.photoUrl?.toString())
    }

    fun getAccessToken(): String? = prefs.getAccessToken()

    /**
     * Clears the cached token and fetches a fresh one. Runs synchronously — call only from
     * a background thread (OkHttp interceptor threads are fine).
     */
    fun refreshToken(): String? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        return try {
            GoogleAuthUtil.clearToken(context, prefs.getAccessToken() ?: return null)
            val newToken = GoogleAuthUtil.getToken(context, account.account!!, TOKEN_SCOPE)
            prefs.setAccessToken(newToken)
            newToken
        } catch (e: Exception) {
            null
        }
    }

    fun signOut(onComplete: () -> Unit) {
        client.signOut().addOnCompleteListener {
            prefs.clearAll()
            onComplete()
        }
    }

    fun getUserName(): String? = prefs.getUserName()
    fun getUserEmail(): String? = prefs.getUserEmail()
    fun getUserPhoto(): String? = prefs.getUserPhoto()
}
