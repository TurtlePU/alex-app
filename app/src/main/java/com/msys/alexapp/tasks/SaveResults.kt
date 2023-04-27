package com.msys.alexapp.tasks

import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.msys.alexapp.services.FirebaseService
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@Composable
fun saveResults(): Task = launchedTask(
  contract = ActivityResultContracts.CreateDocument(
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  ),
  input = Environment.DIRECTORY_DOCUMENTS ?: "",
  resource = { LocalContext.current.contentResolver.openOutputStream(it) },
) { outputStream ->
  val workbook = XSSFWorkbook()
  val sheet = workbook.createSheet()
  var count = 0
  val juryColumns = mutableMapOf<String, Int>()
  val results = FirebaseService.collectResults()
  for ((id, report) in results) {
    sheet.createRow(++count).run {
      createCell(0).setCellValue(id)
      createCell(1).setCellValue(report?.averageRating ?: Double.NaN)
      for ((jury, comment) in report?.comments ?: mapOf()) {
        val column = juryColumns.computeIfAbsent(jury) {
          juryColumns.maxOfOrNull { it.value + 1 } ?: 2
        }
        createCell(column).setCellValue(comment)
      }
    }
    send(.3f * count / results.size)
  }
  sheet.createRow(0).run {
    createCell(0).setCellValue("№")
    createCell(1).setCellValue("Средняя оценка")
    juryColumns.onEachIndexed { index, (jury, col) ->
      createCell(col).setCellValue(jury)
      send(.3f + .3f * index / juryColumns.size)
    }
  }
  outputStream.use(workbook::write)
  send(1f)
}