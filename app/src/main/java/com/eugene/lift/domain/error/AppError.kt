package com.eugene.lift.domain.error

sealed class AppError {

    // Database
    object Database : AppError()
    object Constraint : AppError()

    // Validation
    object Validation : AppError()

    // Future
    object Auth : AppError()
    object Network : AppError()

    data class Unknown(val message: String? = null) : AppError()
}
