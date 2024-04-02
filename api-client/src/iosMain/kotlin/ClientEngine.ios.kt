import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual val engine: HttpClientEngine = Darwin.create()