package com.jccgs.travelplanner_v2.jkim

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.ClusterRenderer
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class MapController(val context: Context?, val googleMap: GoogleMap,
): GoogleMap.OnMapClickListener,
    GoogleMap.OnPoiClickListener {

    private val STREET = 15F

    val TAG = "Log_debug"

    //다른 액티비티에서 장소 정보에 접근하기 위한 companion object
    companion object {
        var selectedPlaceCountryName: String? = ""
        var selectedPlaceName: String? = ""
        var selectedPlaceAddress: String? = ""
        var selectedPlaceLatLng: LatLng? = null
        var selectedPlaceCity: String? = ""
        var selectedPlaceShortName: String? = ""

        fun clearMapInfo(){
            selectedPlaceCountryName = null
            selectedPlaceName = null
            selectedPlaceAddress = null
            selectedPlaceLatLng = null
            selectedPlaceCity = null
            selectedPlaceShortName = null
        }
    }

    var clusterManager: ClusterManager<Cluster>

    private var bound: LatLngBounds.Builder
    private var placesClient: PlacesClient

    init {
        placesClient = Places.createClient(context)
        bound = LatLngBounds.builder()
        clusterManager = CustomClusterManager(context!!, googleMap)
        googleMap.setOnMarkerClickListener(clusterManager.markerManager)
    }


    //맵 클릭시 이벤트. 현재 프로젝트에서 안쓰이는듯...
    override fun onMapClick(latLng: LatLng) {
        val newPosition = LatLng(latLng.latitude, latLng.longitude)
        Log.d("Log_debug", "$newPosition")

        googleMap.clear()
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition))
    }

    //장소 마커 클릭 시 이벤트
    override fun onPoiClick(poi: PointOfInterest) {
        //선택된 장소(Poi)의 LatLng, Name, Address 구함
        val placeFields = listOf<Place.Field>(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.newInstance(poi.placeId, placeFields)
        placesClient.fetchPlace(request).addOnSuccessListener {
            //다른 마커들 지움
            googleMap.clear()

            selectedPlaceName = it.place.name
            selectedPlaceAddress = it.place.address
            selectedPlaceLatLng = it.place.latLng

            addMark(listOf(it.place.latLng))
            moveCamera(it.place.latLng)
        }

    }

    fun moveCamera(latLng: LatLng){
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    //단일 마커를 위한 함수
    fun addMark(locations: List<LatLng>){
        //이 전에 추가된 마커들 삭제
        clusterManager.clearItems()

        for (i in locations) {
            val latLng = LatLng(i.latitude, i.longitude)
            val clusterItem = Cluster(latLng.latitude, latLng.longitude, "$selectedPlaceName", "$selectedPlaceAddress")
            clusterManager.addItem(clusterItem)
            bound.include(latLng)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(i, STREET))
        }

        //추가된 마커들을 지도에 표시
        clusterManager.cluster()
    }

    //일정에 저장된 모든 장소에 마커를 추가하기 위한 함수
    fun addDailyPlanMarkers(dailyPlans: MutableList<DailyPlan>){
//        googleMap.clear()
        clusterManager.clearItems()
        for (i in dailyPlans){
            val latLng = LatLng(i.placeLat, i.placeLng)

            val clusterItem = Cluster(latLng.latitude, latLng.longitude, "${i.placeName}", "${i.placeAddress}")
            clusterManager.addItem(clusterItem)
            bound.include(latLng)
            Log.d("Log_debug", "dailyplans location : ${i}")
        }
        clusterManager.cluster()
    }

    //마커의 색, 마커가 표시될 때 infoWindow를 자동으로 띄우기 위한 Renderer 클래스
    val renderer = object :DefaultClusterRenderer<Cluster>(context, googleMap, clusterManager){

        override fun onBeforeClusterItemRendered(item: Cluster, markerOptions: MarkerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            super.onBeforeClusterItemRendered(item, markerOptions)
        }

        override fun onClusterItemRendered(clusterItem: Cluster, marker: Marker) {
            marker.showInfoWindow()
            super.onClusterItemRendered(clusterItem, marker)
        }
    }

    //주변 검색시 인접한 마커 병합 및 개수 표시를 위한 ClusterItem과 ClusterManager
    inner class Cluster (lat: Double,
                         lng: Double,
                         title: String,
                         snippet: String
    ) : ClusterItem {
        private val position: LatLng
        private val title: String
        private val snippet: String

        override fun getPosition(): LatLng {
            return position
        }

        override fun getTitle(): String? {
            return title
        }

        override fun getSnippet(): String? {
            return snippet
        }

        init {
            position = LatLng(lat, lng)
            this.title = title
            this.snippet = snippet
        }
    }

    inner class CustomClusterManager(val context: Context, val googleMap: GoogleMap): ClusterManager<Cluster>(context, googleMap)
}