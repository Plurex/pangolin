package io.plurex.pangolin.secrets

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator
import java.time.Duration
import java.time.Instant
import javax.crypto.SecretKey

/**
 * A default google authenticator implementation of [TotpAPI].
 *
 *  Uses SHA1, 6 digit codes with a 30 second duration.
 */
class TotpGoogleDefault : TotpAPI {

    private val totp = TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(30), 6, AlgoNames.HMAC_SHA1)

    /**
     * Returns data required to setup a new authenticator with a Base32 encoded secret
     */
    override fun generateTotpSetup(label: String): TotpSetupData {
        val secret = generateSecret()
        return TotpSetupData(
            secret = secret,
            label = label,
            url = "otpauth://totp/$label?secret=$secret"
        )
    }

    override fun getTotp(secret: String, instant: Instant): Int {
        return totp.generateOneTimePassword(importSecret(secret), instant)
    }

    internal fun generateSecret(): String {
        return generateSecretKey(KeySize.BITS_160, algo = AlgoNames.HMAC_SHA1).export(EncodeDecode.ENCODE_32)
    }

    private fun importSecret(secret: String): SecretKey {
        return importSecretKey(secret, algo = AlgoNames.HMAC_SHA1, decode = EncodeDecode.DECODE_32)
    }
}
