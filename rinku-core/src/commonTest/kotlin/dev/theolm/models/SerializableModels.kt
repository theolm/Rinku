@file:Suppress("MaxLineLength")

package dev.theolm.models

import kotlinx.serialization.Serializable

@Serializable
data class Name(val name: String)

@Serializable
data class WrongSerializer(val error: String)

@Serializable
data class ComplexModel(
    val name: Name,
    val timestamp: Long,
    val listOfNames: List<Name>,
    val mapOfNames: Map<String, Name>,
    val nestedList: List<List<String>>
)

val mockName = Name("John")
const val ExpectedMockName = "{\"name\":\"John\"}"

val mockComplexModel = ComplexModel(
    name = Name("John"),
    timestamp = 1234567890,
    listOfNames = listOf(
        Name("Jane"),
        Name("Doe")
    ),
    mapOfNames = mapOf(
        "first" to Name("John"),
        "second" to Name("Jane")
    ),
    nestedList = listOf(
        listOf("a", "b", "c"),
        listOf("d", "e", "f")
    )
)
const val ExpectedMockComplexModel =
    "{\"name\":{\"name\":\"John\"},\"timestamp\":1234567890,\"listOfNames\":[{\"name\":\"Jane\"},{\"name\":\"Doe\"}],\"mapOfNames\":{\"first\":{\"name\":\"John\"},\"second\":{\"name\":\"Jane\"}},\"nestedList\":[[\"a\",\"b\",\"c\"],[\"d\",\"e\",\"f\"]]}"
