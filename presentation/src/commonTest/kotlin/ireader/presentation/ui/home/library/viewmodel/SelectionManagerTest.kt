package ireader.presentation.ui.home.library.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for SelectionManager to verify correct behavior with single and multiple selections
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SelectionManagerTest {

    @Test
    fun `toggle single item should add it to selection`() = runTest {
        val manager = SelectionManager()
        
        manager.toggle(1L)
        
        val selected = manager.selectedIds.first()
        assertEquals(1, selected.size)
        assertTrue(selected.contains(1L))
        assertEquals(1, manager.count)
        assertFalse(manager.isEmpty)
        assertTrue(manager.isNotEmpty)
    }

    @Test
    fun `toggle same item twice should remove it from selection`() = runTest {
        val manager = SelectionManager()
        
        manager.toggle(1L)
        manager.toggle(1L)
        
        val selected = manager.selectedIds.first()
        assertEquals(0, selected.size)
        assertTrue(manager.isEmpty)
        assertFalse(manager.isNotEmpty)
    }

    @Test
    fun `toggle two different items should add both to selection`() = runTest {
        val manager = SelectionManager()
        
        manager.toggle(1L)
        manager.toggle(2L)
        
        val selected = manager.selectedIds.first()
        assertEquals(2, selected.size)
        assertTrue(selected.contains(1L))
        assertTrue(selected.contains(2L))
    }

    @Test
    fun `toSet should return current selection`() = runTest {
        val manager = SelectionManager()
        
        manager.toggle(1L)
        
        val set = manager.toSet()
        assertEquals(1, set.size)
        assertTrue(set.contains(1L))
    }

    @Test
    fun `toSet with single item should return set with one element`() = runTest {
        val manager = SelectionManager()
        
        manager.toggle(42L)
        
        val set = manager.toSet()
        assertEquals(setOf(42L), set)
        assertEquals(1, set.size)
    }

    @Test
    fun `toList should return current selection as list`() = runTest {
        val manager = SelectionManager()
        
        manager.toggle(1L)
        
        val list = manager.toList()
        assertEquals(1, list.size)
        assertTrue(list.contains(1L))
    }

    @Test
    fun `toList with single item should return list with one element`() = runTest {
        val manager = SelectionManager()
        
        manager.toggle(42L)
        
        val list = manager.toList()
        assertEquals(listOf(42L), list)
        assertEquals(1, list.size)
    }

    @Test
    fun `toSet then toList should preserve single element`() = runTest {
        val manager = SelectionManager()
        
        manager.toggle(100L)
        
        val result = manager.toSet().toList()
        assertEquals(1, result.size)
        assertEquals(100L, result[0])
    }

    @Test
    fun `clear should remove all selections`() = runTest {
        val manager = SelectionManager()
        
        manager.toggle(1L)
        manager.toggle(2L)
        manager.toggle(3L)
        manager.clear()
        
        val selected = manager.selectedIds.first()
        assertEquals(0, selected.size)
        assertTrue(manager.isEmpty)
    }

    @Test
    fun `contains should return true for selected item`() = runTest {
        val manager = SelectionManager()
        
        manager.toggle(1L)
        
        assertTrue(manager.contains(1L))
        assertFalse(manager.contains(2L))
    }

    @Test
    fun `addAll should add multiple items`() = runTest {
        val manager = SelectionManager()
        
        manager.addAll(listOf(1L, 2L, 3L))
        
        val selected = manager.selectedIds.first()
        assertEquals(3, selected.size)
        assertTrue(selected.containsAll(listOf(1L, 2L, 3L)))
    }

    @Test
    fun `removeAll should remove specified items`() = runTest {
        val manager = SelectionManager()
        
        manager.addAll(listOf(1L, 2L, 3L))
        manager.removeAll(listOf(1L, 3L))
        
        val selected = manager.selectedIds.first()
        assertEquals(1, selected.size)
        assertTrue(selected.contains(2L))
    }

    @Test
    fun `flip should toggle selection for given items`() = runTest {
        val manager = SelectionManager()
        
        manager.toggle(1L)
        manager.toggle(2L)
        
        // Flip 2 and 3 - 2 should be removed, 3 should be added
        manager.flip(listOf(2L, 3L))
        
        val selected = manager.selectedIds.first()
        assertEquals(2, selected.size)
        assertTrue(selected.contains(1L))
        assertFalse(selected.contains(2L))
        assertTrue(selected.contains(3L))
    }

    @Test
    fun `single selection should work correctly for download scenario`() = runTest {
        val manager = SelectionManager()
        
        // Simulate user selecting a single book
        manager.toggle(12345L)
        
        // Simulate downloadChapters() getting the selection
        val selectedIds = manager.toSet().toList()
        
        // Verify the selection is correct
        assertEquals(1, selectedIds.size)
        assertEquals(12345L, selectedIds[0])
        assertFalse(selectedIds.isEmpty())
    }

    @Test
    fun `two selections should work correctly for download scenario`() = runTest {
        val manager = SelectionManager()
        
        // Simulate user selecting two books
        manager.toggle(12345L)
        manager.toggle(67890L)
        
        // Simulate downloadChapters() getting the selection
        val selectedIds = manager.toSet().toList()
        
        // Verify the selection is correct
        assertEquals(2, selectedIds.size)
        assertTrue(selectedIds.contains(12345L))
        assertTrue(selectedIds.contains(67890L))
        assertFalse(selectedIds.isEmpty())
    }

    @Test
    fun `selection state should be consistent after multiple operations`() = runTest {
        val manager = SelectionManager()
        
        // Complex sequence of operations
        manager.toggle(1L)
        assertEquals(1, manager.count)
        
        manager.toggle(2L)
        assertEquals(2, manager.count)
        
        manager.toggle(1L) // Deselect 1
        assertEquals(1, manager.count)
        
        manager.toggle(3L)
        assertEquals(2, manager.count)
        
        val selected = manager.toSet()
        assertEquals(setOf(2L, 3L), selected)
    }
}
