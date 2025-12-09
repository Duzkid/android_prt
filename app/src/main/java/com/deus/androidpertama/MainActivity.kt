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
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = AbsensiDatabase.getInstance(applicationContext)
            val user = db.userDao().getUserByUsername(username)

            if (user == null || user.password != password) {
                Toast.makeText(this, "Username atau password salah", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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