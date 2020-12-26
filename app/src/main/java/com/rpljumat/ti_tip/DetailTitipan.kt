package com.rpljumat.ti_tip

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Constraints
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_detail_titipan.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DetailTitipan : AppCompatActivity() {
    private lateinit var activityContext: DetailTitipan
    var conn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_titipan)

        activityContext = this

        // Check internet connection first
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                checkNetworkConnection()
            }
            if(!conn) {
                alertNoConnection()
                return@launch
            }
        }

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
                    val agentLoc = getAgentLoc(agentCoords, this@DetailTitipan)
                    val agentPerson = getAgentPerson(agentId)
                    val length = (data["length"] as Double).toFloat()
                    val width = (data["width"] as Double).toFloat()
                    val height = (data["height"] as Double).toFloat()
                    val weight = (data["weight"] as Double).toFloat()
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

                    if(status == AWAITING_PINDAH_TITIP_ORG ||
                        status == AWAITING_PINDAH_TITIP_DEST) {
                        val agentDest = data["agentDest"] as String
                        addAgentDestField(agentDest)
                    }

                    responsible_detail_titipan.text = agentPerson

                    val dimText = if(length == 0f) "Belum diukur" else
                        "${length}cm x ${width}cm x ${height}cm"
                    dimension_detail_titipan.text = dimText

                    weight_detail_titipan.text =
                        if(weight == 0f) "Belum ditimbang" else "${weight}kg"

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
                finish()
            }
        builder.show()
    }

    private suspend fun getAgentPerson(agentId: String): String {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("agent").document(agentId).get().await()
        val data = docRef.data
        return data?.get("responsiblePerson") as String
    }

    private fun addAgentDestField(agentDest: String) {
        val titleTextView = TextView(this)
        titleTextView.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.MATCH_PARENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )
        val titleId = View.generateViewId()
        titleTextView.id = titleId
        val titleText = "Lokasi titipan baru"
        titleTextView.text = titleText
        container_inner_detail_titipan.addView(titleTextView)

        val newLocTextView = TextView(this)
        newLocTextView.layoutParams = Constraints.LayoutParams(
            0,
            Constraints.LayoutParams.WRAP_CONTENT
        )
        val newLocid = View.generateViewId()
        newLocTextView.id = newLocid

        CoroutineScope(Dispatchers.Main).launch {
            val agentDestName = getAgentName(agentDest)
            val agentDestLoc = getAgentLoc(getAgentCoords(agentDest), applicationContext)
            val text = agentDestName + "\n" + agentDestLoc
            newLocTextView.text = text
        }

        container_inner_detail_titipan.addView(newLocTextView)

        // Adjust constraints
        val container = R.id.container_detail_titipan
        val cs = ConstraintSet()
        cs.clone(container_inner_detail_titipan)
        cs.connect(titleId, ConstraintSet.TOP, container, ConstraintSet.BOTTOM, 16f.toPx())
        cs.connect(titleId, ConstraintSet.START, container, ConstraintSet.BOTTOM, 8f.toPx())
        cs.connect(newLocid, ConstraintSet.TOP, titleId, ConstraintSet.BOTTOM, 8f.toPx())
        cs.centerHorizontally(newLocid, container, ConstraintSet.LEFT, 16f.toPx(),
            container, ConstraintSet.RIGHT, 16f.toPx(), 0.5f)
        val responsibleLabel = R.id.responsible_detail_titipan
        cs.clear(responsibleLabel, ConstraintSet.TOP)
        cs.connect(responsibleLabel, ConstraintSet.TOP, newLocid, ConstraintSet.BOTTOM, 16f.toPx())
        cs.applyTo(container_inner_detail_titipan)
    }
}