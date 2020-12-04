package com.rpljumat.ti_tip

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_titipan_baru.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.BlockingQueue

class TitipanBaru : AppCompatActivity() {

    companion object {
        const val ADDRESS_DATA = 1
        const val AWAITING_CONFIRMATION = 1
        const val DETIK_SEHARI = 86400
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
            val auth = FirebaseAuth.getInstance()
            val uId = auth.uid?:""

            val locTxt = location_detail_text_new_titipan.text.lines()
            val nama = locTxt[0]

            val db = FirebaseFirestore.getInstance()
            val allAgents = db.collection("agent")
                .whereEqualTo("agentName", nama).get()
            Tasks.await(allAgents)
            val agent = allAgents.result.documents[0].id

//            val test = agent.addOnSuccessListener {
//                lateinit var agentId: String
//                for(document in it){
//                    agentId = document.id
//                }
//            }
            Log.d("Test", agent)

            val fragileStat = fragile_chkbox_new_titipan.isChecked
            val groceryStat = grocery_chkbox_new_titipan.isChecked

            val currDT = System.currentTimeMillis()
            val expDT = if(groceryStat) currDT + DETIK_SEHARI else currDT + 7 * DETIK_SEHARI

//            val goods = Goods(uId, agentId, nama, AWAITING_CONFIRMATION,
//                currDT, expDT, 15000,
//                0, 0, 0, 0,
//                fragileStat, groceryStat)

//            db.collection("goods")
//                .add(goods)

            finish()
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

//    private fun getAgentId(db: FirebaseFirestore, nama: String): String {
//        CoroutineScope(Dispatchers.IO).launch {
//            val agent = db.collection("agent")
//                .whereEqualTo("agentName", nama).get()
//            agent.addOnSuccessListener {
//                lateinit var agentId: String
//                for(document in it){
//                    agentId = document.id
//                }
//            }
//        }
//
//
//    }
//
//    private fun getAgentIdFirebase(db: FirebaseFirestore, nama: String): String {
//
//    }

}