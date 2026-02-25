package com.eugene.lift.core.util

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult

class SafeExecutor(
    private val logger: Logger? = null
) {

    suspend fun <T> execute(
        block: suspend () -> T
    ): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (e: SQLiteConstraintException) {
            logger?.log(e)
            AppResult.Error(AppError.Constraint)
        } catch (e: SQLiteException) {
            logger?.log(e)
            AppResult.Error(AppError.Database)
        } catch (e: IllegalStateException) {
            logger?.log(e)
            AppResult.Error(AppError.Validation)
        } catch (e: IllegalArgumentException) {
            logger?.log(e)
            AppResult.Error(AppError.Validation)
        } catch (e: Exception) {
            logger?.log(e)
            AppResult.Error(AppError.Unknown(e.message))
        }
    }
}
