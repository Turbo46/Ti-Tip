package com.rpljumat.ti_tip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_help.*

class Help : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        tombol_back_help.setOnClickListener {
            val back = Intent(this, UserRegis::class.java)
            startActivity(back)
            finish()
        }
    }
}