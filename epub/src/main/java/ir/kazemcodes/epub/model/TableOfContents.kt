 package ir.kazemcodes.epub.model

/**
 * Model of publication table of contents.
 *
 * @property tableOfContents Table of contents elements list.
 */
data class EpubTableOfContentsModel(val tableOfContents: List<NavigationItemModel>)

/**
 * Model of table of contents item.
 *
 * @property id Manifest id of element.
 * @property label Element label.
 * @property location Element location.
 * @property subItems List of table of contents children of this element.
 */
data class NavigationItemModel(
    val id: String? = null,
    val label: String? = null,
    val location: String? = null,
    val subItems: List<NavigationItemModel>? = null,
)