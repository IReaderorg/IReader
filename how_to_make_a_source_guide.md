# Guide

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
| Parameter | value |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|
|selector| the css selector that contain all detail for just for one book |
|linkSelector| the css selector to the link of book (can be omited if there is no selector) |
|linkAtt| the css attribute to the link of book (can be omited if there is no attribute) |
|nameSelector| the css selector to the book title of book (can be omited if there is no selector) |
|nameAtt| the css attribute to the book title of book (can be omited if there is no attribute) |
|coverSelector| the css selector to the thumbnail link of of book (can be omited if there is no selector) |
|coverAtt| the css attribute to the thumbnail link of book (can be omited if there is no attribute) |
|nextPageValue| type true if the website page  |
|nextPageLinkSelector| the css selector to the next page ,if the website links are not in order you can use this to the link for next page|
|nextPageLinkAttr| the css attribute to the next page ,if the website links are not in order you can use this to the link for next page|
|supportPageList|type true if the website links are not in order and support page list|
|maxPageIndex| if the website pages are not in order you can use this to the link for next page and max number of pages in one site for example google shows only number 1 to 10 and only when you go to page 2 you will see that it will be changes from 2 to 11|




## Search
| Parameter | value |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page} and search query with "{query}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|
|selector| the css selector that contain all detail for just for one book |
|linkSelector| the css selector to the link of book (can be omited if there is no selector) |
|linkAtt| the css attribute to the link of book (can be omited if there is no attribute) |
|nameSelector| the css selector to the book title of book (can be omited if there is no selector) |
|nameAtt| the css attribute to the book title of book (can be omited if there is no attribute) |
|coverSelector| the css selector to the thumbnail link of of book (can be omited if there is no selector) |
|coverAtt| the css attribute to the thumbnail link of book (can be omited if there is no attribute) |

## Popular
| Parameter | value |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/{query}" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|
|selector| the css selector that contain all detail for just for one book |
|linkSelector| the css selector to the link of book (can be omited if there is no selector) |
|linkAtt| the css attribute to the link of book (can be omited if there is no attribute) |
|nameSelector| the css selector to the book title of book (can be omited if there is no selector) |
|nameAtt| the css attribute to the book title of book (can be omited if there is no attribute) |
|coverSelector| the css selector to the thumbnail link of of book (can be omited if there is no selector) |
|coverAtt| the css attribute to the thumbnail link of book (can be omited if there is no attribute) |


## Chapters
| Parameter | value |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|selector| the css selector that contain all detail for just for one book |
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|

## Content
| Parameter | value |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|selector| the css selector that contain all detail for just for one book |
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|






