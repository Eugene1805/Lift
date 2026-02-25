package com.eugene.lift.ui.util

import com.eugene.lift.domain.error.AppError

fun AppError.toMessage(): String {
    return when (this) {
        AppError.Database -> "A database error occurred. Please try again."
        AppError.Constraint -> "This item already exists."
        AppError.Validation -> "Invalid input. Please check your data."
        AppError.Auth -> "Authentication failed."
        AppError.Network -> "No internet connection."
        is AppError.Unknown -> this.message ?: "An unexpected error occurred."
    }
}
