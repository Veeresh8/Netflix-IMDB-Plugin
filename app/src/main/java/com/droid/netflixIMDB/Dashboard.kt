package com.droid.netflixIMDB

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.droid.netflixIMDB.util.LaunchUtils
import com.droid.netflixIMDB.util.Prefs
import com.droid.netflixIMDB.util.ReaderConstants
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class Dashboard : AppCompatActivity() {

    private lateinit var tvHowDoesItWorkHeader: TextView
    private lateinit var tvServiceEnabledHint: TextView
    private lateinit var btnStartService: Button
    private lateinit var tvPlanUsage: TextView
    private lateinit var tvUpgrade: TextView
    private lateinit var ivInfo: ImageView
    private lateinit var tvShowAdSkipHintImage: TextView
    private lateinit var usageProgressBar: ProgressBar

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var dialog: MaterialDialog? = null

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishAffinity()
        }
    }

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, Dashboard::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextUtils.setAppLocale(this, Prefs.getLanguageSelected())

        setContentView(R.layout.activity_dashboard)

        initUi()

        val onBackPressedDispatcher = onBackPressedDispatcher
        onBackPressedDispatcher.addCallback(callback)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {

            }
    }

    private fun initUi() {
        tvHowDoesItWorkHeader = findViewById<TextView>(R.id.tvHowDoesItWorkHeader)
        tvServiceEnabledHint = findViewById<TextView>(R.id.tvServiceEnabledHint)
        tvPlanUsage = findViewById<TextView>(R.id.tvPlanUsage)
        tvUpgrade = findViewById<TextView>(R.id.tvUpgrade)
        ivInfo = findViewById<ImageView>(R.id.ivInfo)
        tvShowAdSkipHintImage = findViewById<TextView>(R.id.tvShowAdSkipHintImage)
        usageProgressBar = findViewById<ProgressBar>(R.id.usageProgressBar)
        tvHowDoesItWorkHeader.paintFlags =
            tvHowDoesItWorkHeader.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        btnStartService = findViewById<Button>(R.id.btnStartService)

        btnStartService.setOnDebouncedClickListener {
            showAccessibilityServiceDialog()
        }

        tvShowAdSkipHintImage.setOnDebouncedClickListener {
            ImageShowerActivity.launch(this)
        }

        ivInfo.setOnDebouncedClickListener {
            openSettingsBottomMenu()
        }
    }

    private fun openSettingsBottomMenu() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.settings_bottom_sheet)

        bottomSheetDialog.findViewById<LinearLayout>(R.id.llChangeLanguage)?.setOnDebouncedClickListener {
            LanguageActivity.launch(this, true)
            bottomSheetDialog.show()
        }

        bottomSheetDialog.findViewById<LinearLayout>(R.id.llBoostService)?.setOnDebouncedClickListener {
            LaunchUtils.openIgnoreBatteryOptimisations(this)
            bottomSheetDialog.show()
        }

        bottomSheetDialog.show()
    }

    override fun onResume() {
        super.onResume()
        checkAccessibilitySettings()
        checkUsage()
    }

    @SuppressLint("SetTextI18n")
    private fun checkUsage() {
        tvPlanUsage.text = "${Prefs.getSkipCount()} / ${ReaderConstants.MAX_LIMIT}"

        usageProgressBar.updateProgressAndAnimate(
            (Prefs.getSkipCount() * 100) / ReaderConstants.MAX_LIMIT
        )

        if (Prefs.getSkipCount() >= ReaderConstants.MAX_LIMIT) {
            showUpgradeHint()
        }
    }

    private fun showAccessibilityServiceDialog() {
        val startServiceString = getString(R.string.start_service)
        val descriptionString = getString(R.string.accessibility_service_description)
        val enableString = getString(R.string.enable)
        val cancelString = getString(R.string.cancel)

        dialog = MaterialDialog(this)
            .cancelable(false)
            .cancelOnTouchOutside(false)
            .title(text = startServiceString)
            .message(text = descriptionString)
            .positiveButton(text = enableString) {
                LaunchUtils.launchAccessibilityScreen(this)
                dialog?.dismiss()
            }
            .negativeButton(text = cancelString) {
                dialog?.dismiss()
            }

        dialog?.show()
    }

    private fun checkAccessibilitySettings() {
        if (AccessibilityUtils.isAccessibilityServiceEnabled(this, ReaderService::class.java)) {
            tvServiceEnabledHint.visible()
            btnStartService.gone()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkNotificationPermission()
            }
        } else {
            tvServiceEnabledHint.gone()
            btnStartService.visible()

            lifecycleScope.launch {
                delay(3000)
                ObjectAnimator.ofFloat(btnStartService, View.TRANSLATION_X, 0F, 25F, -25F, 25F, -25F,15F, -15F, 6F, -6F, 0F)
                    .setDuration(1500)
                    .start();
                delay(1000)
                ObjectAnimator.ofFloat(btnStartService, View.TRANSLATION_X, 0F, 25F, -25F, 25F, -25F,15F, -15F, 6F, -6F, 0F)
                    .setDuration(1500)
                    .start();
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showUpgradeHint() {
        lifecycleScope.launch {
            delay(2000)
            val exhaustedHint = getString(R.string.exhausted_trail_hint)

            val balloon =
                Balloon.Builder(this@Dashboard)
                    .setWidth(BalloonSizeSpec.WRAP)
                    .setHeight(BalloonSizeSpec.WRAP)
                    .setText(exhaustedHint)
                    .setTextColorResource(R.color.white)
                    .setTextSize(15f)
                    .setMarginRight(12)
                    .setMarginBottom(12)
                    .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                    .setArrowSize(10)
                    .setArrowPosition(0.5f)
                    .setPadding(12)
                    .setCornerRadius(4f)
                    .setBackgroundColorResource(R.color.colorPrimary)
                    .setBalloonAnimation(BalloonAnimation.FADE)
                    .setLifecycleOwner(this@Dashboard)
                    .build()
            balloon.showAlignBottom(tvUpgrade)
        }
    }
}