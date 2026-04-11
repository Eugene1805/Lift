package com.eugene.lift.ui.util

import android.content.Context
import com.eugene.lift.R
import com.eugene.lift.domain.error.AppError

fun AppError.toMessage(context: Context): String {
    val resId = when (this) {
        AppError.Database -> R.string.error_database
        AppError.Constraint -> R.string.error_constraint
        AppError.Validation -> R.string.error_validation
        is AppError.Unknown -> null
    }

    return resId?.let(context::getString)
        ?: (this as? AppError.Unknown)?.message
        ?: context.getString(R.string.error_unexpected)
}

/**
 * Fallback for contexts where Android resources are not available.
 * Prefer [toMessage] with Context whenever possible.
 */
fun AppError.toMessage(): String {
    return when (this) {
        AppError.Database -> "Database error"
        AppError.Constraint -> "Constraint error"
        AppError.Validation -> "Validation error"
        is AppError.Unknown -> this.message ?: "Unexpected error"
    }
}
