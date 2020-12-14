package com.rpljumat.ti_tip

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_titipan_baru.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TitipanBaru : AppCompatActivity() {
    private lateinit var activityContext: TitipanBaru
    var conn = false

    companion object {
        const val ADDRESS_DATA = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_titipan_baru)

        activityContext = this

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

                val builder = AlertDialog.Builder(activityContext)
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
        val builder = AlertDialog.Builder(activityContext)
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

            val locTxt = location_detail_text_new_titipan.text.lines()
            val nama = locTxt[0]

            if(nama == "") {
                Toast.makeText(this@TitipanBaru, "Lokasi belum dipilih!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val db = FirebaseFirestore.getInstance()
            val agentId = getAgentId(db, nama)

            val fragileStat = fragile_chkbox_new_titipan.isChecked
            val groceryStat = grocery_chkbox_new_titipan.isChecked

            val currDT = System.currentTimeMillis()
            val expDays = if(groceryStat) 1 else 7
            val expDT = currDT + expDays * MS_SEHARI

            val goods = Goods(uId, agentId, nama, 1,
                AWAITING_CONFIRMATION, currDT, expDT, 15_000,
                0f, 0f, 0f, 0f,
                fragileStat, groceryStat)

            val goodsCollection = db.collection("goods")
            val docRef = goodsCollection.add(goods).await()

            val doc = docRef.id
            goodsCollection.document(doc).collection("agentHist")
                .document("agentHist").set(hashMapOf("1" to agentId))
                .addOnSuccessListener {
                    Toast.makeText(this@TitipanBaru, "Penitipan berhasil!", Toast.LENGTH_SHORT)
                        .show()

                    val notifMgr = getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
                    val notifBuilder = NotificationCompat.Builder(applicationContext, "Ti-Tip")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Titipan $nama berhasil dibuat")
                        .setContentText("Silahkan selesai titipan dalam $expDays hari")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                    notifMgr.notify(0, notifBuilder.build())
                }
                .addOnFailureListener {
                    Toast.makeText(this@TitipanBaru, "Penitipan gagal!", Toast.LENGTH_SHORT)
                        .show()
                }

            finish()
        }
    }

    private suspend fun getAgentId(db: FirebaseFirestore, nama: String): String {
        val allAgent = db.collection("agent").whereEqualTo("agentName", nama).get().await()
        return allAgent.documents[0].id
    }

}