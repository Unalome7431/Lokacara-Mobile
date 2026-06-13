package com.app.lokacara.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val label: String,
    val contentDescription: String
)

@Composable
fun BottomNavbar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = remember {
        listOf(
            NavigationItem(Screen.Home.route, Icons.Outlined.Home, "Beranda", "Beranda"),
            NavigationItem(Screen.Explore.route, Icons.Outlined.Explore, "Jelajahi", "Jelajahi"),
            NavigationItem(Screen.CreateEvent.route, Icons.Default.Add, "Buat", "Buat Event"),
            NavigationItem(Screen.Tickets.route, Icons.Outlined.ConfirmationNumber, "Tiket", "Tiket"),
            NavigationItem(Screen.Profile.route, Icons.Outlined.Person, "Profil", "Profil")
        )
    }

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
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
    onClick: () -> Unit
) {
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.3f, stiffness = 600f),
        label = "navIconScale"
    )

    val pillShape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(26.dp)
                .widthIn(min = 36.dp)
                .clip(pillShape)
                .background(if (isSelected) Secondary500 else Color.Transparent)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.5f)) + fadeIn(tween(200)),
                exit = scaleOut(targetScale = 0.85f, animationSpec = spring(dampingRatio = 0.5f)) + fadeOut(tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(pillShape)
                        .background(Secondary500)
                )
            }

            Icon(
                imageVector = item.icon,
                contentDescription = item.contentDescription,
                tint = if (isSelected) Color.White else Gray500,
                modifier = Modifier.size(22.dp).scale(iconScale)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        androidx.compose.animation.AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(tween(200)) + slideInVertically(
                animationSpec = tween(200),
                initialOffsetY = { it }
            ),
            exit = fadeOut(tween(150)) + slideOutVertically(
                animationSpec = tween(150),
                targetOffsetY = { it }
            )
        ) {
            Text(
                text = item.label,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Secondary600
                )
            )
        }

        if (!isSelected) {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun CenterActionButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "fabScale"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(56.dp)
            .scale(scale)
            .shadow(elevation = 8.dp, shape = CircleShape)
            .border(3.dp, Color.White, CircleShape)
            .clip(CircleShape)
            .background(Secondary500)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Buat Event",
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
