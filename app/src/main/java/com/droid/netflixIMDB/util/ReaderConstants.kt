package com.droid.netflixIMDB.util

class ReaderConstants {

    companion object {
        const val NETFLIX = "com.netflix.mediaclient"
        const val HOTSTAR = "in.startv.hotstar"
        const val PRIME = "com.amazon.avod.thirdpartyclient"
        const val YOUTUBE = "com.google.android.youtube"
        const val MAX_LIMIT = 100
        const val PLAYSTORE_INIT = 12

        val supportedPackages = listOf("com.netflix.mediaclient",
            "in.startv.hotstar", "com.amazon.avod.thirdpartyclient", "com.google.android.youtube")

        /*Payload*/
        const val PACKAGE_NAME = "packageName"
        const val TITLE = "title"
        const val YEAR = "year"
        const val TYPE = "type"
        const val SEARCH = "search"

        /*Purchase*/
        const val PURCHASE_TYPE = "purchase_type"
        const val PURCHASE = "purchase"

        /*Click Events*/
        const val CLICK_TYPE = "click_type"
        const val CLICK = "click"

        const val BASE_URL = "https://www.omdbapi.com/"
    }
}