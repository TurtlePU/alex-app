package com.msys.alexapp.tasks

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.msys.alexapp.data.Performance
import com.msys.alexapp.services.FirebaseService
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream
import kotlin.math.roundToInt

@Composable
fun loadParticipants(): Task = launchedTask(
  contract = ActivityResultContracts.OpenDocument(),
  input = arrayOf(
    "application/vnd.ms-excel",
    "application/msexcel",
    "application/x-msexcel",
    "application/x-ms-excel",
    "application/x-excel",
    "application/x-dos_ms_excel",
    "application/xls",
    "application/x-xls",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  ),
  resource = { LocalContext.current.contentResolver.openFileDescriptor(it, "r") },
) { spreadSheet ->
  FileInputStream(spreadSheet.fileDescriptor).use { stream ->
    for (sheet in WorkbookFactory.create(stream)) {
      val count = sheet.lastRowNum + 1
      for (row in sheet) {
        val performance = row!!.asPerformance ?: continue
        val progress = 1f * (row.rowNum + 1) / count
        FirebaseService.addPerformance(performance)
        send(progress)
      }
    }
    send(1f)
  }
}

val Row.asPerformance: Performance?
  get() {
    val id =
      if (getCell(6)?.cellTypeEnum == CellType.NUMERIC) getCell(6).numericCellValue.roundToInt()
      else return null
    val nomination =
      if (getCell(7)?.cellTypeEnum == CellType.STRING) getCell(7).stringCellValue
      else return null
    val name =
      if (getCell(10)?.cellTypeEnum == CellType.STRING) getCell(10).stringCellValue
      else return null
    val performance =
      if (getCell(11)?.cellTypeEnum == CellType.STRING) getCell(11).stringCellValue
      else return null
    val age =
      when (getCell('S' - 'A')?.cellTypeEnum) {
        CellType.NUMERIC -> getCell('S' - 'A').numericCellValue.toLong()
        CellType.BLANK -> null
        else -> return null
      }
    val city =
      when (getCell('U' - 'A')?.cellTypeEnum) {
        CellType.STRING -> getCell('U' - 'A').stringCellValue
        CellType.BLANK -> null
        else -> return null
      }
    val category =
      when (getCell('I' - 'A')?.cellTypeEnum) {
        CellType.STRING -> getCell('I' - 'A').stringCellValue
        CellType.BLANK -> null
        else -> return null
      }
    return Performance(
      id = id.toString(),
      nomination = nomination,
      name = name,
      performance = performance,
      age = age,
      city = city,
      category = category,
    )
  }