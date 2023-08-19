package com.msys.alexapp.components.admin

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.msys.alexapp.R
import com.msys.alexapp.ui.theme.AlexAppTheme

@Composable
fun NamedCheckBox(
  text: String,
  enabled: Boolean = true,
  setFlag: (Boolean) -> Unit = {}) {
  Row {
    var flag by rememberSaveable { mutableStateOf(false) }
    Checkbox(
      checked = flag,
      onCheckedChange = {
        flag = !flag
        setFlag(flag)
      },
      enabled = enabled,
    )
    Text(text)
  }
}

@Preview(showBackground = true)
@Composable
fun NamedCheckBoxPreview() {
  AlexAppTheme { NamedCheckBox(stringResource(R.string.comments)) }
}