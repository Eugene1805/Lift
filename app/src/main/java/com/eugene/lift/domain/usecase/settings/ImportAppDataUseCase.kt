package com.eugene.lift.domain.usecase.settings

import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.repository.AppDataTransferRepository
import javax.inject.Inject

class ImportAppDataUseCase @Inject constructor(
    private val repository: AppDataTransferRepository
) {
    suspend operator fun invoke(json: String): AppResult<Unit> = repository.importFromJson(json)
}
