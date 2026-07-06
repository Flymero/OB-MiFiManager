package com.flymero.mifimanager.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.ui.theme.mifiFastEffectsSpec
import com.flymero.mifimanager.ui.theme.mifiFastSpatialSpec
import com.flymero.mifimanager.ui.theme.LocalThemeControl

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    fromLogout: Boolean = false,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rechargeNo by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    val passwordFocus = remember { FocusRequester() }
    val rechargeNoFocus = remember { FocusRequester() }
    val entranceState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    LaunchedEffect(state.savedUsername, state.savedRechargeNo) {
        if (state.savedUsername.isNotEmpty()) {
            username = state.savedUsername
            password = state.savedPassword
            rememberMe = state.shouldRemember
        }
        if (state.savedRechargeNo.isNotEmpty()) {
            rechargeNo = state.savedRechargeNo
        }
    }

    LaunchedEffect(Unit) {
        if (!fromLogout) viewModel.autoLogin()
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLoginSuccess()
    }

    val themeControl = LocalThemeControl.current

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = themeControl.toggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
            )
        ) {
            Icon(
                imageVector = if (themeControl.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (themeControl.isDark) "切换到浅色模式" else "切换到深色模式"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoginEntrance(visibleState = entranceState, delayMillis = 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "MiFi Manager",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "随身WiFi管理工具",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            LoginEntrance(visibleState = entranceState, delayMillis = 80) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() })
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LoginEntrance(visibleState = entranceState, delayMillis = 130) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth().focusRequester(passwordFocus),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { rechargeNoFocus.requestFocus() }),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Crossfade(targetState = passwordVisible, animationSpec = mifiFastEffectsSpec(), label = "password-icon") { visible ->
                                Icon(
                                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LoginEntrance(visibleState = entranceState, delayMillis = 180) {
                OutlinedTextField(
                    value = rechargeNo,
                    onValueChange = { rechargeNo = it },
                    label = { Text("充值号（用于套餐查询）") },
                    modifier = Modifier.fillMaxWidth().focusRequester(rechargeNoFocus),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (username.isNotEmpty() && password.isNotEmpty()) {
                            viewModel.login(username, password, rechargeNo, rememberMe)
                        }
                    })
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LoginEntrance(visibleState = entranceState, delayMillis = 230) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text(
                        text = "记住密码",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn(mifiFastEffectsSpec()) + slideInVertically(mifiFastSpatialSpec()) { -it / 3 },
                exit = fadeOut(mifiFastEffectsSpec()) + slideOutVertically(mifiFastSpatialSpec()) { -it / 3 }
            ) {
                Text(
                    text = state.error.orEmpty(),
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(username, password, rechargeNo, rememberMe) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.large,
                enabled = !state.isLoading && username.isNotEmpty() && password.isNotEmpty()
            ) {
                Crossfade(targetState = state.isLoading, animationSpec = mifiFastEffectsSpec(), label = "login-button") { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("登录", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginEntrance(
    visibleState: MutableTransitionState<Boolean>,
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(durationMillis = 260, delayMillis = delayMillis)) +
            slideInVertically(tween(durationMillis = 260, delayMillis = delayMillis)) { it / 4 },
        exit = fadeOut(tween(120)),
        content = { content() }
    )
}
