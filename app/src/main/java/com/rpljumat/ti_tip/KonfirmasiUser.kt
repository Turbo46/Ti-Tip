package com.rpljumat.ti_tip

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_konfirmasi_user.*

class KonfirmasiUser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konfirmasi_user)

        // Get passed data from TitipanBaru
        val namaTitipan = intent.getStringExtra("Nama Titipan")
        val lokasiTitipan = intent.getStringExtra("Lokasi Titipan")
        val grocery = intent.getBooleanExtra("Grocery", false)
        val fragile = intent.getBooleanExtra("Fragile", false)

        nama_titipan.text = namaTitipan
        lokasi_penitipan.text = lokasiTitipan
        grocery_konfirmasi.text = if(grocery) "Barang basah" else "Tidak basah"
        grocery_konfirmasi.setTextColor(if(grocery) red else black)
        fragile_konfirmasi.text = if(fragile) "Barang pecah belah" else "Tidak pecah belah"
        fragile_konfirmasi.setTextColor(if(grocery) red else black)

        Toast.makeText(this@KonfirmasiUser, "Penitipan berhasil!", Toast.LENGTH_SHORT).show()

        val notifMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifBuilder = NotificationCompat.Builder(applicationContext, "Ti-Tip")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Titipan $namaTitipan berhasil dibuat")
            .setContentText("Silahkan selesaikan titipan dalam 1 x 24 jam")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        notifMgr.notify(0, notifBuilder.build())

        tombol_konfirmasi.setOnClickListener {
            finish()
            val dashboard = Intent(this, Dashboard::class.java)
            startActivity(dashboard)
        }
    }
}