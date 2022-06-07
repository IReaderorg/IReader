package org.ireader.domain.use_cases.epub.epup_parser.internal.parser.toc

import org.ireader.domain.use_cases.epub.epup_parser.model.NavigationItemModel
import org.w3c.dom.Node
import org.w3c.dom.NodeList

internal abstract class EpubTableOfContentsParser : TableOfContentsParser {
    protected abstract fun createNavigationItemModel(node: Node ): NavigationItemModel
    protected abstract fun createNavigationSubItemModel(childrenNodes: NodeList? ): List<NavigationItemModel>
    protected abstract fun Node.isNavPoint(): Boolean
}