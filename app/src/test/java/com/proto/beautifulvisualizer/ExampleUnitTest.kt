package com.proto.beautifulvisualizer

import android.content.Context
import com.proto.beautifulvisualizer_example.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(MockitoJUnitRunner::class)
class ExampleUnitTest {

    @Mock
    private lateinit var context: Context

    @Test
    fun readStringFromContext_LocalizedString() {
        assertEquals(context.getString(R.string.app_name), "lol")
    }
}