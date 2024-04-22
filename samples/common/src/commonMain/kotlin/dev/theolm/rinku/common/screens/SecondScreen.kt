package dev.theolm.rinku.common.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import dev.theolm.rinku.common.components.CardRandomArgument
import dev.theolm.rinku.common.components.DemoScaffold
import dev.theolm.rinku.common.models.RandomArgument

@Composable
fun SecondScreen(
    onBackPress: () -> Unit,
    randomArgument: List<RandomArgument> = emptyList()
) {
    DemoScaffold(
        title = "Second Screen",
        onBackPress = onBackPress
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(randomArgument) {
                CardRandomArgument(randomArgument = it)
            }
        }
    }
}
