package com.fire.chat.firechat

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fire.chat.firechat.R
import com.fire.chat.firechat.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)//CONNECTS JAVA TO THE XML

        login_button_login.setOnClickListener {
            performLogin()
        }

        back_to_register_textview.setOnClickListener{
            finish()
        }
    }

    private fun performLogin() {
        val email = email_edittext_login.text.toString().trim()
        val password = password_edittext_login.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out email/pw.", Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = ProgressDialog(this@LoginActivity)
        dialog.setMessage("Please wait !!!")
        dialog.show()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("Login", "Successfully logged in: ${it.result?.user?.uid}")
                dialog.dismiss()
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(this, "Failed to log in: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}