package dev.theolm.rinku

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

fun <T> DeepLink.getParameter(name: String, kSerializer: KSerializer<T>): T {
    val value = parameters[name] ?: throw IllegalArgumentException("Parameter $name not found.")
    return Json.decodeFromString(kSerializer, value)
}
