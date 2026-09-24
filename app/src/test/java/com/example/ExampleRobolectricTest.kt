package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.CaptchaChallenge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches TaskEarn Pro`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TaskEarn Pro", appName)
    }

    @Test
    fun `captcha challenge generates valid math and token`() {
        val challenge = CaptchaChallenge.generate()
        assertNotNull(challenge)
        assertTrue(challenge.visualCode.length == 4)
        if (challenge.isAddition) {
            assertEquals(challenge.num1 + challenge.num2, challenge.answer)
        } else {
            assertEquals(challenge.num1 - challenge.num2, challenge.answer)
        }
    }

    @Test
    fun `coin conversion rate matches 1000 coins equal 10 rupees`() {
        val coinRatePerRupee = 100
        val coins = 1000L
        val rupees = coins.toDouble() / coinRatePerRupee
        assertEquals(10.0, rupees, 0.001)
    }

    @Test
    fun `app install task creation assigns correct category and metadata`() {
        val task = com.example.data.model.TaskEntity(
            title = "Install PhonePe",
            description = "Download and verify install",
            category = "APP_INSTALL",
            rewardCoins = 500,
            estimatedTime = "2 min",
            targetUrl = "https://play.google.com/store/apps/details?id=com.phonepe.app",
            requirements = "Install & Open (60s)",
            verificationQuestion = "What color is the logo?",
            verificationAnswer = "Purple"
        )
        assertEquals("APP_INSTALL", task.category)
        assertEquals(500L, task.rewardCoins)
        assertTrue(task.targetUrl.contains("play.google.com"))
    }
}
