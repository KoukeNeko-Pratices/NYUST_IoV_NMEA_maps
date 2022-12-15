package dev.koukeneko.nmea

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import dev.koukeneko.nmea.ui.theme.NMEATheme

class MainActivity : ComponentActivity(), LocationListener, OnNmeaMessageListener {

    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var datanmea = ""

    val current_Latitude = mutableStateOf(0.0)
    val current_Longitude = mutableStateOf(0.0)


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
                }else {
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
    }

    // Function that converts the vector form to Bitmap form.
    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    @Preview
    @Composable
    fun Greeting() {

        val current = LatLng(current_Latitude.value, current_Longitude.value)

        val kaohsiung = LatLng(22.63, 120.27)
        val taipei = LatLng(25.04, 121.5)

        //create a bitmap marker from vector drawable baseline_my_location_24
        val bitmap = getBitmapFromVectorDrawable(this, R.drawable.baseline_my_location_24)
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)


        //build camera position
        val cameraPosition = rememberCameraPositionState {
            position = if (current_Latitude.value != 0.0 && current_Longitude.value != 0.0) {
                CameraPosition.fromLatLngZoom(kaohsiung, 10f)
            }else{
                CameraPosition.fromLatLngZoom(LatLng(current_Latitude.value,current_Longitude.value), 10f)
            }
            Log.d("CameraLocationUpdate", "${current_Latitude.value},${current_Longitude.value}")
        }

        //if current_Latitude.value and current_Longitude.value change, camera position will change
        LaunchedEffect(current_Latitude.value, current_Longitude.value) {
            cameraPosition.move(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(LatLng(current_Latitude.value,current_Longitude.value), 10f)))
        }


        Column(modifier = Modifier.fillMaxSize()) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPosition,
            ) {
//                Marker(
//                    state = MarkerState(position = singapore),
//                    title = "Singapore",
//                    snippet = "Marker in Singapore"
//                )
                Marker(
                    state = MarkerState(position = current),
                    title = "Current",
                    snippet = "Marker in Current",
                    icon = bitmapDescriptor
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