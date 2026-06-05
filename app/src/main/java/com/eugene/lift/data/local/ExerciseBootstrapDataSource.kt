package com.eugene.lift.data.local

interface ExerciseBootstrapDataSource {
    suspend fun populateIfEmpty()
}
