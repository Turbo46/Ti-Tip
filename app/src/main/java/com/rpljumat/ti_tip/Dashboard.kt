package com.rpljumat.ti_tip

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Constraints
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        expand_running.tag = R.drawable.ic_collapse
        expand_history.tag = R.drawable.ic_collapse

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        val db = FirebaseFirestore.getInstance()
        db.collection("goods")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener {
                settle(it)
            }
            .addOnFailureListener {

            }

        expand_running.setOnClickListener {
            val status = expand_running.tag as Int
            val expanded = (status == R.drawable.ic_collapse)

            if(expanded) {
                expand_running.setImageDrawable(resources.getDrawable(R.drawable.ic_expand, null))
                expand_running.tag = R.drawable.ic_expand
            }
            else {
                expand_running.setImageDrawable(resources.getDrawable(R.drawable.ic_collapse, null))
                expand_running.tag = R.drawable.ic_collapse
            }

            val runCnt = container_running.childCount
            var i = 2
            while(i < runCnt) {
                val item = container_running.getChildAt(i)
                item.visibility = if(expanded) View.GONE else View.VISIBLE
                i++
            }
        }

        expand_history.setOnClickListener {
            val status = expand_history.tag as Int
            val expanded = (status == R.drawable.ic_collapse)

            if(expanded) {
                expand_history.setImageDrawable(resources.getDrawable(R.drawable.ic_expand, null))
                expand_history.tag = R.drawable.ic_expand
            }
            else {
                expand_history.setImageDrawable(resources.getDrawable(R.drawable.ic_collapse, null))
                expand_history.tag = R.drawable.ic_collapse
            }

            val histCnt = container_history.childCount
            var i = 3
            while(i < histCnt) {
                val item = container_history.getChildAt(i)
                item.visibility = if(expanded) View.GONE else View.VISIBLE
                i++
            }
        }

        btn_add_titipan.setOnClickListener {
            val newTitipan = Intent(this, TitipanBaru::class.java)
            startActivity(newTitipan)
        }

        history_more_text.setOnClickListener {
            val riwayatTitipan = Intent(this, RiwayatTitipan::class.java)
            startActivity(riwayatTitipan)
        }
    }

    private fun settle(goods: QuerySnapshot) {
        CoroutineScope(Dispatchers.Main).launch {
            var prevIdRun = running_title.id
            var prevIdHist = history_title.id
            var runCnt = 0
            var histCnt = 0

            for(good in goods) {
                val data = good.data
                val status = (data["status"] as Long).toInt()
                val goodId = good.id
                val nama = data["nama"] as String
                val length = (data["height"] as Long).toInt()
                val width = (data["width"] as Long).toInt()
                val height = (data["height"] as Long).toInt()
                val weight = (data["weight"] as Long).toInt()
                val estPrice = (data["estPrice"] as Long).toInt()
                val exp = data["exp"] as Long

                val agentId =
                    if(status == AWAITING_PINDAH_TITIP_ORG ||
                        status == AWAITING_PINDAH_TITIP_DEST) {
                        data["agentDest"] as String
                    } else {
                        data["agentId"] as String
                    }
                val agentName = getAgentName(agentId)

                var containerId: Int
                if(status == REJECTED || status == RETURNED || status == EXPIRED) {
                    if(histCnt == 5) continue
                    containerId = createTitipanItemBg(goodId, prevIdHist, HIST_BLOCK)

                    histCnt++
                    prevIdHist = containerId
                }
                else {
                    containerId = createTitipanItemBg(goodId, prevIdRun, RUN_BLOCK)

                    prevIdRun = containerId
                    runCnt++
                }

                var prevIdInner = createTitipanItemTitle(containerId, nama)
                createTitipanItemStatus(containerId, status)
                prevIdInner = createTitipanItemAgentName(containerId, prevIdInner,
                    agentName, status)
                if(length > 0) prevIdInner = createTitipanItemDimWeight(containerId, prevIdInner,
                    length, width, height, weight)
                if(status != REJECTED && status != EXPIRED) prevIdInner =
                    createTitipanItemEstPrice(containerId, prevIdInner, status, estPrice)
                if(status != REJECTED && status != RETURNED && status != EXPIRED)
                    createTitipanItemExp(containerId, prevIdInner, exp)
            }

            setRunCnt(runCnt)
            if(runCnt > 0) addMarginBottomRun(prevIdRun)
            if(histCnt > 0) addMarginBottomHist(prevIdHist)
        }
    }

    private fun createTitipanItemBg(goodsId: String, prevId: Int, block: Int): Int {
        val container = ConstraintLayout(this)

        container.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.MATCH_PARENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        container.id = id
        container.setBackgroundResource(R.color.frame_front_bg)

        container_running.addView(container)

        val constraintSet = ConstraintSet()
        val parentCL = if(block == HIST_BLOCK) container_history else container_running
        constraintSet.clone(parentCL)
        constraintSet.centerHorizontally(id,
            parentCL.id, ConstraintSet.LEFT, 8f.toPx(),
            parentCL.id, ConstraintSet.RIGHT, 8f.toPx(),
            0.5f)
        constraintSet.connect(id, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM, 8f.toPx())
        constraintSet.applyTo(parentCL)

        if(block == RUN_BLOCK)
            container.setOnClickListener {
                val detailTitipan = Intent(this, DetailTitipan::class.java)
                detailTitipan.putExtra("ID", goodsId)
                startActivity(detailTitipan)
            }

        return id
    }

    private fun createTitipanItemTitle(containerId: Int, nama: String): Int {
        val titleTextView = TextView(this)
        titleTextView.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.WRAP_CONTENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        titleTextView.id = id

        titleTextView.text = nama

        val black = Color.parseColor("#000000")
        titleTextView.setTextColor(black)

        titleTextView.textSize = 16f
        titleTextView.setTypeface(titleTextView.typeface, Typeface.BOLD)

        val container = findViewById<ConstraintLayout>(containerId)
        container.addView(titleTextView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(container)
        constraintSet.connect(id, ConstraintSet.START, containerId, ConstraintSet.START, 8f.toPx())
        constraintSet.connect(id, ConstraintSet.TOP, containerId, ConstraintSet.TOP, 8f.toPx())
        constraintSet.applyTo(container)

        return id
    }

    private fun createTitipanItemStatus(containerId: Int, status: Int) {
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

        val container = findViewById<ConstraintLayout>(containerId)
        container.addView(statusTextView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(container)
        constraintSet.connect(id, ConstraintSet.END, containerId, ConstraintSet.END, 8f.toPx())
        constraintSet.connect(id, ConstraintSet.TOP, containerId, ConstraintSet.TOP, 8f.toPx())
        constraintSet.applyTo(container)
    }

    private fun createTitipanItemAgentName(containerId: Int, prevId: Int,
                                           agentName: String, status: Int): Int {

        val agentNameTextView = TextView(this)
        agentNameTextView.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.WRAP_CONTENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        agentNameTextView.id = id

        val label =
            if(status == AWAITING_PINDAH_TITIP_ORG || status == AWAITING_PINDAH_TITIP_DEST) {
                "Lokasi baru: $agentName"
            } else {
                "Lokasi: $agentName"
            }
        agentNameTextView.text = label

        val black = Color.parseColor("#000000")
        agentNameTextView.setTextColor(black)

        val container = findViewById<ConstraintLayout>(containerId)
        container.addView(agentNameTextView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(container)
        constraintSet.connect(id, ConstraintSet.START, containerId, ConstraintSet.START, 8f.toPx())
        constraintSet.connect(id, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM, 8f.toPx())
        constraintSet.applyTo(container)

        return id
    }

    private fun createTitipanItemDimWeight(containerId: Int, prevId: Int,
                                           length: Int, width: Int, height: Int, weight: Int): Int {

        val dimWeightTextView = TextView(this)
        dimWeightTextView.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.WRAP_CONTENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        dimWeightTextView.id = id

        val text = "Dimensi/berat: ${length}cm x ${width}cm x ${height}cm / ${weight}kg"
        dimWeightTextView.text = text

        val black = Color.parseColor("#000000")
        dimWeightTextView.setTextColor(black)

        val container = findViewById<ConstraintLayout>(containerId)
        container.addView(dimWeightTextView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(container)
        constraintSet.connect(id, ConstraintSet.START, containerId, ConstraintSet.START, 8f.toPx())
        constraintSet.connect(id, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM, 4f.toPx())
        constraintSet.applyTo(container)

        return id
    }

    private fun createTitipanItemEstPrice(containerId: Int, prevId: Int,
                                          status: Int, estPrice: Int): Int {

        val estPriceTextView = TextView(this)
        estPriceTextView.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.WRAP_CONTENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        estPriceTextView.id = id

        val text = if(status == AWAITING_CONFIRMATION)
            "Biaya penitipan: Rp$estPrice" else "Est. harga kembali: Rp$estPrice"
        estPriceTextView.text = text

        val black = Color.parseColor("#000000")
        estPriceTextView.setTextColor(black)

        val container = findViewById<ConstraintLayout>(containerId)
        container.addView(estPriceTextView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(container)
        constraintSet.connect(id, ConstraintSet.START, containerId, ConstraintSet.START, 8f.toPx())
        constraintSet.connect(id, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM, 4f.toPx())
        constraintSet.applyTo(container)

        return id
    }

    private fun createTitipanItemExp(containerId: Int, prevId: Int, exp: Long) {
        val expTextView = TextView(this)
        expTextView.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.WRAP_CONTENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        expTextView.id = id

        val dtStr = exp.toDT()

        val text = "Kadalursa pada: $dtStr"
        expTextView.text = text

        val black = Color.parseColor("#000000")
        expTextView.setTextColor(black)

        val container = findViewById<ConstraintLayout>(containerId)
        container.addView(expTextView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(container)
        constraintSet.connect(id, ConstraintSet.START, containerId, ConstraintSet.START, 8f.toPx())
        constraintSet.connect(id, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM, 4f.toPx())
        constraintSet.applyTo(container)
    }

    private fun addMarginBottomRun(lastIdRun: Int) {
        val constraintSetRun = ConstraintSet()
        constraintSetRun.clone(container_running)
        constraintSetRun.connect(lastIdRun, ConstraintSet.BOTTOM,
            container_running.id, ConstraintSet.BOTTOM, 8f.toPx())
        constraintSetRun.applyTo(container_running)
    }

    private fun addMarginBottomHist(lastIdHist: Int) {
        val constraintSetHist = ConstraintSet()
        constraintSetHist.clone(container_history)
        constraintSetHist.connect(lastIdHist, ConstraintSet.BOTTOM,
            container_history.id, ConstraintSet.BOTTOM, 8f.toPx())
        constraintSetHist.applyTo(container_history)
    }

    private fun setRunCnt(cnt: Int) {
        val text = "Titipan Berjalan ($cnt)"
        running_title.text = text
    }

}