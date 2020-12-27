package com.rpljumat.ti_tip

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_pindah_titip.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PindahTitip : AppCompatActivity() {

    companion object {
        const val ADDRESS_DATA = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pindah_titip)

        val goodsId = intent.getStringExtra("ID titipan")!!
        val namaTitipan = intent.getStringExtra("Nama titipan")
        val currLoc = intent.getStringExtra("Lokasi sekarang")

        val titleText = "Pindah Titip $namaTitipan"
        pindah_titip.text = titleText

        curr_loc_pindah_titip.text = currLoc

        back.setOnClickListener {
            finish()
        }

        tombol_pilih_lokasi.setOnClickListener {
            val peta = Intent(this, PetaPilihAgen::class.java)
            startActivityForResult(peta, ADDRESS_DATA)
        }

        tombol_pindah_titip.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {


                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("goods").document(goodsId)
                val destLocText = lokasi_yang_dipilih.text.toString()
                val agentName = destLocText.lines()[0]
                val agentDest = getAgentId(db, agentName)
                val updateData = hashMapOf(
                    "agentCnt" to FieldValue.increment(1),
                    "agentDest" to agentDest,
                    "pindahTitipPrice" to 15000,
                    "status" to AWAITING_PINDAH_TITIP_ORG
                )
                doc.update(updateData).await()

                val documentSnapshot = doc.get().await()
                val currData = documentSnapshot.data!!
                val agentCnt = (currData["agentCnt"] as Long).toString()

                doc.collection("agentHist").document("agentHist")
                    .set(hashMapOf(agentCnt to agentDest), SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this@PindahTitip,
                            "Pindah Titip berhasil!", Toast.LENGTH_SHORT).show()

                        val notifMgr = getSystemService(Context.NOTIFICATION_SERVICE)
                            as NotificationManager
                        val notifBuilder = NotificationCompat.Builder(applicationContext, "Ti-Tip")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Permintaan Pindah Titip $namaTitipan sedang diproses")
                            .setContentText("Mohon menunggu sampai proses Pindah Titip selesai")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                        notifMgr.notify(0, notifBuilder.build())

                        val alamat = Intent(applicationContext, DetailTitipan::class.java)
                        setResult(Activity.RESULT_OK, alamat)
                        finish()
                    }
                    .addOnFailureListener {

                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ADDRESS_DATA) {
            if(resultCode == Activity.RESULT_OK) {
                lokasi_yang_dipilih.text = data!!.getStringExtra("Alamat")
            }
        }
    }
}