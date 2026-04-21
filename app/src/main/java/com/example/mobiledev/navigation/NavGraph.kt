package com.example.mobiledev.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobiledev.data.repository.FirebaseUserRepository
import com.example.mobiledev.ResQApplication
import com.example.mobiledev.data.repository.ApiStaffRepository
import com.example.mobiledev.data.services.StaffApiService
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
    val container = (context.applicationContext as ResQApplication).container
    val resQRepository = container.resQRepository
    val authSessionManager = container.authSessionManager
    val emergencyRepository = container.emergencyRepository
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

    NavHost(
        navController = navController,
        startDestination = Screen.SignIn.route
    ) {
        composable(Screen.SignIn.route) {
            val signInFactory = remember(userRepository, authSessionManager) {
                SignInViewModelFactory(userRepository, authSessionManager)
            }
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
            val signUpFactory = remember(userRepository, authSessionManager) {
                SignUpViewModelFactory(userRepository, authSessionManager)
            }
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
            val emergencyViewModelFactory = remember { EmergencyViewModelFactory(emergencyRepository) }
            val emergencyViewModel: EmergencyViewModel = viewModel(factory = emergencyViewModelFactory)
            val principal by authSessionManager.principal.collectAsState()
            var signedInUser by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.mobiledev.data.model.User?>(null) }

            LaunchedEffect(principal.userId) {
                val activeUserId = principal.userId
                signedInUser = if (activeUserId.isNullOrBlank()) {
                    null
                } else {
                    runCatching {
                        userRepository.getUsers().firstOrNull { it.id == activeUserId }
                    }.getOrNull()
                }
            }

            MainScreen(
                currentPrincipal = principal,
                currentUser = signedInUser,
                onManageStaffClick = {
                    navController.navigate(Screen.StaffManagement.route)
                },
                requestTabContent = {
                    EmergencyDashboardScreen(viewModel = emergencyViewModel)
                }
            )
        }

        composable(Screen.StaffManagement.route) {
            val staffViewModelFactory = remember { StaffViewModelFactory(staffRepository) }
            val viewModel: StaffViewModel = viewModel(factory = staffViewModelFactory)
            StaffManagementScreen(viewModel = viewModel)
        }

        composable(Screen.HospitalSignIn.route) {
            val viewModel: HospitalSignInViewModel = viewModel(
                factory = HospitalSignInViewModelFactory(userRepository, authSessionManager)
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
            if (hospitalId.isBlank()) {
                Text("Invalid hospital session. Please sign in again.")
            } else {
                val viewModel: HospitalDashboardViewModel = viewModel(
                    factory = HospitalDashboardViewModelFactory(resQRepository, hospitalId)
                )
                HospitalDashboardScreen(viewModel = viewModel)
            }
        }
    }
}

