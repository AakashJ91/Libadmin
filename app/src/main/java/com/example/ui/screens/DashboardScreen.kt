package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReminderChannel
import com.example.data.model.Student
import com.example.data.model.SubscriptionStatus
import com.example.ui.components.StudentCard
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusActiveContainer
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiredContainer
import com.example.ui.theme.StatusExpiringSoon
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FilterType
import com.example.ui.viewmodel.LibraryMetrics
import com.example.ui.viewmodel.LibraryViewModel
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: LibraryViewModel,
    metrics: LibraryMetrics,
    students: List<Student>,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onOpenThemeSettings: () -> Unit = onToggleDarkTheme,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()

    // Find urgent students (expiring in <= 3 days or already expired)
    val urgentStudents = students.filter {
        val st = it.getStatus(now)
        st == SubscriptionStatus.EXPIRING_SOON || st == SubscriptionStatus.EXPIRED
    }.sortedBy { it.endDateMillis }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LibAdmin",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reading Library Administration",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Automated Push Expiry Scan button
                    IconButton(
                        onClick = {
                            val (expiring, expired) = viewModel.runAutomatedExpiryScan(context)
                            Toast.makeText(
                                context,
                                "Automated scan complete: Alerted $expiring expiring, $expired expired on mobile.",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier.testTag("btn_dashboard_scan")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Run Automated Expiry Scan & Push Alert",
                            tint = if (metrics.expiringSoonCount > 0) StatusExpiringSoon else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Themes & Accent Colors Customizer button
                    IconButton(
                        onClick = onOpenThemeSettings,
                        modifier = Modifier.testTag("btn_dashboard_theme_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme & Accent Colors",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Settings & Administration button
                    IconButton(
                        onClick = { onNavigate(AppScreen.Settings) },
                        modifier = Modifier.testTag("btn_dashboard_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & Administration",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. Hero Banner
            item {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val greeting = when (hour) {
                    in 4..11 -> "Good Morning"
                    in 12..16 -> "Good Afternoon"
                    else -> "Good Evening"
                }
                val todayDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US).format(Date())

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.LocalLibrary,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "StudySphere Reading Hall",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$greeting, Admin",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = todayDate,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Black.copy(alpha = 0.25f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF4ADE80))
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "${students.size} Enrolled",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1. Automated Expiry Push Alert Card
            if (metrics.expiringSoonCount > 0 || metrics.expiredCount > 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setFilter(FilterType.EXPIRING_3_DAYS)
                                onNavigate(AppScreen.Directory)
                            }
                            .testTag("banner_expiry_alert")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(StatusExpiringSoon.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusExpiringSoon,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automated Expiry Attention",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${metrics.expiringSoonCount} expiring soon, ${metrics.expiredCount} expired. Tap to view & send WhatsApp / SMS / Email.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FilledTonalButton(
                                onClick = {
                                    val (expiring, expired) = viewModel.runAutomatedExpiryScan(context)
                                    Toast.makeText(
                                        context,
                                        "Pushed admin notification to mobile: $expiring expiring, $expired expired.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.testTag("btn_alert_push_now")
                            ) {
                                Text("Notify", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 2. Metrics Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Total Enrolled",
                            value = "${metrics.totalStudents}",
                            icon = Icons.Default.People,
                            iconTint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setFilter(FilterType.ALL)
                            onNavigate(AppScreen.Directory)
                        }
                        MetricCard(
                            title = "Active Access",
                            value = "${metrics.activeCount}",
                            icon = Icons.Default.CheckCircle,
                            iconTint = StatusActive,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setFilter(FilterType.ACTIVE)
                            onNavigate(AppScreen.Directory)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Expiring (≤3D)",
                            value = "${metrics.expiringSoonCount}",
                            icon = Icons.Default.Warning,
                            iconTint = StatusExpiringSoon,
                            highlight = metrics.expiringSoonCount > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setFilter(FilterType.EXPIRING_3_DAYS)
                            onNavigate(AppScreen.Directory)
                        }
                        MetricCard(
                            title = "Expired Access",
                            value = "${metrics.expiredCount}",
                            icon = Icons.Default.Error,
                            iconTint = StatusExpired,
                            highlight = metrics.expiredCount > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setFilter(FilterType.EXPIRED)
                            onNavigate(AppScreen.Directory)
                        }
                    }
                }
            }

            // 3. Fee & Collections Overview Card
            item {
                val totalExpected = metrics.totalRevenue + metrics.totalPendingDues
                val collectionEfficiency = if (totalExpected > 0) (metrics.totalRevenue / totalExpected).toFloat() else 1f

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Subscription Dues & Financials",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StatusActiveContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${(collectionEfficiency * 100).toInt()}% Collected",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusActive,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "FEES COLLECTED",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "₹${String.format(Locale.US, "%.0f", metrics.totalRevenue)}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                color = if (metrics.totalPendingDues > 0) StatusExpiredContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (metrics.totalPendingDues > 0) StatusExpired.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "PENDING DUES",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (metrics.totalPendingDues > 0) StatusExpired else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "₹${String.format(Locale.US, "%.0f", metrics.totalPendingDues)}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (metrics.totalPendingDues > 0) StatusExpired else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Collection rate bar
                        LinearProgressIndicator(
                            progress = { collectionEfficiency },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = StatusActive,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // 4. Quick Action Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onNavigate(AppScreen.AddEditStudent()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_dashboard_enroll"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enroll New Student")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val file = viewModel.exportExcelReport(context)
                                if (file != null) {
                                    Toast.makeText(context, "Exported Excel Sheet: ${file.name}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_dashboard_export_excel"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Excel")
                        }

                        OutlinedButton(
                            onClick = {
                                val file = viewModel.exportPdfReport(context)
                                if (file != null) {
                                    Toast.makeText(context, "Exported PDF Report: ${file.name}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_dashboard_export_pdf"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Theme & Appearance quick setting card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenThemeSettings() }
                            .testTag("card_dashboard_theme_settings"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Theme & Accent Colors",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Light / Dark mode & custom accent palettes",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 5. Urgent Renewals List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Urgent Renewals Needed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (urgentStudents.isNotEmpty()) {
                        Text(
                            text = "View All (${urgentStudents.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                viewModel.setFilter(FilterType.EXPIRING_3_DAYS)
                                onNavigate(AppScreen.Directory)
                            }
                        )
                    }
                }
            }

            if (urgentStudents.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusActive,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All student library access durations are active!",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(urgentStudents.take(4), key = { it.id }) { student ->
                    StudentCard(
                        student = student,
                        onClick = { onNavigate(AppScreen.StudentDetail(student.id)) },
                        onWhatsAppClick = {
                            viewModel.dispatchReminder(context, student, ReminderChannel.WHATSAPP)
                        },
                        onSmsClick = {
                            viewModel.dispatchReminder(context, student, ReminderChannel.SMS)
                        },
                        onCallClick = {
                            NotificationHelper.openDialer(context, student.phone)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (highlight) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = iconTint.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Due",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = iconTint,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = if (highlight) iconTint else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
