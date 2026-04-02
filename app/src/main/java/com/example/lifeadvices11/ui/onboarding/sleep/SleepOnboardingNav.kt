package com.example.lifeadvices11.ui.onboarding.sleep

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SleepOnboardingNav(
    navController: NavController,
    viewModel: SleepOnboardingViewModel = viewModel()
) {
    val onboardingNavController = rememberNavController()

    NavHost(
        navController = onboardingNavController,
        startDestination = "step1"
    ) {
        composable("step1") {
            PersonalSleepInfoStep(
                viewModel = viewModel,
                onNext = { onboardingNavController.navigate("step2") }
            )
        }

        composable("step2") {
            SleepQualityStep(
                viewModel = viewModel,
                onNext = { onboardingNavController.navigate("step3") }
            )
        }

        composable("step3") {
            SleepIssuesStep(
                viewModel = viewModel,
                onComplete = {
                    viewModel.saveOnboardingData {
                        navController.popBackStack()
                        navController.navigate("sleep")
                    }
                }
            )
        }
    }
}