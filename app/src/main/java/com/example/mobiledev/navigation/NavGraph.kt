package com.example.mobiledev.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobiledev.feature.signin.presentation.SignInRoute
import com.example.mobiledev.feature.signin.presentation.SignInViewModel
import com.example.mobiledev.feature.signup.presentation.SignUpRoute
import com.example.mobiledev.feature.signup.presentation.SignUpViewModel

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.SignIn.route
    ) {
        composable(Screen.SignIn.route) {
            val viewModel: SignInViewModel = viewModel()
            SignInRoute(
                viewModel = viewModel,
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }
        composable(Screen.SignUp.route) {
            val viewModel: SignUpViewModel = viewModel()
            SignUpRoute(
                viewModel = viewModel,
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
