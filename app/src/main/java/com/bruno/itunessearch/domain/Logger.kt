package com.bruno.itunessearch.domain

import android.util.Log

/**
 * Simple logging abstraction. Uses Android Log for now.
 * Can be swapped for Timber, Firebase Crashlytics, etc. without touching callers.
 */
object AppLogger {
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }

    fun warn(tag: String, message: String) {
        Log.w(tag, message)
    }

    fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }
}
