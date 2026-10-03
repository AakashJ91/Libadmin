package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Student
import com.example.ui.viewmodel.LibraryViewModel
import com.example.util.AppSettingsManager
import com.example.util.ImportResult
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()

    // 1. Notification message state
    var notificationTemplate by remember {
        mutableStateOf(AppSettingsManager.getNotificationTemplate(context))
    }

    // Sample student for live preview
    val sampleStudent = remember {
        Student(
            id = 1,
            name = "Aarav Sharma",
            phone = "+91 9876543210",
            email = "aarav.sharma@example.com",
            idProofNumber = "LIB-2024-1042",
            address = "Civil Lines, Reading Hub",
            shift = "Full Day (6 AM - 10 PM)",
            planType = "1 Month",
            startDateMillis = System.currentTimeMillis() - 27L * 86400000L,
            endDateMillis = System.currentTimeMillis() + 3L * 86400000L,
            feeAmount = 1000.0,
            feePaid = 800.0,
            avatarKey = "avatar_1"
        )
    }

    // 2. Default fees state
    val initialFees = remember { AppSettingsManager.getAllDefaultFees(context) }
    var fee1Week by remember { mutableStateOf(initialFees["1 Week"]?.toInt()?.toString() ?: "300") }
    var fee1Month by remember { mutableStateOf(initialFees["1 Month"]?.toInt()?.toString() ?: "1000") }
    var fee3Months by remember { mutableStateOf(initialFees["3 Months"]?.toInt()?.toString() ?: "2700") }
    var fee6Months by remember { mutableStateOf(initialFees["6 Months"]?.toInt()?.toString() ?: "5000") }

    // Import status dialog
    var importResultDialog by remember { mutableStateOf<ImportResult?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importUserData(context, uri) { result ->
                importResultDialog = result
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings & Administration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Preferences, Fees, Full Backup & Restore",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_settings_back")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.imePadding(),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // 1. Notification Message Configuration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Notification Message Template",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Used for automated WhatsApp & SMS renewal reminders",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedTextField(
                            value = notificationTemplate,
                            onValueChange = { notificationTemplate = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_notification_template"),
                            minLines = 3,
                            maxLines = 6,
                            label = { Text("Reminder Message Template") }
                        )

                        // Placeholder variable tokens
                        Text(
                            text = "Tap variable token to insert into template:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val tokens = listOf(
                            Pair("{name}", "Name"),
                            Pair("{plan}", "Plan"),
                            Pair("{date}", "Expiry"),
                            Pair("{days}", "Days Left"),
                            Pair("{dues}", "Dues"),
                            Pair("{shift}", "Shift")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tokens.forEach { (token, label) ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        notificationTemplate = if (notificationTemplate.endsWith(" ") || notificationTemplate.isEmpty()) {
                                            "$notificationTemplate$token "
                                        } else {
                                            "$notificationTemplate $token "
                                        }
                                    },
                                    label = { Text("$token ($label)", fontSize = 11.sp) }
                                )
                            }
                        }

                        // Live message preview
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Live Preview (Sample Member):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppSettingsManager.formatMessage(notificationTemplate, sampleStudent),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    AppSettingsManager.saveNotificationTemplate(context, notificationTemplate)
                                    Toast.makeText(context, "Notification message saved!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_save_notification_template"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Template", fontSize = 12.5.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    notificationTemplate = AppSettingsManager.DEFAULT_NOTIFICATION_TEMPLATE
                                    AppSettingsManager.saveNotificationTemplate(context, notificationTemplate)
                                    Toast.makeText(context, "Reset to default template", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_reset_notification_template")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset", fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }

            // 2. Default Fees Configuration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyRupee,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Default Plan Fees (INR)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Auto-fills fee fields when enrolling students into plans",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Grid of 4 fees
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = fee1Week,
                                onValueChange = { fee1Week = it.filter { ch -> ch.isDigit() } },
                                label = { Text("1 Week (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_fee_1_week"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = fee1Month,
                                onValueChange = { fee1Month = it.filter { ch -> ch.isDigit() } },
                                label = { Text("1 Month (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_fee_1_month"),
                                singleLine = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = fee3Months,
                                onValueChange = { fee3Months = it.filter { ch -> ch.isDigit() } },
                                label = { Text("3 Months (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_fee_3_months"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = fee6Months,
                                onValueChange = { fee6Months = it.filter { ch -> ch.isDigit() } },
                                label = { Text("6 Months (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_fee_6_months"),
                                singleLine = true
                            )
                        }

                        Button(
                            onClick = {
                                val w = fee1Week.toDoubleOrNull() ?: AppSettingsManager.DEFAULT_FEE_1_WEEK
                                val m1 = fee1Month.toDoubleOrNull() ?: AppSettingsManager.DEFAULT_FEE_1_MONTH
                                val m3 = fee3Months.toDoubleOrNull() ?: AppSettingsManager.DEFAULT_FEE_3_MONTHS
                                val m6 = fee6Months.toDoubleOrNull() ?: AppSettingsManager.DEFAULT_FEE_6_MONTHS

                                AppSettingsManager.saveDefaultFees(context, w, m1, m3, m6)
                                Toast.makeText(context, "Default fees updated successfully!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_save_default_fees"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Default Fees", fontSize = 13.sp)
                        }
                    }
                }
            }

            // 3. Export All User Data (ZIP Backup) Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Archive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Export Complete Data (ZIP Archive)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Database exported as Excel CSV + photos in dedicated folder",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Archive Contents:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "📁 LibAdmin_FullBackup.zip\n  ├── 📄 students_database.csv (Excel spreadsheet)\n  └── 📂 photos/ (All student profile pictures)",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.exportFullBackup(context) { zipFile, shared ->
                                    if (zipFile != null) {
                                        if (shared) {
                                            Toast.makeText(context, "Exported: ${zipFile.name}", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Backup saved to: ${zipFile.name}", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Failed to create full backup ZIP", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isExporting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_export_zip_backup"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Compressing & Exporting ZIP...")
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export & Share Full Backup ZIP", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // 4. Import User Data Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Import User Data",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Restore database and student photos from a .zip or .csv file",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "You can restore records from a full backup ZIP (which includes photos) or a standard student roster CSV file.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilledTonalButton(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            },
                            enabled = !isImporting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_import_backup"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Restoring database & photos...")
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Select Backup File (.zip / .csv)", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // 5. System & Storage Statistics Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "System & Database Info",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        val photosDir = File(context.filesDir, "student_photos")
                        val photoCount = if (photosDir.exists()) photosDir.listFiles()?.size ?: 0 else 0

                        Text("• Registered Students: ${allStudents.size}", fontSize = 12.sp)
                        Text("• Profile Photos Stored: $photoCount images", fontSize = 12.sp)
                        Text("• Storage Engine: Room SQLite (Offline First)", fontSize = 12.sp)
                        Text("• Application Version: 1.0 (Production)", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Import result dialog
    importResultDialog?.let { result ->
        AlertDialog(
            onDismissRequest = { importResultDialog = null },
            icon = {
                Icon(
                    imageVector = if (result.success) Icons.Default.Check else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (result.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(text = if (result.success) "Import Completed" else "Import Failed")
            },
            text = {
                Text(text = result.message)
            },
            confirmButton = {
                Button(
                    onClick = { importResultDialog = null },
                    modifier = Modifier.testTag("dialog_btn_import_done")
                ) {
                    Text("OK")
                }
            }
        )
    }
}
