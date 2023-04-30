package com.msys.alexapp.tasks

import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.msys.alexapp.R
import com.msys.alexapp.data.Summary
import com.msys.alexapp.services.FirebaseService
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.Closeable
import java.io.OutputStream

enum class Columns {
  ID, NAME, PERFORMANCE, DEGREE, RATING;

  val header
    get(): @StringRes Int = when (this) {
      ID -> R.string.id_header
      NAME -> R.string.name_header
      PERFORMANCE -> R.string.performance_header
      DEGREE -> R.string.degree_header
      RATING -> R.string.rating_header
    }
}

operator fun Summary.get(columns: Columns): String = when (columns) {
  Columns.ID -> id
  Columns.NAME -> name
  Columns.PERFORMANCE -> performance
  Columns.DEGREE -> degree
  Columns.RATING -> rating.toString()
}

data class StreamAndHeaders(val stream: OutputStream, val headers: Map<Columns, String>) :
  Closeable {
  override fun close() = stream.close()
}

@Composable
fun saveResults(): Task = launchedTask(
  contract = ActivityResultContracts.CreateDocument(
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  ),
  input = Environment.DIRECTORY_DOCUMENTS ?: "",
  resource = {
    StreamAndHeaders(
      LocalContext.current.contentResolver.openOutputStream(it) ?: return@launchedTask null,
      Columns.values().associateWith { col -> stringResource(col.header) }
    )
  },
) { (outputStream, headers) ->
  val workbook = XSSFWorkbook()
  val sheet = workbook.createSheet()
  var count = 0
  val juryColumns = mutableMapOf<String, Int>()
  val results = FirebaseService.collectResults()
  for (result in results) {
    sheet.createRow(++count).run {
      for (column in Columns.values()) {
        createCell(column.ordinal).setCellValue(result[column])
      }
      getCell(Columns.RATING.ordinal).setCellValue(result.rating)
      for ((jury, comment) in result.comments) {
        val column = juryColumns.computeIfAbsent(jury) {
          juryColumns.maxOfOrNull { it.value + 2 } ?: Columns.values().size
        }
        createCell(column).setCellValue(comment.rating)
        comment.comment?.let { createCell(column + 1).setCellValue(it) }
      }
    }
    send(.3f * count / results.size)
  }
  sheet.createRow(0).run {
    for (column in Columns.values()) {
      createCell(column.ordinal).setCellValue(headers[column])
    }
    juryColumns.onEachIndexed { index, (jury, col) ->
      createCell(col).setCellValue(jury)
      send(.3f + .3f * index / juryColumns.size)
    }
  }
  outputStream.use(workbook::write)
  send(1f)
}