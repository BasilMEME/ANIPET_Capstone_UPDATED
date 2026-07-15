@file:Suppress("unused")
package com.example.anipet_capstone

import androidx.core.content.edit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.anipet_capstone.screens.ApplyAdoptionScreen
import com.example.anipet_capstone.screens.AppointmentsScreen
import com.example.anipet_capstone.screens.ApplicationTrackingScreen
import com.example.anipet_capstone.screens.BookAppointmentScreen
import com.example.anipet_capstone.screens.DonateScreen
import com.example.anipet_capstone.screens.LoginScreen
import com.example.anipet_capstone.screens.MyApplicationsScreen
import com.example.anipet_capstone.screens.PetDetailsScreen
import com.example.anipet_capstone.screens.PetsListScreen
import com.example.anipet_capstone.screens.QrScannerScreen
import com.example.anipet_capstone.screens.RegisterScreen
import com.example.anipet_capstone.screens.UserProfileScreen
import com.example.anipet_capstone.screens.UserDetailsScreen
import com.example.anipet_capstone.ui.theme.ANIPET_CapstoneTheme

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object PetsList : Screen("pets_list")
    object MyApplications : Screen("my_applications")
    object UserProfile : Screen("user_profile")
    object UserDetails : Screen("user_details")
    object Appointments : Screen("appointments")
    object BookAppointment : Screen("book_appointment")
    object QrScanner : Screen("qr_scanner")
    object Donate : Screen("donate")
    object ApplicationTracking : Screen("application_tracking/{applicationId}") {
        fun createRoute(applicationId: String) = "application_tracking/$applicationId"
    }
    object Otp : Screen("otp/{email}") {
        fun createRoute(email: String) = "otp/$email"
    }

    object PetDetails : Screen("pet_details/{petId}") {
        fun createRoute(petId: String) = "pet_details/$petId"
    }

    object Apply : Screen("apply/{petId}") {
        fun createRoute(petId: String) = "apply/$petId"
    }
}

fun saveUserSession(context: Context, userId: String, fullName: String, email: String) {
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    prefs.edit {
        putString("user_id", userId)
        putString("full_name", fullName)
        putString("email", email)
    }
}

fun saveUserSession(context: Context, userId: String, fullName: String, email: String, username: String?, role: String?) {
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    prefs.edit {
        putString("user_id", userId)
        putString("full_name", fullName)
        putString("email", email)
        putString("username", username)
        putString("role", role)
    }
}

fun getUserId(context: Context): String? {
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    return prefs.getString("user_id", null)
}

fun getFullName(context: Context): String? {
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    return prefs.getString("full_name", null)
}

fun getEmail(context: Context): String? {
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    return prefs.getString("email", null)
}

fun clearUserSession(context: Context) {
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    prefs.edit { clear() }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ANIPET_CapstoneTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val userId = getUserId(context)

                val startDestination =
                    if (userId == null) Screen.Login.route else Screen.PetsList.route

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = { userIdValue, fullName, email, username, role ->
                                saveUserSession(context, userIdValue, fullName, email, username, role)
                                navController.navigate(Screen.PetsList.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onGoToRegister = {
                                navController.navigate(Screen.Register.route)
                            },
                            onNavigateToOtp = { email ->
                                navController.navigate(Screen.Otp.createRoute(email))
                            }
                        )
                    }

                    composable(Screen.Register.route) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.popBackStack()
                            },
                            onBackToLogin = {
                                navController.popBackStack()
                            },
                            onNavigateToOtp = { email ->
                                navController.navigate(Screen.Otp.createRoute(email))
                            }
                        )
                    }

                    composable(
                        route = Screen.Otp.route,
                        arguments = listOf(navArgument("email") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        com.example.anipet_capstone.screens.OtpScreen(
                            email = email,
                            onVerified = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.PetsList.route) {
                        PetsListScreen(
                            onPetClick = { petId ->
                                navController.navigate(Screen.PetDetails.createRoute(petId))
                            },
                            onMyApplicationsClick = {
                                navController.navigate(Screen.MyApplications.route)
                            },
                            onAppointmentsClick = {
                                navController.navigate(Screen.Appointments.route)
                            },
                            onProfileClick = {
                                navController.navigate(Screen.UserProfile.route)
                            },
                            onLogoutClick = {
                                clearUserSession(context)
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.PetsList.route) { inclusive = true }
                                }
                            },
                            onQrScannerClick = {
                                navController.navigate(Screen.QrScanner.route)
                            },
                            onDonateClick = {
                                navController.navigate(Screen.Donate.route)
                            },
                            fullName = getFullName(context) ?: "User"
                        )
                    }

                    composable(
                        route = Screen.PetDetails.route,
                        arguments = listOf(navArgument("petId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val petId = backStackEntry.arguments?.getString("petId") ?: ""

                        PetDetailsScreen(
                            petId = petId,
                            onBack = { navController.popBackStack() },
                            onApply = {
                                navController.navigate(Screen.Apply.createRoute(petId))
                            }
                        )
                    }

                    composable(
                        route = Screen.Apply.route,
                        arguments = listOf(navArgument("petId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val petId = backStackEntry.arguments?.getString("petId") ?: ""

                        ApplyAdoptionScreen(
                            petId = petId,
                            onBack = { navController.popBackStack() },
                            onSubmitSuccess = {
                                navController.navigate(Screen.PetsList.route) {
                                    popUpTo(Screen.PetsList.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.MyApplications.route) {
                        MyApplicationsScreen(
                            onBack = { navController.popBackStack() },
                            onTrackClick = { applicationId ->
                                navController.navigate(Screen.ApplicationTracking.createRoute(applicationId))
                            }
                        )
                    }

                    composable(Screen.UserProfile.route) {
                        UserProfileScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.UserDetails.route) {
                        UserDetailsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Appointments.route) {
                        AppointmentsScreen(
                            onBack = { navController.popBackStack() },
                            onBookAppointmentClick = {
                                navController.navigate(Screen.BookAppointment.route)
                            }
                        )
                    }

                    composable(Screen.BookAppointment.route) {
                        BookAppointmentScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.QrScanner.route) {
                        QrScannerScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Donate.route) {
                        DonateScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        Screen.ApplicationTracking.route,
                        arguments = listOf(
                            navArgument("applicationId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
                        ApplicationTrackingScreen(
                            applicationId = applicationId,
                            onBackClick = { navController.popBackStack() }
                        )
                }
            }
            }
        }
    }
}
