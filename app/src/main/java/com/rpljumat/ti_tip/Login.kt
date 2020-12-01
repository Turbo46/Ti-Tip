package com.rpljumat.ti_tip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if(currentUser != null){
            val dashboard = Intent(this, Dashboard::class.java)
            startActivity(dashboard)
            finish()
        }

        login_btn.setOnClickListener {
            val email = email_text_login.text.toString()
            val pass = password_text.text.toString()

            if(email.isEmpty()){
                Toast.makeText(this@Login, "Email tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(pass.isEmpty()){
                Toast.makeText(this@Login, "Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    Toast.makeText(this@Login, "Login berhasil", Toast.LENGTH_SHORT).show()
                    val dashboard = Intent(this, Dashboard::class.java)
                    startActivity(dashboard)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this@Login, "Login gagal", Toast.LENGTH_SHORT).show()
                }
        }

        regis_btn.setOnClickListener {
            val login = Intent(this, UserRegis::class.java)
            startActivity(login)
            finish()
        }
    }
}