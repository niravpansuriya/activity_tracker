package com.example.activitytracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class Map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    // Fused Location Client is used to retrieve the user's location in
    // a way that is optimized for battery life and accuracy,
    // by combining data from various sensors including GPS,
    // Wi-Fi, and cellular networks.
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var marker: Marker? = null
    private var polyline: Polyline? = null

    /**
     * REQUEST_CODE is used when requesting location permission,
     * and DEFAULT_ZOOM (zoom on the map), UPDATE_INTERVAL, and FASTEST_UPDATE_INTERVAL
     * are used when displaying the user's location on the map.
     */
    companion object {
        private const val REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15f
        private const val UPDATE_INTERVAL = 1000L // 1 second
        private const val FASTEST_UPDATE_INTERVAL = 500L // 0.5 second
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_location)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        map = googleMap

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_CODE
            )
            return
        }

        // Enable my location layer and move camera to current location
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 21f))
            }
        }

        // Add listener to update marker and polyline when location changes
        map.setOnMyLocationChangeListener { location ->
            updateLocationOnMap(location)
        }

        // Start location updates
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        // Request high accuracy and faster update intervals for better location accuracy
        val locationRequest = LocationRequest()
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        // request location update
        // location request contains all necessary parameters
        // when update will be done, locationCallBack will be called
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    // this will be called when location will be udpated
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            locationResult?.lastLocation?.let {
                // update location marker on the map
                // so if user is running, at every 5 seconds
                // location marker will be updated
                updateLocationOnMap(it, marker?.position?.let { previousLocation ->
                    Location("").apply {
                        latitude = previousLocation.latitude
                        longitude = previousLocation.longitude
                    }
                })
            }
        }
    }

    // it will update the location on the map
    // it will update the position of the marker on the map
    private fun updateLocationOnMap(currentLocation: Location?, previousLocation: Location? = null) {
        currentLocation?.let {
            val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
            marker?.let {
                // Update existing marker position and rotation angle
                it.position = latLng
                it.rotation = computeRotation(previousLocation, currentLocation)
            } ?: run {
                // Create new marker if it doesn't exist
                marker = map.addMarker(
                    MarkerOptions().position(latLng).rotation(computeRotation(previousLocation, currentLocation))
                )
            }
            // Move camera to updated location and draw polyline
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
            drawPolyline(currentLocation)
        }
    }

    // it calculates the rotation of the user,
    // so marker can be updated accordingly
    private fun computeRotation(previousLocation: Location?, currentLocation: Location): Float {
        return previousLocation?.let { prevLoc ->
            val bearing = prevLoc.bearingTo(currentLocation)
            (bearing + 360) % 360
        } ?: run {
            0f
        }
    }

    private fun drawPolyline(currentLocation: Location) {
        polyline?.let {
            // Add new point to existing polyline
            val points = it.points
            points.add(LatLng(currentLocation.latitude, currentLocation.longitude))
            it.points = points
        } ?: run {
            // Create new polyline if it doesn't exist
            polyline = map.addPolyline(
                PolylineOptions().apply {
                    color(ContextCompat.getColor(this@Map, R.color.polyline_color))
                    width(resources.getDimensionPixelSize(R.dimen.polyline_width).toFloat())
                    add(LatLng(currentLocation.latitude, currentLocation.longitude))
                }
            )
        }
    }


    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }


    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}