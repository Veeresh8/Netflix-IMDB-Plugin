package com.droid.netflixIMDB

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.droid.netflixIMDB.util.LaunchUtils
import com.droid.netflixIMDB.util.Prefs
import com.droid.netflixIMDB.util.ReaderConstants
import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog

class Dashboard : AppCompatActivity() {

    private lateinit var tvHowDoesItWorkHeader: TextView
    private lateinit var tvServiceEnabledHint: TextView
    private lateinit var btnStartService: Button
    private lateinit var tvPlanUsage: TextView
    private lateinit var usageProgressBar: ProgressBar

    companion object {

        fun launch(context: Context) {
            context.startActivity(Intent(context, Dashboard::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initUi()
    }

    private fun initUi() {
        tvHowDoesItWorkHeader = findViewById<TextView>(R.id.tvHowDoesItWorkHeader)
        tvServiceEnabledHint = findViewById<TextView>(R.id.tvServiceEnabledHint)
        tvPlanUsage = findViewById<TextView>(R.id.tvPlanUsage)
        usageProgressBar = findViewById<ProgressBar>(R.id.usageProgressBar)
        tvHowDoesItWorkHeader.paintFlags =
            tvHowDoesItWorkHeader.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        btnStartService = findViewById<Button>(R.id.btnStartService)

        btnStartService.setOnDebouncedClickListener {
            showAccessibilityServiceDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAccessibilitySettings()
        checkUsage()
    }

    private fun checkUsage() {
        tvPlanUsage.text = "${Prefs.getSkipCount()} / ${ReaderConstants.MAX_LIMIT}"

        usageProgressBar.updateProgressAndAnimate(
            (Prefs.getSkipCount() * 100) / ReaderConstants.MAX_LIMIT
        )
    }

    private fun showAccessibilityServiceDialog() {
        val startServiceString = getString(R.string.start_service)
        val descriptionString = getString(R.string.accessibility_service_description)
        val enableString = getString(R.string.enable)
        val cancelString = getString(R.string.cancel)

        val mBottomSheetDialog = BottomSheetMaterialDialog.Builder(this@Dashboard)
            .setTitle(startServiceString)
            .setMessage(descriptionString)
            .setCancelable(false)
            .setPositiveButton(
                enableString
            ) { dialogInterface, which ->
                LaunchUtils.launchAccessibilityScreen(this)
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                cancelString
            ) { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            .build()

        mBottomSheetDialog.show()
    }

    private fun checkAccessibilitySettings() {
        if (AccessibilityUtils.isAccessibilityServiceEnabled(this, ReaderService::class.java)) {
            tvServiceEnabledHint.visible()
            btnStartService.gone()
        } else {
            tvServiceEnabledHint.gone()
            btnStartService.visible()
        }
    }
}