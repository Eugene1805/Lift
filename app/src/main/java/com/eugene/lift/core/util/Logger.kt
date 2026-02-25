package com.eugene.lift.core.util

import android.util.Log

interface Logger {
    fun log(throwable: Throwable)
}

class DebugLogger : Logger {
    override fun log(throwable: Throwable) {
        Log.e("AppError", throwable.message, throwable)
    }
}
