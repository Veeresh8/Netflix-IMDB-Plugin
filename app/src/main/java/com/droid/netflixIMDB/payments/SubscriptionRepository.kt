package com.droid.netflixIMDB.payments

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class SubscriptionRepository(billingClientWrapper: BillingClient) {

    private val BASIC_SUB = "basic_subscription"

    // Set to true when a returned purchase is an auto-renewing basic subscription.
    val hasBasicSubscription: Flow<Boolean> = billingClientWrapper.purchases.map { purchaseList ->
        purchaseList.any { purchase ->
            purchase.products.contains(BASIC_SUB) && purchase.isAutoRenewing
        }
    }

    // ProductDetails for the basic subscription.
    val basicProductDetails: Flow<ProductDetails> =
        billingClientWrapper.productWithProductDetails.filter {
            it.containsKey(
                BASIC_SUB
            )
        }.map { it[BASIC_SUB]!! }

    // List of current purchases returned by the Google PLay Billing client library.
    val purchases: Flow<List<Purchase>> = billingClientWrapper.purchases

    // Set to true when a purchase is acknowledged.
    val isNewPurchaseAcknowledged: Flow<Boolean> = billingClientWrapper.isNewPurchaseAcknowledged

}