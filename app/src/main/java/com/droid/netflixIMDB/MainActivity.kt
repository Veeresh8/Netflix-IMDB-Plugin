package com.droid.netflixIMDB

import android.content.Context
import android.content.Intent
import android.graphics.Typeface.BOLD
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG: String = this.javaClass.simpleName
    private val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        tvEnableAccessibility.setOnClickListener {
            val intent = Intent(ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        tvAddToWhitelist.setOnClickListener {
            openPowerSettings(this)
        }

        tvGrantOverlay.setOnClickListener {
            checkOverlayPermission()
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
            val isOptimized = pm.isIgnoringBatteryOptimizations(packageName)
            if (!isOptimized) {
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

        if (tvAddToWhitelist.tag == "enabled" && tvGrantOverlay.tag == "enabled" && tvEnableAccessibility.tag == "enabled") {
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
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
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
}
