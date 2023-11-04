package com.droid.netflixIMDB.payments

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class BillingViewModel : ViewModel() {

    private val TAG = "BillingViewModel"

    var billingClient =
        BillingClient(com.droid.netflixIMDB.Application.instance?.applicationContext!!)
    private var repo = SubscriptionRepository(billingClient)

    private val _billingConnectionState = MutableLiveData(false)
    val billingConnectionState: LiveData<Boolean> = _billingConnectionState

    private val _destinationScreen = MutableLiveData<DestinationScreen>()
    val destinationScreen: LiveData<DestinationScreen> = _destinationScreen


    val productsForSaleFlows =
        combine(
            repo.basicProductDetails,
        ) { basicProductDetails ->
            MainState(basicProductDetails = basicProductDetails.firstOrNull())
        }

    private val userCurrentSubscriptionFlow =
        combine(repo.hasBasicSubscription) { hasBasicSubscription ->
            MainState(hasBasicSubscription = hasBasicSubscription.firstOrNull() ?: false)
        }

    private fun billingFlowParamsBuilder(
        productDetails: ProductDetails,
        offerToken: String
    ): BillingFlowParams.Builder {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        )
    }

    private fun upDowngradeBillingFlowParamsBuilder(
        productDetails: ProductDetails,
        offerToken: String,
        oldToken: String
    ): BillingFlowParams {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        ).setSubscriptionUpdateParams(
            BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(oldToken)
                .setReplaceProrationMode(
                    BillingFlowParams.ProrationMode.IMMEDIATE_AND_CHARGE_FULL_PRICE
                )
                .build()
        ).build()
    }

    private fun leastPricedOfferToken(
        offerDetails: List<ProductDetails.SubscriptionOfferDetails>
    ): String {
        var offerToken = String()
        var leastPricedOffer: ProductDetails.SubscriptionOfferDetails
        var lowestPrice = Int.MAX_VALUE

        if (!offerDetails.isNullOrEmpty()) {
            for (offer in offerDetails) {
                for (price in offer.pricingPhases.pricingPhaseList) {
                    if (price.priceAmountMicros < lowestPrice) {
                        lowestPrice = price.priceAmountMicros.toInt()
                        leastPricedOffer = offer
                        offerToken = leastPricedOffer.offerToken
                    }
                }
            }
        }
        return offerToken
    }

    private fun retrieveEligibleOffers(
        offerDetails: MutableList<ProductDetails.SubscriptionOfferDetails>,
        tag: String
    ): List<ProductDetails.SubscriptionOfferDetails> {
        val eligibleOffers = emptyList<ProductDetails.SubscriptionOfferDetails>().toMutableList()
        offerDetails.forEach { offerDetail ->
            if (offerDetail.offerTags.contains(tag)) {
                eligibleOffers.add(offerDetail)
            }
        }

        return eligibleOffers
    }


    // Current purchases.
    val currentPurchasesFlow = repo.purchases

    fun buy(
        productDetails: ProductDetails,
        currentPurchases: List<Purchase>?,
        activity: Activity,
        tag: String
    ) {
        val offers =
            productDetails.subscriptionOfferDetails?.let {
                retrieveEligibleOffers(offerDetails = it, tag = tag.lowercase())
            }
        val offerToken = offers?.let { leastPricedOfferToken(it) }
        val oldPurchaseToken: String

        // Get current purchase. In this app, a user can only have one current purchase at
        // any given time.
        if (!currentPurchases.isNullOrEmpty() &&
            currentPurchases.size == 1
        ) {
            // This either an upgrade, downgrade, or conversion purchase.
            val currentPurchase = currentPurchases.first()

            // Get the token from current purchase.
            oldPurchaseToken = currentPurchase.purchaseToken

            val billingParams =
                offerToken?.let {
                    upDowngradeBillingFlowParamsBuilder(
                        productDetails = productDetails,
                        offerToken = it,
                        oldToken = oldPurchaseToken
                    )
                }

            if (billingParams != null) {
                billingClient.launchBillingFlow(activity, billingParams)
            }
        } else if (currentPurchases == null) {
            // This is a normal purchase.
            val billingParams =
                offerToken?.let {
                    billingFlowParamsBuilder(productDetails = productDetails, offerToken = it)
                }

            if (billingParams != null) {
                billingClient.launchBillingFlow(activity, billingParams.build())
            }
        } else if (!currentPurchases.isNullOrEmpty() &&
            currentPurchases.size > 1
        ) {
            // The developer has allowed users  to have more than 1 purchase, so they need to
            /// implement a logic to find which one to use.
            Log.d(TAG, "User has more than 1 current purchase.")
        }
    }

    init {
        billingClient.startBillingConnection(billingConnectionState = _billingConnectionState)
    }

    init {
        viewModelScope.launch {
            userCurrentSubscriptionFlow.collectLatest { collectedSubscriptions ->
                when (collectedSubscriptions.hasBasicSubscription) {
                    true -> {
                        _destinationScreen.postValue(DestinationScreen.BASIC_RENEWABLE_PROFILE)
                    }

                    else -> {
                        _destinationScreen.postValue(DestinationScreen.SUBSCRIPTIONS_OPTIONS_SCREEN)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        billingClient.terminateBillingConnection()
    }

    data class MainState(
        val hasBasicSubscription: Boolean? = false,
        val basicProductDetails: ProductDetails? = null,
        val purchases: List<Purchase>? = null
    )

    enum class DestinationScreen {
        SUBSCRIPTIONS_OPTIONS_SCREEN,
        BASIC_RENEWABLE_PROFILE
    }
}
