package com.app.lokacara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.lokacara.ui.theme.* // Import warna dari theme

@Composable
fun BottomNavbar() {
    var selectedItem by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .width(338.dp)
            .height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(71.dp),
            shape = RoundedCornerShape(35.5.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavIconItem(Icons.Default.Home, selectedItem == 0) { selectedItem = 0 }
                NavIconItem(Icons.Outlined.Explore, selectedItem == 1) { selectedItem = 1 }
                Spacer(modifier = Modifier.width(64.dp))
                NavIconItem(Icons.Outlined.ConfirmationNumber, selectedItem == 2) { selectedItem = 2 }
                NavIconItem(Icons.Outlined.Person, selectedItem == 3) { selectedItem = 3 }
            }
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .offset(y = (12).dp)
                .align(Alignment.TopCenter)
                .shadow(elevation = 12.dp, shape = CircleShape)
                .background(Primary500, CircleShape) // Pakai Primary500
                .clickable { /* Action (e.g., Create Event) */ },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Primary500, // Pakai Primary500
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun NavIconItem(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) Primary500 else Gray400,
        modifier = Modifier
            .size(26.dp)
            .clickable { onClick() }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F4F6) // 0xFFF3F4F6 adalah Gray100
@Composable
fun BottomNavbarPreview() {
    LokacaraMobileTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            BottomNavbar()
        }
    }
}