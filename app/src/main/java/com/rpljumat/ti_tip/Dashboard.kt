package com.rpljumat.ti_tip

import android.content.Intent
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
import kotlinx.android.synthetic.main.activity_dashboard.*

class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        val db = FirebaseFirestore.getInstance()
        db.collection("goods")
            .whereEqualTo("uId", uid)
            .get()
            .addOnSuccessListener {
                var prevId = running_title.id
                for(goods in it) {
                    val data = goods.data
                    val status = data.get("status") as Int
                    if(status == REJECTED || status == RETURNED || status == EXPIRED) continue

                    val nama = data.get("nama") as String

                    val containerId = createTitipanItemBg(prevId)
                    val titleId = createTitipanItemTitle(containerId, nama)
                    val statusId = createTitipanItemStatus(containerId, status)
                    val locationId = createTitipanItemLocation(containerId, titleId)
                    val dimWeightId = createTitipanItemOthers(containerId, locationId)
                    val estPriceId = createTitipanItemOthers(containerId, dimWeightId)
                    val expId = createTitipanItemOthers(containerId, estPriceId)
                }
            }
            .addOnFailureListener {

            }

        btn_add_titipan.setOnClickListener {
            val newTitipan = Intent(this, TitipanBaru::class.java)
            startActivity(newTitipan)
        }
    }

    private fun createTitipanItemBg(prevId: Int): Int {
        val container = ConstraintLayout(this)
        container.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.MATCH_PARENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        container.id = id
        container.setBackgroundResource(R.color.frame_front_bg)

        val constraintSet = ConstraintSet()
        constraintSet.center(id,
            container_running.id, ConstraintSet.LEFT, 8,
            container_running.id, ConstraintSet.RIGHT, 8,
            0.5f)
        constraintSet.connect(id, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM, 8)

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

        val black = 0x0
        titleTextView.setTextColor(black)

        titleTextView.textSize = 16f
        titleTextView.setTypeface(titleTextView.typeface, Typeface.BOLD)

        val constraintSet = ConstraintSet()
        constraintSet.connect(id, ConstraintSet.LEFT, containerId, ConstraintSet.LEFT, 8)
        constraintSet.connect(id, ConstraintSet.TOP, containerId, ConstraintSet.TOP, 8)

        return id
    }

    private fun createTitipanItemStatus(containerId: Int, status: Int): Int {
        val statusTextView = TextView(this)
        statusTextView.layoutParams = Constraints.LayoutParams(
            Constraints.LayoutParams.WRAP_CONTENT,
            Constraints.LayoutParams.WRAP_CONTENT
        )

        val id = View.generateViewId()
        statusTextView.id = id

        val statOrange = R.color.stat_orange
        val statGreen = R.color.stat_green
        when(status) {
            AWAITING_CONFIRMATION -> {
                statusTextView.setText(R.string.awaiting_confirmation)
                statusTextView.setTextColor(ContextCompat.getColor(this, statOrange))
            }
            STORED -> {
                statusTextView.setText(R.string.stored)
                statusTextView.setTextColor(ContextCompat.getColor(this, statGreen))
            }
            AWAITING_PINDAH_TITIP_ORG, AWAITING_PINDAH_TITIP_DEST -> {
                statusTextView.setText(R.string.pindah_titip)
                statusTextView.setTextColor(ContextCompat.getColor(this, statOrange))
            }
            AWAITING_RETURN -> {
                statusTextView.setText(R.string.awaiting_return)
                statusTextView.setTextColor(ContextCompat.getColor(this, statOrange))
            }
        }

        val constraintSet = ConstraintSet()
        constraintSet.connect(id, ConstraintSet.RIGHT, containerId, ConstraintSet.RIGHT, 8)
        constraintSet.connect(id, ConstraintSet.TOP, containerId, ConstraintSet.TOP, 8)

        return id
    }

    private fun createTitipanItemLocation(containerId: Int, titleId: Int): Int {
        TODO("Not yet implemented")
    }

    private fun createTitipanItemOthers(containerId: Int, prevId: Int): Int {
        TODO("Not yet implemented")
    }
}