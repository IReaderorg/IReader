package ir.kazemcodes.epub.internal.parser.toc

import ir.kazemcodes.epub.model.NavigationItemModel
import org.w3c.dom.Node
import org.w3c.dom.NodeList

internal abstract class EpubTableOfContentsParser : TableOfContentsParser {
    protected abstract fun createNavigationItemModel(node: Node ): NavigationItemModel
    protected abstract fun createNavigationSubItemModel(childrenNodes: NodeList? ): List<NavigationItemModel>
    protected abstract fun Node.isNavPoint(): Boolean
}