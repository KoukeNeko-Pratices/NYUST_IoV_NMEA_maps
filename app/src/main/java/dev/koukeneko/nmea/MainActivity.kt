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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import dev.koukeneko.nmea.ui.theme.NMEATheme

class MainActivity : ComponentActivity(), LocationListener, OnNmeaMessageListener {

    private lateinit var locationManager : LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var datanmea = ""


    override fun onCreate(savedInstanceState: Bundle?) {
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
                }
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // If the app doesn't have the permission, request it
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),101)
            } else {
                // If the app has the permission, access the device's location
                // (e.g. to display a map or provide location-based services)

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10000f, this)
                locationManager.addNmeaListener(this)
            }

        }else{
            Log.d("NMEA_APP", javaClass.name + ":" + "GPS NOT ON")
        }
    }

    override fun onLocationChanged(location: Location) {
        //print location to log
        Log.d("NMEA_APP", javaClass.name + ":" + "GPS")
    }

    override fun onNmeaMessage(message: String?, timestamp: Long) {
        Log.d(
            "NMEA_APP",
            javaClass.name + ":" + "[" + timestamp + "] " + message+ ""
        )
    }

    @Preview
    @Composable
    fun Greeting() {

        val singapore = LatLng(1.35, 103.87)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(singapore, 10f)
        }

        Column(modifier = Modifier.fillMaxSize()) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = singapore),
                    title = "Singapore",
                    snippet = "Marker in Singapore"
                )
            }
        }
    }
}

enum class SatelliteTypes{
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