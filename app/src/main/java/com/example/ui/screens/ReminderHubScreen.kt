package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ReminderLogEntity
import com.example.data.model.ReminderChannel
import com.example.data.model.Student
import com.example.data.model.SubscriptionStatus
import com.example.ui.components.StatusBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiringSoon
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.LibraryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderHubScreen(
    viewModel: LibraryViewModel,
    students: List<Student>,
    reminderLogs: List<ReminderLogEntity>,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()

    val pendingStudents = students.filter {
        val st = it.getStatus(now)
        st == SubscriptionStatus.EXPIRING_SOON || st == SubscriptionStatus.EXPIRED
    }.sortedBy { it.endDateMillis }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Automated Reminder Hub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Push Alerts, WhatsApp, SMS & Email Notifications",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openThemeSheet() },
                        modifier = Modifier.testTag("btn_reminder_hub_theme")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme & Accent Settings",
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
            // Automated Push Scan Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mobile Push Notification Center",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Notify admin and students directly on this device for upcoming expiries.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val (expiring, expired) = viewModel.runAutomatedExpiryScan(context)
                                Toast.makeText(
                                    context,
                                    "Dispatched notification to mobile device: $expiring expiring, $expired expired!",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_hub_run_push_scan"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Auto Expiry Scan & Push Notification")
                        }
                    }
                }
            }

            // Students Requiring Action Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Expiring & Overdue Members (${pendingStudents.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            if (pendingStudents.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No members currently near expiry.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(pendingStudents, key = { it.id }) { student ->
                    val status = student.getStatus(now)
                    val daysRemaining = student.daysRemaining(now)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StudentAvatar(
                                    name = student.name,
                                    avatarKey = student.avatarKey,
                                    photoUri = student.photoUri,
                                    sizeDp = 42
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${student.shift} • ${student.planType}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = status, daysRemaining = daysRemaining)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Send Reminder:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // WhatsApp
                                    FilledTonalIconButton(
                                        onClick = {
                                            viewModel.dispatchReminder(context, student, ReminderChannel.WHATSAPP)
                                        },
                                        modifier = Modifier.size(36.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = Color(0xFF25D366).copy(alpha = 0.15f),
                                            contentColor = Color(0xFF15803D)
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                                    }

                                    // SMS
                                    FilledTonalIconButton(
                                        onClick = {
                                            viewModel.dispatchReminder(context, student, ReminderChannel.SMS)
                                        },
                                        modifier = Modifier.size(36.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            contentColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Message, contentDescription = "SMS", modifier = Modifier.size(16.dp))
                                    }

                                    // Email
                                    FilledTonalIconButton(
                                        onClick = {
                                            viewModel.dispatchReminder(context, student, ReminderChannel.EMAIL)
                                        },
                                        modifier = Modifier.size(36.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                            contentColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(16.dp))
                                    }

                                    // Direct Push to device
                                    FilledTonalIconButton(
                                        onClick = {
                                            viewModel.dispatchReminder(context, student, ReminderChannel.SYSTEM_NOTIFICATION)
                                            Toast.makeText(context, "Dispatched notification for ${student.name}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = StatusExpiringSoon.copy(alpha = 0.15f),
                                            contentColor = StatusExpiringSoon
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Push", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Audit Trail Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dispatched Reminders Audit Trail",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            if (reminderLogs.isEmpty()) {
                item {
                    Text(
                        text = "No reminder history recorded yet.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(reminderLogs, key = { it.id }) { log ->
                    val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (log.channel) {
                                    "WHATSAPP" -> Color(0xFFDCFCE7)
                                    "SMS" -> Color(0xFFE0F2FE)
                                    "EMAIL" -> Color(0xFFFEF3C7)
                                    "PUSH_ALERT" -> Color(0xFFFFEDD5)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = log.channel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (log.channel) {
                                        "WHATSAPP" -> Color(0xFF15803D)
                                        "SMS" -> Color(0xFF0369A1)
                                        "EMAIL" -> Color(0xFFB45309)
                                        "PUSH_ALERT" -> Color(0xFFC2410C)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.studentName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = log.message,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dateFormatted,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
