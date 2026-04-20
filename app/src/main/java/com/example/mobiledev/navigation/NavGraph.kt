package com.example.mobiledev.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobiledev.data.repository.*
import com.example.mobiledev.feature.main.presentation.MainScreen
import com.example.mobiledev.feature.signin.presentation.SignInRoute
import com.example.mobiledev.feature.signin.presentation.SignInViewModel
import com.example.mobiledev.feature.signin.presentation.SignInViewModelFactory
import com.example.mobiledev.feature.signup.presentation.SignUpRoute
import com.example.mobiledev.feature.signup.presentation.SignUpViewModel
import com.example.mobiledev.feature.signup.presentation.SignUpViewModelFactory
import com.example.mobiledev.feature.staff.StaffManagementScreen
import com.example.mobiledev.feature.staff.StaffViewModel
import com.example.mobiledev.feature.staff.StaffViewModelFactory
import com.example.mobiledev.feature.emergency.EmergencyDashboardScreen
import com.example.mobiledev.feature.emergency.EmergencyViewModel
import com.example.mobiledev.feature.emergency.EmergencyViewModelFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object Main : Screen("main")
    object StaffManagement : Screen("staff_management")
    object EmergencyDashboard : Screen("emergency_dashboard")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val userRepository = remember { FirebaseUserRepository(context) }
    
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://your-api-base-url.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val staffApiService = remember { retrofit.create(StaffApiService::class.java) }
    val staffRepository = remember { ApiStaffRepository(staffApiService) }
    
    val emergencyApiService = remember { retrofit.create(EmergencyApiService::class.java) }
    val emergencyRepository = remember { ApiEmergencyRepository(emergencyApiService) }

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
            MainScreen(
                onManageStaffClick = {
                    navController.navigate(Screen.StaffManagement.route)
                },
                onEmergencyDashboardClick = {
                    navController.navigate(Screen.EmergencyDashboard.route)
                }
            )
        }

        composable(Screen.StaffManagement.route) {
            val staffViewModelFactory = remember { StaffViewModelFactory(staffRepository) }
            val viewModel: StaffViewModel = viewModel(factory = staffViewModelFactory)
            StaffManagementScreen(viewModel = viewModel)
        }

        composable(Screen.EmergencyDashboard.route) {
            val emergencyViewModelFactory = remember { EmergencyViewModelFactory(emergencyRepository) }
            val viewModel: EmergencyViewModel = viewModel(factory = emergencyViewModelFactory)
            EmergencyDashboardScreen(viewModel = viewModel)
        }
    }
}
