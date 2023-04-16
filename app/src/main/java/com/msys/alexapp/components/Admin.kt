package com.msys.alexapp.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msys.alexapp.R
import com.msys.alexapp.data.Performance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream
import kotlin.math.roundToInt

interface AdminService {
  val contactsFlow: Flow<Map<String, String>>
  suspend fun setCanComment(canComment: Boolean)
  suspend fun addPerformance(performance: Performance)
  suspend fun addContact(email: String, nickname: String)
  suspend fun deleteContact(email: String)
}

val Row.asPerformance: Performance?
  get() {
    val id =
      if (getCell(6).cellTypeEnum == CellType.NUMERIC) getCell(10).numericCellValue.roundToInt()
      else return null
    val nomination =
      if (getCell(7).cellTypeEnum == CellType.STRING) getCell(7).stringCellValue
      else return null
    val name =
      if (getCell(10).cellTypeEnum == CellType.STRING) getCell(10).stringCellValue
      else return null
    val performance =
      if (getCell(11).cellTypeEnum == CellType.STRING) getCell(11).stringCellValue
      else return null
    return Performance(
      id = id.toString(),
      nomination = nomination,
      name = name,
      performance = performance,
      age = null,
      city = null,
      category = null,
    )
  }

fun performanceFlow(spreadSheetUri: Uri): Flow<Pair<Float, Performance>> = flow {
  withContext(Dispatchers.IO) {
    FileInputStream(spreadSheetUri.path!!).use { stream ->
      for (sheet in WorkbookFactory.create(stream)) {
        val count = sheet.lastRowNum + 1
        for (row in sheet) {
          val performance = row!!.asPerformance ?: continue
          val progress = 1f * (row.rowNum + 1) / count
          emit(progress to performance)
        }
      }
    }
  }
}

suspend fun uploadJob(
  spreadSheetUri: Uri,
  addPerformance: suspend (Performance) -> Unit,
  reportProgress: (Float) -> Unit,
) {
  performanceFlow(spreadSheetUri).onEach {
    addPerformance(it.second)
    reportProgress(it.first)
  }.collect()
  reportProgress(1f)
}

@Composable
fun AdminService.Admin() {
  var spreadSheetUri: Uri? by rememberSaveable { mutableStateOf(null) }
  val spreadSheetPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument(),
    onResult = { if (it != null) spreadSheetUri = it },
  )
  val askSpreadSheet = { spreadSheetPicker.launch(arrayOf("application/vnd.ms-excel")) }
  var performanceUploadProgress by rememberSaveable { mutableStateOf(0f) }
  LaunchedEffect(spreadSheetUri) {
    spreadSheetUri?.let {
      performanceUploadProgress = 0f
      uploadJob(it, ::addPerformance) { progress -> performanceUploadProgress = progress }
    }
  }
  val scope = rememberCoroutineScope()
  Scaffold(
    bottomBar = {
      Row {
        Button(onClick = askSpreadSheet) {
          Column {
            Text(text = stringResource(R.string.load_participants))
            if (performanceUploadProgress < 1f) {
              LinearProgressIndicator(progress = performanceUploadProgress.coerceIn(0f, 1f))
            }
          }
        }
        Row {
          var canComment by rememberSaveable { mutableStateOf(false) }
          Checkbox(
            checked = canComment,
            onCheckedChange = {
              canComment = !canComment
              scope.launch { setCanComment(canComment) }
            },
          )
          Text(text = stringResource(R.string.comments))
        }
      }
    }
  ) { padding ->
    val contacts by contactsFlow.collectAsStateWithLifecycle(initialValue = mapOf())
    Column(modifier = Modifier.padding(padding)) {
      for ((email, nickname) in contacts) {
        Row {
          Text(text = email)
          Text(text = nickname)
          Button(onClick = { scope.launch { deleteContact(email) } }) {
            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = stringResource(R.string.delete_contact),
            )
          }
        }
      }
      Row {
        var editing by rememberSaveable { mutableStateOf(false) }
        if (editing) {
          var email: String? by rememberSaveable { mutableStateOf(null) }
          var nickname: String? by rememberSaveable { mutableStateOf(null) }
          TextField(value = email ?: "", onValueChange = { email = it })
          TextField(value = nickname ?: "", onValueChange = { nickname = it })
          Button(
            onClick = { scope.launch { addContact(email!!, nickname!!); editing = false } },
            enabled = email != null && nickname != null
          ) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = stringResource(R.string.add_contact),
            )
          }
          Button(onClick = { editing = false }) {
            Icon(
              imageVector = Icons.Filled.Cancel,
              contentDescription = stringResource(R.string.cancel),
            )
          }
        } else {
          Button(onClick = { editing = true }) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = stringResource(R.string.add_contact),
            )
          }
        }
      }
    }
  }
}