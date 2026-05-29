package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class VerificationActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String

    // UI Elements
    private lateinit var tvTimer: TextView
    private lateinit var tvPhoneDisplay: TextView
    private lateinit var otpBoxes: Array<EditText>
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        auth = FirebaseAuth.getInstance()

        // Get the phone number passed from the Sign Up / Sign In page
        phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: "+94771234567"

        tvTimer = findViewById(R.id.tvTimer)
        tvPhoneDisplay = findViewById(R.id.tvPhoneDisplay)
        val btnVerify = findViewById<ImageButton>(R.id.btnVerify)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)

        tvPhoneDisplay.text = "Enter the verification code sent to\n$phoneNumber"

        // Setup the 6 OTP Boxes
        otpBoxes = arrayOf(
            findViewById(R.id.otp1), findViewById(R.id.otp2),
            findViewById(R.id.otp3), findViewById(R.id.otp4),
            findViewById(R.id.otp5), findViewById(R.id.otp6)
        )
        setupOtpInputLogic()

        // Send the initial SMS
        sendVerificationCode(phoneNumber)

        // Resend Button Click (Only works if timer is finished)
        tvTimer.setOnClickListener {
            if (tvTimer.text.toString() == "Resend Code") {
                sendVerificationCode(phoneNumber)
            }
        }

        // Verify Button Click
        btnVerify.setOnClickListener {
            val code = otpBoxes.joinToString("") { it.text.toString() }
            if (code.length == 6) {
                verifyCode(code)
            } else {
                Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener { finish() }
    }

    // --- MAGIC AUTO-MOVE OTP BOX LOGIC ---
    private fun setupOtpInputLogic() {
        for (i in otpBoxes.indices) {
            otpBoxes[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < otpBoxes.size - 1) {
                        otpBoxes[i + 1].requestFocus() // Move to next box
                    } else if (s?.isEmpty() == true && i > 0) {
                        otpBoxes[i - 1].requestFocus() // Move back on delete
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    // --- FIREBASE SMS LOGIC ---
    private fun sendVerificationCode(number: String) {
        startTimer() // Start the 45-second countdown

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-retrieval worked (mostly on Android devices reading the SMS automatically)
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@VerificationActivity, "Verification Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    // SMS sent successfully, store the ID
                    storedVerificationId = verificationId
                    resendToken = token
                    Toast.makeText(this@VerificationActivity, "Code Sent!", Toast.LENGTH_SHORT).show()
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        if (storedVerificationId.isNotEmpty()) {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Phone Verified Successfully!", Toast.LENGTH_SHORT).show()

                    // Route to Passenger Home Screen
                    val intent = Intent(this, PassengerHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Incorrect Code!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- COUNTDOWN TIMER ---
    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(45000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "Resend in 00:${String.format("%02d", millisUntilFinished / 1000)}"
                tvTimer.setTextColor(android.graphics.Color.parseColor("#888888"))
            }

            override fun onFinish() {
                tvTimer.text = "Resend Code"
                tvTimer.setTextColor(android.graphics.Color.parseColor("#3B67A8")) // Turn it blue so it looks clickable
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel() // Prevent memory leaks
    }
}
