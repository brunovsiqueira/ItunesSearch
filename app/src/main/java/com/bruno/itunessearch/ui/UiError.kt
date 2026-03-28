package com.bruno.itunessearch.ui

import androidx.annotation.StringRes
import com.bruno.itunessearch.R
import com.bruno.itunessearch.domain.Result

/**
 * User-facing error categories. Contains no implementation details
 * (no status codes, no exception info) — just what the UI needs to
 * pick the right localized message.
 */
enum class UiError {
    Network,
    Server,
    Unknown,
}

/**
 * Maps a UiError to a localized string resource.
 * Called in composables where stringResource() is available.
 */
@StringRes
fun UiError.toStringRes(): Int = when (this) {
    UiError.Network -> R.string.error_network
    UiError.Server -> R.string.error_api
    UiError.Unknown -> R.string.error_unknown
}

/**
 * Maps domain error → UI error. Called in ViewModels.
 * This is the boundary: domain details (status codes, messages) stop here.
 */
fun Result.Failure.toUiError(): UiError = when (this) {
    is Result.Failure.Network -> UiError.Network
    is Result.Failure.Api -> UiError.Server
    Result.Failure.Unknown -> UiError.Unknown
}
