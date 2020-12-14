package com.rpljumat.ti_tip

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*

class Login : AppCompatActivity() {
    private lateinit var activityContext: Login
    var conn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Get current user
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        // If already logged in, go to Dashboard
        if(currentUser != null){
            val dashboard = Intent(applicationContext, Dashboard::class.java)
            startActivity(dashboard)
            finish()
        }

        // Listener to Login button click
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
                    val dashboard = Intent(applicationContext, Dashboard::class.java)
                    startActivity(dashboard)
                    finish()
                }
                .addOnFailureListener {
                    // Check internet connection first
                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.Default) {
                            checkNetworkConnection()
                        }
                        if(!conn) {
                            alertNoConnection()
                            return@launch
                        }
                    }
                    Toast.makeText(this@Login, "Login gagal", Toast.LENGTH_SHORT).show()
                }
        }

        regis_btn.setOnClickListener {
            val login = Intent(applicationContext, UserRegis::class.java)
            startActivity(login)
            finish()
        }
    }

    private fun checkNetworkConnection() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        cm.registerNetworkCallback(
            builder.build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    conn = true
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    conn = false
                }
            }
        )
    }

    private fun alertNoConnection() {
        val builder = AlertDialog.Builder(activityContext)
        builder.setTitle("Tidak ada koneksi!")
            .setMessage("Pastikan Wi-Fi atau data seluler telah dinyalakan, lalu coba lagi")
            .setPositiveButton("Kembali") { _: DialogInterface, _: Int ->

            }
        builder.show()
    }
}