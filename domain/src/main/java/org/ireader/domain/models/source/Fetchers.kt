package org.ireader.domain.models.source

import androidx.room.ColumnInfo
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable


/**
 * @param endpoint the endpint of site: sample:"https://freewebnovel.com"
 * @param ajaxSelector if the site is loaded by ajax, type the first item that is loaded by ajax.
 * @param isHtmlType ishtmlResponse, type false if is json type
 * @param isGetRequestType is it Get Request, type false if it is Post Request
 * @param addBaseUrlToLink should add the base url to the first part of link,
 *      for example the link you get from site is some thing like this "/chapters/ and
 *      if you type true here the base url for example  is  https://freewebnovel.com
 *      will be added to the link
 * @param openInWebView type true if you want the content to be open in webview
 * @param selector the general selector that cover all detail for one book
 * @param nextPageSelector the css selector for the next page, it must contain a value, to let the app that next page exist.
 *
 */
@Serializable
@JsonClass(generateAdapter = true)
data class Latest(
    @ColumnInfo(name = "latest_endpoint")
    override val endpoint: String? = null,
    @ColumnInfo(name = "latest_ajaxSelector")
    override val ajaxSelector: String? = null,
    @ColumnInfo(name = "latest_isGetRequestType")
    override val isGetRequestType: Boolean = true,
    @ColumnInfo(name = "latest_isHtmlType")
    override val isHtmlType: Boolean = true,
    @ColumnInfo(name = "latest_selector")
    override val selector: String? = null,
    @ColumnInfo(name = "latest_addBaseUrlToLink")
    override val addBaseUrlToLink: Boolean = false,
    @ColumnInfo(name = "latest_openInWebView")
    override val openInWebView: Boolean = false,
    @ColumnInfo(name = "latest_nextPageSelector")
    override val nextPageSelector: String? = null,
    @ColumnInfo(name = "latest_nextPageAtt")
    override val nextPageAtt: String? = null,
    @ColumnInfo(name = "latest_nextPageValue")
    override val nextPageValue: String? = null,
    @ColumnInfo(name = "latest_addToStringEnd")
    override val addToStringEnd: String? = null,
    @ColumnInfo(name = "latest_idSelector")
    override val idSelector: String? = null,
    @ColumnInfo(name = "latest_addBaseurlToCoverLink")
    override val addBaseurlToCoverLink: Boolean = false,
    @ColumnInfo(name = "latest_linkSelector")
    val linkSelector: String? = null,
    @ColumnInfo(name = "latest_linkAtt")
    val linkAtt: String? = null,
    @ColumnInfo(name = "latest_nameSelector")
    val nameSelector: String? = null,
    @ColumnInfo(name = "latest_nameAtt")
    val nameAtt: String? = null,
    @ColumnInfo(name = "latest_coverSelector")
    val coverSelector: String? = null,
    @ColumnInfo(name = "latest_coverAtt")
    val coverAtt: String? = null,
    @ColumnInfo(name = "latest_supportPageList")
    val supportPageList: Boolean = false,
    @ColumnInfo(name = "latest_maxPageIndex")
    val maxPageIndex: Int? = null,

    ) : Fetcher

/**
 * @param endpoint the endpint of site: sample:"https://freewebnovel.com"
 * @param ajaxSelector if the site is loaded by ajax, type the first item that is loaded by ajax.
 * @param isHtmlType ishtmlResponse, type false if is json type
 * @param isGetRequestType is it Get Request, type false if it is Post Request
 * @param addBaseUrlToLink should add the base url to the first part of link,
 *      for example the link you get from site is some thing like this "/chapters/ and
 *      if you type true here the base url for example  is  https://freewebnovel.com
 *      will be added to the link
 * @param openInWebView type true if you want the content to be open in webview
 * @param selector the general selector that cover all detail for one book
 * @param nextPageSelector the css selector for the next page, it must contain a value, to let the app that next page exist.
 *
 */
@Serializable
@JsonClass(generateAdapter = true)
data class Popular(
    @ColumnInfo(name = "popular_endpoint")
    override val endpoint: String? = null,
    @ColumnInfo(name = "popular_ajaxSelector")
    override val ajaxSelector: String? = null,
    @ColumnInfo(name = "popular_isGetRequestType")
    override val isGetRequestType: Boolean = true,
    @ColumnInfo(name = "popular_isHtmlType")
    override val isHtmlType: Boolean = true,
    @ColumnInfo(name = "popular_selector")
    override val selector: String? = null,
    @ColumnInfo(name = "popular_addBaseUrlToLink")
    override val addBaseUrlToLink: Boolean = false,
    @ColumnInfo(name = "popular_openInWebView")
    override val openInWebView: Boolean = false,
    @ColumnInfo(name = "popular_nextPageSelector")
    override val nextPageSelector: String? = null,
    @ColumnInfo(name = "popular_nextPageAtt")
    override val nextPageAtt: String? = null,
    @ColumnInfo(name = "popular_nextPageValue")
    override val nextPageValue: String? = null,
    @ColumnInfo(name = "popular_addToStringEnd")
    override val addToStringEnd: String? = null,
    @ColumnInfo(name = "popular_idSelector")
    override val idSelector: String? = null,
    @ColumnInfo(name = "popular_addBaseurlToCoverLink")
    override val addBaseurlToCoverLink: Boolean = false,
    @ColumnInfo(name = "popular_linkSelector")
    val linkSelector: String? = null,
    @ColumnInfo(name = "popular_linkAtt")
    val linkAtt: String? = null,
    @ColumnInfo(name = "popular_nameSelector")
    val nameSelector: String? = null,
    @ColumnInfo(name = "popular_nameAtt")
    val nameAtt: String? = null,
    @ColumnInfo(name = "popular_coverSelector")
    val coverSelector: String? = null,
    @ColumnInfo(name = "popular_coverAtt")
    val coverAtt: String? = null,
) : Fetcher

/**
 * @param endpoint the endpint of site: sample:"/latest-release-novel/{page}/""
 * @param ajaxSelector if the site is loaded by ajax, type the first item that is loaded by ajax.
 * @param isHtmlType ishtmlResponse, type false if is json type
 * @param isGetRequestType is it Get Request, type false if it is Post Request
 * @param addBaseUrlToLink should add the base url to the first part of link,
 *      for example the link you get from site is some thing like this "/chapters/ and
 *      if you type true here the base url for example  is  https://freewebnovel.com
 *      will be added to the link
 * @param openInWebView type true if you want the content to be open in webview
 * @param selector the general selector that cover all detail for one book
 * @param nextPageSelector the css selector for the next page, it must contain a value, to let the app that next page exist.
 *
 */
@Serializable
@JsonClass(generateAdapter = true)
data class Detail(
    @ColumnInfo(name = "detail_endpoint")
    override val endpoint: String? = null,
    @ColumnInfo(name = "detail_ajaxSelector")
    override val ajaxSelector: String? = null,
    @ColumnInfo(name = "detail_isGetRequestType")
    override val isGetRequestType: Boolean = true,
    @ColumnInfo(name = "detail_isHtmlType")
    override val isHtmlType: Boolean = true,
    @ColumnInfo(name = "detail_selector")
    override val selector: String? = null,
    @ColumnInfo(name = "detail_addBaseUrlToLink")
    override val addBaseUrlToLink: Boolean = false,
    @ColumnInfo(name = "detail_openInWebView")
    override val openInWebView: Boolean = false,
    @ColumnInfo(name = "detail_nextPageSelector")
    override val nextPageSelector: String? = null,
    @ColumnInfo(name = "detail_nextPageAtt")
    override val nextPageAtt: String? = null,
    @ColumnInfo(name = "detail_nextPageValue")
    override val nextPageValue: String? = null,
    @ColumnInfo(name = "detail_addToStringEnd")
    override val addToStringEnd: String? = null,
    @ColumnInfo(name = "detail_idSelector")
    override val idSelector: String? = null,
    @ColumnInfo(name = "detail_addBaseurlToCoverLink")
    override val addBaseurlToCoverLink: Boolean = false,
    @ColumnInfo(name = "detail_nameSelector")
    val nameSelector: String? = null,
    @ColumnInfo(name = "detail_nameAtt")
    val nameAtt: String? = null,
    @ColumnInfo(name = "detail_coverSelector")
    val coverSelector: String? = null,
    @ColumnInfo(name = "detail_coverAtt")
    val coverAtt: String? = null,
    @ColumnInfo(name = "detail_descriptionSelector")
    val descriptionSelector: String? = null,
    @ColumnInfo(name = "detail_descriptionBookAtt")
    val descriptionBookAtt: String? = null,
    @ColumnInfo(name = "detail_authorBookSelector")
    val authorBookSelector: String? = null,
    @ColumnInfo(name = "detail_authorBookAtt")
    val authorBookAtt: String? = null,
    @ColumnInfo(name = "detail_categorySelector")
    val categorySelector: String? = null,
    @ColumnInfo(name = "detail_categoryAtt")
    val categoryAtt: String? = null,
) : Fetcher

/**
 * @param endpoint the endpint of site: sample:"https://freewebnovel.com"
 * @param ajaxSelector if the site is loaded by ajax, type the first item that is loaded by ajax.
 * @param isHtmlType ishtmlResponse, type false if is json type
 * @param isGetRequestType is it Get Request, type false if it is Post Request
 * @param addBaseUrlToLink should add the base url to the first part of link,
 *      for example the link you get from site is some thing like this "/chapters/ and
 *      if you type true here the base url for example  is  https://freewebnovel.com
 *      will be added to the link
 * @param openInWebView type true if you want the content to be open in webview
 * @param selector the general selector that cover all detail for one book
 * @param nextPageSelector the css selector for the next page, it must contain a value, to let the app that next page exist.
 *
 */
@Serializable
@JsonClass(generateAdapter = true)
data class Search(
    @ColumnInfo(name = "search_endpoint")
    override val endpoint: String? = null,
    @ColumnInfo(name = "search_ajaxSelector")
    override val ajaxSelector: String? = null,
    @ColumnInfo(name = "search_isGetRequestType")
    override val isGetRequestType: Boolean = true,
    @ColumnInfo(name = "search_isHtmlType")
    override val isHtmlType: Boolean = true,
    @ColumnInfo(name = "search_selector")
    override val selector: String? = null,
    @ColumnInfo(name = "search_addBaseUrlToLink")
    override val addBaseUrlToLink: Boolean = false,
    @ColumnInfo(name = "search_openInWebView")
    override val openInWebView: Boolean = false,
    @ColumnInfo(name = "search_nextPageSelector")
    override val nextPageSelector: String? = null,
    @ColumnInfo(name = "search_nextPageAtt")
    override val nextPageAtt: String? = null,
    @ColumnInfo(name = "search_nextPageValue")
    override val nextPageValue: String? = null,
    @ColumnInfo(name = "search_addToStringEnd")
    override val addToStringEnd: String? = null,
    @ColumnInfo(name = "search_idSelector")
    override val idSelector: String? = null,
    @ColumnInfo(name = "search_addBaseurlToCoverLink")
    override val addBaseurlToCoverLink: Boolean = false,
    @ColumnInfo(name = "search_linkSelector")
    val linkSelector: String? = null,
    @ColumnInfo(name = "search_linkAtt")
    val linkAtt: String? = null,
    @ColumnInfo(name = "search_nameSelector")
    val nameSelector: String? = null,
    @ColumnInfo(name = "search_nameAtt")
    val nameAtt: String? = null,
    @ColumnInfo(name = "search_coverSelector")
    val coverSelector: String? = null,
    @ColumnInfo(name = "search_coverAtt")
    val coverAtt: String? = null,
) : Fetcher

/**
 * @param endpoint the endpint of site: sample:"https://freewebnovel.com"
 * @param ajaxSelector if the site is loaded by ajax, type the first item that is loaded by ajax.
 * @param isHtmlType ishtmlResponse, type false if is json type
 * @param isGetRequestType is it Get Request, type false if it is Post Request
 * @param addBaseUrlToLink should add the base url to the first part of link,
 *      for example the link you get from site is some thing like this "/chapters/ and
 *      if you type true here the base url for example  is  https://freewebnovel.com
 *      will be added to the link
 * @param openInWebView type true if you want the content to be open in webview
 * @param selector the general selector that cover all detail for one book
 * @param nextPageSelector the css selector for the next page, it must contain a value, to let the app that next page exist.
 *
 */
@Serializable
@JsonClass(generateAdapter = true)
data class Chapters(
    @ColumnInfo(name = "chapters_endpoint")
    override val endpoint: String? = null,
    @ColumnInfo(name = "chapters_ajaxSelector")
    override val ajaxSelector: String? = null,
    @ColumnInfo(name = "chapters_isGetRequestType")
    override val isGetRequestType: Boolean = true,
    @ColumnInfo(name = "chapters_isHtmlType")
    override val isHtmlType: Boolean = true,
    @ColumnInfo(name = "chapters_selector")
    override val selector: String? = null,
    @ColumnInfo(name = "chapters_addBaseUrlToLink")
    override val addBaseUrlToLink: Boolean = false,
    @ColumnInfo(name = "chapters_openInWebView")
    override val openInWebView: Boolean = false,
    @ColumnInfo(name = "chapters_nextPageSelector")
    override val nextPageSelector: String? = null,
    @ColumnInfo(name = "chapters_nextPageAtt")
    override val nextPageAtt: String? = null,
    @ColumnInfo(name = "chapters_nextPageValue")
    override val nextPageValue: String? = null,
    @ColumnInfo(name = "chapters_addToStringEnd")
    override val addToStringEnd: String? = null,
    @ColumnInfo(name = "chapters_idSelector")
    override val idSelector: String? = null,
    @ColumnInfo(name = "chapter_addBaseurlToCoverLink")
    override val addBaseurlToCoverLink: Boolean = false,
    @ColumnInfo(name = "chapters_isDownloadable")
    val isDownloadable: Boolean = false,
    @ColumnInfo(name = "chapters_chaptersEndpointWithoutPage")
    val chaptersEndpointWithoutPage: String? = null,
    @ColumnInfo(name = "chapters_isChapterStatsFromFirst")
    val isChapterStatsFromFirst: Boolean = true,
    @ColumnInfo(name = "chapters_linkSelector")
    val linkSelector: String? = null,
    @ColumnInfo(name = "chapters_linkAtt")
    val linkAtt: String? = null,
    @ColumnInfo(name = "chapters_nameSelector")
    val nameSelector: String? = null,
    @ColumnInfo(name = "chapters_nameAtt")
    val nameAtt: String? = null,
    @ColumnInfo(name = "chapters_supportNextPagesList")
    val supportNextPagesList: Boolean = false,
    @ColumnInfo(name = "chapters_subStringSomethingAtEnd")
    val subStringSomethingAtEnd: String? = null,
) : Fetcher

/**
 * @param endpoint the endpint of site: sample:"https://freewebnovel.com"
 * @param ajaxSelector if the site is loaded by ajax, type the first item that is loaded by ajax.
 * @param isHtmlType ishtmlResponse, type false if is json type
 * @param isGetRequestType is it Get Request, type false if it is Post Request
 * @param addBaseUrlToLink should add the base url to the first part of link,
 *      for example the link you get from site is some thing like this "/chapters/ and
 *      if you type true here the base url for example  is  https://freewebnovel.com
 *      will be added to the link
 * @param openInWebView type true if you want the content to be open in webview
 * @param selector the general selector that cover all detail for one book
 * @param nextPageSelector the css selector for the next page, it must contain a value, to let the app that next page exist.
 *
 */
@Serializable
@JsonClass(generateAdapter = true)
data class Content(
    @ColumnInfo(name = "content_endpoint")
    override val endpoint: String? = null,
    @ColumnInfo(name = "content_ajaxSelector")
    override val ajaxSelector: String? = null,
    @ColumnInfo(name = "content_isGetRequestType")
    override val isGetRequestType: Boolean = true,
    @ColumnInfo(name = "content_isHtmlType")
    override val isHtmlType: Boolean = true,
    @ColumnInfo(name = "content_selector")
    override val selector: String? = null,
    @ColumnInfo(name = "content_addBaseUrlToLink")
    override val addBaseUrlToLink: Boolean = false,
    @ColumnInfo(name = "content_openInWebView")
    override val openInWebView: Boolean = false,
    @ColumnInfo(name = "content_nextPageSelector")
    override val nextPageSelector: String? = null,
    @ColumnInfo(name = "content_nextPageAtt")
    override val nextPageAtt: String? = null,
    @ColumnInfo(name = "content_nextPageValue")
    override val nextPageValue: String? = null,
    @ColumnInfo(name = "content_addToStringEnd")
    override val addToStringEnd: String? = null,
    @ColumnInfo(name = "content_idSelector")
    override val idSelector: String? = null,
    @ColumnInfo(name = "content_addBaseurlToCoverLink")
    override val addBaseurlToCoverLink: Boolean = false,
    @ColumnInfo(name = "content_pageTitleSelector")
    val pageTitleSelector: String? = null,
    @ColumnInfo(name = "content_pageTitleAtt")
    val pageTitleAtt: String? = null,
    @ColumnInfo(name = "content_pageContentSelector")
    val pageContentSelector: String? = null,
    @ColumnInfo(name = "content_pageContentAtt")
    val pageContentAtt: String? = null,
) : Fetcher


interface Fetcher {
    val endpoint: String?
    val ajaxSelector: String?
    val isHtmlType: Boolean
    val isGetRequestType: Boolean
    val selector: String?
    val addBaseUrlToLink: Boolean
    val addToStringEnd: String?
    val idSelector: String?
    val openInWebView: Boolean
    val nextPageSelector: String?
    val nextPageAtt: String?
    val nextPageValue: String?
    val addBaseurlToCoverLink: Boolean?
}

sealed class FetchType(val index: Int) {
    object LatestFetchType : FetchType(0)
    object PopularFetchType : FetchType(1)
    object SearchFetchType : FetchType(2)
    object DetailFetchType : FetchType(3)
    object ContentFetchType : FetchType(4)
    object ChapterFetchType : FetchType(5)
}



