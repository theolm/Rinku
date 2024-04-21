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
