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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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

    var isLocationPermissionGranted = mutableStateOf(false)

    val current_Latitude = mutableStateOf(-1.0)
    val current_Longitude = mutableStateOf(-1.0)

    var nmea_latitude = mutableStateOf(-1.0)
    var nmea_longitude = mutableStateOf(-1.0)

    var fist_latitude = mutableStateOf(-1.0)
    var fist_longitude = mutableStateOf(-1.0)


    override fun onCreate(savedInstanceState: Bundle?) {

        MapsInitializer.initialize(applicationContext) //for initialize IBitmapDescriptorFactory
        super.onCreate(savedInstanceState)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )


        setContent {
            NMEATheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()

                val color = MaterialTheme.colorScheme.background
                SideEffect {
                    systemUiController.setNavigationBarColor(
                        color = color,
                        darkIcons = useDarkIcons
                    )

                    systemUiController.setStatusBarColor(
                        color = color,
                        darkIcons = useDarkIcons
                    )
                }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InitGPSGettingLogic()
                    GetCurrentLocation()
                    if (isLocationPermissionGranted.value)
                        Greeting()
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
                    fist_latitude.value = location.latitude
                    fist_longitude.value = location.longitude
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
                isLocationPermissionGranted.value = true
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
            nmea_latitude.value = nmea.latitude
            nmea_longitude.value = nmea.longitude
        }

        Log.d("NMEA_APP", "nmea_latitude : ${nmea_latitude.value}")
        Log.d("NMEA_APP", "nmea_longitude : ${nmea_longitude.value}")

        //get last location from fusedLocationClient
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



    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun Greeting() {

        val current = LatLng(current_Latitude.value, current_Longitude.value)

        val kaohsiung = LatLng(22.63, 120.27)
        val taipei = LatLng(25.04, 121.5)


        var manualLatitude by remember { mutableStateOf(-1.0) }
        var manualLongitude by remember { mutableStateOf(-1.0) }
        var openDialog by remember { mutableStateOf(false) }
        var editMessage by remember { mutableStateOf("") }
        var editMessage1 by remember { mutableStateOf("") }

        //build camera position
        val cameraPosition = rememberCameraPositionState {
            position = if (current_Latitude.value != 0.0 && current_Longitude.value != 0.0) {
                CameraPosition.fromLatLngZoom(kaohsiung, 10f)
            } else {
                CameraPosition.fromLatLngZoom(
                    LatLng(
                        fist_latitude.value,
                        fist_longitude.value
                    ), 10f
                )
            }
            Log.d("CameraLocationUpdate", "${fist_latitude.value},${fist_longitude.value}")
        }

        //if current_Latitude.value and current_Longitude.value change, camera position will change
        LaunchedEffect(fist_latitude.value, fist_longitude.value) {
            cameraPosition.move(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        LatLng(fist_latitude.value, fist_longitude.value),
                        17f
                    )
                )
            )
        }


        val settings = MapUiSettings(
            myLocationButtonEnabled = true,
            mapToolbarEnabled = true,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true,
            tiltGesturesEnabled = true,
            rotationGesturesEnabled = true,
            compassEnabled = true,
            zoomControlsEnabled = true,
            indoorLevelPickerEnabled = true,

            )

        Box {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Location Checker Demo",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 8.dp)

                )

                Card(
                    modifier = Modifier
                        .height(520.dp),
                    //                    .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier,
                        cameraPositionState = cameraPosition,
                        uiSettings = settings,
                        properties = MapProperties(
                            mapType = MapType.SATELLITE,
                            isMyLocationEnabled = true,
                            isBuildingEnabled = true,

                            )

                    ) {

                        //if nmea_latitude.value and nmea_longitude.value change, marker will change
                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    nmea_latitude.value,
                                    nmea_longitude.value
                                )
                            ),
                            visible = nmea_latitude.value != -1.0 && nmea_longitude.value != -1.0,
                            title = "NMEA",
                            snippet = "${nmea_latitude.value},${nmea_longitude.value}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )

                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    manualLatitude,
                                    manualLongitude
                                )
                            ),
                            visible = manualLatitude != -1.0 && manualLatitude != -1.0,
                            title = "Manual",
                            snippet = "${manualLatitude},${manualLongitude}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )

                    }


                }
                Column(
                    modifier = Modifier
                ) {
                    Text("GMS API Location", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    Text("${current_Latitude.value},${current_Longitude.value}")
                    Text("NMEA Location", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    Text("${nmea_latitude.value},${nmea_longitude.value}")
                    Text("Manual Location", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    Text("${manualLatitude},${manualLongitude}")
                }
                Row(
                    modifier = Modifier
                ) {

                    Button(
                        modifier = Modifier,
                        onClick = {
                            openDialog = true
                        }) {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_edit_location_alt_24),
                                contentDescription = "add a location",
                                tint = MaterialTheme.colorScheme.surface
                            )
                            Text("Change manual location")
                        }

                    }
                    val forceManager = LocalFocusManager.current

                    //open dialog
                    if (openDialog) {
                        AlertDialog(
                            onDismissRequest = { openDialog = false },
                            title = { Text("Change manual location") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = editMessage,
                                        onValueChange = { editMessage = it },
                                        label = { Text("Latitude") },
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Next
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                forceManager.moveFocus(FocusDirection.Down)
                                                try{

                                                    manualLatitude = editMessage.toDouble()
                                                }catch (e:Exception){
                                                    //do nothing
                                                }
                                            }
                                        )
                                    )

                                    OutlinedTextField(
                                        value = editMessage1,
                                        onValueChange = { editMessage1 = it },
                                        label = { Text("Longitude") },
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                forceManager.clearFocus()
                                                try{
                                                    manualLongitude = editMessage1.toDouble()
                                                }catch (e:Exception){
                                                    //empty String, do nothing
                                                }

                                            }
                                        )
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        try{
                                            manualLatitude = editMessage.toDouble()
                                            manualLongitude = editMessage1.toDouble()
                                        }catch (e:Exception){
                                            //empty String, do nothing
                                        }
                                        openDialog = false
                                    }
                                ) {
                                    Text("Confirm")
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = {
                                        openDialog = false
                                    }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                }


            }
        }


    }
}