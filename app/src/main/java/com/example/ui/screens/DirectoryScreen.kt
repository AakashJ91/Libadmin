package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.components.StudentCard
import com.example.ui.components.StudentGridCard
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiringSoon
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FilterType
import com.example.ui.viewmodel.SortOrder
import com.example.ui.viewmodel.LibraryViewModel
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryScreen(
    viewModel: LibraryViewModel,
    students: List<Student>,
    searchQuery: String,
    selectedFilter: FilterType,
    sortOrder: SortOrder,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var exportMenuExpanded by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(false) }

    // Multi-selection state
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Long>() }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isSeeding5k by viewModel.isSeeding5k.collectAsStateWithLifecycle()
    val seedingProgress by viewModel.seedingProgress.collectAsStateWithLifecycle()

    // Intercept back button when in selection mode
    BackHandler(enabled = isSelectionMode) {
        isSelectionMode = false
        selectedIds.clear()
    }

    val allStudents = viewModel.allStudents.value
    val now = System.currentTimeMillis()

    val totalRegistered = allStudents.size
    val paidCount = allStudents.count { !it.isPendingFee }
    val duesPendingCount = allStudents.count { it.isPendingFee }
    val expiringCount = allStudents.count { it.getStatus(now) == SubscriptionStatus.EXPIRING_SOON }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                // Contextual Selection Top App Bar
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                isSelectionMode = false
                                selectedIds.clear()
                            },
                            modifier = Modifier.testTag("btn_close_selection")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close selection")
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "${selectedIds.size} Selected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap cards to toggle selection",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        // Select All / Deselect All
                        val allFilteredSelected = students.isNotEmpty() && selectedIds.size == students.size
                        IconButton(
                            onClick = {
                                if (allFilteredSelected) {
                                    selectedIds.clear()
                                } else {
                                    selectedIds.clear()
                                    selectedIds.addAll(students.map { it.id })
                                }
                            },
                            modifier = Modifier.testTag("btn_toggle_select_all")
                        ) {
                            Icon(
                                imageVector = if (allFilteredSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                                contentDescription = if (allFilteredSelected) "Deselect All" else "Select All",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Delete Selected Button
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            enabled = selectedIds.isNotEmpty(),
                            modifier = Modifier.testTag("btn_delete_selected_students")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = if (selectedIds.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                // Regular Top App Bar
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Students",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.5.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${students.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        // View Mode Toggle (Grid vs List)
                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("btn_toggle_view_mode")
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Enter Multi-Select Mode Button
                        IconButton(
                            onClick = { isSelectionMode = true },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("btn_enter_multiselect")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Multi-Select Students",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Export Roster (Excel / PDF) dropdown menu button
                        Box {
                            IconButton(
                                onClick = { exportMenuExpanded = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("btn_export_roster_menu")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Export Roster",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = exportMenuExpanded,
                                onDismissRequest = { exportMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.TableChart,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    text = { Text("Export Excel Sheet (.csv)") },
                                    onClick = {
                                        exportMenuExpanded = false
                                        viewModel.exportExcelReport(context) { file, shared ->
                                            if (file != null) {
                                                if (shared) {
                                                    Toast.makeText(context, "Excel exported: ${file.name}", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Saved report: ${file.name}", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "Failed to export Excel sheet", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("btn_export_excel_roster")
                                )

                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    text = { Text("Export PDF Report") },
                                    onClick = {
                                        exportMenuExpanded = false
                                        viewModel.exportPdfReport(context) { file, shared ->
                                            if (file != null) {
                                                if (shared) {
                                                    Toast.makeText(context, "PDF exported: ${file.name}", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Saved report: ${file.name}", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("btn_export_pdf_roster")
                                )
                            }
                        }

                        // Sort menu button
                        Box {
                            IconButton(
                                onClick = { sortMenuExpanded = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("btn_sort_roster")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                SortOrder.values().forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = order.displayName,
                                                fontWeight = if (order == sortOrder) FontWeight.Bold else FontWeight.Normal,
                                                color = if (order == sortOrder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSort(order)
                                            sortMenuExpanded = false
                                        }
                                    )
                                }

                                if (totalRegistered > 10) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = "🧹 Reset to Default Members",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            sortMenuExpanded = false
                                            viewModel.resetToDefaultMembers {
                                                Toast.makeText(context, "Reset database to default seed members", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            if (isSelectionMode) {
                if (selectedIds.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = { showDeleteConfirmDialog = true },
                        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        text = { Text("Delete (${selectedIds.size})") },
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.testTag("fab_delete_selected")
                    )
                }
            } else {
                FloatingActionButton(
                    onClick = { onNavigate(AppScreen.AddEditStudent()) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_enroll_student")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Enroll Student")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Quick Status Summary Counters (Clickable filter shortcuts)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(
                    label = "Registered",
                    count = totalRegistered,
                    icon = Icons.Default.People,
                    color = MaterialTheme.colorScheme.primary,
                    isSelected = selectedFilter == FilterType.ALL,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.setFilter(FilterType.ALL)
                }

                StatusPill(
                    label = "Paid Full",
                    count = paidCount,
                    icon = Icons.Default.CheckCircle,
                    color = StatusActive,
                    isSelected = selectedFilter == FilterType.PAID_FEES,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.setFilter(FilterType.PAID_FEES)
                }

                StatusPill(
                    label = "Dues Pending",
                    count = duesPendingCount,
                    icon = Icons.Default.Payments,
                    color = StatusExpired,
                    isSelected = selectedFilter == FilterType.PENDING_FEES,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.setFilter(FilterType.PENDING_FEES)
                }

                StatusPill(
                    label = "Expiring",
                    count = expiringCount,
                    icon = Icons.Default.DateRange,
                    color = StatusExpiringSoon,
                    isSelected = selectedFilter == FilterType.EXPIRING_3_DAYS,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.setFilter(FilterType.EXPIRING_3_DAYS)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("input_search_students"),
                placeholder = { Text("Search by name, ID, phone, or shift...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )

            // Horizontal Filter Chips Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterType.values().forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedFilter.value = filter },
                        label = {
                            Text(
                                text = filter.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (filter) {
                                FilterType.PENDING_FEES -> StatusExpired.copy(alpha = 0.2f)
                                FilterType.PAID_FEES -> StatusActive.copy(alpha = 0.2f)
                                FilterType.EXPIRING_3_DAYS, FilterType.EXPIRING_7_DAYS -> StatusExpiringSoon.copy(alpha = 0.2f)
                                FilterType.EXPIRED -> StatusExpired.copy(alpha = 0.2f)
                                FilterType.ACTIVE -> StatusActive.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            selectedLabelColor = when (filter) {
                                FilterType.PENDING_FEES -> StatusExpired
                                FilterType.PAID_FEES -> StatusActive
                                FilterType.EXPIRING_3_DAYS, FilterType.EXPIRING_7_DAYS -> StatusExpiringSoon
                                FilterType.EXPIRED -> StatusExpired
                                FilterType.ACTIVE -> StatusActive
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        ),
                        modifier = Modifier.testTag("filter_chip_${filter.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Scrollable List of Registered Students
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No students match this filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try clearing your search query or switching to 'All Students'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                viewModel.searchQuery.value = ""
                                viewModel.setFilter(FilterType.ALL)
                            }
                        ) {
                            Text("Reset Filters")
                        }
                    }
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("scrollable_students_grid"),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = students,
                        key = { it.id },
                        contentType = { "student_grid_card" }
                    ) { student ->
                        val isStudentSelected = selectedIds.contains(student.id)
                        StudentGridCard(
                            student = student,
                            onClick = {
                                if (isSelectionMode) {
                                    if (isStudentSelected) selectedIds.remove(student.id) else selectedIds.add(student.id)
                                } else {
                                    onNavigate(AppScreen.StudentDetail(student.id))
                                }
                            },
                            onWhatsAppClick = {
                                viewModel.dispatchReminder(context, student, ReminderChannel.WHATSAPP)
                            },
                            onSmsClick = {
                                viewModel.dispatchReminder(context, student, ReminderChannel.SMS)
                            },
                            onCallClick = {
                                NotificationHelper.openDialer(context, student.phone)
                            },
                            isSelectionMode = isSelectionMode,
                            isSelected = isStudentSelected,
                            onToggleSelect = {
                                if (isStudentSelected) selectedIds.remove(student.id) else selectedIds.add(student.id)
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    isSelectionMode = true
                                    selectedIds.add(student.id)
                                }
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("scrollable_students_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = students,
                        key = { it.id },
                        contentType = { "student_card" }
                    ) { student ->
                        val isStudentSelected = selectedIds.contains(student.id)
                        StudentCard(
                            student = student,
                            onClick = {
                                if (isSelectionMode) {
                                    if (isStudentSelected) selectedIds.remove(student.id) else selectedIds.add(student.id)
                                } else {
                                    onNavigate(AppScreen.StudentDetail(student.id))
                                }
                            },
                            onWhatsAppClick = {
                                viewModel.dispatchReminder(context, student, ReminderChannel.WHATSAPP)
                            },
                            onSmsClick = {
                                viewModel.dispatchReminder(context, student, ReminderChannel.SMS)
                            },
                            onCallClick = {
                                NotificationHelper.openDialer(context, student.phone)
                            },
                            isSelectionMode = isSelectionMode,
                            isSelected = isStudentSelected,
                            onToggleSelect = {
                                if (isStudentSelected) selectedIds.remove(student.id) else selectedIds.add(student.id)
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    isSelectionMode = true
                                    selectedIds.add(student.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Multi-Student Deletion
    if (showDeleteConfirmDialog) {
        val count = selectedIds.size
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete $count Student${if (count > 1) "s" else ""}?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete $count registered student record${if (count > 1) "s" else ""}? This will remove their library access credentials, fee status, and attendance history. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idsToDelete = selectedIds.toSet()
                        viewModel.deleteMultipleStudents(idsToDelete)
                        selectedIds.clear()
                        isSelectionMode = false
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "Deleted $count student record${if (count > 1) "s" else ""}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_multiple")
                ) {
                    Text("Delete ($count)")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Progress Dialog during 5,000 members scale generation
    if (isSeeding5k) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Scaling to 5,000 Members...",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Writing and indexing 5,000 members into Room SQLite with Write-Ahead Logging (WAL) for 5k-capacity benchmark.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { seedingProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(seedingProgress * 5000).toInt()} / 5,000 members indexed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun StatusPill(
    label: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) color else color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else color,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "$count",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(if (isSelected) color else Color.Transparent)
            )
        }
    }
}
