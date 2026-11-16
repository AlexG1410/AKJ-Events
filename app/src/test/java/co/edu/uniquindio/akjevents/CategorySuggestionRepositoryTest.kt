package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.remote.ai.AiServiceConfig
import co.edu.uniquindio.akjevents.data.remote.ai.AiSessionTokenProvider
import co.edu.uniquindio.akjevents.data.remote.ai.CategorySuggestionApi
import co.edu.uniquindio.akjevents.data.remote.ai.CategorySuggestionRequest
import co.edu.uniquindio.akjevents.data.remote.ai.CategorySuggestionResponse
import co.edu.uniquindio.akjevents.data.repository.remote.RemoteCategorySuggestionRepository
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionException
import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionFailure
import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionRepository
import co.edu.uniquindio.akjevents.domain.usecase.event.SuggestEventCategoryUseCase
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CategorySuggestionRepositoryTest {
    private fun repository(
        config: String = "https://worker.test/",
        tokens: AiSessionTokenProvider = AiSessionTokenProvider { "test-id-token" },
        answer: suspend () -> CategorySuggestionResponse = { CategorySuggestionResponse("SPORTS") }
    ) = RemoteCategorySuggestionRepository(object : CategorySuggestionApi {
        override suspend fun suggest(authorization: String, request: CategorySuggestionRequest): CategorySuggestionResponse {
            assertEquals("Bearer test-id-token", authorization)
            return answer()
        }
    }, tokens, AiServiceConfig(config))

    private suspend fun failure(expected: CategorySuggestionFailure, action: suspend () -> Unit) {
        try { action(); fail("Expected controlled failure") } catch (error: CategorySuggestionException) {
            assertEquals(expected, error.failure)
        }
    }

    @Test fun noUrlFailsBeforeReadingSessionOrMakingNetworkRequest() = runTest {
        failure(CategorySuggestionFailure.NOT_CONFIGURED) {
            repository(config = "", tokens = { error("must not read session") }).suggest("title", "description")
        }
    }

    @Test fun onlyHttpsRootUrlsWithoutCredentialsQueryOrFragmentAreAccepted() {
        for (url in listOf("", "http://worker.test/", "https://user:pass@worker.test/", "https://worker.test/path", "https://worker.test/?key=a", "https://worker.test/#key")) {
            assertFalse(url, AiServiceConfig(url).isConfigured)
        }
        assertEquals("https://worker.test/", AiServiceConfig(" https://worker.test ").baseUrl.toString())
    }

    @Test fun validCategoriesAreMappedAndUnknownOrMissingCategoryNeverDefaultsToSocial() = runTest {
        for (category in EventCategory.entries) {
            assertEquals(category, repository { CategorySuggestionResponse(category.name) }.suggest("title", "description"))
        }
        for (invalid in listOf(null, "", "OTHER", "sports", " SPORTS ")) {
            failure(CategorySuggestionFailure.INVALID_RESPONSE) { repository { CategorySuggestionResponse(invalid) }.suggest("title", "description") }
        }
    }

    @Test fun sessionAndHttpFailuresAreControlledWithoutExposingProviderMessages() = runTest {
        failure(CategorySuggestionFailure.SESSION) {
            repository(tokens = { throw CategorySuggestionException(CategorySuggestionFailure.SESSION) }).suggest("title", "description")
        }
        for ((status, expected) in listOf(401 to CategorySuggestionFailure.SESSION, 403 to CategorySuggestionFailure.SESSION, 429 to CategorySuggestionFailure.RATE_LIMIT, 502 to CategorySuggestionFailure.UNAVAILABLE, 503 to CategorySuggestionFailure.UNAVAILABLE)) {
            failure(expected) { repository { throw HttpException(Response.error<Unit>(status, "private provider details".toResponseBody())) }.suggest("title", "description") }
        }
        failure(CategorySuggestionFailure.UNAVAILABLE) { repository { throw java.io.IOException("test") }.suggest("title", "description") }
    }

    @Test fun timeoutIsAnUnavailableResultButExternalCancellationPropagates() = runTest {
        failure(CategorySuggestionFailure.UNAVAILABLE) {
            repository { delay(23_000); CategorySuggestionResponse("SPORTS") }.suggest("title", "description")
        }
        try {
            repository { throw CancellationException("cancel test") }.suggest("title", "description")
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }

    @Test fun useCaseTrimsAndValidatesBeforeCallingRepository() = runTest {
        val inputs = mutableListOf<Pair<String, String>>()
        val useCase = SuggestEventCategoryUseCase(CategorySuggestionRepository { title, description -> inputs += title to description; EventCategory.ACADEMIC })
        useCase(" Taller ", " Una descripción suficientemente larga. ")
        assertEquals(listOf("Taller" to "Una descripción suficientemente larga."), inputs)
        for ((title, description) in listOf("a" to "Descripción suficientemente larga", "Título" to "corta", "a".repeat(81) to "Descripción suficientemente larga", "Título" to "a".repeat(601))) {
            try { useCase(title, description); fail("Invalid input") } catch (_: IllegalArgumentException) { }
        }
        assertEquals(1, inputs.size)
    }

    @Test fun retrofitSendsTheExactWorkerContractToASimulatedLocalEndpoint() = runBlocking {
        val authorization = AtomicReference<String>()
        val body = AtomicReference<String>()
        val method = AtomicReference<String>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/suggest-category") { exchange ->
            authorization.set(exchange.requestHeaders.getFirst("Authorization"))
            method.set(exchange.requestMethod)
            body.set(exchange.requestBody.bufferedReader().readText())
            val reply = "{\"category\":\"VOLUNTEERING\"}".toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, reply.size.toLong())
            exchange.responseBody.use { it.write(reply) }
            exchange.close()
        }
        server.start()
        try {
            // HTTP solo en esta prueba JVM; la configuración de producción exige HTTPS.
            val api = Retrofit.Builder().baseUrl("http://127.0.0.1:${server.address.port}/")
                .addConverterFactory(GsonConverterFactory.create()).build().create(CategorySuggestionApi::class.java)
            val repository = RemoteCategorySuggestionRepository(api, { "test-id-token" }, AiServiceConfig("https://worker.test/"))
            assertEquals(EventCategory.VOLUNTEERING, repository.suggest("Huertos", "Cultivamos juntos para ayudar a la comunidad."))
            assertEquals("POST", method.get())
            assertEquals("Bearer test-id-token", authorization.get())
            assertEquals("{\"title\":\"Huertos\",\"description\":\"Cultivamos juntos para ayudar a la comunidad.\"}", body.get())
        } finally { server.stop(0) }
    }
}
