package com.droid.netflixIMDB

data class MessageEvent (
    var rating: String? = null,
    var title: String? = null,
    var year: String? = null
)

data class RemoveRatingViewEvent(
    var mustRemoveView: Boolean
)

