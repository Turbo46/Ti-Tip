package com.rpljumat.ti_tip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_dashboard.*

class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        var db = FirebaseFirestore.getInstance()
        db.collection("goods")
            .document(uid?:"")
            .get()
            .addOnSuccessListener {

            }
            .addOnFailureListener {
                return@addOnFailureListener
            }

        btn_add_titipan.setOnClickListener {
            val newTitipan = Intent(this, Dashboard::class.java)
            startActivity(newTitipan)
        }
    }
}