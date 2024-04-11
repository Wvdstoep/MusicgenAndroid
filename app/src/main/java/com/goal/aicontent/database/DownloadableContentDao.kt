package com.goal.aicontent.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DownloadableContentDao {
    @Query("SELECT * FROM DownloadableContentEntity")
    fun getAll(): List<DownloadableContentEntity>

    @Query("SELECT * FROM DownloadableContentEntity WHERE status = 'PROCESSING' LIMIT 1")
    suspend fun getTemporaryTask(): DownloadableContentEntity?

    @Query("UPDATE DownloadableContentEntity SET status = :status WHERE taskId = :taskId")
    suspend fun updateStatusByTaskId(taskId: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: DownloadableContentEntity)


    @Query("SELECT * FROM DownloadableContentEntity WHERE taskId = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: String): DownloadableContentEntity?

    @Query("DELETE FROM DownloadableContentEntity WHERE title = :title")
    suspend fun deleteByTitle(title: String)

    @Query("SELECT * FROM DownloadableContentEntity WHERE status = :status")
    suspend fun getTasksByStatus(status: String): List<DownloadableContentEntity>

    @Query("UPDATE DownloadableContentEntity SET status = :status, downloadUrl = :downloadUrl WHERE taskId = :taskId")
    suspend fun updateTaskStatusAndDownloadUrl(taskId: String, status: String, downloadUrl: String?)

    @Query("UPDATE DownloadableContentEntity SET status = :status, filePath = :filePath WHERE taskId = :taskId")
    suspend fun updateDownloadStatusAndPath(taskId: String, status: String, filePath: String)

    @Query("SELECT * FROM DownloadableContentEntity WHERE title = :title LIMIT 1")
    suspend fun findDownloadableContentByTitle(title: String): DownloadableContentEntity?

    @Delete
    suspend fun delete(content: DownloadableContentEntity)
    @Update
    suspend fun updateDownloadableContent(content: DownloadableContentEntity)

}
