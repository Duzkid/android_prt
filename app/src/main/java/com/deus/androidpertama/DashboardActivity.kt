package com.deus.androidpertama

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Button

class DashboardActivity : AppCompatActivity() {
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
        val logoutButton = findViewById<Button>(R.id.button2)

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
    }
}