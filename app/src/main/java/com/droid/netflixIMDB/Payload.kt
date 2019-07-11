package com.droid.netflixIMDB

data class Payload (
    var title: String? = null,
    var year: String? = null,
    var type: String? = null
)

data class ResponsePayload (
    var rating: String? = null,
    var title: String? = null,
    var year: String? = null,
    var type: String? = null
)