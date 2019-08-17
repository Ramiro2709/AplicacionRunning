package com.example.aplicacionrunning

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


//NOTE Se agrega el OnMapReadyCallBack para poder usar los mapas


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private lateinit var locationCallback: LocationCallback

    protected var mLastLocation: Location? = null

    private var mapFragment: SupportMapFragment? = null

    private lateinit var mutablePolyline: Polyline
    private lateinit var marcador: Marker

    val listCoordenadas = mutableListOf<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonEmpezar = findViewById(R.id.buttonEmpezar) as Button
        //NOTE  Al apretar el boton empeiza a actualizar ubicacion
        //      Al apretarlo otra vez lo detiene
        var actualizando: Boolean = false;
        buttonEmpezar.setOnClickListener {
            if (!actualizando){
                actualizando = true;
                startLocationUpdates()
                buttonEmpezar.text = "Detener"
            } else {
                actualizando = false;
                stopLocationUpdates()
                buttonEmpezar.text = "Empezar"
            }
        }

        val buttonTerminar = findViewById(R.id.buttonTerminar) as Button
        buttonTerminar.setOnClickListener {
            listCoordenadas.clear()
            marcador.remove()
            mutablePolyline.remove()
            stopLocationUpdates()
            val textView: TextView = findViewById(R.id.textView3) as TextView
            // TODO Al terminar calcular distancia total recorrida y velocidad promedio y mostrarlo en pantalla
        }

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
                    mLastLocation = location;
                }

                //NOTE Agrega coordenada actual a la lista de coordenadas a dibujar
                listCoordenadas.add(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))

                //NOTE Crea Poline con las coordenadas de la lista
                mutablePolyline = mapaRecibido!!.addPolyline(PolylineOptions().apply{ //NOTE !! Es para solo asignar cuando no es null(?
                    color(Color.BLUE)
                    addAll(listCoordenadas)
                    width(10f)
                })

                //NOTE Mueve camara a ultima posicion
                with(mapaRecibido) {
                    this!!.moveCamera(CameraUpdateFactory.newLatLngZoom( listCoordenadas.last(), 17f))
                }

                if (::marcador.isInitialized) marcador.remove()
                marcador = mapaRecibido!!.addMarker(
                    MarkerOptions()
                        .position(listCoordenadas.last())
                        .title("Posicion Actual")
                )

                //NOTE Distancia entre dos ultimas coordenadas
                if (listCoordenadas.size > 1){
                    val loc1 = Location("")
                    loc1.latitude = mLastLocation!!.latitude
                    loc1.longitude = mLastLocation!!.longitude

                    val loc2 = Location("")
                    loc2.latitude = listCoordenadas[listCoordenadas.size-2].latitude
                    loc2.longitude = listCoordenadas[listCoordenadas.size-2].longitude

                    var distancia = loc1.distanceTo(loc2)

                    val textView: TextView = findViewById(R.id.textView3) as TextView
                    textView.text = distancia.toString()
                }
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
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
        //val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest) //Necesario?
    }

    override fun onResume() {
        super.onResume()
        //if (requestingLocationUpdates) //?
        //startLocationUpdates()
    }

    @SuppressLint("MissingPermission")

    //NOTE Empieza a recibir la ubicacion
    private fun startLocationUpdates() {
        //NOTE  el FuzedLocation hace un pedido de ubicacion usando la configuracion del locationRequest
        //      y al recibir respuesta llama el locationCallback
        mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    //NOTE Para de recibir
    private fun stopLocationUpdates() {
        mFusedLocationClient!!.removeLocationUpdates(locationCallback)
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
