package ir.kazemcodes.infinity.sources.models


data class Latest(
    override val endpoint: String? = null,
    override val ajaxSelector: String? =null,
    override val isGetRequestType: Boolean = true,
    override val isHtmlType: Boolean= true,
    override val selector: String? = null,
    val nextPageSelector: String? = null,
    val nextPageValue: String? = null,
    val linkSelector: String? = null,
    val linkAtt: String? = null,
    val linkSubString: Boolean = false,
    val nameSelector: String? = null,
    val nameAtt: String? = null,
    val coverSelector: String? = null,
    val coverAtt: String? = null,
    val supportPageList: Boolean = false,
    val nextPageLinkSelector: String? = null,
    val nextPageLinkAtt: String? = null,
    val maxPageIndex: Int? = null,
) : Fetcher

data class Popular(
    override val endpoint: String? = null,
    override val ajaxSelector: String? =null,
    override val isGetRequestType: Boolean = true,
    override val isHtmlType: Boolean= true,
    override val selector: String? = null,
    val nextBookSelector: String? = null,
    val nextBookValue: String? = null,
    val linkSelector: String? = null,
    val linkAtt: String? = null,
    val linkSubString: Boolean = false,
    val nameSelector: String? = null,
    val nameAtt: String? = null,
    val coverSelector: String? = null,
    val coverAtt: String? = null,
) : Fetcher

data class Detail(
    override val endpoint: String? = null,
    override val ajaxSelector: String? =null,
    override val isGetRequestType: Boolean = true,
    override val isHtmlType: Boolean= true,
    override val selector: String? = null,
    val nameSelector: String? = null,
    val nameAtt: String? = null,
    val coverSelector: String? = null,
    val coverAtt: String? = null,
    val descriptionSelector: String? = null,
    val descriptionBookAtt: String? = null,
    val authorBookSelector: String? = null,
    val authorBookAtt: String? = null,
    val categorySelector: String? = null,
    val categoryAtt: String? = null,
) : Fetcher

data class Search(
    override val endpoint: String? = null,
    override val ajaxSelector: String? =null,
    override val isGetRequestType: Boolean = true,
    override val isHtmlType: Boolean= true,
    override val selector: String? = null,
    val linkSelector: String? = null,
    val linkAtt: String? = null,
    val linkSearchedSubString: Boolean = false,
    val nameSelector: String? = null,
    val nameAtt: String? = null,
    val coverSelector: String? = null,
    val coverAtt: String? = null,
    val hasNextSearchedBooksNextPageSelector: String? = null,
    val hasNextSearchedBookNextPageValue: String? = null,
) : Fetcher

data class Chapters(
    override val endpoint: String? = null,
    override val ajaxSelector: String? =null,
    override val isGetRequestType: Boolean = true,
    override val isHtmlType: Boolean= true,
    override val selector: String? = null,
    val chaptersEndpointWithoutPage: String? = null,
    val _isChapterStatsFromFirst: Boolean? = null,
    val nameSelector: String? = null,
    val nameAtt: String? = null,
    val linkSelector: String? = null,
    val linkAtt: String? = null,
    val linkSubString: Boolean = false,
    val supportNextPagesList: Boolean = false,
    val hasNextChapterListSelector: String? = null,
    val hasNextChapterListAtt: String? = null,
    val hasNextChapterListValue: String? = null,
    val _shouldSubstringBaseUrlAtFirst: Boolean? = null,
    val _shouldStringSomethingAtEnd: Boolean = false,
    val _subStringSomethingAtEnd: String?=null,
    val chapterPageNumberSelector: String?=null,
) : Fetcher

data class Content(
    override val endpoint: String? = null,
    override val ajaxSelector: String? = null,
    override val isHtmlType: Boolean = true,
    override val isGetRequestType: Boolean = true,
    override val selector: String? = null,
    val pageTitleSelector : String?=null,
    val pageTitleAtt : String?=null,
    val pageContentSelector: String? = null,
    val pageContentAtt: String? = null,
    ) : Fetcher

interface Fetcher {
    val endpoint: String?
    val ajaxSelector : String?
    val isHtmlType : Boolean
    val isGetRequestType: Boolean
    val selector: String?
}



