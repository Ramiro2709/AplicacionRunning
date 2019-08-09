package com.example.aplicacionrunning

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private lateinit var locationCallback: LocationCallback

    protected var mLastLocation: Location? = null

    private var mLatitudeText: TextView? = null
    private var mLongitudeText: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mLatitudeText = findViewById<View>(R.id.textViewLat) as TextView
        mLongitudeText = findViewById<View>(R.id.textViewLong) as TextView
        //NOTE Variable con el cliente de FusedLocation
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //NOTE Callback del pedido de locacion, objeto con la locacion, llamado al recebirlo
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return //?
                for (location in locationResult.locations){
                    //NOTE !! Es para solo asignar cuando no es null(?
                    mLongitudeText!!.text = location.toString();
                }
            }
        }
        askForPermissions();

    }

    override fun onStart() {
        super.onStart()

        //NOTE Crea el pedido de locacion
        createLocationRequest()
    }

    fun createLocationRequest() {
        locationRequest = LocationRequest.create()?.apply { //Quitado ? despues de create()
            interval = 10000
            fastestInterval = 5000 //TODO Bajar intervalo?
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        //val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest) //Necesario?

    }

    override fun onResume() {
        super.onResume()
        //if (requestingLocationUpdates) //?
        startLocationUpdates()
        //TODO Detener updates
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    //SECTION Pide Permisos
    private fun askForPermissions() {
        //NOTE Si el FINE o COARSE no tienen permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        {
            //NOTE ?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION))
            {

            }
            //NOTE Pregunta por permisos
            else {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 0
                )
            }
        }
    }
}
