package com.jccgs.travelplanner_v2.jkim

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.cyun.DetailActivity_CYun
import com.jccgs.travelplanner_v2.databinding.ActivitySearchPlaceJkimBinding
import com.jccgs.travelplanner_v2.sjeong.CalendarActivity_SJeong
import java.util.*

class SearchPlaceActivity_JKim : AppCompatActivity(), OnMapReadyCallback {
    val binding by lazy { ActivitySearchPlaceJkimBinding.inflate(layoutInflater) }

    lateinit var mapController: MapController
    lateinit var googleMap: GoogleMap

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    private var currentLocation: Location? = null

    private var searchFlag = false

    val geocoder by lazy {
        Geocoder(this)
    }

    lateinit var specificPlace: MutableList<Address>

    lateinit var planTitle: String
    var invitedUsers: ArrayList<User> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        DetailActivity_CYun.editMode = false
        binding.btnNextSearch.isEnabled = false
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (intent.hasExtra("planTitle")){
            planTitle = intent.getStringExtra("planTitle") as String
        }
        if (intent.hasExtra("invitedUsers")){
            invitedUsers = intent.getSerializableExtra("invitedUsers") as ArrayList<User>
        }

        // 검색
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setActivityMode(AutocompleteActivityMode.FULLSCREEN)
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.ADDRESS
            )
        )
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                searchFlag = true

                autocompleteFragment.setText("")

                MapController.selectedPlaceLatLng = place.latLng
                MapController.selectedPlaceName = place.name
                MapController.selectedPlaceAddress = place.address
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 15f))
                mapController.addMark(listOf(place.latLng))
                val addressComponents = place.addressComponents.asList()

                MapController.selectedPlaceShortName = addressComponents.run {
                    this.filter {
                        it.types[0].equals("country")
                    }
                }.run {
                    this[0].shortName
                }

                val locale = Locale("", MapController.selectedPlaceShortName)

                MapController.selectedPlaceCountryName = locale.displayCountry

                MapController.selectedPlaceCity = addressComponents.run {
                    this.filter {
                        it.types[0].equals("administrative_area_level_1")
                    }
                }.run {
                    this[0].name
                }
            }

            override fun onError(status: Status) {
                Log.i("log", "An error occurred: $status")
            }
        })

        // 지도
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@SearchPlaceActivity_JKim)

        binding.btnNextSearch.setOnClickListener {
            if (!searchFlag && currentLocation != null) {
                Log.d("Log_debug", "$currentLocation")
                Log.d("Log_debug", "$specificPlace")
                MapController.selectedPlaceShortName = specificPlace[0].countryCode
                MapController.selectedPlaceLatLng =
                    LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                MapController.selectedPlaceName = specificPlace.first().featureName
                MapController.selectedPlaceCountryName = specificPlace.first().countryName
                MapController.selectedPlaceCity = specificPlace.first().adminArea
                MapController.selectedPlaceAddress = specificPlace.first().getAddressLine(0)
            }

            if (!MapController.selectedPlaceShortName.isNullOrBlank()) {
                val intent = Intent(this, CalendarActivity_SJeong::class.java)
                intent.putExtra("invitedUsers", invitedUsers)
                intent.putExtra("planTitle", planTitle)
                startActivity(intent)
            }
        }
    }

    override fun onStop() {
        searchFlag = false
        super.onStop()
    }

    //****************************이 밑으로 현재 위치 구하기 및 구글 맵 카메라 이동, 현재위치 권한 요청***********************//
    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onMapReady(googleMap: GoogleMap) {

        mapController = MapController(this, googleMap)
        this.googleMap = googleMap
        this.googleMap.setOnCameraIdleListener(mapController.clusterManager)
//        this.googleMap.setOnPoiClickListener(mapController)
        mapController.clusterManager.renderer = mapController.renderer
        getLocation()
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        currentLocation = location
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
                        specificPlace = geocoder.getFromLocation(
                            currentLocation!!.latitude,
                            currentLocation!!.longitude,
                            1
                        )

                        binding.btnNextSearch.isEnabled = true
                    } else {
                        Log.d("Log_debug", "failed to get current location")
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }
}