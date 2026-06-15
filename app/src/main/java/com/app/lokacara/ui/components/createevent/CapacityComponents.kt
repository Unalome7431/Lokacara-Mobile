package com.app.lokacara.ui.components.createevent

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.ui.theme.*

@Composable
fun CapacityControl(value: Int, onValueChange: (Int) -> Unit) {
    var textValue by remember { mutableStateOf(value.toString()) }

    LaunchedEffect(value) {
        val normalized = value.toString()
        if (textValue != normalized) {
            textValue = normalized
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CapacityIconButton(
                icon = Icons.Default.Remove,
                enabled = value > 1,
                onClick = { onValueChange((value - 1).coerceAtLeast(1)) }
            )
            TextField(
                value = textValue,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(6)
                    textValue = digits
                    digits.toIntOrNull()?.let { parsed ->
                        onValueChange(parsed.coerceIn(1, 100_000))
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CreateEventNeutralSurface,
                    unfocusedContainerColor = CreateEventNeutralSurface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Gray900,
                    unfocusedTextColor = Gray900
                ),
                shape = RoundedCornerShape(12.dp)
            )
            CapacityIconButton(
                icon = Icons.Default.Add,
                enabled = value < 100_000,
                onClick = { onValueChange((value + 1).coerceAtMost(100_000)) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(25, 50, 100, 250).forEach { preset ->
                val selected = value == preset
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onValueChange(preset) },
                    shape = RoundedCornerShape(50),
                    color = if (selected) SvgPrimaryBlue else Color.White,
                    border = BorderStroke(1.dp, if (selected) SvgPrimaryBlue else Color.White.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = preset.toString(),
                        modifier = Modifier.padding(vertical = 9.dp),
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (selected) Color.White else Gray800,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CapacityIconButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (enabled) SvgOrange else Gray500.copy(alpha = 0.25f),
        label = "capacityButtonBackground"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
