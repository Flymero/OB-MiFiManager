package com.flymero.mifimanager.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flymero.mifimanager.data.local.DataStoreHelper
import com.flymero.mifimanager.ui.auth.InternetAuthScreen
import com.flymero.mifimanager.ui.GlobalMessageBus
import com.flymero.mifimanager.ui.dashboard.DashboardScreen
import com.flymero.mifimanager.ui.dashboard.DashboardViewModel
import com.flymero.mifimanager.ui.dashboard.PackageDetailScreen
import com.flymero.mifimanager.ui.device.DeviceScreen
import com.flymero.mifimanager.ui.devices.DevicesScreen
import com.flymero.mifimanager.ui.login.LoginScreen
import com.flymero.mifimanager.ui.login.LoginViewModel
import com.flymero.mifimanager.ui.signal.SignalScreen
import com.flymero.mifimanager.ui.wifi.WifiScreen
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "仪表盘", Icons.Default.Dashboard)
    data object Signal : Screen("signal", "信号", Icons.Default.CellTower)
    data object Wifi : Screen("wifi", "WiFi", Icons.Default.Wifi)
    data object Devices : Screen("devices", "设备", Icons.Default.Devices)
    data object Device : Screen("device", "管理", Icons.Default.Router)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Signal,
    Screen.Wifi,
    Screen.Devices,
    Screen.Device
)

val LocalGlobalSnackbar = compositionLocalOf<SnackbarHostState> { error("No SnackbarHostState provided") }

private const val PLAN_DETAIL_ROUTE = "dashboard/plan-detail"
private const val AUTH_ROUTE = "auth"
private const val PLAN_DETAIL_TRANSITION_MS = 280

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GlobalMessageBusEntryPoint {
    fun globalMessageBus(): GlobalMessageBus
}

@Composable
fun MiFiNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentBaseRoute = currentRoute?.substringBefore("?")

    val context = androidx.compose.ui.platform.LocalContext.current
    val shouldAutoLogin = remember {
        val prefs = context.getSharedPreferences("mifi_prefs", android.content.Context.MODE_PRIVATE)
        prefs.getBoolean("remember", false) &&
            prefs.getString("username", "")?.isNotEmpty() == true &&
            prefs.getString("password", "")?.isNotEmpty() == true
    }
    val startDestination = if (shouldAutoLogin) "dashboard" else "login"

    val isLoginRoute = currentBaseRoute?.startsWith("login") == true
    val fullScreenRoutes = setOf(PLAN_DETAIL_ROUTE)
    val showBottomBar = currentBaseRoute != null && !isLoginRoute && currentBaseRoute !in fullScreenRoutes

    val globalSnackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val globalMessageBus = remember {
        EntryPointAccessors.fromApplication(context.applicationContext, GlobalMessageBusEntryPoint::class.java)
            .globalMessageBus()
    }

    LaunchedEffect(Unit) {
        globalMessageBus.messages.collect { message ->
            scope.launch { globalSnackbarHostState.showSnackbar(message) }
        }
    }

    CompositionLocalProvider(LocalGlobalSnackbar provides globalSnackbarHostState) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = globalSnackbarHostState) },
            bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = navBackStackEntry?.destination?.hierarchy?.any {
                                it.route == screen.route
                            } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                val target = targetState.destination.route?.substringBefore("?")
                val initial = initialState.destination.route?.substringBefore("?")
                when {
                    initial == "devices" && target == AUTH_ROUTE -> {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                        ) + fadeIn(tween(180, delayMillis = 40))
                    }
                    initial == AUTH_ROUTE && target == "devices" -> {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                        ) + fadeIn(tween(180, delayMillis = 40))
                    }
                    target == PLAN_DETAIL_ROUTE -> {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                        ) + fadeIn(tween(180, delayMillis = 40))
                    }
                    initial == PLAN_DETAIL_ROUTE && target == "dashboard" -> {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                        ) + fadeIn(tween(180, delayMillis = 40))
                    }
                    target == "dashboard" -> fadeIn(tween(0))
                    else -> fadeIn(tween(220))
                }
            },
            exitTransition = {
                val target = targetState.destination.route?.substringBefore("?")
                val initial = initialState.destination.route?.substringBefore("?")
                when {
                    initial == "devices" && target == AUTH_ROUTE -> {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                        ) + fadeOut(tween(140))
                    }
                    initial == AUTH_ROUTE && target == "devices" -> {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                        ) + fadeOut(tween(140))
                    }
                    target == PLAN_DETAIL_ROUTE -> {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                        ) + fadeOut(tween(140))
                    }
                    initial == PLAN_DETAIL_ROUTE && target == "dashboard" -> {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                        ) + fadeOut(tween(140))
                    }
                    target == "dashboard" -> fadeOut(tween(0))
                    else -> fadeOut(tween(180))
                }
            },
            popEnterTransition = {
                val target = targetState.destination.route?.substringBefore("?")
                val initial = initialState.destination.route?.substringBefore("?")
                if (initial == PLAN_DETAIL_ROUTE && target == "dashboard") {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                    ) + fadeIn(tween(180, delayMillis = 40))
                } else if (initial == AUTH_ROUTE && target == "devices") {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                    ) + fadeIn(tween(180, delayMillis = 40))
                } else {
                    fadeIn(tween(220))
                }
            },
            popExitTransition = {
                val target = targetState.destination.route?.substringBefore("?")
                val initial = initialState.destination.route?.substringBefore("?")
                if (initial == PLAN_DETAIL_ROUTE && target == "dashboard") {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                    ) + fadeOut(tween(140))
                } else if (initial == AUTH_ROUTE && target == "devices") {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(PLAN_DETAIL_TRANSITION_MS)
                    ) + fadeOut(tween(140))
                } else {
                    fadeOut(tween(180))
                }
            }
        ) {
            composable(
                "login?fromLogout={fromLogout}",
                arguments = listOf(navArgument("fromLogout") {
                    type = NavType.BoolType
                    defaultValue = false
                })
            ) { backStackEntry ->
                val fromLogout = backStackEntry.arguments?.getBoolean("fromLogout") ?: false
                LoginScreen(
                    fromLogout = fromLogout,
                    onLoginSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    onOpenPlanDetail = { navController.navigate(PLAN_DETAIL_ROUTE) }
                )
            }
            composable(PLAN_DETAIL_ROUTE) {
                val dashboardBackStackEntry = remember(navController) {
                    navController.getBackStackEntry("dashboard")
                }
                val dashboardViewModel: DashboardViewModel = hiltViewModel(dashboardBackStackEntry)
                PackageDetailScreen(
                    viewModel = dashboardViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("signal") { SignalScreen() }
            composable("wifi") { WifiScreen() }
            composable("devices") {
                DevicesScreen(onNavigateToAuth = { navController.navigate(AUTH_ROUTE) })
            }
            composable(AUTH_ROUTE) { InternetAuthScreen() }
            composable("device") {
                DeviceScreen(
                    onLogout = {
                        navController.navigate("login?fromLogout=true") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
}
