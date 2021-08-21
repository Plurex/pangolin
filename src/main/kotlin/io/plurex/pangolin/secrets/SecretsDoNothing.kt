package io.plurex.pangolin.secrets

class SecretsDoNothing : SecretsAPI {

    override fun encrypt(inData: String): String {
        return inData
    }

    override fun encrypt(inData: ByteArray): ByteArray {
        return inData
    }

    override fun decrypt(encrypted: String): String {
        return encrypted
    }

    override fun decrypt(encrypted: ByteArray): ByteArray {
        return encrypted
    }
}