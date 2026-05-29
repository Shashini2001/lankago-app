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
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class DriverVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String = ""
    private lateinit var phoneNumber: String

    private lateinit var otpBoxes: Array<EditText>
    private lateinit var tvResendTimer: TextView
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_verification)

        auth = FirebaseAuth.getInstance()

        // Receive the phone number passed from the Register Activity
        phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""

        findViewById<TextView>(R.id.tvVerificationSubtitle).text = "Enter the verification code sent to\n$phoneNumber"

        val btnBack = findViewById<LinearLayout>(R.id.btnBack)
        val btnVerify = findViewById<ImageButton>(R.id.btnVerify)
        tvResendTimer = findViewById(R.id.tvResendTimer)

        otpBoxes = arrayOf(
            findViewById(R.id.otp1), findViewById(R.id.otp2), findViewById(R.id.otp3),
            findViewById(R.id.otp4), findViewById(R.id.otp5), findViewById(R.id.otp6)
        )

        setupOtpBoxAutoAdvance()
        startResendTimer()
        sendVerificationCode(phoneNumber)

        btnBack.setOnClickListener { finish() }

        btnVerify.setOnClickListener {
            val code = otpBoxes.joinToString("") { it.text.toString() }
            if (code.length == 6) {
                verifyCode(code)
            } else {
                Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupOtpBoxAutoAdvance() {
        for (i in otpBoxes.indices) {
            otpBoxes[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < otpBoxes.size - 1) {
                        otpBoxes[i + 1].requestFocus() // Move to next box
                    } else if (s?.length == 0 && i > 0) {
                        otpBoxes[i - 1].requestFocus() // Move back if deleted
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-retrieval completed successfully
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@DriverVerificationActivity, "Verification Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = verId
                    Toast.makeText(this@DriverVerificationActivity, "Code Sent!", Toast.LENGTH_SHORT).show()
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        // Link the verified phone number to the current user, or sign them in
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Phone Verified Successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to the Driver Dashboard
                val intent = Intent(this, DriverHomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid Verification Code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startResendTimer() {
        tvResendTimer.isClickable = false
        countDownTimer = object : CountDownTimer(45000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvResendTimer.text = "Resend in 00:${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                tvResendTimer.text = "Resend Code"
                tvResendTimer.setTextColor(android.graphics.Color.parseColor("#3B67A8"))
                tvResendTimer.isClickable = true

                tvResendTimer.setOnClickListener {
                    sendVerificationCode(phoneNumber)
                    startResendTimer()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}
