package com.example.lifeadvices11.ui.sections.study

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.data.entities.DailyStudyEntity
import com.example.lifeadvices11.data.entities.StudyProfileEntity
import com.example.lifeadvices11.ui.navigation.Screen
import com.example.lifeadvices11.ui.onboarding.study.StudyOnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(navController: NavController) {
    val viewModel: StudyViewModel = viewModel()
    val onboardingViewModel: StudyOnboardingViewModel = viewModel()
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var needsOnboarding by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.studyProfile.value?.preferredStudyTime?.takeIf { it.isNotBlank() }?.let {
                StudyReminderScheduler.schedule(context, it)
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasOnboarding = withContext(Dispatchers.IO) {
            onboardingViewModel.hasCompletedOnboarding()
        }
        needsOnboarding = !hasOnboarding
        isLoading = false

        if (needsOnboarding) {
            navController.navigate(Screen.StudyOnboarding.route) {
                popUpTo(Screen.Study.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        val preferredTime = viewModel.studyProfile.value?.preferredStudyTime.orEmpty()
        if (preferredTime.isNotBlank()) {
            if (hasPermission) {
                StudyReminderScheduler.schedule(context, preferredTime)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (needsOnboarding) return

    val profile by viewModel.studyProfile.collectAsState()
    val todayStudy by viewModel.todayStudy.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val lastWeekStudy by viewModel.lastWeekStudy.collectAsState()
    val isLoadingData by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Учеба") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoadingData) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            StudyContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                profile = profile,
                todayStudy = todayStudy,
                categories = categories,
                lastWeekStudy = lastWeekStudy,
                onSaveCategoryHours = viewModel::updateTodayCategoryHours,
                onAddCategory = viewModel::addCategory,
                onRenameCategory = viewModel::renameCategory,
                onDeleteCategory = viewModel::deleteCategory
            )
        }
    }
}

@Composable
private fun StudyContent(
    modifier: Modifier,
    profile: StudyProfileEntity?,
    todayStudy: DailyStudyEntity?,
    categories: List<StudyCategoryUiModel>,
    lastWeekStudy: List<DailyStudyEntity>,
    onSaveCategoryHours: (Long, Float) -> Unit,
    onAddCategory: (String) -> Unit,
    onRenameCategory: (Long, String) -> Unit,
    onDeleteCategory: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategoryId by remember { mutableStateOf<Long?>(null) }
    var editingCategoryName by remember { mutableStateOf("") }
    val inputByCategory = remember(categories) {
        mutableStateMapOf<Long, String>().apply {
            categories.forEach { category ->
                this[category.id] = if (category.actualHours > 0f) {
                    String.format(Locale.US, "%.1f", category.actualHours)
                } else {
                    ""
                }
            }
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Цель на сегодня", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${String.format(Locale.US, "%.1f", todayStudy?.actualStudyHours ?: 0f)} / ${String.format(Locale.US, "%.1f", profile?.targetStudyHours ?: 0.0)} ч",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Напоминание: ${profile?.preferredStudyTime.orEmpty()} • Сессия: ${profile?.sessionDurationMinutes ?: 0} мин",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Категории", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить предмет")
                        Spacer(modifier = Modifier.height(0.dp))
                        Text("Добавить предмет")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (categories.isEmpty()) {
                    Text("Предметы пока не добавлены.")
                } else {
                    categories.forEach { category ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(category.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "План: ${String.format(Locale.US, "%.1f", category.plannedHours)} ч",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            editingCategoryId = category.id
                                            editingCategoryName = category.name
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                                    }
                                    IconButton(onClick = { onDeleteCategory(category.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = inputByCategory[category.id].orEmpty(),
                                        onValueChange = { inputByCategory[category.id] = it },
                                        label = { Text("Фактическое время") },
                                        placeholder = { Text("Например, 1.5") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            onSaveCategoryHours(
                                                category.id,
                                                inputByCategory[category.id]?.toFloatOrNull() ?: 0f
                                            )
                                        }
                                    ) {
                                        Text("Сохранить")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (lastWeekStudy.isNotEmpty()) {
            val formatter = remember { SimpleDateFormat("dd MMM", Locale("ru")) }
            Spacer(modifier = Modifier.height(16.dp))
            Text("История учебы", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            lastWeekStudy.forEach { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatter.format(Date(entry.date)))
                        Text("${String.format(Locale.US, "%.1f", entry.actualStudyHours)} ч")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CategoryNameDialog(
            title = "Добавить предмет",
            initialValue = "",
            onDismiss = { showAddDialog = false },
            onConfirm = {
                onAddCategory(it)
                showAddDialog = false
            }
        )
    }

    if (editingCategoryId != null) {
        CategoryNameDialog(
            title = "Изменить название",
            initialValue = editingCategoryName,
            onDismiss = { editingCategoryId = null },
            onConfirm = { updatedName ->
                onRenameCategory(editingCategoryId ?: return@CategoryNameDialog, updatedName)
                editingCategoryId = null
            }
        )
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
                enabled = value.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
