package io.plurex.pangolin.secrets

import java.time.Instant

interface SecretsAPI {

    fun encrypt(inData: String): String

    fun decrypt(encrypted: String): String

    fun encrypt(inData: ByteArray): ByteArray

    fun decrypt(encrypted: ByteArray): ByteArray

}

interface TotpAPI {

    fun generateTotpSetup(label: String): TotpSetupData

    fun getTotp(secret: String, instant: Instant): Int

}

data class TotpSetupData(
    val secret: String,
    val label: String,
    val url: String
)
