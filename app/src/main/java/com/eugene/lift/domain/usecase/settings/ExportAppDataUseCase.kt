package com.eugene.lift.domain.usecase.settings

import com.eugene.lift.domain.repository.AppDataTransferRepository
import javax.inject.Inject

class ExportAppDataUseCase @Inject constructor(
    private val repository: AppDataTransferRepository
) {
    suspend operator fun invoke(): String = repository.exportToJson()
}
