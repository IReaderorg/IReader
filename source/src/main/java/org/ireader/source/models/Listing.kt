package org.ireader.source.models

abstract class Listing(val name: String)

class PopularListing : Listing("popular")
class LatestListing : Listing("latest")
class SearchListing : Listing("search")