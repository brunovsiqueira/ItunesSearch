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
 */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (_: IOException) {
        Result.Failure.Network
    } catch (e: HttpException) {
        Result.Failure.Api(e.code(), e.message())
    } catch (_: Exception) {
        Result.Failure.Unknown
    }
