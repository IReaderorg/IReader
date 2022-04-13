package org.ireader.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RemoveSameItemsFromListKtTest {

    @Test
    fun `when the list is empty`() {
        val result: List<String> =
            removeSameItemsFromList(emptyList<String>(), emptyList<String>()) {
                it
            }
        assertThat(result.isEmpty()).isTrue()
    }

    @Test
    fun `when there is multiple string`() {
        val old = listOf("Peter", "Card")
        val new = listOf("Peter", "Jack")
        val result: List<String> = removeSameItemsFromList(old, new) {
            it
        }

        assertThat(result.size == 3).isTrue()
    }

    @Test
    fun `diff based on id`() {
      data class Book(val id: Int)

        val old = listOf(Book(1), Book(2))
        val new = listOf(Book(2), Book(3), Book(4), Book(1), Book(2))
        val result: List<Book> = removeSameItemsFromList(old, new) {
            it.id
        }
        assertThat(result.size == 4).isTrue()
    }

}