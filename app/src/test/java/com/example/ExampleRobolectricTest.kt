package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.blocker.BlockerManager
import com.example.data.model.StudySubject
import com.example.motivation.JeeMotivationManager
import com.example.youtube.YouTubeFilterEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JEE Focus", appName)
    }

    @Test
    fun `test good luck start message contains JEE encouragement`() {
        val message = JeeMotivationManager.getRandomStartGoodLuck(StudySubject.PHYSICS, "Aryan")
        assertTrue(message.contains("Aryan"))
        assertTrue(message.contains("Good luck for JEE"))
    }

    @Test
    fun `test break encouragement provides rejuvenation message`() {
        val breakMsg = JeeMotivationManager.getRandomBreakEncouragement()
        assertTrue(breakMsg.isNotBlank())
    }

    @Test
    fun `test youtube filter blocks entertainment queries`() {
        val gamingResult = YouTubeFilterEngine.filterAndSanitizeQuery("funny comedy prank video")
        assertFalse(gamingResult.isAllowed)
        assertTrue(gamingResult.blockedReason?.contains("classified as non-educational entertainment") == true)

        val musicResult = YouTubeFilterEngine.filterAndSanitizeQuery("latest bollywood song")
        assertFalse(musicResult.isAllowed)
    }

    @Test
    fun `test youtube filter allows academic JEE queries`() {
        val studyResult = YouTubeFilterEngine.filterAndSanitizeQuery("rotational motion formulas")
        assertTrue(studyResult.isAllowed)
    }

    @Test
    fun `test whatsapp remains whitelisted and unblocked`() {
        val whatsapp = BlockerManager.DEFAULT_APPS.find { it.packageName == "com.whatsapp" }
        assertTrue(whatsapp != null)
        assertTrue(whatsapp?.isWhitelisted == true)
        assertFalse(whatsapp?.isBlocked == true)
    }
}
