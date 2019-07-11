package com.droid.netflixIMDB

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import retrofit2.Response

object RatingRequester {

    private val TAG: String = javaClass.simpleName
    private var response: Response<OMDBResponse>? = null

    var lastYear: String? = null
    var lastTitle: String? = null

    interface RatingRequesterCallback {
        fun onSuccess(responsePayload: ResponsePayload)
        fun onRequestException(exception: Exception)
        fun onFailure(message: String)
    }

    fun requestRating(
        payload: Payload,
        ratingRequesterCallback: RatingRequesterCallback
    ) {
        try {
            runBlocking(Dispatchers.IO) {

                Log.i(TAG, "Requesting rating for $payload")

                val type = payload.type
                val title = payload.title
                val year = payload.year

                if (type == null) {
                    response = NetworkManager.getInstance()?.getRatingAsync(title, null, year)?.await()
                } else if (type == "series" && year != null) {
                    response = NetworkManager.getInstance()?.getRatingAsync(title, type, null)?.await()
                }

                response?.let { response ->
                    if (response.isSuccessful) {
                        when (response.code()) {
                            200 -> {
                                val rating = response.body()?.imdbRating
                                val itemType = response.body()?.Type

                                val responsePayload = ResponsePayload(rating, title, year, itemType)

                                lastYear = year
                                lastTitle = title

                                ratingRequesterCallback.onSuccess(responsePayload)
                            }
                            500 -> {
                                Log.e(TAG, "OMDB server error: ${response.message()}")
                                ratingRequesterCallback.onFailure("Server did not respond, try after sometime")
                            }
                            else -> {
                                Log.e(
                                    TAG,
                                    "Failed to fetch rating: ${response.message()}"
                                )
                                ratingRequesterCallback.onFailure("Failed to fetch ratings, try again!")
                            }
                        }
                    } else {
                        Log.e(TAG, "Response was not successful: ${response.message()}")
                        ratingRequesterCallback.onFailure("Failed to fetch ratings, try again!")
                    }
                }
            }
        } catch (exception: Exception) {
            ratingRequesterCallback.onRequestException(exception)
        }
    }
}