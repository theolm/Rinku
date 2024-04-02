package di

import home.HomeScreenModel
import org.koin.dsl.module

val commonModule = module {
    factory { HomeScreenModel() }
}
