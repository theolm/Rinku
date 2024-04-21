package dev.theolm.rinku.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class DeepLinkParam<T>(
    val name: String,
    private val value: T,
    private val kSerializer: KSerializer<T>
) {
    fun serialize(): String = Json.encodeToString(kSerializer, value)
}
