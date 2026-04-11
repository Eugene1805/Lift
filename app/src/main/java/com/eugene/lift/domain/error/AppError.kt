package com.eugene.lift.domain.error

sealed class AppError {

    object Database : AppError()
    object Constraint : AppError()

    object Validation : AppError()
    data class Unknown(val message: String? = null) : AppError()
}
