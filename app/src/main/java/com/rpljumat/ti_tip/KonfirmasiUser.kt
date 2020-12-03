package com.rpljumat.ti_tip

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatViewInflater

class KonfirmasiUser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konfirmasi_user)
        }
        fun konfirmasi(view: View?) {
        val pop_up = AlertDialog.Builder(this)
        pop_up.setTitle("Pemberitahuan")
        pop_up.setMessage("Titipan berhasil dibuat!")
        pop_up.show()

    }
}