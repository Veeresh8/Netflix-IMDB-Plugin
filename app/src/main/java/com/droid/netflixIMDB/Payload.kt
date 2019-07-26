package com.droid.netflixIMDB

data class Payload(
    var title: String? = null,
    var year: String? = null,
    var type: String? = null
)

data class ResponsePayload(
    var rating: String? = null,
    var title: String? = null,
    var year: String? = null,
    var type: String? = null
)

data class PayloadCount(
    var netflix: Int = 0,
    var hotstar: Int = 0,
    var youtube: Int = 0,
    var prime: Int = 0,
    var total: Int = netflix.plus(youtube).plus(prime).plus(hotstar)
)