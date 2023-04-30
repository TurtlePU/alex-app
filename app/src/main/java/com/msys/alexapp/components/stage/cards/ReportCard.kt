package com.msys.alexapp.components.stage.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.msys.alexapp.R
import com.msys.alexapp.components.common.example
import com.msys.alexapp.data.JuryReport
import com.msys.alexapp.data.Performance
import com.msys.alexapp.data.StageReport

@Composable
fun Pair<Performance, StageReport>.Card(
  isExpanded: (String) -> Boolean,
  onClick: (String) -> Unit,
  degree: (Double) -> String,
) {
  Column(modifier = Modifier.clickable { onClick(first.id) }) {
    first.Card()
    AnimatedVisibility(visible = isExpanded(first.id)) { second.Card(degree) }
  }
}

@Composable
fun StageReport.Card(degree: (Double) -> String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val commentText = comments.entries.joinToString(separator = "\n") {
      it.value.comment?.let { comment -> "${it.key}: $comment" } ?: ""
    }
    Text(
      text = commentText,
      modifier = Modifier
        .weight(3f)
        .padding(5.dp),
    )
    val clipboard = LocalClipboardManager.current
    IconButton(
      onClick = { clipboard.setText(AnnotatedString(commentText)) },
      modifier = Modifier.weight(.5f),
    ) {
      Icon(
        imageVector = Icons.Filled.CopyAll,
        contentDescription = stringResource(R.string.copy_comments),
      )
    }
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.Center,
    ) {
      Text(text = averageRating.toString())
      Text(text = degree(averageRating))
    }
  }
}

@Preview(showBackground = true)
@Composable
fun ReportCardPreview() {
  var expand by rememberSaveable { mutableStateOf(false) }
  (example to exampleReport).Card(
    isExpanded = { expand },
    onClick = { expand = !expand },
    degree = { "УЧ" }
  )
}

@Preview(showBackground = true)
@Composable
fun ReportPreview() {
  exampleReport.Card { "УЧ" }
}

val exampleReport = StageReport(
  7.5, mapOf(
    "Android" to JuryReport(9.0, "Wow, amazing!"),
    "Safari" to JuryReport(6.5, "Mediocre, wouldn't recommend"),
    "Talky" to JuryReport(7.0, "Wow tg do fg jd fg xcv talk talk talk I really liked the way you"),
    "Another" to JuryReport(7.5, "ok, i guess"),
  )
)