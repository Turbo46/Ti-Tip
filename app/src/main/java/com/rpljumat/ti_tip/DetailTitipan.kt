package com.rpljumat.ti_tip

import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_detail_titipan.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DetailTitipan : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_titipan)

        back.setOnClickListener {
            finish()
        }

        val goodsId = intent.getStringExtra("ID")?:""
        val db = FirebaseFirestore.getInstance()
        db.collection("goods").document(goodsId).get()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val data = it.data
                    val nama = data?.get("nama") as String
                    val status = (data["status"] as Long).toInt()
                    val statusPair = status.getStatusInfo()
                    val statusText = statusPair.first
                    val statusColor = statusPair.second
                    val agentId = data["agentId"] as String
                    val agentName = getAgentName(agentId)
                    val agentCoords = getAgentCoords(agentId)
                    val agentLoc = getAgentLoc(agentCoords, applicationContext)
                    val agentPerson = getAgentPerson(agentId)
                    val length = (data["length"] as Long).toInt()
                    val width = (data["width"] as Long).toInt()
                    val height = (data["height"] as Long).toInt()
                    val weight = (data["weight"] as Long).toInt()
                    val grocery = data["grocery"] as Boolean
                    val fragile = data["fragile"] as Boolean
                    val exp = (data["exp"] as Long).toDT()
                    val estPrice = (data["estPrice"] as Long).toInt()

                    title_detail_titipan.text = nama
                    status_detail_titipan.text = statusText
                    status_detail_titipan.setTextColor(
                        ContextCompat.getColor(applicationContext, statusColor))

                    val agentLocText = "$agentName\n$agentLoc"
                    location_detail_titipan.text = agentLocText

                    responsible_detail_titipan.text = agentPerson

                    val dimText = if(length == 0) "Belum diukur" else
                        "${length}cm x ${width}cm x ${height}cm"
                    dimension_detail_titipan.text = dimText

                    weight_detail_titipan.text =
                        if(weight == 0) "Belum ditimbang" else "${weight}kg"

                    grocery_chkbox_detail_titipan.isChecked = grocery
                    fragile_chkbox_detail_titipan.isChecked = fragile

                    expired_date_detail_titipan.text = exp
                    if(status == AWAITING_CONFIRMATION) {
                        val text = "Biaya penitipan"
                        est_price_text_detail_titipan.text = text
                    }
                    val estPriceText = "Rp$estPrice"
                    est_price_detail_titipan.text = estPriceText
                }
            }
            .addOnFailureListener {

            }

    }

    private suspend fun getAgentPerson(agentId: String): String {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("agent").document(agentId).get().await()
        val data = docRef.data
        return data?.get("responsiblePerson") as String
    }
}