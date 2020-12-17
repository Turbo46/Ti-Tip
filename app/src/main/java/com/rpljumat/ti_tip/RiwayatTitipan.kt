package com.rpljumat.ti_tip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_help.*
import kotlinx.android.synthetic.main.activity_riwayat_titipan.*

class RiwayatTitipan : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_titipan)
        tombol_back_riwayat.setOnClickListener {
            val back = Intent(this, Dashboard::class.java)
            startActivity(back)
            finish()
        }
    }
}