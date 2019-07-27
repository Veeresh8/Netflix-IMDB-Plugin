package com.droid.netflixIMDB

import PurchaseUtils
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings.canDrawOverlays
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.airbnb.lottie.LottieAnimationView
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.droid.netflixIMDB.analytics.Analytics
import com.droid.netflixIMDB.notifications.NotificationManager
import com.droid.netflixIMDB.util.LaunchUtils
import com.droid.netflixIMDB.util.LaunchUtils.forceLaunchOverlay
import com.droid.netflixIMDB.util.LaunchUtils.launchAppWithPackageName
import com.droid.netflixIMDB.util.LaunchUtils.openPowerSettings
import com.droid.netflixIMDB.util.Prefs
import com.droid.netflixIMDB.util.ReaderConstants
import com.droid.netflixIMDB.util.TextUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.billingLayout
import kotlinx.android.synthetic.main.activity_main.ivHelp
import kotlinx.android.synthetic.main.activity_main.ivMenu
import kotlinx.android.synthetic.main.activity_main.lottieAnimationBilling
import kotlinx.android.synthetic.main.activity_main.rootLayout
import kotlinx.android.synthetic.main.activity_main.tvThank
import kotlinx.android.synthetic.main.activity_main_new.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BillingProcessor.IBillingHandler {

    private val TAG: String = this.javaClass.simpleName
    private var dialog: MaterialDialog? = null

    private var drawer: DrawerLayout? = null
    private lateinit var billingProcessor: BillingProcessor

    companion object {
        const val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)
        setupClickListeners()
        setUpNavDrawer()
        initBilling()
        initFCM()
        checkForPlayStoreIntent()
    }

    private fun checkForPlayStoreIntent() {
        if (intent.getStringExtra("open_playstore") != null) {
            LaunchUtils.openPlayStore(this)
        }
        checkForProIntent(intent)
    }

    private fun checkForProIntent(intent: Intent?) {
        val notificationToClear = intent?.getIntExtra("notification_id", 0)
        notificationToClear?.run {
            if (this > 0) {
                NotificationManager.getNotificationManager()?.cancel(this)
                launchSupportSheet()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        checkForProIntent(intent)
    }

    private fun initFCM() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result?.token
                Prefs.setPushToken(token ?: "NULL TOKEN")
                Log.d(TAG, "Push notification token: $token")
            })
    }

    private fun initBilling() {
        billingProcessor = BillingProcessor(this, BuildConfig.BILLING_KEY, this)
        billingProcessor.initialize()
        val purchaseListingDetails = billingProcessor.getPurchaseTransactionDetails(PurchaseUtils.SMALL_DONATION)
        if (purchaseListingDetails != null) {
            Prefs.setIsPremiumUser(true)
        }
    }

    private fun setUpNavDrawer() {
        drawer = findViewById(R.id.drawerLayout)

        val navigationView: NavigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

        val headerView = navigationView.getHeaderView(0)

        val lottieAnimation: LottieAnimationView = headerView.findViewById(R.id.lottieAnimation)
        lottieAnimation.playAnimation()
        lottieAnimation.loop(true)
    }

    override fun onBackPressed() {
        if (billingLayout.isVisible)
            return

        if (drawer?.isDrawerOpen(GravityCompat.START)!!) {
            drawer?.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun toggleDrawer() {
        drawer?.run {
            if (isDrawerOpen(GravityCompat.START)) {
                closeDrawer(GravityCompat.START)
            } else {
                openDrawer(GravityCompat.START)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.customizeRatingView -> {
                Analytics.postClickEvents(Analytics.ClickTypes.CUSTOMIZE)
                startActivity(Intent(this@MainActivity, CustomizeRatingViewActivity::class.java))
            }
            R.id.feedback -> {
                Analytics.postClickEvents(Analytics.ClickTypes.FEEDBACK)
                LaunchUtils.sendFeedbackIntent(this)
            }
            R.id.rateApp -> {
                Analytics.postClickEvents(Analytics.ClickTypes.PLAYSTORE)
                LaunchUtils.openPlayStore(this)
            }
            R.id.policy -> {
                Analytics.postClickEvents(Analytics.ClickTypes.PRIVACY_POLICY)
                LaunchUtils.openPrivacyPolicy(this)
            }
            R.id.pro -> {
                launchSupportSheet()
            }

            R.id.faq -> {
                launchFAQSheet(false)
            }
        }
        return true
    }

    private fun launchSupportSheet(mustToggleDrawer: Boolean = false) {
        Analytics.postClickEvents(Analytics.ClickTypes.SUPPORT)

        if (mustToggleDrawer)
            toggleDrawer()

        val mBottomSheetDialog = BottomSheetDialog(this)
        mBottomSheetDialog.window?.setDimAmount(0.9F)
        val sheetView = layoutInflater.inflate(
            R.layout.support_bottom_sheet,
            null
        )

        val donationLow = sheetView.findViewById(R.id.btnSmallDonation) as Button

        donationLow.setOnClickListener {
            Analytics.postClickEvents(Analytics.ClickTypes.SMALL_PURCHASE)
            billingProcessor.purchase(this, PurchaseUtils.SMALL_DONATION)
            mBottomSheetDialog.dismiss()
        }

        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.show()
    }

    private fun launchFAQSheet(mustToggleDrawer: Boolean = false) {
        Analytics.postClickEvents(Analytics.ClickTypes.FAQ)

        if (mustToggleDrawer)
            toggleDrawer()

        val mBottomSheetDialog = BottomSheetDialog(this)
        mBottomSheetDialog.window?.setDimAmount(0.9F)
        val sheetView = layoutInflater.inflate(
            R.layout.faq_bottom_sheet,
            null
        )

        val tvAnswerOne = sheetView.findViewById(R.id.tvAnswerOne) as TextView
        val tvAnswerTwo = sheetView.findViewById(R.id.tvAnswerTwo) as TextView
        val tvAnswerThree = sheetView.findViewById(R.id.tvAnswerThree) as TextView

        tvAnswerOne.text = TextUtils.getSpanOne(tvAnswerOne, this)
        tvAnswerOne.movementMethod = LinkMovementMethod.getInstance()

        tvAnswerTwo.text = TextUtils.getSpanTwo(tvAnswerTwo, this)
        tvAnswerTwo.movementMethod = LinkMovementMethod.getInstance()

        tvAnswerThree.text = TextUtils.getSpanThree(tvAnswerThree, this)
        tvAnswerThree.movementMethod = LinkMovementMethod.getInstance()

        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.show()
    }

    private fun setupClickListeners() {
        sbAccessibilityService.setOnClickListener {
            launchAccessibilityScreen()
        }

        sbBattery.setOnClickListener {
            openPowerSettings(this)
        }

        sbOverlay.setOnClickListener {
            forceLaunchOverlay(this)
        }

        ivMenu.setOnClickListener {
            toggleDrawer()
        }

        ivHelp.setOnClickListener {
            launchFAQSheet()
        }

        btNetflix.setOnClickListener {
            launchAppWithPackageName(this, ReaderConstants.NETFLIX)
        }

        btHotstar.setOnClickListener {
            launchAppWithPackageName(this, ReaderConstants.HOTSTAR)
        }

        btPrime.setOnClickListener {
            launchAppWithPackageName(this, ReaderConstants.PRIME)
        }

        btYoutube.setOnClickListener {
            launchAppWithPackageName(this, ReaderConstants.YOUTUBE)
        }

        tvBuyPro.setOnClickListener {
            launchSupportSheet(false)
        }
    }

    private fun launchOverlayScreen() {
        LaunchUtils.launchOverlayScreen(this)
    }

    private fun launchAccessibilityScreen() {
        LaunchUtils.launchAccessibilityScreen(this)
    }

    private fun checkOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canDrawOverlays(this)) {
            sbOverlay.isChecked = true
        } else {
            dialog = MaterialDialog(this)
                .cancelable(false)
                .cancelOnTouchOutside(false)
                .title(text = "Please grant overlay permissions")
                .message(text = "Overlay permissions are used to show IMDb ratings of the title you are about to watch.")
                .positiveButton(text = "Grant") {
                    launchOverlayScreen()
                }
                .negativeButton(text = "Exit") {
                    finishAffinity()
                }
        }
    }

    private fun checkAccessibilitySettings() {
        if (ReaderService.isConnected) {
            sbAccessibilityService.isChecked = true
        } else {
            dialog = MaterialDialog(this)
                .cancelable(false)
                .cancelOnTouchOutside(false)
                .title(text = "Please turn on Accessibility Services")
                .message(text = "Accessibility Services are core to the app and cannot function without it.")
                .positiveButton(text = "Turn on") {
                    launchAccessibilityScreen()
                }
                .negativeButton(text = "Exit") {
                    finishAffinity()
                }

        }
    }

    private fun checkBatterySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoring = pm.isIgnoringBatteryOptimizations(packageName)
            sbBattery.isChecked = isIgnoring
        }
    }

    override fun onResume() {
        super.onResume()
        checkOverlaySettings()
        checkAccessibilitySettings()
        checkBatterySettings()
        setTextViews()

        if (sbAccessibilityService.isChecked && sbOverlay.isChecked) {
            launchGroup.visible()
            dialog?.dismiss()
        } else {
            launchGroup.gone()
            dialog?.show()
        }

        checkIfPremiumUser()
    }

    private fun checkIfPremiumUser() {
        val isPremiumUser = Prefs.getIsPremiumUser()
        val menu = navigationView.menu
        isPremiumUser?.run {
            if (this) {
                tvBuyPro.gone()
                tvTotalCount.text = "${Prefs.getPayloadTotalCount()}"
                menu.removeItem(R.id.pro)
            } else {
                tvBuyPro.visible()
                tvTotalCount.text = "${Prefs.getPayloadTotalCount()} / 500"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTextViews() {
        val youtube = Prefs.getPayloadCount()?.youtube
        val netflix = Prefs.getPayloadCount()?.netflix
        val prime = Prefs.getPayloadCount()?.prime
        val hotstar = Prefs.getPayloadCount()?.hotstar

        tvYoutubeCount.text = "Youtube Ads Skipped - $youtube"
        tvNetflixCount.text = "Netflix IMDb ratings shown - $netflix"
        tvPrimeVideoCount.text = "Prime video ratings shown  - $prime"
        tvHotstartCount2.text = "Hotstar IMDb ratings shown  - $hotstar"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        } else if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !canDrawOverlays(this)) {
                Toast.makeText(
                    this,
                    "Draw over other app permission not granted, cannot function without it",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBillingInitialized() {

    }

    override fun onPurchaseHistoryRestored() {

    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        Analytics.postPurchasePayload(productId)
        Prefs.setIsPremiumUser(true)
        performBillingAnimation()
    }

    private fun performBillingAnimation() {
        rootLayout.gone()
        tvThank.gone()
        billingLayout.visible()
        lottieAnimationBilling.playAnimation()
        lottieAnimationBilling.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                tvThank.visible()
                Handler().postDelayed({
                    rootLayout.visible()
                    billingLayout.gone()
                    checkIfPremiumUser()
                }, 3000)
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {

            }

        })
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Log.e(TAG, "Error in billing $error: ${error?.message}")
    }

    public override fun onDestroy() {
        if (::billingProcessor.isInitialized) {
            billingProcessor.release()
        }
        dialog?.dismiss()
        super.onDestroy()
    }
}
