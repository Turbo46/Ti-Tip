package com.rpljumat.ti_tip

import android.app.Activity
import android.content.Intent
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
            storeNewTitipan()
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
            val expDT = if(groceryStat) currDT + MS_SEHARI else currDT + 7 * MS_SEHARI

            val goods = Goods(uId, agentId, nama, 1,
                AWAITING_CONFIRMATION, currDT, expDT, 15_000,
                0, 0, 0, 0,
                fragileStat, groceryStat)

            val goodsCollection = db.collection("goods")
            val docRef = goodsCollection.add(goods).await()

            val doc = docRef.id
            goodsCollection.document(doc).collection("agentHist")
                .document("agentHist").set(hashMapOf("1" to agentId))

            finish()
        }
    }

    private suspend fun getAgentId(db: FirebaseFirestore, nama: String): String {
        val allAgent = db.collection("agent").whereEqualTo("agentName", nama).get().await()
        return allAgent.documents[0].id
    }

}