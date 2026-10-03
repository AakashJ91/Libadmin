package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import com.example.data.model.Student
import com.example.data.model.SubscriptionStatus
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusActiveContainer
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiredContainer
import com.example.ui.theme.StatusExpiringSoon
import com.example.ui.theme.StatusExpiringSoonContainer

@Composable
fun StatusBadge(
    status: SubscriptionStatus,
    daysRemaining: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        SubscriptionStatus.ACTIVE -> Triple(
            StatusActiveContainer.copy(alpha = 0.8f),
            StatusActive,
            "$daysRemaining days left"
        )
        SubscriptionStatus.EXPIRING_SOON -> Triple(
            StatusExpiringSoonContainer.copy(alpha = 0.9f),
            StatusExpiringSoon,
            if (daysRemaining == 0) "Expires Today!" else "$daysRemaining days left"
        )
        SubscriptionStatus.EXPIRED -> Triple(
            StatusExpiredContainer.copy(alpha = 0.85f),
            StatusExpired,
            if (daysRemaining == 0) "Expired Today" else "Expired ${-daysRemaining}d ago"
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StudentAvatar(
    name: String,
    avatarKey: String = "avatar_1",
    photoUri: String? = null,
    modifier: Modifier = Modifier,
    sizeDp: Int = 46,
    shape: Shape = CircleShape
) {
    val isFileValid = !photoUri.isNullOrBlank() && (photoUri.startsWith("content:") || File(photoUri).exists())
    if (isFileValid) {
        Box(
            modifier = modifier
                .size(sizeDp.dp)
                .clip(shape)
                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = if (photoUri!!.startsWith("/")) File(photoUri) else photoUri,
                contentDescription = "$name's photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        val colors = listOf(
            Pair(Color(0xFF0284C7), Color(0xFF0369A1)),
            Pair(Color(0xFF0D9488), Color(0xFF0F766E)),
            Pair(Color(0xFF7C3AED), Color(0xFF6D28D9)),
            Pair(Color(0xFFD97706), Color(0xFFB45309)),
            Pair(Color(0xFFDB2777), Color(0xFFBE185D)),
            Pair(Color(0xFF4F46E5), Color(0xFF4338CA))
        )
        val colorIndex = kotlin.math.abs(name.hashCode()) % colors.size
        val (c1, c2) = colors[colorIndex]

        val initials = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "ST" }

        Box(
            modifier = modifier
                .size(sizeDp.dp)
                .clip(shape)
                .background(Brush.linearGradient(listOf(c1, c2)))
                .border(1.5.dp, MaterialTheme.colorScheme.surface, shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (sizeDp * 0.38).sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentCard(
    student: Student,
    onClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onSmsClick: () -> Unit,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val now = System.currentTimeMillis()
    val status = student.getStatus(now)
    val daysRemaining = student.daysRemaining(now)

    val totalDuration = maxOf(1L, student.endDateMillis - student.startDateMillis)
    val elapsed = now - student.startDateMillis
    val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onToggleSelect() else onClick()
                },
                onLongClick = onLongClick
            )
            .testTag("student_card_${student.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Avatar/Photo, Name + ID, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("checkbox_student_${student.id}")
                    )
                }
                StudentAvatar(
                    name = student.name,
                    avatarKey = student.avatarKey,
                    photoUri = student.photoUri,
                    sizeDp = 48
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "ID: ${student.idProofNumber} • ${student.planType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                StatusBadge(status = status, daysRemaining = daysRemaining)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Shift and Timings Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = student.shift,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlighted Dual Cards: CURRENT FEE STATUS & EXPIRY DATE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Current Fee Status Card
                Surface(
                    color = if (student.isPendingFee) StatusExpiredContainer.copy(alpha = 0.4f) else StatusActiveContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (student.isPendingFee) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (student.isPendingFee) StatusExpired else StatusActive,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "FEE STATUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (student.isPendingFee) StatusExpired else StatusActive
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        if (student.isPendingFee) {
                            Text(
                                text = "Due: ₹${String.format("%.0f", student.pendingFeeAmount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusExpired
                            )
                            Text(
                                text = "Paid ₹${student.feePaid.toInt()} of ₹${student.feeAmount.toInt()}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Paid ₹${student.feePaid.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusActive
                            )
                            Text(
                                text = "Full payment completed",
                                fontSize = 10.sp,
                                color = StatusActive.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // 2. Expiry Date Card
                val expiryBg = when (status) {
                    SubscriptionStatus.ACTIVE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    SubscriptionStatus.EXPIRING_SOON -> StatusExpiringSoonContainer.copy(alpha = 0.4f)
                    SubscriptionStatus.EXPIRED -> StatusExpiredContainer.copy(alpha = 0.4f)
                }
                Surface(
                    color = expiryBg,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = when (status) {
                                    SubscriptionStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                                    SubscriptionStatus.EXPIRING_SOON -> StatusExpiringSoon
                                    SubscriptionStatus.EXPIRED -> StatusExpired
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EXPIRY DATE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (status) {
                                    SubscriptionStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                                    SubscriptionStatus.EXPIRING_SOON -> StatusExpiringSoon
                                    SubscriptionStatus.EXPIRED -> StatusExpired
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = student.formattedEndDate(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                daysRemaining < 0 -> "Expired ${-daysRemaining}d ago"
                                daysRemaining == 0 -> "Expires today!"
                                else -> "$daysRemaining days left"
                            },
                            fontSize = 10.sp,
                            color = when (status) {
                                SubscriptionStatus.ACTIVE -> MaterialTheme.colorScheme.onSurfaceVariant
                                SubscriptionStatus.EXPIRING_SOON -> StatusExpiringSoon
                                SubscriptionStatus.EXPIRED -> StatusExpired
                            },
                            fontWeight = if (status != SubscriptionStatus.ACTIVE) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Duration Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = when (status) {
                        SubscriptionStatus.ACTIVE -> StatusActive
                        SubscriptionStatus.EXPIRING_SOON -> StatusExpiringSoon
                        SubscriptionStatus.EXPIRED -> StatusExpired
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(progress * 100).toInt()}% elapsed",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: WhatsApp Reminder, SMS, Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Contact:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 6.dp)
                )

                // WhatsApp
                FilledTonalIconButton(
                    onClick = onWhatsAppClick,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("student_whatsapp_${student.id}"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFF25D366).copy(alpha = 0.15f),
                        contentColor = Color(0xFF15803D)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send WhatsApp Reminder",
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // SMS
                FilledTonalIconButton(
                    onClick = onSmsClick,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("student_sms_${student.id}"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Send SMS Reminder",
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Call
                FilledTonalIconButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("student_call_${student.id}"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Student",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentGridCard(
    student: Student,
    onClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onSmsClick: () -> Unit,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val now = System.currentTimeMillis()
    val status = student.getStatus(now)
    val daysRemaining = student.daysRemaining(now)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onToggleSelect() else onClick()
                },
                onLongClick = onLongClick
            )
            .testTag("student_grid_card_${student.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.5.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column {
            // Photo Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                if (!student.photoUri.isNullOrBlank() && File(student.photoUri).exists()) {
                    AsyncImage(
                        model = File(student.photoUri),
                        contentDescription = "${student.name}'s photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    StudentAvatar(
                        name = student.name,
                        avatarKey = student.avatarKey,
                        photoUri = null,
                        sizeDp = 64
                    )
                }

                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelect() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    StatusBadge(status = status, daysRemaining = daysRemaining)
                }
            }

            // Details Body
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "ID: ${student.idProofNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = student.shift,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Fee Status Pill
                Surface(
                    color = if (student.isPendingFee) StatusExpiredContainer.copy(alpha = 0.5f) else StatusActiveContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (student.isPendingFee) "Due: ₹${student.pendingFeeAmount.toInt()}" else "Fee: Paid",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (student.isPendingFee) StatusExpired else StatusActive,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = onWhatsAppClick,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color(0xFF25D366).copy(alpha = 0.15f),
                            contentColor = Color(0xFF15803D)
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "WhatsApp", modifier = Modifier.size(14.dp))
                    }

                    FilledTonalIconButton(
                        onClick = onSmsClick,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Message, contentDescription = "SMS", modifier = Modifier.size(14.dp))
                    }

                    FilledTonalIconButton(
                        onClick = onCallClick,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
