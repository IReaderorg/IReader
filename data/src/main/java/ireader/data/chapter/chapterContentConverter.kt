package ireader.data.chapter

import com.squareup.sqldelight.ColumnAdapter
import ireader.core.source.model.Page
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal val chapterContentConvertor = object : ColumnAdapter<List<Page>, String> {
  override fun decode(databaseValue: String): List<Page> {
    return if (databaseValue.isEmpty()) listOf() else Json.decodeFromString(databaseValue)
  }

  override fun encode(value: List<Page>): String {
    return Json.encodeToString(value)
  }
}