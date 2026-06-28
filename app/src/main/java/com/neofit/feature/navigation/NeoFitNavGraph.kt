package com.neofit.feature.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neofit.R
import com.neofit.feature.coach.CoachScreen
import com.neofit.feature.dashboard.DashboardScreen
import com.neofit.feature.exercise.ExerciseDetailScreen
import com.neofit.feature.exercise.ExercisePlansScreen
import com.neofit.feature.foodlog.AddMealScreen
import com.neofit.feature.foodlog.FoodLogScreen
import com.neofit.feature.foodlog.MealDetailScreen
import com.neofit.feature.foodlog.MealSearchScreen
import com.neofit.feature.foodlog.PhotoFoodLogScreen
import com.neofit.feature.insights.InsightsScreen
import com.neofit.feature.onboarding.OnboardingScreen
import com.neofit.feature.profile.ProfileScreen
import com.neofit.feature.profile.RegionPreferencesScreen
import com.neofit.feature.progress.ProgressScreen
import com.neofit.feature.progress.WeightHistoryScreen

private data class TabSpec(val tab: BottomTab, val icon: ImageVector, val labelRes: Int)

private val tabs = listOf(
    TabSpec(BottomTab.HOME, Icons.Filled.Home, R.string.nav_home),
    TabSpec(BottomTab.FOOD, Icons.Filled.Restaurant, R.string.nav_food),
    TabSpec(BottomTab.COACH, Icons.Filled.Psychology, R.string.nav_coach),
    TabSpec(BottomTab.EXERCISE, Icons.Filled.FitnessCenter, R.string.nav_exercise),
    TabSpec(BottomTab.PROGRESS, Icons.AutoMirrored.Filled.TrendingUp, R.string.nav_progress),
    TabSpec(BottomTab.PROFILE, Icons.Filled.Person, R.string.nav_profile),
)

@Composable
fun NeoFitNavGraph(startDestination: String) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = tabs.any { it.tab.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { spec ->
                        NavigationBarItem(
                            selected = currentRoute == spec.tab.route,
                            onClick = {
                                if (currentRoute != spec.tab.route) {
                                    navController.navigate(spec.tab.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(spec.icon, contentDescription = null) },
                            label = { Text(stringResource(spec.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier,
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.HOME) {
                DashboardScreen(contentPadding = padding, onNavigate = { navController.navigate(it) })
            }

            composable(Routes.COACH) {
                CoachScreen(contentPadding = padding)
            }

            composable(Routes.FOOD_LOG) {
                FoodLogScreen(
                    contentPadding = padding,
                    onSearch = { navController.navigate(Routes.FOOD_SEARCH) },
                    onPhoto = { navController.navigate(Routes.FOOD_PHOTO) },
                    onOpenMeal = { navController.navigate(Routes.mealDetail(it)) },
                )
            }

            composable(
                route = "${Routes.FOOD_ADD}?foodId={foodId}&mealId={mealId}",
                arguments = listOf(
                    navArgument("foodId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("mealId") { type = NavType.LongType; defaultValue = -1L },
                ),
            ) { entry ->
                AddMealScreen(
                    onDone = { navController.popBackStack(Routes.FOOD_LOG, inclusive = false) },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.FOOD_SEARCH) {
                MealSearchScreen(
                    onPick = { foodId ->
                        navController.navigate(Routes.foodAdd(foodId)) {
                            popUpTo(Routes.FOOD_SEARCH) { inclusive = true }
                        }
                    },
                    onAddCustom = {
                        navController.navigate(Routes.foodAdd()) {
                            popUpTo(Routes.FOOD_SEARCH) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.FOOD_PHOTO) {
                PhotoFoodLogScreen(
                    onConfirm = { foodId ->
                        navController.navigate(Routes.foodAdd(foodId)) {
                            popUpTo(Routes.FOOD_PHOTO) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = "${Routes.MEAL_DETAIL}/{mealId}",
                arguments = listOf(navArgument("mealId") { type = NavType.LongType }),
            ) { entry ->
                MealDetailScreen(
                    mealId = entry.arguments?.getLong("mealId") ?: 0L,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.foodEdit(it)) },
                )
            }

            composable(Routes.EXERCISE_PLANS) {
                ExercisePlansScreen(
                    contentPadding = padding,
                    onOpenPlan = { navController.navigate(Routes.exerciseDetail(it)) },
                )
            }

            composable(
                route = "${Routes.EXERCISE_DETAIL}/{planId}",
                arguments = listOf(navArgument("planId") { type = NavType.StringType }),
            ) { entry ->
                ExerciseDetailScreen(
                    planId = entry.arguments?.getString("planId").orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.PROGRESS) {
                ProgressScreen(
                    contentPadding = padding,
                    onOpenWeight = { navController.navigate(Routes.WEIGHT_HISTORY) },
                    onOpenInsights = { navController.navigate(Routes.INSIGHTS) },
                )
            }

            composable(Routes.WEIGHT_HISTORY) {
                WeightHistoryScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    contentPadding = padding,
                    onOpenRegionPrefs = { navController.navigate(Routes.REGION_PREFS) },
                    onOpenInsights = { navController.navigate(Routes.INSIGHTS) },
                )
            }

            composable(Routes.REGION_PREFS) {
                RegionPreferencesScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.INSIGHTS) {
                InsightsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
