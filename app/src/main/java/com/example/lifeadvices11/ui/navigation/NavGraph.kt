package com.example.lifeadvices11.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.navigation.compose.rememberNavController
import com.example.lifeadvices11.ui.sections.MainScreen
import com.example.lifeadvices11.ui.sections.nutrition.NutritionScreen
import com.example.lifeadvices11.ui.sections.sport.SportScreen
import com.example.lifeadvices11.ui.sections.psychology.PsychologyScreen
import com.example.lifeadvices11.ui.sections.sleep.SleepScreen
import com.example.lifeadvices11.ui.sections.study.StudyScreen
import com.example.lifeadvices11.ui.onboarding.nutrition.NutritionOnboardingScreen
import com.example.lifeadvices11.ui.sections.sleep.AddSleepEntryScreen
import com.example.lifeadvices11.ui.onboarding.sleep.SleepOnboardingScreen
import com.example.lifeadvices11.ui.sections.sleep.SleepPracticeDetailScreen
import com.example.lifeadvices11.ui.sections.sleep.SleepPracticesScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Onboarding : Screen("onboarding")
    object Nutrition : Screen("nutrition")
    object Sport : Screen("sport")
    object Psychology : Screen("psychology")
    object Sleep : Screen("sleep")
    object Study : Screen("study")
    object NutritionOnboarding : Screen("nutrition_onboarding")
    object SleepOnboarding : Screen("sleep_onboarding")
    object SleepAddEntry : Screen("sleep_add_entry")
    object SleepPractices : Screen("sleep_practices")
    object SleepPracticeDetail : Screen("sleep_practice_detail/{practiceId}")
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Main.route  // ✅ Здесь String!
) {
    NavHost(
        navController = navController,
        startDestination = startDestination  // ✅ startDestination должен быть String
    ) {
        composable(Screen.Main.route) {  // ✅ .route дает String
            MainScreen(navController = navController)
        }

        composable(Screen.Nutrition.route) {
            NutritionScreen(navController = navController)
        }

        composable(Screen.Sport.route) {
            SportScreen(navController = navController)
        }

        composable(Screen.Psychology.route) {
            PsychologyScreen(navController = navController)
        }

        composable(Screen.Sleep.route) {
            SleepScreen(navController = navController)
        }

        composable(Screen.Study.route) {
            StudyScreen(navController = navController)
        }
        composable(Screen.NutritionOnboarding.route) {
            NutritionOnboardingScreen(navController = navController)
        }
        composable(Screen.SleepOnboarding.route) {
            SleepOnboardingScreen(navController = navController)
        }
        composable(Screen.SleepAddEntry.route) {
            AddSleepEntryScreen(navController = navController)
        }
        composable(Screen.SleepPractices.route) {
            SleepPracticesScreen(navController = navController)
        }

        composable(
            route = Screen.SleepPracticeDetail.route,
            arguments = listOf(navArgument("practiceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val practiceId = backStackEntry.arguments?.getLong("practiceId") ?: 0L
            SleepPracticeDetailScreen(navController = navController, practiceId = practiceId)
        }


    }
}