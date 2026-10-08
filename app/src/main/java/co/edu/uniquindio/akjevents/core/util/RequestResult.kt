package co.edu.uniquindio.akjevents.core.util

sealed class RequestResult {
    data object Loading : RequestResult()                       // La operación está en curso
    data class Success(val message: String) : RequestResult()   // La operación terminó bien
    data class Failure(val errorMessage: String) : RequestResult() // La operación falló
}
