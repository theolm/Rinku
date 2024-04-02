package di

import Greeting
import home.HomeScreenModel
import org.koin.dsl.module

val commonModule = module {
    single { Greeting() }
    factory { HomeScreenModel(get()) }
}
