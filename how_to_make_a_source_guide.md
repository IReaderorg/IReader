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
|selector| the css selector that contain all detail for just for one book |
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|


## Search
| Parameter | value |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|selector| the css selector that contain all detail for just for one book |
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|

## Popular
| Parameter | value |
|-------|---------|
| endpoint | the endpoint of site: sample:"/latest-release-novel/{page}/" Note: replace the page number of novel to "{page}"|
| ajaxSelector | if the site is loaded by ajax, type the first item that is loaded by ajax. (can be ommited if Ajax is not supported)|
| isHtmlType | type "true" if it is html Response, type "false" if is json type, the default value is true.(can be ommited)|
|isGetRequestType| type "true" if is GET Request else type false for Post Request, default is true (can be ommited)|
|addBaseUrlToLink| type true if the link to book is incomplete( without baseurl like "/chapters/)  default is false (can be ommited)|
|selector| the css selector that contain all detail for just for one book |
|nextPageSelector| the css selector for the next page, it must contain a value, to let the app that next page exist|


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






