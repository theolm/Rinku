import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

internal class KtorClient(
    private val engine: HttpClientEngine
) {
    val instance: HttpClient by lazy {
        HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }
    }
}