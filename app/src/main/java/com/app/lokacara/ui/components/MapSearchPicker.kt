package com.app.lokacara.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.SvgOrange
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class MapLocation(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

@SuppressLint("MissingPermission")
@Composable
fun MapSearchPicker(
    initialLat: Double = -7.5615,
    initialLng: Double = 110.8317,
    selectedLocationName: String = "",
    selectedLocationAddress: String = "",
    selectedLatitude: Double? = null,
    selectedLongitude: Double? = null,
    onLocationSelected: (MapLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initialPosition = LatLng(selectedLatitude ?: initialLat, selectedLongitude ?: initialLng)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, if (selectedLatitude != null) 16f else 14f)
    }
    val markerState = rememberMarkerState(position = initialPosition)

    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<com.google.android.libraries.places.api.model.AutocompletePrediction>>(emptyList()) }
    var showDropdown by remember { mutableStateOf(false) }

    var pickedName by remember { mutableStateOf(selectedLocationName) }
    var pickedAddress by remember { mutableStateOf(selectedLocationAddress) }
    var isResolvingLocation by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun applySelectedLocation(location: MapLocation, animateCamera: Boolean = true) {
        val latLng = LatLng(location.latitude, location.longitude)
        markerState.position = latLng
        pickedName = location.name
        pickedAddress = location.address
        onLocationSelected(location)
        if (animateCamera) {
            scope.launch {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }
        }
    }

    fun resolveAndSelectLocation(latLng: LatLng, fallbackName: String, animateCamera: Boolean = true) {
        markerState.position = latLng
        isResolvingLocation = true
        scope.launch {
            val location = resolveMapLocation(context, latLng, fallbackName)
            applySelectedLocation(location, animateCamera)
            isResolvingLocation = false
        }
    }

    fun useCurrentLocation() {
        if (hasLocationPermission(context)) {
            isResolvingLocation = true
            scope.launch {
                val location = getCurrentDeviceLocation(context)
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    markerState.position = latLng
                    val resolvedLocation = resolveMapLocation(context, latLng, "Lokasi Saat Ini")
                    applySelectedLocation(resolvedLocation)
                } else {
                    SnackbarManager.showError("Lokasi saat ini belum tersedia")
                }
                isResolvingLocation = false
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            useCurrentLocation()
        } else {
            SnackbarManager.showError("Izin lokasi diperlukan untuk memakai lokasi saat ini")
        }
    }

    val placesClient = remember {
        runCatching {
            if (!Places.isInitialized()) {
                val ai = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                val key = ai.metaData.getString("com.google.android.geo.API_KEY") ?: ""
                if (key.isBlank()) return@runCatching null
                Places.initialize(context, key)
            }
            Places.createClient(context)
        }.getOrNull()
    }

    LaunchedEffect(selectedLocationName, selectedLocationAddress) {
        pickedName = selectedLocationName
        pickedAddress = selectedLocationAddress
    }

    LaunchedEffect(selectedLatitude, selectedLongitude) {
        if (selectedLatitude != null && selectedLongitude != null) {
            val latLng = LatLng(selectedLatitude, selectedLongitude)
            markerState.position = latLng
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length < 2) {
            predictions = emptyList()
            showDropdown = false
            return@LaunchedEffect
        }
        delay(250)
        val client = placesClient ?: run {
            predictions = emptyList()
            showDropdown = false
            return@LaunchedEffect
        }
        try {
            val result = findPredictions(client, searchQuery)
            predictions = result
            showDropdown = result.isNotEmpty()
        } catch (_: Exception) {
            predictions = emptyList()
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val hasSelectedLocation = pickedName.isNotBlank() || pickedAddress.isNotBlank()

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hasSelectedLocation) Icons.Outlined.CheckCircle else Icons.Outlined.Place,
                    contentDescription = "Lokasi terpilih",
                    tint = if (hasSelectedLocation) SvgOrange else Gray500,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pickedName.ifBlank { "Belum ada lokasi dipilih" },
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Gray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            isResolvingLocation -> "Mencari detail alamat..."
                            pickedAddress.isNotBlank() -> pickedAddress
                            else -> "Cari tempat, ketuk peta, atau gunakan lokasi saat ini"
                        },
                        fontFamily = NunitoFont,
                        fontSize = 11.sp,
                        color = Gray500,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari venue atau alamat...", fontFamily = NunitoFont, fontSize = 13.sp, color = Gray500) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Cari", tint = Gray500, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Gray900,
                    unfocusedTextColor = Gray900
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                textStyle = TextStyle(fontFamily = NunitoFont, fontSize = 14.sp)
            )

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier.fillMaxWidth(0.85f).background(Color.White).heightIn(max = 240.dp)
            ) {
                predictions.take(5).forEach { prediction ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(prediction.getPrimaryText(null).toString(), fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Gray900)
                                Text(prediction.getSecondaryText(null).toString(), fontSize = 11.sp, color = Gray500)
                            }
                        },
                        onClick = {
                            val placeId = prediction.placeId
                            val primary = prediction.getPrimaryText(null).toString()
                            val secondary = prediction.getSecondaryText(null).toString()
                            searchQuery = ""
                            showDropdown = false
                            val client = placesClient ?: return@DropdownMenuItem
                            fetchPlace(client, placeId, primary, secondary) { loc ->
                                applySelectedLocation(loc)
                            }
                        }
                    )
                }
            }
        }

        if (placesClient == null) {
            Text(
                text = "Pencarian tempat belum aktif. Ketuk peta atau gunakan lokasi saat ini.",
                fontFamily = NunitoFont,
                fontSize = 11.sp,
                color = Gray500
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    resolveAndSelectLocation(
                        latLng = latLng,
                        fallbackName = "Lokasi Event",
                        animateCamera = false
                    )
                }
            ) {
                Marker(
                    state = markerState,
                    title = pickedName.ifEmpty { "Lokasi Event" },
                    snippet = pickedAddress.ifEmpty { "Ketuk peta untuk memilih lokasi" },
                    draggable = true
                )
            }
            if (isResolvingLocation) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.95f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = SvgOrange,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Mencari alamat",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Gray900
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
                    useCurrentLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            enabled = !isResolvingLocation,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SvgOrange),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.MyLocation, contentDescription = "Lokasi", tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gunakan Lokasi Saat Ini", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
private suspend fun getCurrentDeviceLocation(context: Context): Location? {
    if (!hasLocationPermission(context)) return null
    return suspendCancellableCoroutine { continuation ->
        val tokenSource = CancellationTokenSource()
        LocationServices.getFusedLocationProviderClient(context)
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location ->
                if (continuation.isActive) {
                    continuation.resume(location ?: getLastKnownLocation(context))
                }
            }
            .addOnFailureListener {
                if (continuation.isActive) {
                    continuation.resume(getLastKnownLocation(context))
                }
            }
        continuation.invokeOnCancellation {
            tokenSource.cancel()
        }
    }
}

@SuppressLint("MissingPermission")
private fun getLastKnownLocation(context: Context): Location? {
    if (!hasLocationPermission(context)) return null
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return runCatching {
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }.getOrNull()
}

@Suppress("DEPRECATION")
private suspend fun resolveMapLocation(
    context: Context,
    latLng: LatLng,
    fallbackName: String
): MapLocation = withContext(Dispatchers.IO) {
    val fallbackAddress = formatCoordinateAddress(latLng.latitude, latLng.longitude)
    runCatching {
        val address = Geocoder(context, Locale("id", "ID"))
            .getFromLocation(latLng.latitude, latLng.longitude, 1)
            ?.firstOrNull()

        val addressLine = address?.getAddressLine(0)?.trim().orEmpty()
        val name = listOf(
            address?.featureName?.trim().orEmpty(),
            address?.thoroughfare?.trim().orEmpty(),
            address?.locality?.trim().orEmpty(),
            fallbackName
        ).firstOrNull { it.isNotBlank() } ?: fallbackName

        MapLocation(
            name = name,
            address = addressLine.ifBlank { fallbackAddress },
            latitude = latLng.latitude,
            longitude = latLng.longitude
        )
    }.getOrElse {
        MapLocation(
            name = fallbackName,
            address = fallbackAddress,
            latitude = latLng.latitude,
            longitude = latLng.longitude
        )
    }
}

private fun formatCoordinateAddress(latitude: Double, longitude: Double): String {
    return String.format(Locale.US, "Koordinat %.5f, %.5f", latitude, longitude)
}

private suspend fun findPredictions(
    client: PlacesClient,
    query: String
): List<com.google.android.libraries.places.api.model.AutocompletePrediction> {
    return suspendCancellableCoroutine { cont ->
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries(listOf("ID"))
            .build()
        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                cont.resume(response.autocompletePredictions)
            }
            .addOnFailureListener {
                cont.resume(emptyList())
            }
    }
}

@Suppress("DEPRECATION")
private fun fetchPlace(
    client: PlacesClient,
    placeId: String,
    primaryText: String,
    secondaryText: String,
    onResult: (MapLocation) -> Unit
) {
    val request = FetchPlaceRequest.builder(
        placeId,
        listOf(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS)
    ).build()
    client.fetchPlace(request)
        .addOnSuccessListener { response ->
            val place = response.place
            val latLng = place.latLng
            if (latLng == null) {
                SnackbarManager.showError("Detail koordinat tempat tidak tersedia")
                return@addOnSuccessListener
            }
            onResult(
                MapLocation(
                    name = place.name?.trim().orEmpty().ifBlank { primaryText },
                    address = place.address?.trim().orEmpty().ifBlank { secondaryText.ifBlank { primaryText } },
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                )
            )
        }
        .addOnFailureListener {
            SnackbarManager.showError("Gagal mengambil detail tempat")
        }
}
