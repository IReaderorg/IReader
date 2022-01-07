package ir.kazemcodes.infinity.sources.models


data class Latest(
    val endpoint: String,
    val selector: String,
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
)

data class Popular(
    val endpoint: String,
    val selector: String,
    val isGetRequest: Boolean = true,
    val nextBookSelector: String? = null,
    val nextBookValue: String? = null,
    val linkSelector: String? = null,
    val linkAtt: String? = null,
    val linkSubString: Boolean = false,
    val nameSelector: String? = null,
    val nameAtt: String? = null,
    val coverSelector: String? = null,
    val coverAtt: String? = null,
)

data class Detail(
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
)

data class Search(
    val endpoint: String,
    val selector: String? = null,
    val jsonPath: String? = null,
    val isGetRequest: Boolean = true,
    val isHTMLType: Boolean = true,
    val linkSelector: String? = null,
    val linkAtt: String? = null,
    val linkSearchedSubString: Boolean = false,
    val nameSelector: String? = null,
    val nameAtt: String? = null,
    val coverSelector: String? = null,
    val coverAtt: String? = null,
    val hasNextSearchedBooksNextPageSelector: String? = null,
    val hasNextSearchedBookNextPageValue: String? = null,
)

data class ChapterList(
    val endpoint: String? = null,
    val chaptersEndpointWithoutPage: String? = null,
    val selector: String? = null,
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
    val isHtmlType : Boolean = true,
    val isGetRequest: Boolean=true,
    val chapterPageNumberSelector: String?=null
)

data class Content(
    val endpoint: String? = null,
    val pageTitleSelector : String?=null,
    val pageTitleAtt : String?=null,
    val pageContentSelector: String? = null,
    val pageContentAtt: String? = null,

)



