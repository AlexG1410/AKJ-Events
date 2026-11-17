package co.edu.uniquindio.akjevents.data.remote.ai

import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionException
import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionFailure
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

fun interface AiSessionTokenProvider {
    suspend fun token(): String
}

class FirebaseAiSessionTokenProvider @Inject constructor(private val auth: FirebaseAuth) : AiSessionTokenProvider {
    override suspend fun token(): String {
        val user = auth.currentUser ?: throw CategorySuggestionException(CategorySuggestionFailure.SESSION)
        // Firebase renueva automáticamente un token vencido. Nunca se registra ni se persiste aquí.
        val token = user.getIdToken(false).await().token
        if (token.isNullOrBlank() || auth.currentUser?.uid != user.uid) {
            throw CategorySuggestionException(CategorySuggestionFailure.SESSION)
        }
        return token
    }
}
