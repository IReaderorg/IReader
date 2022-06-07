package ir.kazemcodes.epub.internal.parser.toc

import ir.kazemcodes.epub.epubvalidator.ValidationListeners
import ir.kazemcodes.epub.internal.constants.EpubConstants
import ir.kazemcodes.epub.internal.extensions.firstWithAttributeNS
import ir.kazemcodes.epub.internal.extensions.forEach
import ir.kazemcodes.epub.internal.extensions.getFirstElementByTag
import ir.kazemcodes.epub.internal.extensions.orNullIfEmpty
import ir.kazemcodes.epub.internal.extensions.orValidationError
import ir.kazemcodes.epub.model.EpubTableOfContentsModel
import ir.kazemcodes.epub.model.NavigationItemModel
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.zip.ZipEntry

internal class Epub3TableOfContentsParser : EpubTableOfContentsParser() {

    private var validationListeners: ValidationListeners? = null

    override fun parse(
        tocDocument: Document?,
        validationListeners: ValidationListeners?,
        zipFile: Map<String, Pair<ZipEntry, ByteArray>>
    ): EpubTableOfContentsModel {

        this.validationListeners = validationListeners
        val tableOfContentsReferences = mutableListOf<NavigationItemModel>()
        val tocNav = tocDocument?.getElementsByTagName(NAV_TAG)
            .orValidationError { validationListeners?.onTableOfContentsMissing() }
            ?.firstWithAttributeNS(EpubConstants.ND_NAMESPACE, TYPE_ATTR, TOC_ATTR_VALUE)
            .orValidationError {
                validationListeners?.onAttributeMissing(TOC_ATTR_VALUE, TABLE_OF_CONTENTS_TAG)
            } as Element

        tocNav.getFirstElementByTag(OL_TAG)
            .orValidationError {
                validationListeners?.onAttributeMissing(OL_TAG, TABLE_OF_CONTENTS_TAG)
            }
            ?.childNodes.forEach {
            if (it.isNavPoint()) {
                tableOfContentsReferences.add(createNavigationItemModel(it))
            } else {
                orValidationError {
                    validationListeners?.onAttributeMissing(TABLE_OF_CONTENTS_TAG, NAV_POINT_TAG)
                }
            }
        }

        return EpubTableOfContentsModel(tableOfContentsReferences)
    }

    override fun createNavigationItemModel(node: Node): NavigationItemModel {
        val ref = (node as Element).getFirstElementByTag(A_TAG)
        val label = ref?.textContent
            ?.orNullIfEmpty()
            .orValidationError {
                validationListeners?.onAttributeMissing(LABEL_FIELD_NAME, TABLE_OF_CONTENTS_TAG)
            }
        val source = ref?.getAttribute(HREF_ATTR)
            ?.orNullIfEmpty()
            .orValidationError {
                validationListeners?.onAttributeMissing(HREF_ATTR, TABLE_OF_CONTENTS_TAG)
            }
        val subItems = createNavigationSubItemModel(node.getFirstElementByTag(OL_TAG)?.childNodes)

        return NavigationItemModel(null, label, source, subItems)
    }

    override fun createNavigationSubItemModel(childrenNodes: NodeList?): List<NavigationItemModel> {
        val navSubItems = mutableListOf<NavigationItemModel>()
        childrenNodes?.forEach {
            if (it.isNavPoint()) {
                createNavigationItemModel(it).let { navigationItem ->
                    navSubItems.add(navigationItem)
                }
            }
        }
        return navSubItems
    }

    override fun Node.isNavPoint() =
        (this as? Element)?.tagName == NAV_POINT_TAG

    private companion object {
        private const val NAV_POINT_TAG = "li"
        private const val OL_TAG = "ol"
        private const val A_TAG = "a"
        private const val NAV_TAG = "nav"
        private const val LABEL_FIELD_NAME = "label"
        private const val TYPE_ATTR = "type"
        private const val TOC_ATTR_VALUE = "toc"
        private const val HREF_ATTR = "href"
        private const val TABLE_OF_CONTENTS_TAG = "table of contents"
    }
}