package com.eugene.lift.ui.event

import com.eugene.lift.domain.error.AppError

sealed class UiEvent {
    data class ShowSnackbar(val error: AppError) : UiEvent()
}
