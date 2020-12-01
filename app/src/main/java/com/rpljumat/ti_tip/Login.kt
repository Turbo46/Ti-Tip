package com.rpljumat.ti_tip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login.*

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_btn.setOnClickListener {

        }

        regis_btn.setOnClickListener {
            val intent = Intent(this, User_Regis::class.java)
            startActivity(intent)
        }
    }
}