package com.kntransport.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.data.AppNotification
import com.kntransport.app.data.SampleData
import com.kntransport.app.data.UserRole
import com.kntransport.app.network.UserDto
import com.kntransport.app.network.VehicleDto
import com.kntransport.app.ui.screens.*
import com.kntransport.app.viewmodel.AdminViewModel

object Routes {
    const val ONBOARDING      = "onboarding"
    const val SPLASH          = "splash"
    const val LOGIN           = "login"
    const val SIGN_UP         = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME            = "home"
    const val BOOK_TRIP       = "book_trip"
    const val MY_TRIPS        = "my_trips"
    const val TRIP_DETAIL     = "trip_detail/{tripId}"
    const val LIFT_CLUBS      = "lift_clubs"
    const val LIFT_CLUB_DETAIL= "lift_club_detail/{clubId}"
    const val CREATE_LIFT_CLUB= "create_lift_club"
    const val QUOTE_REVIEW    = "quote_review/{quoteId}/{type}"
    const val NOTIFICATIONS        = "notifications"
    const val NOTIFICATION_DETAIL = "notification_detail"
    const val PROFILE         = "profile"
    const val APPEARANCE      = "appearance"
    const val EDIT_PROFILE    = "edit_profile"
    const val RATE_TRIP       = "rate_trip/{tripId}"

    // Driver
    const val DRIVER_DASHBOARD    = "driver_dashboard"
    const val DRIVER_TRIPS        = "driver_trips"
    const val DRIVER_TRIP_DETAIL  = "driver_trip_detail/{tripId}"
    const val DRIVER_EARNINGS     = "driver_earnings"

    fun driverTripDetail(id: String) = "driver_trip_detail/$id"

    // Admin
    const val ADMIN_DASHBOARD      = "admin_dashboard"
    const val ADMIN_USERS          = "admin_users"
    const val ADMIN_USER_DETAIL    = "admin_user_detail"
    const val ADMIN_CREATE_DRIVER  = "admin_create_driver"
    const val ADMIN_ANALYTICS      = "admin_analytics"
    const val ADMIN_FINANCIALS     = "admin_financials"
    const val ADMIN_FLEET          = "admin_fleet"
    const val ADMIN_VEHICLE_DETAIL = "admin_vehicle_detail"
    const val ADMIN_ADD_VEHICLE    = "admin_add_vehicle"
    const val ADMIN_EDIT_VEHICLE   = "admin_edit_vehicle"
    const val ADMIN_TRIPS          = "admin_trips"

    fun rateTrip(id: String) = "rate_trip/$id"

    fun tripDetail(id: String) = "trip_detail/$id"
    fun liftClubDetail(id: String) = "lift_club_detail/$id"
    fun quoteReview(id: String, type: String) = "quote_review/$id/$type"
}

@Composable
fun KntNavHost(
    navController    : NavHostController,
    showOnboarding   : Boolean = false,
    onOnboardingDone : () -> Unit = {},
) {
    val adminViewModel: AdminViewModel = viewModel()
    // Holds selected objects so detail screens can read them without serialising into the route.
    var selectedNotification by remember { mutableStateOf<AppNotification?>(null) }
    var selectedAdminUser    by remember { mutableStateOf<UserDto?>(null) }
    var selectedVehicle      by remember { mutableStateOf<VehicleDto?>(null) }
    val start = if (showOnboarding) Routes.ONBOARDING else Routes.SPLASH
    NavHost(
        navController    = navController,
        startDestination = start,
        enterTransition  = { slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300)) },
        exitTransition   = { slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(300)) },
        popEnterTransition  = { slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300)) },
        popExitTransition   = { slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300)) },
    ) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(onFinished = {
                onOnboardingDone()
                navController.navigate(Routes.SPLASH) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { role ->
                    val dest = when (role.uppercase()) {
                        "ADMIN"  -> Routes.ADMIN_DASHBOARD
                        "DRIVER" -> Routes.DRIVER_DASHBOARD
                        else     -> Routes.HOME
                    }
                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSignUp         = { navController.navigate(Routes.SIGN_UP) },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onDemoLogin      = { role ->
                    SampleData.currentUser = when (role) {
                        UserRole.DRIVER -> SampleData.driverUser
                        UserRole.ADMIN  -> SampleData.adminUser
                        else            -> SampleData.currentUser
                    }
                    val dest = when (role) {
                        UserRole.ADMIN  -> Routes.ADMIN_DASHBOARD
                        UserRole.DRIVER -> Routes.DRIVER_DASHBOARD
                        else            -> Routes.HOME
                    }
                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onSignedUp = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onLogin = { navController.popBackStack() },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onBookTrip        = { navController.navigate(Routes.BOOK_TRIP) },
                onMyTrips         = { navController.navigate(Routes.MY_TRIPS) },
                onLiftClubs       = { navController.navigate(Routes.LIFT_CLUBS) },
                onLiftClubDetail  = { id -> navController.navigate(Routes.liftClubDetail(id)) },
                onNotifications   = { navController.navigate(Routes.NOTIFICATIONS) },
                onProfile         = { navController.navigate(Routes.PROFILE) },
                onTripDetail      = { id -> navController.navigate(Routes.tripDetail(id)) },
            )
        }

        composable(Routes.BOOK_TRIP) {
            BookTripScreen(
                onBack      = { navController.popBackStack() },
                onSubmitted = { navController.navigate(Routes.MY_TRIPS) { popUpTo(Routes.HOME) } },
            )
        }

        composable(Routes.MY_TRIPS) {
            MyTripsScreen(
                onBack       = { navController.popBackStack() },
                onTripDetail = { id -> navController.navigate(Routes.tripDetail(id)) },
                onBookTrip   = { navController.navigate(Routes.BOOK_TRIP) },
            )
        }

        composable(
            route = Routes.TRIP_DETAIL,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType }),
        ) { back ->
            val tripId = back.arguments?.getString("tripId") ?: return@composable
            TripDetailScreen(
                tripId       = tripId,
                onBack       = { navController.popBackStack() },
                onQuoteReview= { qId -> navController.navigate(Routes.quoteReview(qId, "TRIP")) },
                onRateTrip   = { id -> navController.navigate(Routes.rateTrip(id)) },
            )
        }

        composable(
            route = Routes.RATE_TRIP,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType }),
        ) { back ->
            val tripId = back.arguments?.getString("tripId") ?: return@composable
            RateTripScreen(
                tripId  = tripId,
                onDone  = { navController.popBackStack() },
            )
        }

        composable(Routes.LIFT_CLUBS) {
            LiftClubsScreen(
                onBack        = { navController.popBackStack() },
                onClubDetail  = { id -> navController.navigate(Routes.liftClubDetail(id)) },
                onCreateClub  = { navController.navigate(Routes.CREATE_LIFT_CLUB) },
            )
        }

        composable(
            route = Routes.LIFT_CLUB_DETAIL,
            arguments = listOf(navArgument("clubId") { type = NavType.StringType }),
        ) { back ->
            val clubId = back.arguments?.getString("clubId") ?: return@composable
            LiftClubDetailScreen(
                clubId       = clubId,
                onBack       = { navController.popBackStack() },
                onQuoteReview= { qId -> navController.navigate(Routes.quoteReview(qId, "LIFT_CLUB")) },
            )
        }

        composable(Routes.CREATE_LIFT_CLUB) {
            CreateLiftClubScreen(
                onBack      = { navController.popBackStack() },
                onSubmitted = { navController.navigate(Routes.LIFT_CLUBS) { popUpTo(Routes.HOME) } },
            )
        }

        composable(
            route = Routes.QUOTE_REVIEW,
            arguments = listOf(
                navArgument("quoteId") { type = NavType.StringType },
                navArgument("type")    { type = NavType.StringType },
            ),
        ) { back ->
            val quoteId = back.arguments?.getString("quoteId") ?: return@composable
            val type    = back.arguments?.getString("type") ?: "TRIP"
            QuoteReviewScreen(
                quoteId = quoteId,
                type    = type,
                onBack  = { navController.popBackStack() },
                onDone  = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack              = { navController.popBackStack() },
                onNotificationClick = { notif ->
                    selectedNotification = notif
                    navController.navigate(Routes.NOTIFICATION_DETAIL)
                },
            )
        }

        composable(Routes.NOTIFICATION_DETAIL) {
            val notif = selectedNotification
            if (notif == null) {
                navController.popBackStack()
                return@composable
            }
            NotificationDetailScreen(
                notification   = notif,
                onBack         = { navController.popBackStack() },
                onViewTrip     = { id ->
                    navController.navigate(Routes.tripDetail(id)) {
                        popUpTo(Routes.NOTIFICATIONS)
                    }
                },
                onViewLiftClub = { id ->
                    navController.navigate(Routes.liftClubDetail(id)) {
                        popUpTo(Routes.NOTIFICATIONS)
                    }
                },
                onReviewQuote  = { id ->
                    navController.navigate(Routes.quoteReview(id, notif.referenceType ?: "TRIP")) {
                        popUpTo(Routes.NOTIFICATIONS)
                    }
                },
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack        = { navController.popBackStack() },
                onSignOut     = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAppearance  = { navController.navigate(Routes.APPEARANCE) },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
            )
        }

        composable(Routes.APPEARANCE) {
            AppearanceScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                onBack  = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        // ── Driver ────────────────────────────────────────────────────────────

        composable(Routes.DRIVER_DASHBOARD) {
            DriverDashboardScreen(
                onBack         = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onMyTrips      = { navController.navigate(Routes.DRIVER_TRIPS) },
                onEarnings     = { navController.navigate(Routes.DRIVER_EARNINGS) },
                onProfile      = { navController.navigate(Routes.PROFILE) },
                onNotifications= { navController.navigate(Routes.NOTIFICATIONS) },
                onTripDetail   = { id -> navController.navigate(Routes.driverTripDetail(id)) },
            )
        }

        composable(Routes.DRIVER_TRIPS) {
            DriverTripsScreen(
                onBack       = { navController.popBackStack() },
                onTripDetail = { id -> navController.navigate(Routes.driverTripDetail(id)) },
            )
        }

        composable(
            route     = Routes.DRIVER_TRIP_DETAIL,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType }),
        ) { back ->
            val tripId = back.arguments?.getString("tripId") ?: return@composable
            DriverTripDetailScreen(
                tripId = tripId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.DRIVER_EARNINGS) {
            DriverEarningsScreen(onBack = { navController.popBackStack() })
        }

        // ── Admin ─────────────────────────────────────────────────────────────

        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onBack       = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onUsers      = { navController.navigate(Routes.ADMIN_USERS) },
                onTrips      = { navController.navigate(Routes.ADMIN_TRIPS) },
                onAnalytics  = { navController.navigate(Routes.ADMIN_ANALYTICS) },
                onFinancials = { navController.navigate(Routes.ADMIN_FINANCIALS) },
                onFleet      = { navController.navigate(Routes.ADMIN_FLEET) },
                onProfile    = { navController.navigate(Routes.PROFILE) },
                viewModel    = adminViewModel,
            )
        }

        composable(Routes.ADMIN_USERS) {
            AdminUsersScreen(
                onBack         = { navController.popBackStack() },
                onCreateDriver = { navController.navigate(Routes.ADMIN_CREATE_DRIVER) },
                onEditUser     = { userId ->
                    selectedAdminUser = (adminViewModel.users.value as? com.kntransport.app.network.ApiResult.Success)
                        ?.data?.find { it.id == userId }
                    navController.navigate(Routes.ADMIN_USER_DETAIL)
                },
                viewModel      = adminViewModel,
            )
        }

        composable(Routes.ADMIN_USER_DETAIL) {
            val user = selectedAdminUser
            if (user == null) {
                navController.popBackStack()
                return@composable
            }
            AdminUserDetailScreen(
                user      = user,
                onBack    = { navController.popBackStack() },
                onEdit    = { navController.navigate(Routes.ADMIN_CREATE_DRIVER) },
                viewModel = adminViewModel,
            )
        }

        composable(Routes.ADMIN_CREATE_DRIVER) {
            AdminCreateDriverScreen(
                onBack    = { navController.popBackStack() },
                onCreated = {
                    navController.navigate(Routes.ADMIN_USERS) {
                        popUpTo(Routes.ADMIN_USERS) { inclusive = true }
                    }
                },
                viewModel = adminViewModel,
            )
        }

        composable(Routes.ADMIN_ANALYTICS) {
            AdminAnalyticsScreen(onBack = { navController.popBackStack() }, viewModel = adminViewModel)
        }

        composable(Routes.ADMIN_FINANCIALS) {
            AdminFinancialsScreen(onBack = { navController.popBackStack() }, viewModel = adminViewModel)
        }

        composable(Routes.ADMIN_FLEET) {
            AdminFleetScreen(
                onBack          = { navController.popBackStack() },
                onVehicleDetail = { vehicleId ->
                    selectedVehicle = (adminViewModel.vehicles.value as? com.kntransport.app.network.ApiResult.Success)
                        ?.data?.find { it.id == vehicleId }
                    navController.navigate(Routes.ADMIN_VEHICLE_DETAIL)
                },
                onAddVehicle    = { navController.navigate(Routes.ADMIN_ADD_VEHICLE) },
                viewModel       = adminViewModel,
            )
        }

        composable(Routes.ADMIN_VEHICLE_DETAIL) {
            val vehicle = selectedVehicle
            if (vehicle == null) {
                navController.popBackStack()
                return@composable
            }
            AdminVehicleDetailScreen(
                vehicle   = vehicle,
                onBack    = { navController.popBackStack() },
                onEdit    = { navController.navigate(Routes.ADMIN_EDIT_VEHICLE) },
                viewModel = adminViewModel,
            )
        }

        composable(Routes.ADMIN_EDIT_VEHICLE) {
            val vehicle = selectedVehicle
            if (vehicle == null) {
                navController.popBackStack()
                return@composable
            }
            AdminEditVehicleScreen(
                vehicle   = vehicle,
                onBack    = { navController.popBackStack() },
                onSaved   = {
                    navController.popBackStack()
                    adminViewModel.loadVehicles()
                },
                viewModel = adminViewModel,
            )
        }

        composable(Routes.ADMIN_ADD_VEHICLE) {
            AdminAddVehicleScreen(
                onBack    = { navController.popBackStack() },
                onAdded   = {
                    navController.navigate(Routes.ADMIN_FLEET) {
                        popUpTo(Routes.ADMIN_FLEET) { inclusive = true }
                    }
                },
                viewModel = adminViewModel,
            )
        }

        composable(Routes.ADMIN_TRIPS) {
            AdminTripsScreen(
                onBack    = { navController.popBackStack() },
                viewModel = adminViewModel,
            )
        }
    }
}
