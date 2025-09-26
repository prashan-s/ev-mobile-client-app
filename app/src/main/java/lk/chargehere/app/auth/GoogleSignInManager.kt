package lk.chargehere.app.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class GoogleSignInManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "GoogleSignInManager"
        // Default web client ID from google-services.json
        private const val SERVER_CLIENT_ID = "109184460554075693946"
    }
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private var activity: ComponentActivity? = null
    private var signInLauncher: ActivityResultLauncher<Intent>? = null
    private var pendingSignInContinuation: kotlin.coroutines.Continuation<GoogleSignInResult>? = null
    
    fun initialize(activity: ComponentActivity) {
        this.activity = activity
        
        // Configure Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(SERVER_CLIENT_ID)
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        
        // Register activity result launcher
        signInLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleSignInResult(result.data)
        }
    }
    
    suspend fun signIn(): GoogleSignInResult {
        return suspendCancellableCoroutine { continuation ->
            // Check if already signed in to Firebase
            val currentUser = auth.currentUser
            if (currentUser != null) {
                Log.d(TAG, "User already signed in to Firebase")
                currentUser.getIdToken(true).addOnCompleteListener {
                    Log.d(TAG, "User ID token: ${it.result.token}")
                    continuation.resume(
                        GoogleSignInResult.Success(
                            idToken = it.result.token ?: "", // Use Firebase UID as token
                            email = currentUser.email ?: "",
                            displayName = currentUser.displayName ?: ""
                        )
                    )
                }
                return@suspendCancellableCoroutine
            }
            
            val currentActivity = activity
            val launcher = signInLauncher
            
            if (currentActivity == null || launcher == null) {
                Log.e(TAG, "GoogleSignInManager not initialized. Call initialize() first.")
                continuation.resume(GoogleSignInResult.Error("GoogleSignInManager not initialized. Call initialize() first."))
                return@suspendCancellableCoroutine
            }
            
            // Store the continuation to be resumed when result is received
            pendingSignInContinuation = continuation
            
            // Start Google Sign-In intent
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
            
            // Handle cancellation
            continuation.invokeOnCancellation {
                Log.d(TAG, "Sign-in was cancelled")
                pendingSignInContinuation = null
            }
        }
    }
    
    private fun handleSignInResult(data: Intent?) {
        val continuation = pendingSignInContinuation
        if (continuation == null) {
            Log.w(TAG, "No pending continuation for sign-in result")
            return
        }
        
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            if (account?.idToken != null) {
                // Sign in to Firebase with Google
                firebaseAuthWithGoogle(account.idToken!!, continuation)
            } else {
                Log.e(TAG, "Google Sign-In failed: No ID token received")
                continuation.resume(GoogleSignInResult.Error("Google Sign-In failed: No ID token received"))
                pendingSignInContinuation = null
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed with code: ${e.statusCode}", e)
            continuation.resume(GoogleSignInResult.Error("Google Sign-In failed: ${e.message}"))
            pendingSignInContinuation = null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            continuation.resume(GoogleSignInResult.Error("Sign-in error: ${e.message}"))
            pendingSignInContinuation = null
        }
    }
    
    private fun firebaseAuthWithGoogle(
        idToken: String,
        continuation: kotlin.coroutines.Continuation<GoogleSignInResult>
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success ")
                    val user = auth.currentUser
                    if (user != null) {
                        user.getIdToken(true).addOnCompleteListener {
                            Log.d(TAG, "User ID token: ${it.result.token}")
                            continuation.resume(
                                GoogleSignInResult.Success(
                                    idToken = it.result.token ?: "",
                                    email = user.email ?: "",
                                    displayName = user.displayName ?: ""
                                )
                            )
                        }

                    } else {
                        continuation.resume(GoogleSignInResult.Error("Firebase user is null after successful sign-in"))
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    continuation.resume(GoogleSignInResult.Error("Firebase authentication failed: ${task.exception?.message}"))
                }
                pendingSignInContinuation = null
            }
    }
    
    suspend fun signOut(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            try {
                // Sign out from Firebase
                auth.signOut()
                
                // Sign out from Google Sign-In if initialized
                if (::googleSignInClient.isInitialized) {
                    googleSignInClient.signOut().addOnCompleteListener {
                        Log.d(TAG, "User signed out successfully from Firebase and Google")
                        continuation.resume(true)
                    }
                } else {
                    Log.d(TAG, "User signed out successfully from Firebase")
                    continuation.resume(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign out failed", e)
                continuation.resume(false)
            }
        }
    }
    
    suspend fun revokeAccess(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "No user signed in to revoke access")
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }
            
            // Revoke access from Google Sign-In if initialized
            if (::googleSignInClient.isInitialized) {
                googleSignInClient.revokeAccess().addOnCompleteListener { revokeTask ->
                    if (revokeTask.isSuccessful) {
                        // Delete Firebase user account
                        currentUser.delete().addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                Log.d(TAG, "User account deleted and access revoked successfully")
                                continuation.resume(true)
                            } else {
                                Log.e(TAG, "Account deletion failed", deleteTask.exception)
                                continuation.resume(false)
                            }
                        }
                    } else {
                        Log.e(TAG, "Access revocation failed", revokeTask.exception)
                        continuation.resume(false)
                    }
                }
            } else {
                // Just delete Firebase user if Google Sign-In not initialized
                currentUser.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User account deleted successfully")
                        continuation.resume(true)
                    } else {
                        Log.e(TAG, "Account deletion failed", task.exception)
                        continuation.resume(false)
                    }
                }
            }
        }
    }
    
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }
}

sealed class GoogleSignInResult {
    data class Success(
        val idToken: String,
        val email: String,
        val displayName: String
    ) : GoogleSignInResult()
    
    data class Error(val message: String) : GoogleSignInResult()
}