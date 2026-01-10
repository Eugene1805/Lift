package com.eugene.lift.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object ProfileRoute
@Serializable object HistoryRoute
@Serializable object WorkoutRoute
@Serializable object ExerciseListRoute
@Serializable object SettingsRoute
@Serializable data class ExerciseAddRoute(val exerciseId: String? = null)
@Serializable data class ExerciseDetailRoute(val exerciseId: String)
@Serializable data class TemplateEditRoute(val templateId: String? = null)
@Serializable object ExercisePickerRoute