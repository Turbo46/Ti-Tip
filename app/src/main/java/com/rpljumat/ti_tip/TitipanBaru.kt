package com.rpljumat.ti_tip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_titipan_baru.*

class TitipanBaru : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_titipan_baru)

        back.setOnClickListener {
            finish()
        }
    }
}