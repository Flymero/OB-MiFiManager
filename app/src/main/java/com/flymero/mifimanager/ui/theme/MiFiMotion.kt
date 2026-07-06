package com.flymero.mifimanager.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun <T> mifiFastEffectsSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.fastEffectsSpec()

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun <T> mifiDefaultEffectsSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.defaultEffectsSpec()

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun <T> mifiFastSpatialSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.fastSpatialSpec()

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun <T> mifiDefaultSpatialSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.defaultSpatialSpec()

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun <T> mifiSlowSpatialSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.slowSpatialSpec()
