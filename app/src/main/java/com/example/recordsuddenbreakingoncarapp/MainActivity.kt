package com.example.recordsuddenbreakingoncarapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.recordsuddenbreakingoncarapp.DBHelper
import com.example.recordsuddenbreakingoncarapp.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationManager: LocationManager
    private lateinit var dbHelper: DBHelper
    private lateinit var locationListener: LocationListener
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    private val thresholdSpeed = -10.0 // Define the threshold speed for breaking events

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        dbHelper = DBHelper(applicationContext)

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val speed = location.speed
                if (speed <= thresholdSpeed) {
                    saveBrakingEvent(speed, location)
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        mapView.onResume()

        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        locationManager.removeUpdates(locationListener)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        displayBrakingEvents()
    }

    private fun saveBrakingEvent(speed: Float, location: Location) {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(DBHelper.COLUMN_SPEED, speed)
            put(DBHelper.COLUMN_TIMESTAMP, currentTime)
            put(DBHelper.COLUMN_LATITUDE, location.latitude)
            put(DBHelper.COLUMN_LONGITUDE, location.longitude)
        }
        db.insert(DBHelper.TABLE_SPEED, null, contentValues)
        db.close()
        displayBrakingEvents()
    }

    private fun displayBrakingEvents() {
        googleMap.clear()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DBHelper.TABLE_SPEED}", null)
        if (cursor.moveToFirst()) {
            do {
                val speed = cursor.getFloat(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_SPEED))
                val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_LATITUDE))
                val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_LONGITUDE))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP))

                val latLng = LatLng(latitude, longitude)
                googleMap.addMarker(
                    MarkerOptions().position(latLng).title("Braking Event")
                        .snippet("Speed: $speed m/s\nTime: $timestamp")
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
            } while (cursor.moveToNext())
        } else {
            Toast.makeText(this, "No braking events recorded", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
        db.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onResume()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied. Cannot track braking events.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }
}