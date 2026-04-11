package com.eugene.lift.ui.feature.workout.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.usecase.exercise.GetExerciseDetailUseCase
import com.eugene.lift.domain.usecase.template.GetTemplateDetailUseCase
import com.eugene.lift.domain.usecase.template.SaveTemplateUseCase
import com.eugene.lift.ui.event.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

const val MAX_TEMPLATE_NAME_LENGTH = 50

@HiltViewModel
class EditTemplateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTemplateDetailUseCase: GetTemplateDetailUseCase,
    private val saveTemplateUseCase: SaveTemplateUseCase,
    private val getExerciseDetailUseCase: GetExerciseDetailUseCase
) : ViewModel() {

    private val templateId: String? = savedStateHandle["templateId"]

    private val _name = MutableStateFlow("")
    private val _exercises = MutableStateFlow<List<TemplateExercise>>(emptyList())
    private val _isSaving = MutableStateFlow(false)
    private val _isSaveCompleted = MutableStateFlow(false)
    private val _reorderState = MutableStateFlow(ReorderUiState())

    private val _events = Channel<UiEvent>()

    val uiState: StateFlow<EditTemplateUiState> = combine(
        _name,
        _exercises,
        _isSaving,
        _isSaveCompleted,
        _reorderState
    ) { name, exercises, isSaving, isSaveCompleted, reorderState ->
        val isNameError = name.isBlank() || name.length > MAX_TEMPLATE_NAME_LENGTH
        EditTemplateUiState(
            name = name,
            exercises = exercises,
            isNameError = isNameError,
            isSaving = isSaving,
            isSaveCompleted = isSaveCompleted,
            reorderState = reorderState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EditTemplateUiState()
    )

    init {
        if (templateId != null) {
            viewModelScope.launch {
                getTemplateDetailUseCase(templateId).collect { template ->
                    template?.let {
                        _name.value = it.name
                        _exercises.value = it.exercises
                    }
                }
            }
        }
    }

    fun onEvent(event: EditTemplateUiEvent) {
        when (event) {
            is EditTemplateUiEvent.NameChanged -> updateName(event.value)
            is EditTemplateUiEvent.ExerciseRemoved -> removeExercise(event.exerciseId)
            is EditTemplateUiEvent.ExerciseConfigChanged -> updateExerciseConfig(event.exerciseId, event.sets, event.reps)
            is EditTemplateUiEvent.ExercisesSelected -> onExercisesSelected(event.exerciseIds)
            is EditTemplateUiEvent.ExerciseReplaced -> replaceExercise(event.exerciseIndex, event.newExerciseId)
            is EditTemplateUiEvent.ExercisesReordered -> reorderExercise(event.fromIndex, event.toIndex)
            EditTemplateUiEvent.ToggleReorderMode -> toggleReorderMode()
            EditTemplateUiEvent.SaveClicked -> saveTemplate()
            EditTemplateUiEvent.AddExerciseClicked,
            is EditTemplateUiEvent.ExerciseReplaceClicked -> Unit
            EditTemplateUiEvent.NavigationHandled -> _isSaveCompleted.value = false
        }
    }

    private fun toggleReorderMode() {
        val current = _reorderState.value
        _reorderState.value = current.copy(isReorderMode = !current.isReorderMode)
    }

    private fun updateName(newName: String) {
        if (newName.length <= MAX_TEMPLATE_NAME_LENGTH) {
            _name.value = newName
        }
    }

    private fun addExercise(exercise: Exercise) {
        val newTemplateExercise = TemplateExercise(
            id = UUID.randomUUID().toString(),
            exercise = exercise,
            orderIndex = _exercises.value.size,
            targetSets = 3,
            targetReps = "8-12",
            restTimerSeconds = 60
        )
        _exercises.update { it + newTemplateExercise }
    }

    private fun removeExercise(id: String) {
        _exercises.update { list -> list.filterNot { it.id == id } }
    }

    private fun reorderExercise(fromIndex: Int, toIndex: Int) {
        _exercises.update { list ->
            if (fromIndex !in list.indices || toIndex !in list.indices) return@update list
            list.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        }
    }

    private fun updateExerciseConfig(id: String, sets: String, reps: String) {
        _exercises.update { list ->
            list.map { item ->
                if (item.id == id) item.copy(
                    targetSets = sets.toIntOrNull() ?: item.targetSets,
                    targetReps = reps
                ) else item
            }
        }
    }

    private fun onExercisesSelected(exerciseIds: List<String>) {
        if (exerciseIds.isEmpty()) return
        viewModelScope.launch {
            exerciseIds.forEach { id ->
                val exercise = getExerciseDetailUseCase(id).firstOrNull()
                if (exercise != null) {
                    addExercise(exercise)
                }
            }
        }
    }

    private fun replaceExercise(exerciseIndex: Int, newExerciseId: String) {
        viewModelScope.launch {
            val replacement = getExerciseDetailUseCase(newExerciseId).firstOrNull() ?: return@launch
            _exercises.update { list ->
                if (exerciseIndex !in list.indices) return@update list
                val current = list[exerciseIndex]
                list.toMutableList().apply {
                    this[exerciseIndex] = current.copy(
                        id = UUID.randomUUID().toString(),
                        exercise = replacement
                    )
                }
            }
        }
    }

    private fun saveTemplate() {
        val currentName = _name.value
        if (currentName.isBlank() || _isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            val template = WorkoutTemplate(
                id = templateId ?: UUID.randomUUID().toString(),
                name = currentName,
                exercises = _exercises.value.mapIndexed { index, ex -> ex.copy(orderIndex = index) }
            )
            when (val result = saveTemplateUseCase(template)) {
                is AppResult.Success -> {
                    _isSaveCompleted.value = true
                }
                is AppResult.Error -> {
                    _events.send(UiEvent.ShowSnackbar(result.error))
                }
            }
            _isSaving.value = false
        }
    }
}