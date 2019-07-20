package com.droid.netflixIMDB

import PurchaseUtils
import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Typeface.BOLD
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
import android.provider.Settings.canDrawOverlays
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
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
                LaunchUtils.openPlayStore(this, packageName)
            }
            R.id.policy -> {
                Analytics.postClickEvents(Analytics.ClickTypes.PRIVACY_POLICY)
                LaunchUtils.openPrivacyPolicy(this)
            }
            R.id.support -> {
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
        }
        return true
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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=9o_Ccc5O0X0"))
            intent.putExtra("force_fullscreen", true)
            startActivity(intent)
        }
    }

    private fun launchApp(packageName: String) {
        val pm = packageManager
        try {
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            startActivity(launchIntent)
        } catch (exception: Exception) {
            Log.e(TAG, "Exception launching Netflix - ${exception.message}")
            Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPowerSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            context.startActivity(intent)
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
            val spannable = getSpan()
            tvAllDone.text = spannable
            tvAllDone.movementMethod = LinkMovementMethod.getInstance()
            tvAllDone.visible()
        } else {
            tvAllDone.gone()
        }
    }

    private fun getSpan(): SpannableString {
        val spannable = SpannableString(tvAllDone.text.toString())

        /*Netflix Span*/
        spannable.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.netflixRed)),
            7, 14,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            StyleSpan(BOLD),
            7, 14,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            RelativeSizeSpan(1.1f),
            7, 14,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickSpanNetflix = object : ClickableSpan() {
            override fun onClick(p0: View) {
                launchApp("com.netflix.mediaclient")
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = ds.linkColor
                ds.isUnderlineText = true
            }
        }

        spannable.setSpan(clickSpanNetflix, 7, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        /*Hotstar Span*/
        spannable.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.hotstarYellow)),
            18, 25,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            StyleSpan(BOLD),
            18, 25,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            RelativeSizeSpan(1.1f),
            18, 25,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickSpanHotstar = object : ClickableSpan() {
            override fun onClick(p0: View) {
                launchApp("in.startv.hotstar")
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = true
            }
        }

        spannable.setSpan(clickSpanHotstar, 18, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
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
