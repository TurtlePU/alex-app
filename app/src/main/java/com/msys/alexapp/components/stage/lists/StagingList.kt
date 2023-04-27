package com.msys.alexapp.components.stage.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.msys.alexapp.R
import com.msys.alexapp.components.common.Commitment
import com.msys.alexapp.components.common.HiddenForm
import com.msys.alexapp.data.Performance

@Composable
fun StagingList(
  performances: List<Performance>,
  onClick: (String) -> Unit,
  background: @Composable (String) -> Color,
  newPerformanceInitialID: Long,
  newPerformance: suspend (Performance) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(modifier = modifier) {
    items(items = performances, key = { it.id }) {
      it.Card(modifier = Modifier
        .clickable { onClick(it.id) }
        .background(background(it.id)))
    }
    item { NewPerformance(newPerformanceInitialID, newPerformance) }
  }
}

@Composable
fun NewPerformance(initialID: Long, send: suspend (Performance) -> Unit) {
  HiddenForm(commitDescription = stringResource(R.string.add_performance)) {
    var id: Long? by rememberSaveable { mutableStateOf(initialID) }
    var name: String? by rememberSaveable { mutableStateOf(null) }
    var performance: String? by rememberSaveable { mutableStateOf(null) }
    Text(text = "#")
    TextField(
      value = id?.toString() ?: "",
      onValueChange = { id = it.toLongOrNull() },
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next,
      ),
    )
    TextField(
      value = name ?: "",
      onValueChange = { name = it },
      keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Next,
      ),
    )
    TextField(
      value = performance ?: "",
      onValueChange = { performance = it },
      keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done,
      ),
    )
    Commitment((id?.let { it >= initialID } ?: false) && name != null && performance != null) {
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