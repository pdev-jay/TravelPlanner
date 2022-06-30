package com.jccgs.travelplanner_v2.jkim

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.coroutineScope
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
import com.google.maps.android.ktx.awaitMap
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivitySearchPlaceJkimBinding
import com.jccgs.travelplanner_v2.sjeong.CalendarActivity_SJeong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchPlaceActivity_JKim : AppCompatActivity(), OnMapReadyCallback {
    val binding by lazy { ActivitySearchPlaceJkimBinding.inflate(layoutInflater) }

    lateinit var mapController: MapController
    lateinit var googleMap: GoogleMap

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    private var currentLocation: Location? = null

    private var searchFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.d("Log_debug", "onCreate")

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 검색
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setActivityMode(AutocompleteActivityMode.FULLSCREEN)
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS, Place.Field.ADDRESS))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val geocoder = Geocoder(this@SearchPlaceActivity_JKim)

                val specificPlace = geocoder.getFromLocation(place.latLng.latitude, place.latLng.longitude, 1)
                val countryName = specificPlace[0].countryName
                val cityName = specificPlace[0].adminArea

                Log.i("log", "Place: ${place.name}, ${place.id}, ${place.latLng}, ${countryName}")
                CoroutineScope(Dispatchers.Main).launch {
//                    mapController.moveCamera(place.latLng)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 15f))
                    mapController.addMark(listOf(place.latLng))
                }
                searchFlag = true
                MapController.selectedPlaceLatLng = place.latLng
                MapController.selectedPlaceName = place.name
                MapController.selectedPlaceCountryName = countryName
                MapController.selectedPlaceCity = cityName
                MapController.selectedPlaceAddress = place.address

//                val addressComponents = place.addressComponents.asList()
//                MapController.selectedPlaceShortName = addressComponents.run {
//                    this.filter {
//                        it.types[0].equals("country")
//                    }
//                }.run {
//                    this[0].shortName
//                }
            }

            override fun onError(status: Status) {
                Log.i("log", "An error occurred: $status")
            }
        })

        // 지도

        lifecycle.coroutineScope.launchWhenCreated {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            googleMap = mapFragment.awaitMap()
            mapFragment.getMapAsync(this@SearchPlaceActivity_JKim)
        }

        binding.tvNextSearch.setOnClickListener {
            if (!searchFlag && currentLocation != null){
                val geocoder = Geocoder(this@SearchPlaceActivity_JKim)
                val specificPlace = geocoder.getFromLocation(currentLocation!!.latitude, currentLocation!!.longitude, 1)
                MapController.selectedPlaceLatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                MapController.selectedPlaceName = specificPlace.first().featureName
                MapController.selectedPlaceCountryName = specificPlace.first().countryName
                MapController.selectedPlaceCity = specificPlace.first().adminArea
                MapController.selectedPlaceAddress = specificPlace.first().getAddressLine(0)
            }

            Log.d("Log_debug", "${MapController.selectedPlaceAddress}")
            val intent = Intent(this, CalendarActivity_SJeong::class.java)
            startActivity(intent)
        }
    }

    override fun onStop() {
        searchFlag = false
        super.onStop()
    }

    //****************************이 밑으로 현재 위치 구하기 및 구글 맵 카메라 이동, 현재위치 권한 요청***********************//
    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("Log_debug", "onMapReady")

//        googleMap.isMyLocationEnabled = true
//        googleMap.uiSettings.isMyLocationButtonEnabled = true
//        googleMap.setOnMyLocationButtonClickListener {
//            getLocation()
//            true
//        }

        mapController = MapController(this, googleMap)

        googleMap.setOnCameraIdleListener(mapController.clusterManager)
//        googleMap.setOnMarkerClickListener(mapController.clusterManager)

        getLocation()
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                Log.d("Log_debug", "isLocationEnabled")

                mFusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnCompleteListener { task ->
                    val location: Location? = task.result
                    Log.d("Log_debug", "$location")
                    if (location != null) {
                        CoroutineScope(Dispatchers.Main).launch {
//                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
                            currentLocation = location
                            Log.d("Log_debug", "$location")
                        }
                    } else {
                        Log.d("Log_debug", "fail")
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
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }
}