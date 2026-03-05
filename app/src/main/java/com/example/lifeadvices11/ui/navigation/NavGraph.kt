package com.example.lifeadvices11.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lifeadvices11.ui.sections.MainScreen
import com.example.lifeadvices11.ui.sections.nutrition.NutritionScreen
import com.example.lifeadvices11.ui.sections.sport.SportScreen
import com.example.lifeadvices11.ui.sections.psychology.PsychologyScreen
import com.example.lifeadvices11.ui.sections.sleep.SleepScreen
import com.example.lifeadvices11.ui.sections.study.StudyScreen

// ✅ ПРАВИЛЬНО: sealed class с route:String
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Onboarding : Screen("onboarding")
    object Nutrition : Screen("nutrition")
    object Sport : Screen("sport")
    object Psychology : Screen("psychology")
    object Sleep : Screen("sleep")
    object Study : Screen("study")
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
    }
}