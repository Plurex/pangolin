package io.plurex.pangolin.secrets

class SecretsDoNothing : SecretsAPI {

    override fun encrypt(inData: String): String {
        return inData
    }

    override fun decrypt(encrypted: String): String {
        return encrypted
    }
}