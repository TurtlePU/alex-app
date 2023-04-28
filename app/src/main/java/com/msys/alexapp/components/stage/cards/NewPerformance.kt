package com.msys.alexapp.components.stage.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.R
import com.msys.alexapp.components.common.Commitment
import com.msys.alexapp.components.common.HiddenForm
import com.msys.alexapp.data.Performance
import com.msys.alexapp.ui.theme.AlexAppTheme

@Composable
fun NewPerformance(
  initialID: Long,
  isGoodId: (Long) -> Boolean,
  send: suspend (Performance) -> Unit,
) {
  HiddenForm(
    modifier = Modifier.fillMaxWidth().padding(5.dp),
    contentArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
    commitDescription = stringResource(R.string.add_performance),
  ) {
    var id: Long? by rememberSaveable { mutableStateOf(initialID) }
    var name: String? by rememberSaveable { mutableStateOf(null) }
    var performance: String? by rememberSaveable { mutableStateOf(null) }
    OutlinedTextField(
      value = id?.toString() ?: "",
      onValueChange = { id = it.toLongOrNull() },
      label = { Text("#") },
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next,
      ),
    )
    OutlinedTextField(
      value = name ?: "",
      onValueChange = { name = it },
      placeholder = { Text(stringResource(R.string.name_label)) },
      keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Next,
      ),
    )
    OutlinedTextField(
      value = performance ?: "",
      onValueChange = { performance = it },
      placeholder = { Text(stringResource(R.string.performance_label)) },
      keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done,
      ),
    )
    Commitment((id?.let(isGoodId) ?: false) && name != null && performance != null) {
      send(anonymousPerformance(id!!, name!!, performance!!))
    }
  }
}

fun anonymousPerformance(id: Long, name: String, performance: String) = Performance(
  id = id.toString(),
  name = name,
  performance = performance,
  null, null, null, null
)

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=480")
@Composable
fun NewPerformancePreview() {
  AlexAppTheme { NewPerformance(initialID = 0, isGoodId = { true }) { } }
}