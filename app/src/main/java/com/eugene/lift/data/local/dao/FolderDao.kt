package com.eugene.lift.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.eugene.lift.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM workout_folders ORDER BY createdAt DESC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Query("DELETE FROM workout_folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: String)
}