package ireader.data.book

import app.cash.sqldelight.ColumnAdapter

internal val bookGenresConverter = object : ColumnAdapter<List<String>, String> {
  override fun decode(databaseValue: String): List<String> {
    return if (databaseValue.isEmpty()) listOf() else databaseValue.split(";")
  }

  override fun encode(value: List<String>): String {
    return value.joinToString(separator = ";")
  }
}

internal val longConverter = object : ColumnAdapter<Long, Long> {
  override fun decode(databaseValue: Long): Long {
    return databaseValue
  }

  override fun encode(value: Long): Long {
    return value
  }
}
internal val floatDoubleColumnAdapter = object : ColumnAdapter<Float, Double> {
  override fun decode(databaseValue: Double): Float {
    return databaseValue.toFloat()
  }

  override fun encode(value: Float): Double {
    return  value.toDouble()
  }
}
internal val intLongColumnAdapter = object : ColumnAdapter<Int, Long> {
  override fun decode(databaseValue: Long): Int {
    return databaseValue.toInt()
  }

  override fun encode(value: Int): Long {
    return  value.toLong()
  }
}