package com.eugene.lift.domain.repository

import com.eugene.lift.domain.error.AppResult

interface AppDataTransferRepository {
    suspend fun exportToJson(): String
    suspend fun importFromJson(json: String): AppResult<Unit>
}
