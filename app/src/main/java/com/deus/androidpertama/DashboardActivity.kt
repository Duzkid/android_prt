package com.deus.androidpertama

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import android.widget.TextView
import android.widget.Button
import android.widget.Toast

class DashboardActivity : AppCompatActivity() {
    
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val usernameView = findViewById<TextView>(R.id.tvUsername)
        val emailView = findViewById<TextView>(R.id.tvEmail)
        val firstNameView = findViewById<TextView>(R.id.tvFirstname)
        val lastNameView = findViewById<TextView>(R.id.tvLastname)
        val logoutButton = findViewById<Button>(R.id.buttonLogout)
        val absenButton = findViewById<Button>(R.id.buttonAbsen)

        val db = AbsensiDatabase.getInstance(applicationContext)
        val latestUser = db.userDao().getLatestUser()

        val username = intent.getStringExtra("username") ?: latestUser?.username ?: "-"
        val email = intent.getStringExtra("email") ?: latestUser?.email ?: "-"
        val firstName = intent.getStringExtra("firstName") ?: latestUser?.firstName ?: "-"
        val lastName = intent.getStringExtra("lastName") ?: latestUser?.lastName ?: "-"

        usernameView.text = username
        emailView.text = email
        firstNameView.text = firstName
        lastNameView.text = lastName

        logoutButton.setOnClickListener {
            finish()
        }

        absenButton.setOnClickListener {
            // Cek permission lokasi
            checkLocationPermissionAndShowMap()
        }
    }
    
    private fun checkLocationPermissionAndShowMap() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val hasPermission = permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        if (hasPermission) {
            // Permission sudah diberikan, tampilkan map dengan lokasi user
            showMapDialog(true)
            Toast.makeText(this, "Absen Berhasil", Toast.LENGTH_SHORT).show()
        } else {
            // Request permission
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            val permissionGranted = grantResults.isNotEmpty() && 
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            
            if (permissionGranted) {
                // Permission diberikan, tampilkan map dengan lokasi user
                showMapDialog(true)
                Toast.makeText(this, "Absen Berhasil", Toast.LENGTH_SHORT).show()
            } else {
                // Permission ditolak, tampilkan map dengan lokasi default Bandung
                showMapDialog(false)
                Toast.makeText(this, "Absen Berhasil (Lokasi default: Bandung)", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showMapDialog(hasLocationPermission: Boolean) {
        val dialog = MapDialogFragment.newInstance(hasLocationPermission)
        dialog.show(supportFragmentManager, "MapDialog")
    }
}