package com.app.lokacara.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.Manifest
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
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.launch
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
    onLocationSelected: (MapLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initialPosition = LatLng(initialLat, initialLng)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 14f)
    }
    val markerState = rememberMarkerState(position = initialPosition)

    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<com.google.android.libraries.places.api.model.AutocompletePrediction>>(emptyList()) }
    var showDropdown by remember { mutableStateOf(false) }

    var selectedName by remember { mutableStateOf("") }
    var selectedAddress by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val placesClient = remember {
        if (!Places.isInitialized()) {
            val ai = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val key = ai.metaData.getString("com.google.android.geo.API_KEY") ?: ""
            Places.initialize(context, key)
        }
        Places.createClient(context)
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length < 2) {
            predictions = emptyList()
            showDropdown = false
            return@LaunchedEffect
        }
        try {
            val result = findPredictions(placesClient, searchQuery)
            predictions = result
            showDropdown = result.isNotEmpty()
        } catch (_: Exception) {
            predictions = emptyList()
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari tempat...", fontFamily = NunitoFont, fontSize = 13.sp, color = Gray500) },
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
                            fetchPlace(placesClient, placeId, primary, secondary) { loc ->
                                val latLng = LatLng(loc.latitude, loc.longitude)
                                selectedName = loc.name
                                selectedAddress = loc.address
                                markerState.position = latLng
                                scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f)) }
                                onLocationSelected(loc)
                            }
                        }
                    )
                }
            }
        }

        GoogleMap(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                markerState.position = latLng
                onLocationSelected(MapLocation(name = "", address = "", latitude = latLng.latitude, longitude = latLng.longitude))
            }
        ) {
            Marker(
                state = markerState,
                title = selectedName.ifEmpty { "Lokasi Event" },
                snippet = selectedAddress.ifEmpty { "Geser marker untuk menyesuaikan" },
                draggable = true
            )
        }

        Button(
            onClick = {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
                    val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null) {
                        val loc = MapLocation("Lokasi Saat Ini", "", location.latitude, location.longitude)
                        val latLng = LatLng(loc.latitude, loc.longitude)
                        markerState.position = latLng
                        scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f)) }
                        selectedName = loc.name
                        selectedAddress = loc.address
                        onLocationSelected(loc)
                    }
                }
            },
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
            onResult(MapLocation(
                name = place.name ?: primaryText,
                address = place.address ?: secondaryText,
                latitude = place.latLng?.latitude ?: -7.5615,
                longitude = place.latLng?.longitude ?: 110.8317
            ))
        }
}
