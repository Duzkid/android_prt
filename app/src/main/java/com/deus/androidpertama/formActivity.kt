package com.deus.androidpertama

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.deus.androidpertama.R

class formActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        val usernameField = findViewById<EditText>(R.id.editEmail)
        val emailField = findViewById<EditText>(R.id.editPhone)
        val firstNameField = findViewById<EditText>(R.id.editFirstName)
        val lastNameField = findViewById<EditText>(R.id.editLastName)
        val passwordField = findViewById<EditText>(R.id.editPassword)
        val confirmPasswordField = findViewById<EditText>(R.id.editConfirmPassword)

        val submitButton = findViewById<Button>(R.id.btnSubmit)
        val cancelButton = findViewById<Button>(R.id.btnCancel)

        submitButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val firstName = firstNameField.text.toString().trim()
            val lastName = lastNameField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || firstName.isEmpty() ||
                lastName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
            ) {
                Toast.makeText(this, "Semua field wajib diisi ya kak", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password salah cok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "User $firstName $lastName berhasil didaftarkan omedetto",
                Toast.LENGTH_LONG
            ).show()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }
}
