package com.app.lokacara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.*

@Immutable
data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val contentDescription: String
)

@Composable
fun BottomNavbar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = remember {
        listOf(
            NavigationItem(Screen.Home.route, Icons.Outlined.Home, "Home"),
            NavigationItem(Screen.Explore.route, Icons.Outlined.Explore, "Explore"),
            NavigationItem(Screen.CreateEvent.route, Icons.Default.Add, "Create"),
            NavigationItem(Screen.Tickets.route, Icons.Outlined.ConfirmationNumber, "Tickets"),
            NavigationItem(Screen.Profile.route, Icons.Outlined.Person, "Profile")
        )
    }

    val onNavigate: (String) -> Unit = remember(navController) {
        { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
            }
        }
    }

    val indicatorShape = remember { RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp) }
    val highlightColor = remember { Secondary500.copy(alpha = 0.12f) }

    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        HorizontalDivider(thickness = 0.5.dp, color = Gray200)

        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = currentRoute == item.route
                val isCenter = index == 2

                if (isCenter) {
                    CenterActionButton(onClick = {
                        if (!isSelected) onNavigate(item.route)
                    })
                } else {
                    NavItem(
                        item = item,
                        isSelected = isSelected,
                        highlightColor = highlightColor,
                        indicatorShape = indicatorShape,
                        onClick = {
                            if (!isSelected) onNavigate(item.route)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(
    item: NavigationItem,
    isSelected: Boolean,
    highlightColor: Color,
    indicatorShape: RoundedCornerShape,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .background(if (isSelected) highlightColor else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.5f)
                    .height(3.dp)
                    .background(Secondary500, indicatorShape)
            )
        }

        Icon(
            imageVector = item.icon,
            contentDescription = item.contentDescription,
            tint = if (isSelected) Secondary600 else Gray500,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun CenterActionButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(48.dp)
            .clip(CircleShape)
            .background(Secondary500)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Create",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LokacaraNavbarPreview() {
    LokacaraMobileTheme(darkTheme = false) {
        BottomNavbar(navController = rememberNavController())
    }
}
