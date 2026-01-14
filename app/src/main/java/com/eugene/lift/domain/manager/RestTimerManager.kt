package com.eugene.lift.domain.manager

import com.eugene.lift.domain.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestTimerManager @Inject constructor() {

    private val _timerState = MutableStateFlow(TimerState())
    val timerState = _timerState.asStateFlow()

    private var timerJob: Job? = null
    // Scope propio para que el timer siga vivo aunque el ViewModel muera (si cambias de pantalla)
    private val scope = CoroutineScope(Dispatchers.Main)

    fun startTimer(seconds: Long) {
        stopTimer() // Reiniciar si ya existía uno

        _timerState.value = TimerState(
            isRunning = true,
            timeRemainingSeconds = seconds,
            totalTimeSeconds = seconds,
            progress = 1f
        )

        timerJob = scope.launch {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + (seconds * 1000)

            while (System.currentTimeMillis() < endTime) {
                val millisRemaining = endTime - System.currentTimeMillis()
                val secRemaining = (millisRemaining / 1000) + 1 // +1 para no mostrar 0 antes de tiempo

                _timerState.update {
                    it.copy(
                        timeRemainingSeconds = secRemaining,
                        progress = secRemaining.toFloat() / seconds.toFloat()
                    )
                }
                delay(100) // Actualizamos cada 100ms para suavidad en UI si quisieras milisegundos
            }

            // Fin del timer
            stopTimer()
            // Aquí podrías disparar un sonido o vibración en el futuro
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerState.update {
            it.copy(isRunning = false, timeRemainingSeconds = 0, progress = 0f)
        }
    }

    fun addTime(seconds: Long) {
        if (!_timerState.value.isRunning) return

        // Cancelamos el job actual y reiniciamos con el nuevo tiempo
        val currentRemaining = _timerState.value.timeRemainingSeconds
        val newTotal = currentRemaining + seconds
        startTimer(newTotal)
    }
}