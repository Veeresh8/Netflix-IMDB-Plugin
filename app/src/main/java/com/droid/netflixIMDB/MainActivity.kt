package com.droid.netflixIMDB

import PurchaseUtils
import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
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
import com.airbnb.lottie.LottieAnimationView
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.droid.netflixIMDB.analytics.Analytics
import com.droid.netflixIMDB.util.LaunchUtils
import com.droid.netflixIMDB.util.LaunchUtils.openPowerSettings
import com.droid.netflixIMDB.util.Prefs
import com.droid.netflixIMDB.util.TextUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BillingProcessor.IBillingHandler {

    private val TAG: String = this.javaClass.simpleName
    private val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084

    private var drawer: DrawerLayout? = null
    private lateinit var billingProcessor: BillingProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
            R.id.support -> {
                launchSupportSheet()
            }

            R.id.faq -> {
                launchFAQSheet(true)
            }
        }
        return true
    }

    private fun launchSupportSheet() {
        Analytics.postClickEvents(Analytics.ClickTypes.SUPPORT)

        toggleDrawer()

        val mBottomSheetDialog = BottomSheetDialog(this)
        mBottomSheetDialog.window?.setDimAmount(0.9F)
        val sheetView = layoutInflater.inflate(
            R.layout.support_bottom_sheet,
            null
        )

        val donationLow = sheetView.findViewById(R.id.btnSmallDonation) as Button
        val donationHigh = sheetView.findViewById(R.id.btnHighDonation) as Button
        val donationMedium = sheetView.findViewById(R.id.btnMediumDonation) as Button

        donationLow.setOnClickListener {
            Analytics.postClickEvents(Analytics.ClickTypes.SMALL_PURCHASE)
            billingProcessor.purchase(this, PurchaseUtils.SMALL_DONATION)
            mBottomSheetDialog.dismiss()
        }

        donationMedium.setOnClickListener {
            Analytics.postClickEvents(Analytics.ClickTypes.MEDIUM_PURCHASE)
            billingProcessor.purchase(this, PurchaseUtils.MEDIUM_DONATION)
            mBottomSheetDialog.dismiss()
        }

        donationHigh.setOnClickListener {
            Analytics.postClickEvents(Analytics.ClickTypes.HIGH_PURCHASE)
            billingProcessor.purchase(this, PurchaseUtils.HIGH_DONATION)
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
        tvEnableAccessibility.setOnClickListener {
            Analytics.postClickEvents(Analytics.ClickTypes.ACC_SERV)
            val intent = Intent(ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        tvAddToWhitelist.setOnClickListener {
            Analytics.postClickEvents(Analytics.ClickTypes.WHITELIST)
            openPowerSettings(this)
        }

        tvGrantOverlay.setOnClickListener {
            Analytics.postClickEvents(Analytics.ClickTypes.OVERLAY)
            checkOverlayPermission()
        }

        ivMenu.setOnClickListener {
            toggleDrawer()
        }

        ivHelp.setOnClickListener {
            launchFAQSheet()
        }
    }

    private fun launchAppWithPackageName(packageName: String) {
        val pm = packageManager
        try {
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            startActivity(launchIntent)
        } catch (exception: Exception) {
            Log.e(TAG, "Exception launching - ${exception.message}")
            Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION)
        }
    }


    private fun checkOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canDrawOverlays(this)) {
            setIconToTextView(tvGrantOverlay, true)
        } else {
            setIconToTextView(tvGrantOverlay, false)
        }
    }

    private fun checkAccessibilitySettings() {
        if (ReaderService.isConnected) {
            setIconToTextView(tvEnableAccessibility, true)
        } else {
            setIconToTextView(tvEnableAccessibility, false)
        }
    }

    private fun setIconToTextView(textView: TextView, isEnabled: Boolean) {
        val drawable: Int = if (isEnabled) {
            textView.tag = "enabled"
            R.drawable.round_check_circle_outline_24px
        } else {
            textView.tag = "disabled"
            R.drawable.round_navigate_next_24px
        }

        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
    }

    private fun checkBatterySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoring = pm.isIgnoringBatteryOptimizations(packageName)
            if (isIgnoring) {
                setIconToTextView(tvAddToWhitelist, true)
            } else {
                setIconToTextView(tvAddToWhitelist, false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkOverlaySettings()
        checkBatterySettings()
        checkAccessibilitySettings()

        if (tvGrantOverlay.tag == "enabled" && tvEnableAccessibility.tag == "enabled") {
            val spannable = TextUtils.getSpan(this, tvAllDone, object : TextUtils.SpanClickCallback {
                override fun launchApp(packageName: String) {
                    launchAppWithPackageName(packageName)
                }
            })
            tvAllDone.text = spannable
            tvAllDone.movementMethod = LinkMovementMethod.getInstance()
            tvAllDone.visible()
        } else {
            tvAllDone.gone()
        }
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
        super.onDestroy()
    }
}
