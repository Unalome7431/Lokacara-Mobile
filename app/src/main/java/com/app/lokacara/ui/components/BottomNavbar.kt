package com.app.lokacara.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.navigation.matchesRoute
import com.app.lokacara.ui.navigation.navigateToCreateEvent
import com.app.lokacara.ui.navigation.navigateToMainTab
import com.app.lokacara.ui.theme.*

@Immutable
data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String,
    val activeColor: Color
)

@Composable
fun BottomNavbar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = remember {
        listOf(
            NavigationItem(Screen.Home.route, Icons.Outlined.Home, "Beranda", "Beranda", Primary500),
            NavigationItem(Screen.Explore.route, Icons.Outlined.Explore, "Jelajahi", "Jelajahi", SvgOrange),
            NavigationItem(Screen.CreateEvent.route, Icons.Default.Add, "Buat", "Buat Event", Secondary500),
            NavigationItem(Screen.Tickets.route, Icons.Outlined.ConfirmationNumber, "Tiket", "Tiket", SemanticInfoBase),
            NavigationItem(Screen.Profile.route, Icons.Outlined.Person, "Profil", "Profil", Gray800)
        )
    }

    val onNavigate: (String) -> Unit = remember(navController) {
        { route ->
            if (route == Screen.CreateEvent.route) {
                navController.navigateToCreateEvent()
            } else {
                navController.navigateToMainTab(route)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 18.dp, top = 0.dp, end = 18.dp, bottom = 10.dp)
    ) {
        val barShape = RoundedCornerShape(26.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 4.dp, shape = barShape, clip = false)
                .clip(barShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.98f),
                            Primary100.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.72f), shape = barShape)
                .padding(horizontal = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = currentRoute.matchesRoute(item.route)
                val isCenter = index == 2

                if (isCenter) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .offset(y = (-2).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CenterActionButton(onClick = {
                            if (!isSelected) onNavigate(item.route)
                        })
                    }
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 700f),
        label = "navIconScale"
    )
    val pillColor by animateColorAsState(
        targetValue = if (isSelected) item.activeColor.copy(alpha = 0.14f) else Color.Transparent,
        label = "navPillColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) item.activeColor else Gray500,
        label = "navIconTint"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) Gray900 else Gray500,
        label = "navLabelColor"
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (isSelected) item.activeColor else Color.Transparent,
        label = "navIndicatorColor"
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(top = 7.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(if (isSelected) 46.dp else 42.dp)
                .height(29.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(pillColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.contentDescription,
                tint = iconTint,
                modifier = Modifier
                    .size(23.dp)
                    .scale(iconScale)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.label,
            style = TextStyle(
                fontFamily = PlusJakartaSansFont,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = labelColor
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(16.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(indicatorColor)
        )
    }
}

@Composable
private fun CenterActionButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 700f),
        label = "fabScale"
    )

    Box(
        modifier = Modifier
            .size(50.dp)
            .scale(scale)
            .shadow(elevation = 5.dp, shape = CircleShape, clip = false)
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
