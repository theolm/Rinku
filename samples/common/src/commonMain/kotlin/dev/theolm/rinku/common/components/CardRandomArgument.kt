package dev.theolm.rinku.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.theolm.rinku.common.models.RandomArgument

@Composable
fun CardRandomArgument(randomArgument: RandomArgument) {
    Card(
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Deeplink Argument",
                style = TextStyle.Default.copy(fontWeight = FontWeight.Bold)
            )
            Text(text = "randomString: " + randomArgument.randomString)
            Text(text = "randomInt: " + randomArgument.randomInt.toString())
            Text(text = "randomDouble: " + randomArgument.randomDouble.toString())
        }
    }
}
