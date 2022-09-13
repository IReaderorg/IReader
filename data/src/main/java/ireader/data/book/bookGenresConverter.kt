package ireader.data.book

import com.squareup.sqldelight.ColumnAdapter

internal val bookGenresConverter = object : ColumnAdapter<List<String>, String> {
  override fun decode(databaseValue: String): List<String> {
    return if (databaseValue.isEmpty()) listOf() else databaseValue.split(";")
  }

  override fun encode(value: List<String>): String {
    return value.joinToString(separator = ";")
  }
}