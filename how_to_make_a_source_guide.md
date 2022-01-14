# Guide

This is a guide about how to make a source, you can take a look at [samples](https://github.com/kazemcodes/Infinity/blob/master/app/src/main/java/ir/kazemcodes/infinity/sources/SourceList.kt), in order to understand how everything work.

## Main Parameter
| Parameter | value |
|-------|---------|
|context|No need to change The value of this parameter|
| _name | the name of source - this name will shown inside app|
| _baseUrl | the base url of source - need to be like : "https://freewebnovel.com" without "/" at end   |
| _lang | the language of source - this name will shown inside app |
| _supportsLatest| type 'true' if your app support getting latest novels  else 'false'|
| _supportsSearch| type 'true' if your app support getting Most Popular novels else 'false' |
| _supportsMostPopular| 'true' true if your app support getting Search novels else 'false'|

NOTE : ALL Uneeded Parameter can be left empty. 
## Latest
| Parameter | Description |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|
|nextPageValue| type true if the website page  |
|nextPageAtt| the css attribute to the next page ,if the website links are not in order you can use this to the link for next page|
|selector| the css selector that contain all detail for just for one book |
|linkSelector| the css selector to the link of book (can be omited if there is no selector) |
|linkAtt| the css attribute to the link of book (can be omited if there is no attribute) |
|nameSelector| the css selector to the book title of book (can be omited if there is no selector) |
|nameAtt| the css attribute to the book title of book (can be omited if there is no attribute) |
|coverSelector| the css selector to the thumbnail link of of book (can be omited if there is no selector) |
|coverAtt| the css attribute to the thumbnail link of book (can be omited if there is no attribute) |
|supportPageList|type true if the website links are not in order and support page list|
|maxPageIndex| if the website pages are not in order you can use this to the link for next page and max number of pages in one site for example google shows only number 1 to 10 and only when you go to page 2 you will see that it will be changes from 2 to 11|




## Search
| Parameter | Description |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page} and search query with "{query}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|
|nextPageValue| type true if the website page  |
|nextPageAtt| the css attribute to the next page ,if the website links are not in order you can use this to the link for next page|
|selector| the css selector that contain all detail for just for one book |
|linkSelector| the css selector to the link of book (can be omited if there is no selector) |
|linkAtt| the css attribute to the link of book (can be omited if there is no attribute) |
|nameSelector| the css selector to the book title of book (can be omited if there is no selector) |
|nameAtt| the css attribute to the book title of book (can be omited if there is no attribute) |
|coverSelector| the css selector to the thumbnail link of of book (can be omited if there is no selector) |
|coverAtt| the css attribute to the thumbnail link of book (can be omited if there is no attribute) |


## Popular
| Parameter | Description |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/{query}" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|
|nextPageValue| type true if the website page  |
|nextPageAtt| the css attribute to the next page ,if the website links are not in order you can use this to the link for next page|
|selector| the css selector that contain all detail for just for one book |
|linkSelector| the css selector to the link of book (can be omited if there is no selector) |
|linkAtt| the css attribute to the link of book (can be omited if there is no attribute) |
|nameSelector| the css selector to the book title of book (can be omited if there is no selector) |
|nameAtt| the css attribute to the book title of book (can be omited if there is no attribute) |
|coverSelector| the css selector to the thumbnail link of of book (can be omited if there is no selector) |
|coverAtt| the css attribute to the thumbnail link of book (can be omited if there is no attribute) |


## Chapters
| Parameter | Description |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|selector| the css selector that contain all detail for just for one book |
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|
|linkSelector| the css selector to the link of chapter (can be omited if there is no selector) |
|linkAtt| the css attribute to the link of chapter (can be omited if there is no attribute) |
|nameSelector| the css selector to the title of chapter (can be omited if there is no selector) |
|nameAtt| the css attribute to the title of chapter (can be omited if there is no attribute) |
|isChapterStatsFromFirst| type false if the chapters start to end else type "true" (can be empty)|
|subStringSomethingAtEnd| type what it should add at end it need to be with out "/" at the beginning |


## Content

| Parameter | Description |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|selector| the css selector that contain all html related to reading content |
|pageTitleSelector| the css selector to the page title of chapter (can be omited if there is no selector) |
|pageTitleAtt| the css attribute to the page title of chapter (can be omited if there is no attribute) |
|pageContentSelector| the css selector the chapter content of chapter (can be omited if there is no selector) |
|pageContentAtt| the css attribute tothe chapter content of chapter (can be omited if there is no attribute) |

##Detail
| Parameter | Description |
|-------|---------|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|selector| the css selector that contain all detail for just for one book |
|linkSelector| the css selector to the link of book (can be omited if there is no selector) |
|linkAtt| the css attribute to the link of book (can be omited if there is no attribute) |
|nameSelector| the css selector to the book title of book (can be omited if there is no selector) |
|nameAtt| the css attribute to the book title of book (can be omited if there is no attribute) |
|coverSelector| the css selector to the thumbnail link of of book (can be omited if there is no selector) |
|coverAtt| the css attribute to the thumbnail link of book (can be omited if there is no attribute) |
|descriptionSelector| the css selector to the description of of book (can be omited if there is no selector) |
|descriptionBookAtt| the css attribute to the description of book (can be omited if there is no attribute) |
|authorBookSelector| the css selector to the author of of book (can be omited if there is no selector) |
|authorBookAtt| the css attribute to the author link of book (can be omited if there is no attribute) |
|categorySelector| the css selector to the category link of of book (can be omited if there is no selector) |
|categoryAtt| the css attribute to the category link of book (can be omited if there is no attribute) |



