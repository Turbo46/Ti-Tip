package com.rpljumat.ti_tip

import android.content.Context
import android.content.res.Resources
import android.location.Geocoder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

const val AWAITING_CONFIRMATION = 1
const val STORED = 2
const val REJECTED = 3
const val AWAITING_PINDAH_TITIP_ORG = 4
const val AWAITING_PINDAH_TITIP_DEST = 5
const val AWAITING_RETURN = 6
const val RETURNED = 7
const val EXPIRED = 8

const val RUN_BLOCK = 1
const val HIST_BLOCK = 2

const val MS_SEHARI = 86_400_000

class User(val nama: String, val username: String, val nik: String, val phone: String)
class Goods(val userId: String, val agentId: String, val nama: String, val agentCnt: Int,
                   val status: Int, val ts: Long, val exp: Long, val estPrice: Int,
                   val length: Int, val width: Int, val height: Int, val weight: Int,
                   val fragile: Boolean, val grocery: Boolean)

fun Float.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Long.toDT(): String {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.timeInMillis = this
    val formatter = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(calendar.timeInMillis)
}

fun Int.getStatusInfo(): Pair<String, Int> {
    val statRed = R.color.stat_red
    val statOrange = R.color.stat_orange
    val statGreen = R.color.stat_green
    when(this) {
        AWAITING_CONFIRMATION -> return Pair("Menunggu konfirmasi", statOrange)
        STORED -> return Pair("Dititipkan", statGreen)
        REJECTED -> return Pair("Ditolak", statRed)
        AWAITING_PINDAH_TITIP_ORG, AWAITING_PINDAH_TITIP_DEST ->
            return Pair("Pindah Titip", statOrange)
        AWAITING_RETURN -> return Pair("Menunggu pengembalian", statOrange)
        RETURNED -> return Pair("Dikembalikan", statGreen)
        EXPIRED -> return Pair("Kadaluarsa", statRed)
    }
    return Pair("", 0) // else condition
}

suspend fun getAgentName(agentId: String): String {
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("agent").document(agentId).get().await()
    val data = docRef.data
    return data?.get("agentName") as String
}

suspend fun getAgentCoords(agentId: String): Pair<Double, Double> {
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("agent").document(agentId).get().await()
    val pos = docRef.getGeoPoint("pos")!!
    val lat = pos.latitude
    val long = pos.longitude
    return Pair(lat, long)
}

fun getAgentLoc(coords: Pair<Double, Double>, context: Context): String {
    val geocoder = Geocoder(context)
    val lat = coords.first
    val long = coords.second
    val addrList = geocoder.getFromLocation(lat, long, 1)
    return addrList[0].getAddressLine(0)
}