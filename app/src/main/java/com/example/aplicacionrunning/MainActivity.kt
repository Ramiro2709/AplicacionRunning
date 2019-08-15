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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions


//NOTE Se agrega el OnMapReadyCallBack para poder usar los mapas


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private lateinit var locationCallback: LocationCallback

    protected var mLastLocation: Location? = null

    private var mLatitudeText: TextView? = null
    private var mLongitudeText: TextView? = null

    private var mapFragment: SupportMapFragment? = null

    private lateinit var mutablePolyline: Polyline

    val listCoordenadas = mutableListOf<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //TODO Sacar estos textviews
        mLatitudeText = findViewById<View>(R.id.textViewLat) as TextView
        mLongitudeText = findViewById<View>(R.id.textViewLong) as TextView
        //NOTE  Variable con el cliente de FusedLocation
        //      Es lo que hace el pedido de ubicacion
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //NOTE  Callback del pedido de ubicacion
        //      Cuando FuzedLocation haga el pedido, al recibirlo llama este callback
        //      Contiene el locationResult con la ubicacion
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return //?
                for (location in locationResult.locations){
                    //NOTE !! Es para solo asignar cuando no es null(?
                    mLastLocation = location;
                    mLongitudeText!!.text = location.toString();
                }

                //NOTE Agrega un marcador en la posicion actual
                /*
                mapaRecibido!!.addMarker(
                    MarkerOptions()
                        //.position(LatLng(0.0, 0.0))
                        .position(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
                        .title("Marker")
                )
                */

                //NOTE Agrega coordenada actual a la lista de coordenadas a dibujar
                listCoordenadas.add(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))

                //NOTE Crea Poline con las coordenadas de la lista
                mutablePolyline = mapaRecibido!!.addPolyline(PolylineOptions().apply{
                    addAll(listCoordenadas)
                })
            }
        }
        askForPermissions()

        //NOTE Asigna el fragment con el mapa
            mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()

        createLocationRequest()
    }

    fun createLocationRequest() {
        //NOTE Crea el locationRequest, parametros usados para el pedido de ubicacion
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
        //NOTE  el FuzedLocation hace un pedido de ubicacion usando la configuracion del locationRequest
        //      y al recibir respuesta llama el locationCallback
        mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }


    private var mapaRecibido: GoogleMap? = null
    //NOTE Cuando recibe un mapa, lo asigna a una variable
    override fun onMapReady(map: GoogleMap) {
        mapaRecibido = map
    }

//SECTION Pide Permisos
private fun askForPermissions() {
//NOTE Si no estan los permisos FINE o COARSE
if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED)
{
    //NOTE (?
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
