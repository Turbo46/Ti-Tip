package com.rpljumat.ti_tip

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Constraints
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_riwayat_titipan.*
import kotlinx.android.synthetic.main.activity_riwayat_titipan.container_history
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RiwayatTitipan : AppCompatActivity() {

    private var conn = false

    private lateinit var itemContainer: ConstraintLayout
    private var prevId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_titipan)

        itemContainer = container_history

        tombol_back_riwayat.setOnClickListener {
            finish()
        }

        CoroutineScope(Dispatchers.Main).launch {
            // Check internet connection first
            withContext(Dispatchers.Default) {
                checkNetworkConnection()
            }
            if(!conn) {
                alertNoConnection()
                return@launch
            }

            val db = FirebaseFirestore.getInstance()
            val allHistoryStatus = mutableListOf(REJECTED, RETURNED, EXPIRED)
            val uid = intent.getStringExtra("User ID")!!
            val query = db.collection("goods")
                .whereEqualTo("userId", uid)
                .whereIn("status", allHistoryStatus)
                .get().await()
            val documents = query.documents
            for((itemCnt, document) in documents.withIndex()) {
                val data = document.data!!
                val status = (data["status"] as Long).toInt()
                val namaTitipan = data["nama"].toString()
                val agentId = data["agentId"].toString()
                val agentName = getAgentName(agentId)
                val fragile = data["fragile"] as Boolean
                val grocery = data["grocery"] as Boolean

                createHistoryBg(itemCnt)
                createTitipanTitle(namaTitipan)
                createTitipanStatus(status)
                createTitipanAgentName(agentName)
                createTitipanFragileGrocery(fragile, grocery)
            }
            addMarginBottom()
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
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }

    private fun createHistoryBg(itemCnt: Int) {
        val prevItemContainerId = itemContainer.id
        itemContainer = ConstraintLayout(this)

        itemContainer.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.MATCH_PARENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        itemContainer.id = id
        itemContainer.setBackgroundResource(R.color.frame_front_bg)
        container_history.addView(itemContainer)

        val constraintSet = ConstraintSet()
        constraintSet.clone(container_history)
        constraintSet.centerHorizontally(id,
            container_history.id, ConstraintSet.LEFT, 8f.toPx(),
            container_history.id, ConstraintSet.RIGHT, 8f.toPx(),
            0.5f)
        constraintSet.connect(id, ConstraintSet.TOP,
            prevItemContainerId, if(itemCnt == 0) ConstraintSet.TOP else ConstraintSet.BOTTOM,
            8f.toPx())
        constraintSet.applyTo(container_history)
    }

    private fun createTitipanTitle(nama: String) {
        val titleTextView = TextView(this)
        titleTextView.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.WRAP_CONTENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        titleTextView.id = id

        titleTextView.text = nama
        titleTextView.setTextColor(black)

        titleTextView.textSize = 16f
        titleTextView.setTypeface(titleTextView.typeface, Typeface.BOLD)

        itemContainer.addView(titleTextView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(itemContainer)
        constraintSet.connect(id, ConstraintSet.START,
            itemContainer.id, ConstraintSet.START, 8f.toPx())
        constraintSet.connect(id, ConstraintSet.TOP, itemContainer.id, ConstraintSet.TOP, 8f.toPx())
        constraintSet.applyTo(itemContainer)

        prevId = id
    }

    private fun createTitipanStatus(status: Int) {
        val statusTextView = TextView(this)
        statusTextView.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.WRAP_CONTENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        statusTextView.id = id

        val pair = status.getStatusInfo()
        val text = pair.first
        val color = pair.second
        statusTextView.text = text
        statusTextView.setTextColor(ContextCompat.getColor(this, color))

        itemContainer.addView(statusTextView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(itemContainer)
        constraintSet.connect(id, ConstraintSet.END, itemContainer.id, ConstraintSet.END, 8f.toPx())
        constraintSet.connect(id, ConstraintSet.TOP, itemContainer.id, ConstraintSet.TOP, 8f.toPx())
        constraintSet.applyTo(itemContainer)
    }

    private fun createTitipanAgentName(agentName: String) {
        val agentNameTextView = TextView(this)
        agentNameTextView.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.WRAP_CONTENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        agentNameTextView.id = id

        val text = "Lokasi: $agentName"
        agentNameTextView.text = text
        agentNameTextView.setTextColor(black)

        itemContainer.addView(agentNameTextView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(itemContainer)
        constraintSet.connect(id, ConstraintSet.START,
            itemContainer.id, ConstraintSet.START, 8f.toPx())
        constraintSet.connect(id, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM, 8f.toPx())
        constraintSet.applyTo(itemContainer)

        prevId = id
    }

    private fun createTitipanFragileGrocery(fragile: Boolean, grocery: Boolean) {
        val fragileGroceryTextView = TextView(this)
        fragileGroceryTextView.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.WRAP_CONTENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        fragileGroceryTextView.id = id

        val groceryText = if(grocery) "Basah" else "Tidak basah"
        val fragileText = if(fragile) "Pecah belah" else "Tidak pecah belah"
        val text = "$groceryText / $fragileText"
        fragileGroceryTextView.text = text
        fragileGroceryTextView.setTextColor(black)

        itemContainer.addView(fragileGroceryTextView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(itemContainer)
        constraintSet.connect(id, ConstraintSet.START,
            itemContainer.id, ConstraintSet.START, 8f.toPx())
        constraintSet.connect(id, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM, 4f.toPx())
        constraintSet.applyTo(itemContainer)

        prevId = id
    }

    private fun addMarginBottom() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(container_history)
        constraintSet.connect(itemContainer.id, ConstraintSet.BOTTOM,
            container_history.id, ConstraintSet.BOTTOM, 8f.toPx())
        constraintSet.applyTo(container_history)
    }
}