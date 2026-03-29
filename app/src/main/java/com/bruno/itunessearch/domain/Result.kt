package com.bruno.itunessearch.domain

import java.io.IOException
import retrofit2.HttpException

/**
 * Represents the outcome of an operation that can fail.
 * Every repository method returns Result<T> — no exception ever reaches the UI.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>

    sealed interface Failure : Result<Nothing> {
        data object Network : Failure
        data class Api(val code: Int, val message: String) : Failure
        data object Unknown : Failure
    }
}

/**
 * Wraps a suspend block in try/catch and maps exceptions to Result.Failure.
 * Used in repository implementations to guarantee no exception leaks.
 * All failures are logged for debugging.
 */
suspend fun <T> safeApiCall(tag: String = "API", block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: IOException) {
        AppLogger.error(tag, "Network error", e)
        Result.Failure.Network
    } catch (e: HttpException) {
        AppLogger.error(tag, "API error: ${e.code()} ${e.message()}", e)
        Result.Failure.Api(e.code(), e.message())
    } catch (e: Exception) {
        AppLogger.error(tag, "Unexpected error", e)
        Result.Failure.Unknown
    }
