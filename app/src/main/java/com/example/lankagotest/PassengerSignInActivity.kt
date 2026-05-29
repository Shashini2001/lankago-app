package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class PassengerSignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_sign_in)

        // Initialize Firebase Auth and Credential Manager
        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        // Find UI elements
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignIn = findViewById<ImageButton>(R.id.btnSignIn)
        val tvCreate = findViewById<TextView>(R.id.tvCreate)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val btnGoogle = findViewById<LinearLayout>(R.id.btnGoogle)
        val btnFacebook = findViewById<LinearLayout>(R.id.btnFacebook)

        // 1. Handle Sign In Button Click
        btnSignIn.setOnClickListener {
            val phone = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Basic validation
            if (phone.isEmpty()) {
                etUsername.error = "Phone number is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                return@setOnClickListener
            }

            // Create a Firebase-friendly email behind the scenes using phone number
            val firebaseEmail = "$phone@passenger.lankago.app"

            // Authenticate with Firebase
            auth.signInWithEmailAndPassword(firebaseEmail, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, PassengerNotificationActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Better error handling
                        val errorMessage = when (task.exception) {
                            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "No account found with this email. Please Sign Up."
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                            else -> "Authentication failed: ${task.exception?.message}"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }

        // 2. Handle 'Create' text click (Navigate to Sign Up Screen)
        tvCreate.setOnClickListener {
            val intent = Intent(this, PassengerSignUpActivity::class.java)
            startActivity(intent)
        }

        // 3. Handle Forgot Password
        tvForgotPassword.setOnClickListener {
            val phone = etUsername.text.toString().trim()
            if (phone.isEmpty()) {
                Toast.makeText(this, "Enter your phone number above to reset password", Toast.LENGTH_SHORT).show()
            } else {
                val firebaseEmail = "$phone@passenger.lankago.app"
                auth.sendPasswordResetEmail(firebaseEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Reset email sent to $firebaseEmail", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 4. Handle Google Sign In
        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // 5. Handle Facebook Sign In
        btnFacebook.setOnClickListener {
            // Facebook Login implementation requires the Facebook SDK
            // For now, showing a message.
            Toast.makeText(this, "Facebook Sign-In coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithGoogle() {
        val serverClientId = getString(R.string.default_web_client_id)
        if (serverClientId == "YOUR_WEB_CLIENT_ID_HERE") {
            Toast.makeText(this, "Please configure Google Web Client ID in strings.xml", Toast.LENGTH_LONG).show()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Show all accounts for now
            .setServerClientId(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@PassengerSignInActivity
                )
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e("GoogleSignIn", "Error: ${e.message}")
                Toast.makeText(this@PassengerSignInActivity, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Error parsing ID token: ${e.message}")
            }
        } else {
            Log.e("GoogleSignIn", "Unexpected credential type: ${credential.type}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Google Login Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, PassengerNotificationActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Firebase Auth failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
