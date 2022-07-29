package io.plurex.pangolin.secrets

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.spyk
import io.plurex.pangolin.random.randString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URLEncoder
import java.time.Instant

internal class TotpGoogleDefaultTest {

    private lateinit var testObj: TotpGoogleDefault

    @BeforeEach
    fun setUp() {
        testObj = spyk(TotpGoogleDefault())
    }

    @Test
    fun generateTotpSetup() {
        val secret = randString()
        val label = "${randString()} foo @"
        val encodedLabel = URLEncoder.encode(label, "UTF-8")
        every { testObj.generateSecret() } returns secret

        val expected = TotpSetupData(
            secret = secret,
            label = label,
            url = "otpauth://totp/$encodedLabel?secret=$secret"
        )

        val actual = testObj.generateTotpSetup(label)

        assertThat(actual).isEqualTo(expected)

    }

    @Test
    fun buildTotpSetup() {
        val secret = randString()
        val label = "${randString()} foo @"
        val encodedLabel = URLEncoder.encode(label, "UTF-8")

        val expected = TotpSetupData(
            secret = secret,
            label = label,
            url = "otpauth://totp/$encodedLabel?secret=$secret"
        )

        val actual = testObj.buildTotpSetup(label, secret)

        assertThat(actual).isEqualTo(expected)

    }

    @Test
    fun getTotp() {
        val secret = "MMVONHZY6HR22PRNEZHZUCYWG267ALN2"
        val instant = Instant.ofEpochMilli(1658919624360)
        val expected = 287440
        val actual = testObj.getTotp(secret, instant)
        assertThat(actual).isEqualTo(expected)
    }
}
