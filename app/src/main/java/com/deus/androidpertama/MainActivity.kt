package com.deus.androidpertama

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val usernameField = findViewById<EditText>(R.id.editTextusername)
        val passwordField = findViewById<EditText>(R.id.editTextpassword)
        val btnLogin = findViewById<Button>(R.id.buttonSubmit)
        val btnDaftar = findViewById<Button>(R.id.buttonDaftar)

        btnLogin.setOnClickListener {
            val usernameOrEmail = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username/Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = AbsensiDatabase.getInstance(applicationContext)
            // Coba login dengan username dulu
            var user = db.userDao().getUserByUsername(usernameOrEmail)
            
            // Jika tidak ditemukan dengan username, coba dengan email
            if (user == null) {
                user = db.userDao().getUserByEmail(usernameOrEmail)
            }

            if (user == null || user.password != password) {
                Toast.makeText(this, "Username/Email atau password salah", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Anda login sebagai ${user.username}", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, DashboardActivity::class.java).apply {
                putExtra("username", user.username)
                putExtra("email", user.email)
                putExtra("firstName", user.firstName)
                putExtra("lastName", user.lastName)
                putExtra("userId", user.id)
            }
            startActivity(intent)
        }

        btnDaftar.setOnClickListener {
            val intent = Intent(this, formActivity::class.java)
            startActivity(intent)
        }
    }
}