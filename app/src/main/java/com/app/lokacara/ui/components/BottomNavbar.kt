package com.app.lokacara.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
            NavigationItem("tickets", Icons.Outlined.ConfirmationNumber, "Tickets"),
            NavigationItem(Screen.Profile.route, Icons.Outlined.Person, "Profile")
        )
    }

    // Optimization: Memoize the navigation handler to ensure stability across recompositions
    val onNavigate: (String) -> Unit = remember(navController) {
        { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Optimization: Unified background color (Always White)
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        HorizontalDivider(thickness = 0.5.dp, color = Gray200)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = currentRoute == item.route
                val isCenter = index == 2

                if (isCenter) {
                    CenterActionButton(onClick = {
                        if (currentRoute != item.route) {
                            onNavigate(item.route)
                        }
                    })
                } else {
                    NavItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                onNavigate(item.route)
                            }
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
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Modification: Using Yellow (Secondary500) for highlight instead of Blue
    val highlightColor = Secondary500.copy(alpha = 0.12f)
    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) highlightColor else Color.Transparent,
        animationSpec = tween(300),
        label = "bgTint"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .drawBehind {
                drawRect(animatedBgColor)
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Top Indicator Line - Using Yellow (Secondary500)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.5f)
                    .height(3.dp)
                    .background(Secondary500, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
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
            // Modification: Using Yellow (Secondary500) for center button
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
