package com.rpljumat.ti_tip

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_titipan_baru.*

class TitipanBaru : AppCompatActivity() {

    companion object {
        const val ADDRESS_DATA = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_titipan_baru)

        back.setOnClickListener {
            finish()
        }

        location_btn_new_titipan.setOnClickListener {
            val peta = Intent(this, PetaPilihAgen::class.java)
            startActivityForResult(peta, ADDRESS_DATA)
        }

        btn_buat_titipan.setOnClickListener {

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ADDRESS_DATA) {
            if(resultCode == Activity.RESULT_OK) {
                location_detail_text_new_titipan.text = data?.getStringExtra("Alamat")
            }
        }
    }
}