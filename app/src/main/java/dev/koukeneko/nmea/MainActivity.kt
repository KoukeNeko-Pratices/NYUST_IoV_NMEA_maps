package dev.koukeneko.nmea

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import dev.koukeneko.nmea.ui.theme.NMEATheme
import dev.koukeneko.nmea.utility.NMEAFormatter


class MainActivity : ComponentActivity(), LocationListener, OnNmeaMessageListener {

    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var datanmea = ""

    val current_Latitude = mutableStateOf(0.0)
    val current_Longitude = mutableStateOf(0.0)

    var NMEASet = mutableSetOf<dev.koukeneko.nmea.data.Location>(dev.koukeneko.nmea.data.Location(0.0, 0.0))


    override fun onCreate(savedInstanceState: Bundle?) {

        MapsInitializer.initialize(applicationContext) //for initialize IBitmapDescriptorFactory
        super.onCreate(savedInstanceState)
        setContent {
            NMEATheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InitGPSGettingLogic()
                    Greeting()
                    GetCurrentLocation()
                }
            }
        }
    }

    fun GetCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                this
            )
        }
        //convert locationManager to fusedLocationClient and log location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    Log.d("Location", location.toString())
                    current_Latitude.value = location.latitude
                    current_Longitude.value = location.longitude
                    Log.d("Location", "${current_Latitude.value},${current_Longitude.value}")
                } else {
                    Log.d("Location", "Location is null")
                }
            }


    }

    private fun InitGPSGettingLogic() {
//   create a location manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gspEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (gspEnabled) {
            //gps on
            Log.d("NMEA_APP", javaClass.name + ":" + "GPS ON :)")
            //check android.permission.ACCESS_FINE_LOCATION and android.permission.ACCESS_COARSE_LOCATION permission whether to enable
            // Check if the app has the ACCESS_FINE_LOCATION permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // If the app doesn't have the permission, request it
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 101
                )
            } else {
                // If the app has the permission, access the device's location
                // (e.g. to display a map or provide location-based services)

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    10000f,
                    this
                )
                locationManager.addNmeaListener(this)
            }

        } else {
            Log.d("NMEA_APP", javaClass.name + ":" + "GPS NOT ON")
        }

        //initialized fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onLocationChanged(location: Location) {
        //print location to log
        Log.d("NMEA_APP", javaClass.name + ":" + "GPS")

    }

    override fun onNmeaMessage(message: String?, timestamp: Long) {
        Log.d(
            "NMEA_APP",
            javaClass.name + ":" + "[" + timestamp + "] " + message + ""
        )
        Log.d("NMEA_APP_MESSAGE", message.toString())

        val nmea = NMEAFormatter(message.toString()).getLatLong()
        //check nmea whether is null
        if (nmea != null) {
            NMEASet.add(nmea)
        }
        if (NMEASet.size > 20) {
            NMEASet.remove(NMEASet.first())
        }
        Log.d("NMEAFormatter", nmea.toString())
        Log.d("NMEASet_APP", NMEASet.toString())

    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun Greeting() {

        val current = LatLng(current_Latitude.value, current_Longitude.value)

        val kaohsiung = LatLng(22.63, 120.27)
        val taipei = LatLng(25.04, 121.5)


        //build camera position
        val cameraPosition = rememberCameraPositionState {
            position = if (current_Latitude.value != 0.0 && current_Longitude.value != 0.0) {
                CameraPosition.fromLatLngZoom(kaohsiung, 10f)
            } else {
                CameraPosition.fromLatLngZoom(
                    LatLng(
                        current_Latitude.value,
                        current_Longitude.value
                    ), 10f
                )
            }
            Log.d("CameraLocationUpdate", "${current_Latitude.value},${current_Longitude.value}")
        }

        //if current_Latitude.value and current_Longitude.value change, camera position will change
        LaunchedEffect(current_Latitude.value, current_Longitude.value) {
            cameraPosition.move(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        LatLng(current_Latitude.value, current_Longitude.value),
                        17f
                    )
                )
            )
        }

        //create a set store markers


        val settings = MapUiSettings(
            myLocationButtonEnabled = true,
            mapToolbarEnabled = true,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true,
            tiltGesturesEnabled = true,
            rotationGesturesEnabled = true,
            compassEnabled = true
        )

        Column(modifier = Modifier.fillMaxSize()) {

            Card(
                modifier = Modifier
                    .padding(16.dp),

                shape = RoundedCornerShape(16.dp)
            ) {
                GoogleMap(
                    modifier = Modifier,
                    cameraPositionState = cameraPosition,
                    uiSettings = settings,

                    ) {

                    Marker(
                        state = MarkerState(position = current),
                        title = "Current",
                        snippet = "Marker in Current",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )

                    //put the marker on the map
                    Marker(
                        state = MarkerState(position = kaohsiung),
                        title = "Kaohsiung",
                        snippet = "Marker in Kaohsiung",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )

                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                NMEASet.first().latitude,
                                NMEASet.first().longitude
                            )
                        ),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                    )


                }
            }
            Row(
                modifier = Modifier
            ) {
                //Filed to input latitude and longitude
                TextField(
                    value = "",
                    onValueChange = { TODO() },
                    label = { Text("Latitude") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                )
                TextField(
                    value = "",
                    onValueChange = { TODO() },
                    label = { Text("Longitude") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                )
            }


        }
    }
}

enum class SatelliteTypes {
    GPS,
    Galileo,
    BeiDou,
}

data class Position(
    val type: SatelliteTypes,
    val quality_indicator: Double,
    val timestamp: Long,

    val latitude: Double,
    val longitude: Double,

    )


//
//@Preview(showBackground = false)
//@Composable
//fun DefaultPreview() {
//    NMEATheme {
//        initGPSgettingLogic()
//    }
//}