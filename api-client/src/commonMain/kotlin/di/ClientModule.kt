package di

import KtorClient
import engine
import io.ktor.client.HttpClient
import org.koin.dsl.module

val clientModule = module {
    single {
        KtorClient(engine)
    }

    single<HttpClient> {
        get<KtorClient>().instance
    }
}