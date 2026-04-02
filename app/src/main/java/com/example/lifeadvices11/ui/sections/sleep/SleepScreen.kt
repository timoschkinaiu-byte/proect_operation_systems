package com.example.lifeadvices11.ui.sections.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifeadvices11.ui.navigation.Screen
import com.example.lifeadvices11.ui.onboarding.sleep.SleepOnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(navController: NavController) {
    val viewModel: SleepViewModel = viewModel<SleepViewModel>()
    val onboardingViewModel: SleepOnboardingViewModel = viewModel<SleepOnboardingViewModel>()

    var isLoading by remember { mutableStateOf(true) }
    var needsOnboarding by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        android.util.Log.d("SleepScreen", "=== НАЧАЛО ===")

        val hasOnboarding = withContext(Dispatchers.IO) {
            try {
                val result = onboardingViewModel.hasCompletedOnboarding()
                android.util.Log.d("SleepScreen", "hasCompletedOnboarding = $result")
                result
            } catch (e: Exception) {
                android.util.Log.e("SleepScreen", "Ошибка: ${e.message}")
                false
            }
        }
        needsOnboarding = !hasOnboarding
        android.util.Log.d("SleepScreen", "needsOnboarding = $needsOnboarding")
        isLoading = false

        if (needsOnboarding) {
            android.util.Log.d("SleepScreen", "ПЕРЕХОД НА ОНБОРДИНГ")
            navController.navigate(Screen.SleepOnboarding.route) {
                popUpTo(Screen.Sleep.route) { inclusive = true }
            }
        } else {
            android.util.Log.d("SleepScreen", "ОНБОРДИНГ НЕ НУЖЕН, ПОКАЗЫВАЕМ ОСНОВНОЙ ЭКРАН")
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (!needsOnboarding) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Сон") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "😴 СОН",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Раздел в разработке",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Здесь будет трекер сна и рекомендации",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}