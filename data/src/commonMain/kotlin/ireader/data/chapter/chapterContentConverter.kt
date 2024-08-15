package ireader.data.chapter

import app.cash.sqldelight.ColumnAdapter
import ireader.core.source.model.Page
import ireader.core.source.model.decode
import ireader.core.source.model.encode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal val chapterContentConvertor = object : ColumnAdapter<List<Page>, String> {
  override fun decode(databaseValue: String): List<Page> {
    return kotlin.runCatching {
     return if (databaseValue.isEmpty()) listOf() else Json.decodeFromString(databaseValue)
    }.getOrElse {
      databaseValue.decode()
    }

  }

  override fun encode(value: List<Page>): String {
    return value.encode()
  }
}