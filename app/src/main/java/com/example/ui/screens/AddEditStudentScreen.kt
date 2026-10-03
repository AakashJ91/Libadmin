package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.StudentAvatar
import com.example.ui.viewmodel.LibraryViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentScreen(
    studentId: Long?,
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val existing = if (studentId != null) {
        viewModel.allStudents.value.firstOrNull { it.id == studentId }
    } else null

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var idProofNumber by remember {
        mutableStateOf(existing?.idProofNumber ?: "LIB-2024-${(1000..9999).random()}")
    }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var emergencyContact by remember { mutableStateOf(existing?.emergencyContact ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var avatarKey by remember { mutableStateOf(existing?.avatarKey ?: "avatar_1") }

    var selectedShift by remember {
        mutableStateOf(existing?.shift ?: "Full Day (6 AM - 10 PM)")
    }

    val shifts = listOf(
        "Morning (6 AM - 2 PM)",
        "Evening (2 PM - 10 PM)",
        "Full Day (6 AM - 10 PM)",
        "24 Hours Access"
    )

    var selectedPlan by remember {
        mutableStateOf(existing?.planType ?: "1 Month")
    }

    val plans = listOf(
        Pair("1 Week", 7),
        Pair("1 Month", 30),
        Pair("3 Months", 90),
        Pair("6 Months", 180),
        Pair("Custom", 15)
    )

    var customDays by remember { mutableStateOf("15") }

    var feeAmount by remember {
        mutableStateOf(existing?.feeAmount?.toInt()?.toString() ?: "1000")
    }
    var feePaid by remember {
        mutableStateOf(existing?.feePaid?.toInt()?.toString() ?: "1000")
    }

    val avatarKeys = listOf("avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5", "avatar_6")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existing != null) "Edit Student Profile" else "Enroll Student",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_form_back")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Profile & Avatar Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        StudentAvatar(
                            name = name.ifBlank { "New" },
                            avatarKey = avatarKey,
                            sizeDp = 64
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select Avatar Tone",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            avatarKeys.forEach { key ->
                                val isSelected = avatarKey == key
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (key) {
                                                "avatar_1" -> Color(0xFF0284C7)
                                                "avatar_2" -> Color(0xFF0D9488)
                                                "avatar_3" -> Color(0xFF7C3AED)
                                                "avatar_4" -> Color(0xFFD97706)
                                                "avatar_5" -> Color(0xFFDB2777)
                                                else -> Color(0xFF4F46E5)
                                            }
                                        )
                                        .clickable { avatarKey = key }
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Student Information Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Student Identification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_student_name"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number (for WhatsApp/SMS) *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_student_phone"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address (for Reminders)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_student_email"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = idProofNumber,
                            onValueChange = { idProofNumber = it },
                            label = { Text("Student ID / Gov Card ID *") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_student_id"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Residential Address") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_student_address"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = emergencyContact,
                            onValueChange = { emergencyContact = it },
                            label = { Text("Emergency Contact (Name & Phone)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Access Duration & Shift
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Access Duration & Shift",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        // Shifts selection
                        Text(text = "Study Shift / Time Slot:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            shifts.forEach { shift ->
                                val isSelected = selectedShift == shift
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedShift = shift },
                                    label = { Text(shift, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Duration Plans
                        Text(text = "Duration for Library Access:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            plans.forEach { (planLabel, _) ->
                                val isSelected = selectedPlan == planLabel
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedPlan = planLabel
                                        when (planLabel) {
                                            "1 Week" -> feeAmount = "400"
                                            "1 Month" -> feeAmount = "1000"
                                            "3 Months" -> feeAmount = "2700"
                                            "6 Months" -> feeAmount = "5000"
                                        }
                                        feePaid = feeAmount
                                    },
                                    label = { Text(planLabel, fontSize = 11.sp) }
                                )
                            }
                        }

                        if (selectedPlan == "Custom") {
                            OutlinedTextField(
                                value = customDays,
                                onValueChange = { customDays = it },
                                label = { Text("Custom Duration in Days") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // Fees
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = feeAmount,
                                onValueChange = { feeAmount = it },
                                label = { Text("Total Fee (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = feePaid,
                                onValueChange = { feePaid = it },
                                label = { Text("Amount Paid (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Special Notes / Study Goal") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            }

            // Save Submit Button
            item {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Please enter student name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (phone.isBlank()) {
                            Toast.makeText(context, "Please enter phone number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val durationDays = when (selectedPlan) {
                            "1 Week" -> 7
                            "1 Month" -> 30
                            "3 Months" -> 90
                            "6 Months" -> 180
                            else -> customDays.toIntOrNull() ?: 30
                        }

                        val now = System.currentTimeMillis()
                        val start = existing?.startDateMillis ?: now
                        val end = if (existing != null) {
                            existing.endDateMillis
                        } else {
                            now + TimeUnit.DAYS.toMillis(durationDays.toLong())
                        }

                        val studentToSave = Student(
                            id = existing?.id ?: 0L,
                            name = name.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            idProofNumber = idProofNumber.trim(),
                            address = address.trim(),
                            avatarKey = avatarKey,
                            shift = selectedShift,
                            planType = selectedPlan,
                            startDateMillis = start,
                            endDateMillis = end,
                            feeAmount = feeAmount.toDoubleOrNull() ?: 0.0,
                            feePaid = feePaid.toDoubleOrNull() ?: 0.0,
                            emergencyContact = emergencyContact.trim(),
                            notes = notes.trim(),
                            createdAt = existing?.createdAt ?: now
                        )

                        viewModel.saveStudent(studentToSave)
                        Toast.makeText(context, "Student enrolled successfully!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_student"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (existing != null) "Update Profile" else "Enroll Student",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
