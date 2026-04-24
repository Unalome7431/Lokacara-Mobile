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
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.*

@Composable
fun BottomNavbar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                NavIconItem(Icons.Default.Home, currentRoute == Screen.Home.route) {
                    if (currentRoute != Screen.Home.route) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                }
                NavIconItem(Icons.Outlined.Explore, currentRoute == Screen.Explore.route) {
                    if (currentRoute != Screen.Explore.route) {
                        navController.navigate(Screen.Explore.route)
                    }
                }
                Spacer(modifier = Modifier.width(64.dp))
                NavIconItem(Icons.Outlined.ConfirmationNumber, false) { /* Action Tickets */ }
                NavIconItem(Icons.Outlined.Person, currentRoute == Screen.Profile.route) {
                    if (currentRoute != Screen.Profile.route) {
                        navController.navigate(Screen.Profile.route)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .offset(y = (12).dp)
                .align(Alignment.TopCenter)
                .shadow(elevation = 12.dp, shape = CircleShape)
                .background(Primary500, CircleShape)
                .clickable {
                    navController.navigate(Screen.CreateEvent.route)
                },
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
                    tint = Primary500,
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
            BottomNavbar(navController = rememberNavController())
        }
    }
}
