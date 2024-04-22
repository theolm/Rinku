package dev.theolm.rinku.common.models

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class RandomArgument(
    val randomString: String = Random.nextInt().toString(),
    val randomInt: Int = Random.nextInt(),
    val randomDouble: Double = Random.nextDouble()
)
