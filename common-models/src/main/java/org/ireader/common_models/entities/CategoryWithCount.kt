package org.ireader.common_models.entities

data class CategoryWithCount(val category: Category, val bookCount: Int) {

  val id get() = category.id

  val name get() = category.name

}
