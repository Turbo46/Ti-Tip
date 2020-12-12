package com.rpljumat.ti_tip

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_peta_pilih_agen.*
import kotlinx.android.synthetic.main.activity_peta_pilih_agen.back
import kotlinx.android.synthetic.main.activity_titipan_baru.*

class PetaPilihAgen : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object{
        const val MY_PERMISSION_ACCESS_FINE_LOCATION = 1
        const val MY_PERMISSION_ACCESS_COARSE_LOCATION = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peta_pilih_agen)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(applicationContext, "AIzaSyAIFeyTS267frdxcX7ok5CXT8CmDvJxRsU")
        initAutoCompleteFragment()

        back.setOnClickListener {
            finish()
        }

        done.setOnClickListener {
            val name = choosen_agent.text
            val addr = choosen_agent_loc.text

            // Pass address data to TitipanBaru
            val alamat = Intent(this, TitipanBaru::class.java)
            alamat.putExtra("Alamat", "$name\n$addr")
            setResult(Activity.RESULT_OK, alamat)

            finish()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSION_ACCESS_FINE_LOCATION
            )
            return
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSION_ACCESS_COARSE_LOCATION
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                var latitude = it?.latitude
                var longitude = it?.longitude
                if(latitude == null || longitude == null){
                    latitude = -6.208763
                    longitude = 106.845599
                }
                val loc = LatLng(latitude, longitude)

                val db = FirebaseFirestore.getInstance()
                val allAgent = db.collection("agent").get()
                allAgent
                    .addOnSuccessListener {res ->
                        for(document in res){
                            val agentLat = document.getGeoPoint("pos")!!.latitude
                            val agentLong = document.getGeoPoint("pos")!!.longitude
                            val agentPos = LatLng(agentLat, agentLong)
                            val agentTitle = document.getString("agentName")

                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(agentPos)
                                    .title(agentTitle)
                            )
                        }
                    }

                googleMap.moveCamera(
                    CameraUpdateFactory
                        .newLatLngZoom(loc, 15f)
                )

                googleMap.setOnMarkerClickListener(this)
            }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        val lat = marker!!.position!!.latitude
        val long = marker.position.longitude
        val name = marker.title
        val addr = getAgentLoc(Pair(lat, long), this)

        choosen_agent.text = name
        choosen_agent_loc.text = addr

        done.visibility = View.VISIBLE

        return true
    }

    private fun initAutoCompleteFragment() {
        val autoCompleteFragment = supportFragmentManager
            .findFragmentById(R.id.search_field_loc) as AutocompleteSupportFragment
        autoCompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))
        autoCompleteFragment.setOnPlaceSelectedListener(object: PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

            }
            override fun onError(status: Status) {
//                TODO("Not yet implemented")
                Log.d("PetaPilihAgen", "$status")
            }
        })
    }
}