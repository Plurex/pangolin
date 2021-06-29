package io.plurex.pangolin.validation

import kotlinx.serialization.Serializable

interface Validatable {

    /**
     * @throws [ValidationErrorException]
     */
    fun enforceValidation()

    fun getValidationErrors(): List<ValidationError>
}


data class ValidationErrorException(
    val errors: List<ValidationError>
) : Exception("ValidationError: $errors") {

    /***
     * Convenience method for getting a data object from the Exception.
     */
    fun toValidationErrors() = ValidationErrors(errors)

}

/**
 * Data object to represent the data of an Exception. Convenient for serialization.
 */
@Serializable
data class ValidationErrors(
    val errors: List<ValidationError>
)

@Serializable
data class ValidationError(
    val path: List<String>,
    val message: String
)

fun List<ValidationError>.prependPath(parentPath: List<String>): List<ValidationError> {
    return this.map {
        it.copy(
            path = parentPath + it.path
        )
    }
}


