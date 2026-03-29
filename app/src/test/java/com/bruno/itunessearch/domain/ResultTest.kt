package com.bruno.itunessearch.domain

import com.bruno.itunessearch.fakes.NoOpLogWriter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ResultTest {

    @Before
    fun setup() {
        AppLogger.writer = NoOpLogWriter()
    }

    @Test
    fun `safeApiCall returns Success on successful block`() = runTest {
        val result = safeApiCall { "data" }
        assertTrue(result is Result.Success)
        assertEquals("data", (result as Result.Success).data)
    }

    @Test
    fun `safeApiCall returns Network failure on IOException`() = runTest {
        val result = safeApiCall<String> { throw IOException("no internet") }
        assertTrue(result is Result.Failure.Network)
    }

    @Test
    fun `safeApiCall returns Unknown failure on generic exception`() = runTest {
        val result = safeApiCall<String> { throw RuntimeException("oops") }
        assertTrue(result is Result.Failure.Unknown)
    }

    @Test(expected = CancellationException::class)
    fun `safeApiCall rethrows CancellationException`() = runTest {
        safeApiCall<String> { throw CancellationException("cancelled") }
    }
}
