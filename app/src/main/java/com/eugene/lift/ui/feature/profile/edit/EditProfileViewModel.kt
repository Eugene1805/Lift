package com.eugene.lift.ui.feature.profile.edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.model.UserProfile
import com.eugene.lift.domain.usecase.profile.GenerateUsernameSuggestionsUseCase
import com.eugene.lift.domain.usecase.profile.GetCurrentProfileUseCase
import com.eugene.lift.domain.usecase.profile.UpdateProfileUseCase
import com.eugene.lift.domain.usecase.profile.UpdateUsernameUseCase
import com.eugene.lift.domain.usecase.profile.UploadAvatarUseCase
import com.eugene.lift.ui.event.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val profileId: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarColor: String = "#6200EE",
    val avatarUrl: String? = null,
    val isLoading: Boolean = true,
    val isUploadingAvatar: Boolean = false,
    // Username-specific state
    val usernameError: String? = null,
    val usernameSuggestions: List<String> = emptyList()
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updateUsernameUseCase: UpdateUsernameUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val generateUsernameSuggestionsUseCase: GenerateUsernameSuggestionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>()
    val events = _events.receiveAsFlow()

    private var originalProfile: UserProfile? = null

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = getCurrentProfileUseCase.getOrCreate()
            originalProfile = profile
            _uiState.value = EditProfileUiState(
                profileId = profile.id,
                username = profile.username,
                displayName = profile.displayName,
                bio = profile.bio ?: "",
                avatarColor = profile.avatarColor,
                avatarUrl = profile.avatarUrl,
                isLoading = false,
                usernameSuggestions = generateUsernameSuggestionsUseCase()
            )
        }
    }

    fun updateDisplayName(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name)
    }

    fun updateBio(bio: String) {
        _uiState.value = _uiState.value.copy(bio = bio)
    }

    fun updateAvatarColor(color: String) {
        _uiState.value = _uiState.value.copy(avatarColor = color)
    }

    fun updateUsername(username: String) {
        val error = updateUsernameUseCase.validate(username)
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = if (error != null) "3–30 chars, lowercase letters, digits and _ only (no leading/trailing _)" else null
        )
    }

    fun applySuggestion(suggestion: String) {
        _uiState.value = _uiState.value.copy(username = suggestion, usernameError = null)
    }

    fun refreshSuggestions() {
        _uiState.value = _uiState.value.copy(
            usernameSuggestions = generateUsernameSuggestionsUseCase()
        )
    }

    fun uploadAvatar(uri: Uri) {
        val state = _uiState.value
        if (state.isUploadingAvatar) return
        _uiState.value = state.copy(isUploadingAvatar = true)

        viewModelScope.launch {
            val result = uploadAvatarUseCase(
                profileId = state.profileId,
                sourceUri = uri,
                oldAvatarPath = state.avatarUrl
            )
            when (result) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    avatarUrl = result.data,
                    isUploadingAvatar = false
                )
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(isUploadingAvatar = false)
                    _events.send(UiEvent.ShowSnackbar(result.error))
                }
            }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            val original = originalProfile ?: return@launch

            // Save display name / bio / color / avatarUrl via the bulk update
            val updatedProfile = original.copy(
                displayName = state.displayName,
                bio = state.bio.ifBlank { null },
                avatarColor = state.avatarColor,
                avatarUrl = state.avatarUrl
            )
            when (val result = updateProfileUseCase.updateProfile(updatedProfile)) {
                is AppResult.Success -> Unit
                is AppResult.Error -> {
                    _events.send(UiEvent.ShowSnackbar(result.error))
                    return@launch
                }
            }

            // Save username only if it changed and is valid
            if (state.username != original.username) {
                when (val result = updateUsernameUseCase(state.profileId, state.username)) {
                    is AppResult.Success -> Unit
                    is AppResult.Error -> _events.send(UiEvent.ShowSnackbar(result.error))
                }
            }
        }
    }
}
