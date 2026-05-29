package com.example.lankagotest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class QrScannerActivity : AppCompatActivity() {

    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private lateinit var tvScanStatus: TextView
    private lateinit var database: DatabaseReference

    private val CAMERA_PERMISSION_CODE = 100
    private var isScanningPaused = false // Prevents scanning the same code 100 times a second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        database = FirebaseDatabase.getInstance().reference

        barcodeScannerView = findViewById(R.id.barcodeScannerView)
        tvScanStatus = findViewById(R.id.tvScanStatus)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // 1. Check Camera Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            startScanning()
        }
    }

    private fun startScanning() {
        barcodeScannerView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result != null && !isScanningPaused) {
                    val scannedCode = result.text // This will be "VERIFY_TICKET_778899"

                    isScanningPaused = true // Pause camera so we can process this code
                    tvScanStatus.text = "Verifying Ticket..."
                    tvScanStatus.setTextColor(android.graphics.Color.YELLOW)

                    verifyTicketInFirebase(scannedCode)
                }
            }
        })
    }

    private fun verifyTicketInFirebase(scannedCode: String) {
        // Search the "Bookings" folder for a ticket where the "qrData" matches what the camera just saw
        val query = database.child("Bookings").orderByChild("qrData").equalTo(scannedCode)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // TICKET FOUND!
                    for (ticketSnapshot in snapshot.children) {
                        val passengerName = ticketSnapshot.child("passengerName").getValue(String::class.java)
                        val seatNumber = ticketSnapshot.child("seatNumber").getValue(String::class.java)
                        val status = ticketSnapshot.child("status").getValue(String::class.java)

                        if (status == "BOARDED") {
                            // Uh oh, they already scanned this ticket!
                            tvScanStatus.text = "Warning: Ticket already used!"
                            tvScanStatus.setTextColor(android.graphics.Color.RED)
                            Toast.makeText(this@QrScannerActivity, "Ticket Already Used!", Toast.LENGTH_LONG).show()
                        } else {
                            // Valid Ticket! Update Firebase to mark them as Boarded
                            ticketSnapshot.ref.child("status").setValue("BOARDED")

                            tvScanStatus.text = "Success! $passengerName (Seat: $seatNumber)"
                            tvScanStatus.setTextColor(android.graphics.Color.GREEN)
                            Toast.makeText(this@QrScannerActivity, "Passenger Verified!", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    // TICKET NOT FOUND!
                    tvScanStatus.text = "Invalid Ticket Found"
                    tvScanStatus.setTextColor(android.graphics.Color.RED)
                    Toast.makeText(this@QrScannerActivity, "Fake or Invalid Ticket!", Toast.LENGTH_LONG).show()
                }

                // Resume scanning after 3 seconds so the driver can scan the next person
                barcodeScannerView.postDelayed({
                    isScanningPaused = false
                    tvScanStatus.text = "Align QR Code within the frame"
                    tvScanStatus.setTextColor(android.graphics.Color.WHITE)
                }, 3000)
            }

            override fun onCancelled(error: DatabaseError) {
                isScanningPaused = false
            }
        })
    }

    // Required lifecycle methods for the camera view to work properly
    override fun onResume() {
        super.onResume()
        barcodeScannerView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeScannerView.pause()
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanning()
        } else {
            Toast.makeText(this, "Camera permission is required to scan tickets", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
