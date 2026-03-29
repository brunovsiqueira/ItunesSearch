package com.bruno.itunessearch.domain

/**
 * Simple logging abstraction. Delegates to a [LogWriter] implementation.
 * Default is Android Log. Tests can swap to a no-op or capturing writer.
 * Production can swap to Timber, Crashlytics, etc.
 */
object AppLogger {
    var writer: LogWriter = AndroidLogWriter()

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        writer.log(LogLevel.ERROR, tag, message, throwable)
    }

    fun warn(tag: String, message: String) {
        writer.log(LogLevel.WARN, tag, message)
    }

    fun debug(tag: String, message: String) {
        writer.log(LogLevel.DEBUG, tag, message)
    }
}

enum class LogLevel { DEBUG, WARN, ERROR }

interface LogWriter {
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null)
}

internal class AndroidLogWriter : LogWriter {
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        when (level) {
            LogLevel.DEBUG -> android.util.Log.d(tag, message)
            LogLevel.WARN -> android.util.Log.w(tag, message)
            LogLevel.ERROR -> android.util.Log.e(tag, message, throwable)
        }
    }
}
