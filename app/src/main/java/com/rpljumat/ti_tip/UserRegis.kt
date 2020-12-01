package com.rpljumat.ti_tip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user__regis.*

class UserRegis : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user__regis)

        login_btn_user.setOnClickListener {
            val login = Intent(this, Login::class.java)
            startActivity(login)
            finish()
        }

        help_btn.setOnClickListener {
            val intent = Intent(this, Help::class.java)
            startActivity(intent)
        }
        regis_btn.setOnClickListener {
            val nama = fullname_text.text.toString()
            val username = username_text.text.toString()
            val nik = nik_text.text.toString()
            val phone = nope_text.text.toString()
            val email = email_text.text.toString()
            val pass = pass_text.text.toString()

            if(nama.isEmpty()){
                Toast.makeText(this@UserRegis, "Nama belum diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(username.isEmpty()){
                Toast.makeText(this@UserRegis, "Username belum diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(nik.isEmpty()){
                Toast.makeText(this@UserRegis, "NIK belum diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(phone.isEmpty()){
                Toast.makeText(this@UserRegis, "Nomor HP belum diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(email.isEmpty()){
                Toast.makeText(this@UserRegis, "Email belum diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(pass.isEmpty()){
                Toast.makeText(this@UserRegis, "Password belum diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
                            Toast.makeText(this@UserRegis, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()
                            val dashboard = Intent(this, Dashboard::class.java)
                            startActivity(dashboard)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@UserRegis, "Pendaftaran gagal", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this@UserRegis, "Pendaftaran gagal", Toast.LENGTH_SHORT).show()
                }
        }
    }
}