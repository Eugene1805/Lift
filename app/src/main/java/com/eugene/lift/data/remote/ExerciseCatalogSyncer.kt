package com.eugene.lift.data.remote

interface ExerciseCatalogSyncer {
    suspend fun syncExercises(): Int
}
