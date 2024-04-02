package home

import Greeting
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel

class HomeScreenModel(
    greeting: Greeting
) : ScreenModel {
    val uiState = mutableStateOf(UiState())

    init {
        uiState.value = uiState.value.copy(message = greeting.greet())
    }

    data class UiState(
        val showContent: Boolean = false,
        val message : String = ""
    )
}
