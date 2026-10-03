package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.AppScreen

data class NavItem(
    val screen: AppScreen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun AppBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    expiringAlertCount: Int = 0
) {
    val items = listOf(
        NavItem(
            screen = AppScreen.Dashboard,
            label = "Dashboard",
            selectedIcon = Icons.Filled.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard,
            testTag = "nav_dashboard"
        ),
        NavItem(
            screen = AppScreen.Directory,
            label = "Students",
            selectedIcon = Icons.Filled.People,
            unselectedIcon = Icons.Outlined.People,
            testTag = "nav_students"
        ),
        NavItem(
            screen = AppScreen.ReminderHub,
            label = "Reminders",
            selectedIcon = Icons.Filled.NotificationsActive,
            unselectedIcon = Icons.Outlined.NotificationsActive,
            testTag = "nav_reminders"
        )
    )

    NavigationBar(
        tonalElevation = 6.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val isSelected = currentScreen::class == item.screen::class

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    if (item.screen is AppScreen.ReminderHub && expiringAlertCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text("$expiringAlertCount")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    }
                },
                label = { Text(item.label) },
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
