package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: Long,
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()

    val student = viewModel.allStudents.value.firstOrNull { it.id == studentId }

    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Student not found")
        }
        return
    }

    var showRenewDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val status = student.getStatus(now)
    val daysRemaining = student.daysRemaining(now)
    val totalDuration = maxOf(1L, student.endDateMillis - student.startDateMillis)
    val elapsed = now - student.startDateMillis
    val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_detail_back")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate(AppScreen.AddEditStudent(studentId = student.id)) },
                        modifier = Modifier.testTag("btn_detail_edit")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.testTag("btn_detail_delete")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Student",
                            tint = MaterialTheme.colorScheme.error
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
            // Profile Card Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        StudentAvatar(
                            name = student.name,
                            avatarKey = student.avatarKey,
                            photoUri = student.photoUri,
                            sizeDp = 84
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Student ID: ${student.idProofNumber}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusBadge(status = status, daysRemaining = daysRemaining)
                    }
                }
            }

            // Quick Actions Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Automated Expiry & Direct Reminders",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // WhatsApp
                            ActionButtonItem(
                                icon = Icons.Default.Send,
                                label = "WhatsApp",
                                tint = Color(0xFF25D366),
                                testTag = "btn_detail_whatsapp"
                            ) {
                                viewModel.dispatchReminder(context, student, ReminderChannel.WHATSAPP)
                            }
                            // SMS
                            ActionButtonItem(
                                icon = Icons.Default.Message,
                                label = "SMS",
                                tint = MaterialTheme.colorScheme.primary,
                                testTag = "btn_detail_sms"
                            ) {
                                viewModel.dispatchReminder(context, student, ReminderChannel.SMS)
                            }
                            // Email
                            ActionButtonItem(
                                icon = Icons.Default.Email,
                                label = "Email",
                                tint = MaterialTheme.colorScheme.secondary,
                                testTag = "btn_detail_email"
                            ) {
                                viewModel.dispatchReminder(context, student, ReminderChannel.EMAIL)
                            }
                            // Push to mobile
                            ActionButtonItem(
                                icon = Icons.Default.Notifications,
                                label = "Push Alert",
                                tint = StatusExpiringSoon,
                                testTag = "btn_detail_push"
                            ) {
                                viewModel.dispatchReminder(context, student, ReminderChannel.SYSTEM_NOTIFICATION)
                                Toast.makeText(context, "Dispatched Push Notification for ${student.name}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            // Subscription & Access Duration Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Library Access & Duration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = { showRenewDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_detail_renew")
                            ) {
                                Icon(imageVector = Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Renew / Extend", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        DetailRow(icon = Icons.Default.AccessTime, label = "Study Shift", value = student.shift)
                        DetailRow(icon = Icons.Default.Badge, label = "Duration Plan", value = student.planType)
                        DetailRow(icon = Icons.Default.Notifications, label = "Valid Period", value = "${student.formattedStartDate()} → ${student.formattedEndDate()}")

                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = when (status) {
                                SubscriptionStatus.ACTIVE -> StatusActive
                                SubscriptionStatus.EXPIRING_SOON -> StatusExpiringSoon
                                SubscriptionStatus.EXPIRED -> StatusExpired
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Fee Paid: ₹${String.format("%.2f", student.feePaid)} / ₹${String.format("%.2f", student.feeAmount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (student.isPendingFee) {
                                Text(
                                    text = "Due: ₹${String.format("%.2f", student.pendingFeeAmount)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusExpired
                                )
                            } else {
                                Text(
                                    text = "Fully Paid",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusActive
                                )
                            }
                        }
                    }
                }
            }

            // Contact & Personal Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Contact & Personal Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        DetailRow(
                            icon = Icons.Default.Phone,
                            label = "Phone Number",
                            value = student.phone,
                            actionLabel = "Call",
                            onAction = { NotificationHelper.openDialer(context, student.phone) }
                        )
                        DetailRow(
                            icon = Icons.Default.Email,
                            label = "Email Address",
                            value = student.email.ifBlank { "Not provided" }
                        )
                        DetailRow(
                            icon = Icons.Default.Home,
                            label = "Residential Address",
                            value = student.address.ifBlank { "Not provided" }
                        )
                        DetailRow(
                            icon = Icons.Default.Phone,
                            label = "Emergency Contact",
                            value = student.emergencyContact.ifBlank { "Not provided" }
                        )
                        if (student.notes.isNotBlank()) {
                            DetailRow(
                                icon = Icons.Default.Edit,
                                label = "Admin Notes",
                                value = student.notes
                            )
                        }
                    }
                }
            }
        }

        // Renew Dialog
        if (showRenewDialog) {
            var selectedDurationDays by remember { mutableStateOf(30) }
            var planLabel by remember { mutableStateOf("1 Month") }
            var feeToAdd by remember { mutableStateOf("1000") }

            AlertDialog(
                onDismissRequest = { showRenewDialog = false },
                title = { Text("Renew Library Access") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Select duration to extend for ${student.name}:", fontSize = 13.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RenewOptionChip("1 Week", 7, 400.0, selectedDurationDays) { days, label, fee ->
                                selectedDurationDays = days
                                planLabel = label
                                feeToAdd = fee.toInt().toString()
                            }
                            RenewOptionChip("1 Month", 30, 1000.0, selectedDurationDays) { days, label, fee ->
                                selectedDurationDays = days
                                planLabel = label
                                feeToAdd = fee.toInt().toString()
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RenewOptionChip("3 Months", 90, 2700.0, selectedDurationDays) { days, label, fee ->
                                selectedDurationDays = days
                                planLabel = label
                                feeToAdd = fee.toInt().toString()
                            }
                            RenewOptionChip("6 Months", 180, 5000.0, selectedDurationDays) { days, label, fee ->
                                selectedDurationDays = days
                                planLabel = label
                                feeToAdd = fee.toInt().toString()
                            }
                        }

                        OutlinedTextField(
                            value = feeToAdd,
                            onValueChange = { feeToAdd = it },
                            label = { Text("Renewal Fee Amount (₹)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_renew_fee"),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val feeVal = feeToAdd.toDoubleOrNull() ?: 0.0
                            viewModel.renewStudent(student, selectedDurationDays, planLabel, feeVal)
                            showRenewDialog = false
                            Toast.makeText(context, "Renewed for $planLabel successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("btn_confirm_renew")
                    ) {
                        Text("Confirm Renewal")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenewDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Student Record") },
                text = { Text("Are you sure you want to remove ${student.name} from the library database?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteStudent(student.id)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("btn_confirm_delete")
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ActionButtonItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag(testTag)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = tint.copy(alpha = 0.15f),
                contentColor = tint
            )
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RenewOptionChip(
    label: String,
    days: Int,
    defaultFee: Double,
    selectedDays: Int,
    onSelect: (Int, String, Double) -> Unit
) {
    val isSelected = days == selectedDays
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .clickable { onSelect(days, label, defaultFee) }
    ) {
        Text(
            text = "$label (₹${defaultFee.toInt()})",
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}
