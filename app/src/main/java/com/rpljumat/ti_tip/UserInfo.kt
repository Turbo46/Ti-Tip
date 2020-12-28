package com.rpljumat.ti_tip

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.layout_dlg_change_email.*
import kotlinx.android.synthetic.main.layout_dlg_change_pass.*
import kotlinx.android.synthetic.main.layout_dlg_change_phone.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserInfo : AppCompatActivity() {
    var conn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        lateinit var auth: FirebaseAuth
        lateinit var currUser: FirebaseUser
        lateinit var email: String
        lateinit var doc: DocumentReference

        CoroutineScope(Dispatchers.Main).launch {
            // Check internet connection first
            withContext(Dispatchers.Default) {
                checkNetworkConnection()
            }
            if (!conn) {
                alertNoConnection(false)
                return@launch
            }

            auth = FirebaseAuth.getInstance()
            currUser = auth.currentUser!!
            val uId = currUser.uid
            email = currUser.email!!
            val emailMaskRule = Regex("^(.{3}).+(.{3}@.+)")
            val maskedEmail = email.replace(emailMaskRule, "$1***$2")
            email_info.text = maskedEmail

            val db = FirebaseFirestore.getInstance()
            doc = db.collection("users").document(uId)
            doc.get()
                .addOnSuccessListener {
                    val data = it.data!!
                    val nama = data["nama"] as String
                    val phone = data["phone"] as String
                    val phoneMaskRule = Regex("^(\\d{4})\\d+(\\d{3})")
                    val maskedPhone = phone.replace(phoneMaskRule, "$1***$2")

                    user_fullname_info.text = nama
                    phone_info.text = maskedPhone
                }
                .addOnFailureListener {

                }
        }

        back.setOnClickListener {
            finish()
        }

        change_email_btn.setOnClickListener {
            // Create dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Permintaan pengubahan email")
                .setView(R.layout.layout_dlg_change_email)
                .setPositiveButton("Ubah") { dialogInterface: DialogInterface, _: Int ->
                    CoroutineScope(Dispatchers.Main).launch {
                        // Check internet connection first
                        withContext(Dispatchers.Default) {
                            checkNetworkConnection()
                        }
                        if (!conn) {
                            dialogInterface.dismiss()
                            alertNoConnection(true)
                            return@launch
                        }

                        val dialog = dialogInterface as Dialog
                        val newEmail = dialog.new_email.text.toString()
                        val newEmailMaskRule = Regex("^(.{3}).+(.{3}@.+)")
                        val maskedNewEmail = newEmail.replace(newEmailMaskRule, "$1***$2")
                        val currPass = dialog.email_curr_pass.text.toString()

                        val credential = EmailAuthProvider.getCredential(email, currPass)
                        currUser.reauthenticate(credential)
                            .addOnSuccessListener {
                                currUser.updateEmail(newEmail)
                                    .addOnSuccessListener {
                                        email = newEmail
                                        email_info.text = maskedNewEmail
                                        Toast.makeText(this@UserInfo,
                                            "Email berhasil diubah!",Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@UserInfo, "Perubahan email gagal!",
                                            Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@UserInfo, "Sandi tidak cocok\n" +
                                    "Perubahan email gagal!", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Batal") { _: DialogInterface, _: Int ->

                }
                .show()
        }

        change_phone_btn.setOnClickListener {
            // Create dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Permintaan pengubahan nomor telepon")
                .setView(R.layout.layout_dlg_change_phone)
                .setPositiveButton("Ubah") { dialogInterface: DialogInterface, _: Int ->
                    CoroutineScope(Dispatchers.Main).launch {
                        // Check internet connection first
                        withContext(Dispatchers.Default) {
                            checkNetworkConnection()
                        }
                        if (!conn) {
                            dialogInterface.dismiss()
                            alertNoConnection(true)
                            return@launch
                        }

                        val dialog = dialogInterface as Dialog
                        val newPhone = dialog.new_phone.text.toString()
                        val newPhoneMaskRule = Regex("^(\\d{4})\\d+(\\d{3})")
                        val maskedNewPhone = newPhone.replace(newPhoneMaskRule, "$1***$2")
                        val currPass = dialog.phone_curr_pass.text.toString()

                        val credential = EmailAuthProvider.getCredential(email, currPass)
                        currUser.reauthenticate(credential)
                            .addOnSuccessListener {
                                doc.update("phone", newPhone)
                                    .addOnSuccessListener {
                                        phone_info.text = maskedNewPhone
                                        Toast.makeText(this@UserInfo,
                                            "Perubahan nomor telepon berhasil!", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@UserInfo,
                                            "Perubahan nomor telepon gagal!", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@UserInfo, "Sandi tidak cocok\n" +
                                    "Perubahan email gagal!", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Batal") { _: DialogInterface, _: Int ->

                }
                .show()
        }

        change_pass_btn.setOnClickListener {
            // Create dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Permintaan pengubahan kata sandi")
                .setView(R.layout.layout_dlg_change_pass)
                .setPositiveButton("Ubah") { dialogInterface: DialogInterface, _: Int ->
                    CoroutineScope(Dispatchers.Main).launch {
                        // Check internet connection first
                        withContext(Dispatchers.Default) {
                            checkNetworkConnection()
                        }
                        if (!conn) {
                            dialogInterface.dismiss()
                            alertNoConnection(true)
                            return@launch
                        }

                        val dialog = dialogInterface as Dialog
                        val currPass = dialog.pass_curr_pass.text.toString()
                        val newPass = dialog.new_pass.text.toString()
                        val confirmPass = dialog.confirm_pass.text.toString()

                        if(newPass != confirmPass) {
                            Toast.makeText(this@UserInfo, "Sandi baru tidak sama dengan " +
                                "konfirmasi\nPerubahan kata sandi gagal!", Toast.LENGTH_SHORT)
                                .show()
                            return@launch
                        }

                        val credential = EmailAuthProvider.getCredential(email, currPass)
                        currUser.reauthenticate(credential)
                            .addOnSuccessListener {
                                currUser.updatePassword(newPass)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@UserInfo,
                                            "Sandi berhasil diubah!",Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@UserInfo, "Perubahan kata sandi gagal!",
                                            Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@UserInfo, "Sandi lama tidak cocok\n" +
                                    "Perubahan kata sandi gagal!", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Batal") { _: DialogInterface, _: Int ->

                }
                .show()
        }

        logout_btn.setOnClickListener {
            // Create dialog
            val builder = AlertDialog.Builder(this@UserInfo)
            builder.setTitle("Konfirmasi keluar")
                .setMessage("Yakin untuk keluar?")
                .setPositiveButton("Ya") { _: DialogInterface, _: Int ->
                    auth.signOut()

                    // Close all child activities and refresh the dashboard
                    finishAffinity()
                    val login = Intent(applicationContext, Login::class.java)
                    startActivity(login)
                }
                .setNegativeButton("Tidak") { _: DialogInterface, _: Int ->

                }
                .show()
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

    private fun alertNoConnection(isOpenedBefore: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tidak ada koneksi!")
            .setMessage("Pastikan Wi-Fi atau data seluler telah dinyalakan, lalu coba lagi")
            .setPositiveButton("Kembali") { _: DialogInterface, _: Int ->
                if(!isOpenedBefore) finish()
            }
            .setOnCancelListener {
                if(!isOpenedBefore) finish()
            }
            .show()
    }
}