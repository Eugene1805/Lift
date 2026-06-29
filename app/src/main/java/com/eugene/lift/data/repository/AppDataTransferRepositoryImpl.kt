package com.eugene.lift.data.repository

import com.eugene.lift.data.backup.AppBackupLocalStore
import com.eugene.lift.data.backup.AppDataBackupPayload
import com.eugene.lift.data.backup.RoomAppBackupLocalStore
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.repository.AppDataTransferRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataTransferRepositoryImpl @Inject constructor(
    private val localStore: AppBackupLocalStore
) : AppDataTransferRepository {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override suspend fun exportToJson(): String {
        return json.encodeToString(AppDataBackupPayload.serializer(), localStore.exportPayload())
    }

    override suspend fun importFromJson(json: String): AppResult<Unit> {
        return try {
            val payload = this.json.decodeFromString(AppDataBackupPayload.serializer(), json)
            if (payload.schemaVersion != RoomAppBackupLocalStore.BACKUP_SCHEMA_VERSION) {
                AppResult.Error(AppError.Validation)
            } else {
                localStore.importPayload(payload)
                AppResult.Success(Unit)
            }
        } catch (_: IllegalArgumentException) {
            AppResult.Error(AppError.Validation)
        } catch (_: IllegalStateException) {
            AppResult.Error(AppError.Validation)
        } catch (_: kotlinx.serialization.SerializationException) {
            AppResult.Error(AppError.Validation)
        } catch (error: Exception) {
            AppResult.Error(AppError.Unknown(error.message))
        }
    }
}
