package com.rpljumat.ti_tip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user__regis.*

class User_Regis : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user__regis)

        regis_btn.setOnClickListener {
            val nama = fullname_text.text.toString()
            val username = username_text.text.toString()
            val nik = nik_text.text.toString()
            val phone = nope_text.text.toString()
            val email = email_text.text.toString()
            val pass = pass_text.text.toString()

            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    val uid = auth.uid?:""
                    val db = FirebaseFirestore.getInstance()
                    val user = User(nama, username, nik, phone)

                    db.collection("users")
                        .document(uid)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this@User_Regis, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this@User_Regis, "Pendaftaran gagal", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

class User(val nama: String, val username: String, val nik: String, val phone: String)