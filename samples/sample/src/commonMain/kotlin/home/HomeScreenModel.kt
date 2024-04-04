package home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import dev.theolm.rinku.DeepLink

class HomeScreenModel() : ScreenModel {
    val uiState = mutableStateOf(UiState())


    fun onDeepLinkReceived(deepLink: DeepLink) {
        uiState.value = uiState.value .copy(deepLink = deepLink)
    }

    data class UiState(
        val deepLink: DeepLink? = null,
    )
}
