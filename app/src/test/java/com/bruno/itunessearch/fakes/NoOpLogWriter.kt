package com.bruno.itunessearch.fakes

import com.bruno.itunessearch.domain.LogLevel
import com.bruno.itunessearch.domain.LogWriter

class NoOpLogWriter : LogWriter {
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        // No-op for unit tests
    }
}
