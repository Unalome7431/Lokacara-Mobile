package com.app.lokacara.ui.components.createevent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.ui.theme.*

@Composable
fun CategoryDropdownField(
    selectedCategoryName: String,
    categories: List<CategoryDto>,
    onCategorySelected: (CategoryDto) -> Unit,
    label: String,
    containerColor: Color
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gray800)
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { showDialog = true },
            shape = RoundedCornerShape(16.dp),
            color = containerColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCategoryName.ifEmpty { "Pilih kategori acara" },
                    color = if (selectedCategoryName.isEmpty()) Gray500 else Gray900,
                    fontSize = 14.sp, fontFamily = NunitoFont
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = SvgOrange, modifier = Modifier.size(24.dp))
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Pilih Kategori", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategoryName == cat.name
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onCategorySelected(cat); showDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Primary500.copy(alpha = 0.1f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat.name,
                                    fontFamily = NunitoFont, fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Primary500 else Gray900
                                )
                                if (isSelected) Icon(Icons.Default.Check, "Selected", tint = Primary500, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
