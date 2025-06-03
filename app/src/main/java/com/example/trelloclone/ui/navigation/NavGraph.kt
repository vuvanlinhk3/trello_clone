package com.example.trelloclone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.trelloclone.ui.HomeScreenTest
import com.example.trelloclone.ui.LoginScreen
import com.example.trelloclone.ui.RegisterScreen
import com.example.trelloclone.ui.SplashScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreenTest(navController) }
    }
}
