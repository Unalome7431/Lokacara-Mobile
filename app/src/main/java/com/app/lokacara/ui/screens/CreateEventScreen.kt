package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.PlusJakartaSansFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(onBack: () -> Unit) {
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var ticketPrice by remember { mutableStateOf("") }
    var ticketStock by remember { mutableIntStateOf(50) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Buat Event",
                        style = TextStyle(
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back", 
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFAF8FF)
                )
            )
        },
        containerColor = Color(0xFFFAF8FF)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Section: Information
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Informasi Dasar",
                        style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFCFDEFC))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudUpload, null, tint = Color(0xFF0D59F2), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Unggah Poster Event", color = Color(0xFF0D59F2), fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Input Fields
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CreateEventTextField("Nama Event", "Masukkan nama event...", eventName) { eventName = it }
                    CreateEventTextField("Deskripsi", "Ceritakan tentang event kamu...", eventDescription, singleLine = false) { eventDescription = it }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            CreateEventTextField("Tanggal", "DD/MM/YYYY", eventDate) { eventDate = it }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CreateEventTextField("Lokasi", "Nama tempat...", eventLocation) { eventLocation = it }
                        }
                    }
                }
            }

            // Section: Ticket & Capacity
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Tiket & Kapasitas",
                        style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(0.6f)) {
                            CreateEventTextField("Harga Tiket", "Rp 0", ticketPrice, KeyboardType.Number) { ticketPrice = it }
                        }
                        
                        Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.End) {
                            Text("Stok Tiket", style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 14.sp, fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(25.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(25.dp))
                                    .padding(4.dp)
                            ) {
                                IconButton(
                                    onClick = { if (ticketStock > 0) ticketStock-- },
                                    modifier = Modifier.size(32.dp).background(Color(0xFFFFAA00), CircleShape)
                                ) {
                                    Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = ticketStock.toString(),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    style = TextStyle(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
                                )
                                IconButton(
                                    onClick = { ticketStock++ },
                                    modifier = Modifier.size(32.dp).background(Color(0xFF0D59F2), CircleShape)
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Publish Button
            item {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 10.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D59F2))
                ) {
                    Text("Tambahkan Event", style = TextStyle(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                }
            }
        }
    }
}

@Composable
fun CreateEventTextField(
    label: String,
    placeholder: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 14.sp, fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray, fontSize = 14.sp) },
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0D59F2),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateEventScreenPreview() {
    CreateEventScreen(onBack = {})
}
