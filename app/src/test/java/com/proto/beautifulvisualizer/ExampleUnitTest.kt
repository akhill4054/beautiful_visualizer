package com.proto.beautifulvisualizer

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.proto.beautifulvisualizer_example.R
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun readStringFromContext_LocalizedString() {
        // Given a Context object retrieved from Robolectric...
        val appName = context.getString(R.string.app_name)

        // ...then the result should be the expected one.
        assertEquals(appName, "lol")
    }
}