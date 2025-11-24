package ireader.domain.js

import ireader.core.source.model.Filter
import ireader.domain.js.util.JSFilterConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for converting LNReader filter definitions to IReader Filter objects.
 */
class LNReaderToIReaderFilterConversionTest {
    
    private val converter = JSFilterConverter()
    
    @Test
    fun testConvertPickerToSelect() {
        val jsFilters = mapOf(
            "status" to mapOf(
                "type" to "Picker",
                "label" to "Status",
                "options" to listOf(
                    mapOf("label" to "All", "value" to "all"),
                    mapOf("label" to "Ongoing", "value" to "ongoing"),
                    mapOf("label" to "Completed", "value" to "completed")
                ),
                "defaultValue" to "all"
            )
        )
        
        val filters = converter.convertToIReaderFilters(jsFilters)
        
        assertEquals(1, filters.size)
        val filter = filters.first()
        assertTrue(filter is Filter.Select)
        assertEquals("Status", filter.name)
        assertEquals(3, filter.options.size)
        assertEquals("All", filter.options[0])
        assertEquals(0, filter.value)
    }
    
    @Test
    fun testConvertTextInputToText() {
        val jsFilters = mapOf(
            "author" to mapOf(
                "type" to "TextInput",
                "label" to "Author Name",
                "defaultValue" to ""
            )
        )
        
        val filters = converter.convertToIReaderFilters(jsFilters)
        
        assertEquals(1, filters.size)
        val filter = filters.first()
        assertTrue(filter is Filter.Text)
        assertEquals("Author Name", filter.name)
        assertEquals("", filter.value)
    }
    
    @Test
    fun testConvertCheckboxGroupToGroup() {
        val jsFilters = mapOf(
            "genres" to mapOf(
                "type" to "CheckboxGroup",
                "label" to "Genres",
                "options" to listOf(
                    mapOf("label" to "Action", "value" to "action"),
                    mapOf("label" to "Romance", "value" to "romance"),
                    mapOf("label" to "Fantasy", "value" to "fantasy")
                ),
                "defaultValues" to listOf("action")
            )
        )
        
        val filters = converter.convertToIReaderFilters(jsFilters)
        
        assertEquals(1, filters.size)
        val filter = filters.first()
        assertTrue(filter is Filter.Group)
        assertEquals("Genres", filter.name)
        assertEquals(3, filter.filters.size)
        
        val checkboxes = filter.filters.filterIsInstance<Filter.Check>()
        assertEquals(3, checkboxes.size)
        assertEquals("Action", checkboxes[0].name)
        assertEquals(true, checkboxes[0].value)
        assertEquals(false, checkboxes[1].value)
    }
    
    @Test
    fun testConvertExcludableCheckboxGroupToGroup() {
        val jsFilters = mapOf(
            "genresAdvanced" to mapOf(
                "type" to "ExcludableCheckboxGroup",
                "label" to "Advanced Genre Filter",
                "options" to listOf(
                    mapOf("label" to "Action", "value" to "action"),
                    mapOf("label" to "Romance", "value" to "romance"),
                    mapOf("label" to "Horror", "value" to "horror")
                ),
                "included" to listOf("action"),
                "excluded" to listOf("horror")
            )
        )
        
        val filters = converter.convertToIReaderFilters(jsFilters)
        
        assertEquals(1, filters.size)
        val filter = filters.first()
        assertTrue(filter is Filter.Group)
        assertEquals("Advanced Genre Filter", filter.name)
        
        val checkboxes = filter.filters.filterIsInstance<Filter.Check>()
        assertEquals(3, checkboxes.size)
        assertTrue(checkboxes[0].allowsExclusion)
        assertEquals(true, checkboxes[0].value) // included
        assertEquals(null, checkboxes[1].value) // none
        assertEquals(false, checkboxes[2].value) // excluded
    }
    
    @Test
    fun testConvertSortToSort() {
        val jsFilters = mapOf(
            "sort" to mapOf(
                "type" to "Sort",
                "label" to "Sort By",
                "options" to listOf(
                    mapOf("label" to "Latest", "value" to "latest"),
                    mapOf("label" to "Popular", "value" to "popular"),
                    mapOf("label" to "Rating", "value" to "rating")
                ),
                "defaultValue" to mapOf(
                    "index" to 0,
                    "ascending" to true
                )
            )
        )
        
        val filters = converter.convertToIReaderFilters(jsFilters)
        
        assertEquals(1, filters.size)
        val filter = filters.first()
        assertTrue(filter is Filter.Sort)
        assertEquals("Sort By", filter.name)
        assertEquals(3, filter.options.size)
        assertNotNull(filter.value)
        assertEquals(0, filter.value?.index)
        assertEquals(true, filter.value?.ascending)
    }
    
    @Test
    fun testConvertMultipleFilters() {
        val jsFilters = mapOf(
            "status" to mapOf(
                "type" to "Picker",
                "label" to "Status",
                "options" to listOf(
                    mapOf("label" to "All", "value" to "all"),
                    mapOf("label" to "Ongoing", "value" to "ongoing")
                ),
                "defaultValue" to "all"
            ),
            "author" to mapOf(
                "type" to "TextInput",
                "label" to "Author",
                "defaultValue" to ""
            ),
            "sort" to mapOf(
                "type" to "Sort",
                "label" to "Sort",
                "options" to listOf(
                    mapOf("label" to "Latest", "value" to "latest")
                )
            )
        )
        
        val filters = converter.convertToIReaderFilters(jsFilters)
        
        assertEquals(3, filters.size)
        assertTrue(filters.any { it is Filter.Select })
        assertTrue(filters.any { it is Filter.Text })
        assertTrue(filters.any { it is Filter.Sort })
    }
    
    @Test
    fun testConvertIReaderFiltersBackToJS() {
        val filters = listOf(
            Filter.Text("Author", "John Doe"),
            Filter.Select("Status", arrayOf("All", "Ongoing", "Completed"), 1),
            Filter.Check("Include Genre", false, true)
        )
        
        val jsFilters = converter.convertIReaderFiltersToJS(filters)
        
        assertEquals(3, jsFilters.size)
        assertEquals("John Doe", jsFilters["Author"])
        assertEquals("Ongoing", jsFilters["Status"])
        assertEquals(true, jsFilters["Include Genre"])
    }
    
    @Test
    fun testConvertTriStateCheckbox() {
        val filters = listOf(
            Filter.Check("Mature Content", true, true),
            Filter.Check("Completed Only", true, false),
            Filter.Check("Ignore Status", true, null)
        )
        
        val jsFilters = converter.convertIReaderFiltersToJS(filters)
        
        assertEquals("included", jsFilters["Mature Content"])
        assertEquals("excluded", jsFilters["Completed Only"])
        assertEquals("none", jsFilters["Ignore Status"])
    }
    
    @Test
    fun testRoundTripConversion() {
        // LNReader format -> IReader -> LNReader format
        val originalJsFilters = mapOf(
            "status" to mapOf(
                "type" to "Picker",
                "label" to "Status",
                "options" to listOf(
                    mapOf("label" to "All", "value" to "all"),
                    mapOf("label" to "Ongoing", "value" to "ongoing")
                ),
                "defaultValue" to "ongoing"
            )
        )
        
        val iReaderFilters = converter.convertToIReaderFilters(originalJsFilters)
        val convertedBack = converter.convertIReaderFiltersToJS(iReaderFilters)
        
        // Should have the selected value
        assertEquals("Ongoing", convertedBack["Status"])
    }
}
