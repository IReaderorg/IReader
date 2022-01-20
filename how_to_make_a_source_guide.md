# Guide

This is a guide about how to make a source, you can take a look at [samples](https://github.com/kazemcodes/Infinity/blob/master/app/src/main/java/ir/kazemcodes/infinity/sources/SourceList.kt), in order to understand how everything, work.

The first thing, everyone should know, all unnecessary fields can be omitted, and you should not type them or you can set the value as “null”.
You need to fill this field in order to have full source functionality:
``` kotlin
SourceCreator(
    context,
    _name = "",
    _baseUrl = "",
    _lang = "",
    _supportsLatest = false,
    _supportsMostPopular = false,
    _supportsSearch = false,
    latest = Latest(
        endpoint = null,
        ajaxSelector = null,
        isGetRequestType = true,
        isHtmlType = true,
        selector = null,
        addBaseUrlToLink = false,
        openInWebView = false,
        nextPageSelector = null,
        nextPageAtt = null,
        nextPageValue =null ,
        linkSelector = null,
        linkAtt = null,
        nameSelector = null,
        nameAtt = null,
        coverSelector = null,
        coverAtt = null,
        supportPageList = false,
        maxPageIndex = null,
    ),
    popular = Popular(
        endpoint = null,
        ajaxSelector = null,
        isGetRequestType = true,
        isHtmlType = true,
        selector = null,
        addBaseUrlToLink = false,
        openInWebView = false,
        nextPageSelector =null ,
        nextPageAtt = null,
        nextPageValue = null,
        linkSelector = null,
        linkAtt = null,
        nameSelector = null,
        nameAtt = null,
        coverSelector = null,
        coverAtt = null,
    ),
    search = Search(
        endpoint = null,
        ajaxSelector = null,
        isGetRequestType = true,
        isHtmlType = true,
        selector = null,
        addBaseUrlToLink = false,
        openInWebView = false,
        nextPageSelector = null,
        nextPageAtt = null,
        nextPageValue = null,
        linkSelector = null,
        linkAtt = null,
        nameSelector = null,
        nameAtt = null,
        coverSelector = null,
        coverAtt = null,
    ),
    detail = Detail(
        endpoint = null,
        ajaxSelector = null,
        isGetRequestType = true,
        isHtmlType = true,
        selector = null,
        addBaseUrlToLink = false,
        openInWebView = false,
        nextPageSelector = null,
        nextPageAtt = null,
        nextPageValue = null,
        nameSelector = null,
        nameAtt = null,
        coverSelector = null,
        coverAtt = null,
        descriptionSelector = null,
        descriptionBookAtt = null,
        authorBookSelector = null,
        authorBookAtt = null,
        categorySelector = null,
        categoryAtt = null,
    ),
    chapters = Chapters(
        endpoint = null,
        ajaxSelector = null,
        isGetRequestType = true,
        isHtmlType = true,
        selector = null,
        addBaseUrlToLink = false,
        openInWebView = false,
        nextPageSelector = null,
        nextPageAtt = null,
        nextPageValue = null,
        nameSelector = null,
        nameAtt = null,
        linkSelector = null,
        linkAtt = null,
        supportNextPagesList = false,
        chaptersEndpointWithoutPage = null,
        isChapterStatsFromFirst = true,
        isDownloadable = false,
        subStringSomethingAtEnd = null
    ),
    content = Content(
        endpoint = null,
        ajaxSelector = null,
        isGetRequestType = true,
        isHtmlType = true,
        selector = null,
        addBaseUrlToLink = false,
        openInWebView = false,
        nextPageSelector = null,
        nextPageAtt = null,
        nextPageValue = null,
        pageContentSelector = null,
        pageTitleAtt = null,
        pageTitleSelector = null,
        pageContentAtt = null,
    ),
)
```
  


## Main Parameter
| Parameter | value |
|-------|---------|
|context|No need to change The value of this parameter|
| _name | the name of source - this name will shown inside app|
| _baseUrl | the base url of source - need to be like : "https://freewebnovel.com" without "/" at end   |
| _lang | the language of source - this name will shown inside app |
| _supportsLatest| type 'true' if your app support getting latest novels  else 'false'|
| _supportsSearch| type 'true' if your app support getting Search novels else 'false' |
| _supportsMostPopular| 'true' true if your app support getting Most Popular  novels else 'false'|

NOTE : ALL Uneeded Parameter can be left empty. 
## Latest
| Parameter | Description |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be omitted if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be omitted )|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be omitted )|
|addBaseUrlToLink| type true if the link to book is incomplete( without base url like "/chapters/)  default is false (can be omitted )|
|nextPageSelector| the CSS selector for the next page, it must contain a value, to let the app that next page exist|
|nextPageValue| type true if the website page  |
|nextPageAtt| the CSS attribute to the next page ,if the website links are not in order you can use this to the link for next page|
|selector| the CSS selector that contain all detail for just for one book |
|linkSelector| the CSS selector to the link of book (can be omitted if there is no selector) |
|linkAtt| the CSS attribute to the link of book (can be omitted if there is no attribute) |
|nameSelector| the CSS selector to the book title of book (can be omitted if there is no selector) |
|nameAtt| the CSS attribute to the book title of book (can be omitted if there is no attribute) |
|coverSelector| the CSS selector to the thumbnail link of book (can be omitted if there is no selector) |
|coverAtt| the CSS attribute to the thumbnail link of book (can be omitted if there is no attribute) |
|supportPageList|type true if the website links are not in order and support page list|
|maxPageIndex| if the website pages are not in order you can use this to the link for next page and max number of pages in one site for example google shows only number 1 to 10 and only when you go to page 2 you will see that it will be changes from 2 to 11|




## Search
| Parameter | Description |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page} and search query with "{query}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be omitted if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be omitted )|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be omitted )|
|addBaseUrlToLink| type true if the link to book is incomplete( without base url like "/chapters/)  default is false (can be omitted)|
|nextPageSelector| the CSS selector for the next page, it must contain a value, to let the app that next page exist|
|nextPageValue| type true if the website page  |
|nextPageAtt| the CSS attribute to the next page ,if the website links are not in order you can use this to the link for next page|
|selector| the CSS selector that contain all detail for just for one book |
|linkSelector| the CSS selector to the link of book (can be omitted if there is no selector) |
|linkAtt| the CSS attribute to the link of book (can be omitted if there is no attribute) |
|nameSelector| the CSS selector to the book title of book (can be omitted if there is no selector) |
|nameAtt| the CSS attribute to the book title of book (can be omitted if there is no attribute) |
|coverSelector| the CSS selector to the thumbnail link of book (can be omitted if there is no selector) |
|coverAtt| the CSS attribute to the thumbnail link of book (can be omitted if there is no attribute) |


## Popular
| Parameter | Description |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/{query}" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be omitted if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be omitted )|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be omitted )|
|addBaseUrlToLink| type true if the link to book is incomplete( without base url like "/chapters/)  default is false (can be omitted )|
|nextPageSelector| the CSS selector for the next page, it must contain a value, to let the app that next page exist|
|nextPageValue| type true if the website page  |
|nextPageAtt| the CSS attribute to the next page ,if the website links are not in order you can use this to the link for next page|
|selector| the CSS selector that contain all detail for just for one book |
|linkSelector| the CSS selector to the link of book (can be omitted if there is no selector) |
|linkAtt| the CSS attribute to the link of book (can be omitted if there is no attribute) |
|nameSelector| the CSS selector to the book title of book (can be omitted if there is no selector) |
|nameAtt| the CSS attribute to the book title of book (can be omitted if there is no attribute) |
|coverSelector| the CSS selector to the thumbnail link of of book (can be omitted if there is no selector) |
|coverAtt| the CSS attribute to the thumbnail link of book (can be omitted if there is no attribute) |


## Chapters
| Parameter | Description |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be omitted if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be omitted )|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be omitted )|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be omitted )|
|selector| the CSS selector that contain all detail for just for one book |
|nextPageSelector| the CSS selector for the next page, it must contain a value, to let the app that next page exist|
|linkSelector| the CSS selector to the link of chapter (can be omitted if there is no selector) |
|linkAtt| the CSS attribute to the link of chapter (can be omitted if there is no attribute) |
|nameSelector| the CSS selector to the title of chapter (can be omitted if there is no selector) |
|nameAtt| the CSS attribute to the title of chapter (can be omitted if there is no attribute) |
|isChapterStatsFromFirst| type false if the chapters start to end else type "true" (can be empty)|
|subStringSomethingAtEnd| type what it should add at end it need to be with out "/" at the beginning |


## Content

| Parameter | Description |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be omitted if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be omitted )|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be omitted )|
|addBaseUrlToLink| type true if the link to book is incomplete( without base url like "/chapters/)  default is false (can be omitted )|
|selector| the CSS selector that contain all html related to reading content |
|pageTitleSelector| the CSS selector to the page title of chapter (can be omitted if there is no selector) |
|pageTitleAtt| the CSS attribute to the page title of chapter (can be omitted if there is no attribute) |
|pageContentSelector| the CSS selector the chapter content of chapter (can be omitted if there is no selector) |
|pageContentAtt| the CSS attribute to the chapter content of chapter (can be omitted if there is no attribute) |

##Detail
| Parameter | Description |
|-------|---------|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be omitted if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be omitted )|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be omitted )|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be omitted )|
|selector| the CSS selector that contain all detail for just for one book |
|linkSelector| the CSS selector to the link of book (can be omitted if there is no selector) |
|linkAtt| the CSS attribute to the link of book (can be omitted if there is no attribute) |
|nameSelector| the CSS selector to the book title of book (can be omitted if there is no selector) |
|nameAtt| the CSS attribute to the book title of book (can be omitted if there is no attribute) |
|coverSelector| the CSS selector to the thumbnail link of of book (can be omitted if there is no selector) |
|coverAtt| the CSS attribute to the thumbnail link of book (can be omitted if there is no attribute) |
|descriptionSelector| the CSS selector to the description of of book (can be omitted if there is no selector) |
|descriptionBookAtt| the CSS attribute to the description of book (can be omitted if there is no attribute) |
|authorBookSelector| the CSS selector to the author of of book (can be omitted if there is no selector) |
|authorBookAtt| the CSS attribute to the author link of book (can be omitted if there is no attribute) |
|categorySelector| the CSS selector to the category link of of book (can be omitted if there is no selector) |
|categoryAtt| the CSS attribute to the category link of book (can be omitted if there is no attribute) |



