package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNav
import com.example.ui.components.ThemeSelectionSheet
import com.example.ui.screens.AddEditStudentScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DirectoryScreen
import com.example.ui.screens.ReminderHubScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FilterType
import com.example.ui.viewmodel.LibraryViewModel
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel for system alerts
        NotificationHelper.createNotificationChannel(this)

        setContent {
            val libraryViewModel: LibraryViewModel = viewModel()

            // Handle notification intent extras
            LaunchedEffect(intent) {
                if (intent?.getStringExtra("navigate_to") == "directory_expiring") {
                    libraryViewModel.selectedFilter.value = FilterType.EXPIRING_3_DAYS
                    libraryViewModel.navigateTo(AppScreen.Directory)
                }
            }

            // Request Notification Permission for Android 13+ (API 33+)
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* granted / denied handled */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            // Theme & Accent handling
            val themeMode by libraryViewModel.themeMode.collectAsStateWithLifecycle()
            val accentColor by libraryViewModel.accentColor.collectAsStateWithLifecycle()
            val showThemeSheet by libraryViewModel.showThemeSheet.collectAsStateWithLifecycle()

            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MyApplicationTheme(themeMode = themeMode, accent = accentColor) {
                MainAppContent(
                    viewModel = libraryViewModel,
                    isDarkTheme = isDarkTheme,
                    onOpenThemeSettings = { libraryViewModel.openThemeSheet() }
                )

                if (showThemeSheet) {
                    ThemeSelectionSheet(
                        currentMode = themeMode,
                        currentAccent = accentColor,
                        onModeSelect = { libraryViewModel.setThemeMode(it) },
                        onAccentSelect = { libraryViewModel.setAccentColor(it) },
                        onDismiss = { libraryViewModel.closeThemeSheet() }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: LibraryViewModel,
    isDarkTheme: Boolean,
    onOpenThemeSettings: () -> Unit = {}
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val filteredStudents by viewModel.filteredStudents.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val reminderLogs by viewModel.reminderLogs.collectAsStateWithLifecycle()

    // Handle back press
    BackHandler(enabled = currentScreen != AppScreen.Dashboard) {
        viewModel.navigateBack()
    }

    val isTopLevel = currentScreen is AppScreen.Dashboard ||
            currentScreen is AppScreen.Directory ||
            currentScreen is AppScreen.ReminderHub

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isTopLevel) {
                AppBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) },
                    expiringAlertCount = metrics.expiringSoonCount + metrics.expiredCount
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isTopLevel) innerPadding.calculateBottomPadding() else 0.dp),
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is AppScreen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        metrics = metrics,
                        students = allStudents,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = onOpenThemeSettings,
                        onOpenThemeSettings = onOpenThemeSettings,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
                is AppScreen.Directory -> {
                    DirectoryScreen(
                        viewModel = viewModel,
                        students = filteredStudents,
                        searchQuery = searchQuery,
                        selectedFilter = selectedFilter,
                        sortOrder = sortOrder,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
                is AppScreen.ReminderHub -> {
                    ReminderHubScreen(
                        viewModel = viewModel,
                        students = allStudents,
                        reminderLogs = reminderLogs,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
                is AppScreen.StudentDetail -> {
                    StudentDetailScreen(
                        studentId = screen.studentId,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() },
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
                is AppScreen.AddEditStudent -> {
                    AddEditStudentScreen(
                        studentId = screen.studentId,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is AppScreen.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
            }
        }
    }
}
