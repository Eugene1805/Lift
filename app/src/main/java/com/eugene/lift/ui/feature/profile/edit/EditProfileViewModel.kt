package com.eugene.lift.ui.feature.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.lift.domain.model.UserProfile
import com.eugene.lift.domain.usecase.profile.GetCurrentProfileUseCase
import com.eugene.lift.domain.usecase.profile.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val profileId: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarColor: String = "#6200EE",
    val isLoading: Boolean = true
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

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
                isLoading = false
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

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            val original = originalProfile ?: return@launch

            val updatedProfile = original.copy(
                displayName = state.displayName,
                bio = state.bio.ifBlank { null },
                avatarColor = state.avatarColor
            )

            updateProfileUseCase.updateProfile(updatedProfile)
        }
    }
}
