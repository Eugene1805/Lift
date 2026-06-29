package com.eugene.lift.data.backup

interface AppBackupLocalStore {
    suspend fun exportPayload(): AppDataBackupPayload
    suspend fun importPayload(payload: AppDataBackupPayload)
}
