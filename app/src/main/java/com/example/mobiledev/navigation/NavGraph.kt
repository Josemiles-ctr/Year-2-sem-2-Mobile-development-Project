package com.example.mobiledev.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobiledev.data.repository.FirebaseUserRepository
import com.example.mobiledev.feature.main.presentation.MainScreen
import com.example.mobiledev.feature.signin.presentation.SignInRoute
import com.example.mobiledev.feature.signin.presentation.SignInViewModel
import com.example.mobiledev.feature.signin.presentation.SignInViewModelFactory
import com.example.mobiledev.feature.signup.presentation.SignUpRoute
import com.example.mobiledev.feature.signup.presentation.SignUpViewModel
import com.example.mobiledev.feature.signup.presentation.SignUpViewModelFactory

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object Main : Screen("main")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val userRepository = remember { FirebaseUserRepository() }

    NavHost(
        navController = navController,
        startDestination = Screen.SignIn.route
    ) {
        composable(Screen.SignIn.route) {
            val signInFactory = remember(userRepository) { SignInViewModelFactory(userRepository) }
            val viewModel: SignInViewModel = viewModel(factory = signInFactory)
            SignInRoute(
                viewModel = viewModel,
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                },
                onAuthSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.SignUp.route) {
            val signUpFactory = remember(userRepository) { SignUpViewModelFactory(userRepository) }
            val viewModel: SignUpViewModel = viewModel(factory = signUpFactory)
            SignUpRoute(
                viewModel = viewModel,
                onLoginClick = {
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen()
        }
    }
}
