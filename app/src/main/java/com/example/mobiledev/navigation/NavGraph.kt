package com.example.mobiledev.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobiledev.data.repository.FirebaseUserRepository
import com.example.mobiledev.ResQApplication
import com.example.mobiledev.data.repository.ApiEmergencyRepository
import com.example.mobiledev.data.repository.ApiStaffRepository
import com.example.mobiledev.data.repository.EmergencyApiService
import com.example.mobiledev.data.repository.StaffApiService
import com.example.mobiledev.feature.hospital.presentation.HospitalDashboardScreen
import com.example.mobiledev.feature.hospital.presentation.HospitalDashboardViewModel
import com.example.mobiledev.feature.hospital.presentation.HospitalDashboardViewModelFactory
import com.example.mobiledev.feature.hospital.presentation.HospitalSignInRoute
import com.example.mobiledev.feature.hospital.presentation.HospitalSignInViewModel
import com.example.mobiledev.feature.hospital.presentation.HospitalSignInViewModelFactory
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
    object HospitalSignIn : Screen("hospital_signin")
    object HospitalDashboard : Screen("hospital_dashboard/{hospitalId}") {
        fun createRoute(hospitalId: String) = "hospital_dashboard/$hospitalId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val resQRepository = (context.applicationContext as ResQApplication).container.resQRepository
    val userRepository = remember(context) { FirebaseUserRepository(context) }
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val staffRepository = remember(retrofit) {
        ApiStaffRepository(retrofit.create(StaffApiService::class.java))
    }
    val emergencyRepository = remember(retrofit) {
        ApiEmergencyRepository(retrofit.create(EmergencyApiService::class.java))
    }

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
                },
                onHospitalSignInClick = {
                    navController.navigate(Screen.HospitalSignIn.route)
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
                        popUpTo(Screen.SignUp.route) { inclusive = true }
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

        composable(Screen.HospitalSignIn.route) {
            val viewModel: HospitalSignInViewModel = viewModel(
                factory = HospitalSignInViewModelFactory(resQRepository)
            )
            HospitalSignInRoute(
                viewModel = viewModel,
                onSignInSuccess = { hospitalId ->
                    navController.navigate(Screen.HospitalDashboard.createRoute(hospitalId)) {
                        popUpTo(Screen.HospitalSignIn.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.HospitalDashboard.route) { backStackEntry ->
            val hospitalId = backStackEntry.arguments?.getString("hospitalId") ?: ""
            val viewModel: HospitalDashboardViewModel = viewModel(
                factory = HospitalDashboardViewModelFactory(resQRepository, hospitalId)
            )
            HospitalDashboardScreen(viewModel = viewModel)
        }
    }
}

