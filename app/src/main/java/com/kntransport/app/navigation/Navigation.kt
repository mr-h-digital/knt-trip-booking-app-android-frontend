package com.kntransport.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kntransport.app.ui.screens.*

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
    const val NOTIFICATIONS   = "notifications"
    const val PROFILE         = "profile"
    const val APPEARANCE      = "appearance"
    const val EDIT_PROFILE    = "edit_profile"
    const val RATE_TRIP       = "rate_trip/{tripId}"

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
                onLogin         = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSignUp        = { navController.navigate(Routes.SIGN_UP) },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
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
            NotificationsScreen(onBack = { navController.popBackStack() })
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
    }
}
