package com.budgetr.app.util

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
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
    }

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestScopes(Scope(SHEETS_SCOPE))
        .requestServerAuthCode(/* clientId = */ "YOUR_WEB_CLIENT_ID", true)
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
