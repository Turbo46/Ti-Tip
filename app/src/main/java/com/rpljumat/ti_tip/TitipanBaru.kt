package com.rpljumat.ti_tip

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_titipan_baru.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TitipanBaru : AppCompatActivity() {
    var conn = false

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
            // Check all fields are filled
            if(name_new_titipan.text.isEmpty()) {
                Toast.makeText(this@TitipanBaru,
                    "Nama titipan tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(location_detail_text_new_titipan.text.isEmpty()) {
                Toast.makeText(this@TitipanBaru,
                    "Silahkan pilih lokasi penitipan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check internet connection first
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Default) {
                    checkNetworkConnection()
                }
                if(!conn) {
                    alertNoConnection()
                    return@launch
                }

                val builder = AlertDialog.Builder(this@TitipanBaru)
                builder.setMessage("Informasi yang dimasukkan sudah benar?")
                    .setPositiveButton("Ya") { _: DialogInterface, _: Int ->
                        storeNewTitipan()
                    }
                    .setNegativeButton("Tidak") { _: DialogInterface, _: Int ->

                    }
                builder.show()
            }
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

    private fun checkNetworkConnection() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        cm.registerNetworkCallback(
            builder.build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    conn = true
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    conn = false
                }
            }
        )
    }

    private fun alertNoConnection() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tidak ada koneksi!")
            .setMessage("Pastikan Wi-Fi atau data seluler telah dinyalakan, lalu coba lagi")
            .setPositiveButton("Kembali") { _: DialogInterface, _: Int ->

            }
        builder.show()
    }

    private fun storeNewTitipan(){
        CoroutineScope(Dispatchers.Main).launch {
            val auth = FirebaseAuth.getInstance()
            val uId = auth.uid?:""

            val namaTitipan = name_new_titipan.text.toString()
            if(namaTitipan == "") {
                Toast.makeText(this@TitipanBaru, "Nama titipan tidak boleh kosong!",
                    Toast.LENGTH_SHORT).show()
                return@launch
            }

            val loc = location_detail_text_new_titipan.text.toString()
            if(loc == "") {
                Toast.makeText(this@TitipanBaru, "Lokasi belum dipilih!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val agentName = loc.lines()[0]

            val db = FirebaseFirestore.getInstance()
            val agentId = getAgentId(db, agentName)

            val fragileStat = fragile_chkbox_new_titipan.isChecked
            val groceryStat = grocery_chkbox_new_titipan.isChecked

            val currDT = System.currentTimeMillis()
            val expDT = currDT + MS_SEHARI

            val goods = Goods(uId, agentId, namaTitipan, 1,
                AWAITING_CONFIRMATION, currDT, expDT, 15_000,
                0f, 0f, 0f, 0f,
                fragileStat, groceryStat)

            val goodsCollection = db.collection("goods")
            val docRef = goodsCollection.add(goods).await()

            val doc = docRef.id
            goodsCollection.document(doc).collection("agentHist")
                .document("agentHist").set(hashMapOf("1" to agentId))
                .addOnSuccessListener {
                    // Close all child activities and refresh the dashboard
                    finishAffinity()
                    val konfirmasiUser = Intent(applicationContext, KonfirmasiUser::class.java)
                    konfirmasiUser.putExtra("Nama Titipan", namaTitipan)
                    konfirmasiUser.putExtra("Lokasi Titipan", loc)
                    konfirmasiUser.putExtra("Grocery", groceryStat)
                    konfirmasiUser.putExtra("Fragile", fragileStat)
                    startActivity(konfirmasiUser)
                }
                .addOnFailureListener {
                    Toast.makeText(this@TitipanBaru, "Penitipan gagal!", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
        }
    }

}