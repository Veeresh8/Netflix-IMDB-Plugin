package com.droid.netflixIMDB.util

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import com.droid.netflixIMDB.R
import com.droid.netflixIMDB.util.LaunchUtils.openDontKillMyApp
import com.droid.netflixIMDB.util.LaunchUtils.openPowerSettings
import com.droid.netflixIMDB.util.LaunchUtils.openPrivacyPolicy

object TextUtils {

    interface SpanClickCallback {
        fun launchApp(packageName: String)
    }

    fun getSpan(context: Context, textView: TextView, spanClickCallback: SpanClickCallback): SpannableString {
        val spannable = SpannableString(textView.text.toString())

        /*Netflix Span*/
        spannable.setSpan(
            ForegroundColorSpan(context.resources.getColor(R.color.netflixRed)),
            7, 14,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
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
                spanClickCallback.launchApp("com.netflix.mediaclient")
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = ds.linkColor
                ds.isUnderlineText = true
            }
        }

        spannable.setSpan(clickSpanNetflix, 7, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        /*Hotstar Span*/
        spannable.setSpan(
            ForegroundColorSpan(context.resources.getColor(R.color.hotstarYellow)),
            18, 25,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
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
                spanClickCallback.launchApp("in.startv.hotstar")
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = true
            }
        }

        spannable.setSpan(clickSpanHotstar, 18, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    fun getSpanOne(textView: TextView, context: Context): SpannableString {
        val spannable = SpannableString(textView.text.toString())
        spannable.setSpan(
            ForegroundColorSpan(context.resources.getColor(R.color.white)),
            7, 14,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickFirstSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=9o_Ccc5O0X0"))
                intent.putExtra("force_fullscreen", true)
                context.startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = context.resources.getColor(R.color.white)
                ds.isUnderlineText = true
            }
        }

        spannable.setSpan(
            clickFirstSpan,
            textView.length() - 10,
            textView.length() - 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }

    fun getSpanTwo(textView: TextView, context: Context): SpannableString {
        val spannable = SpannableString(textView.text.toString())
        spannable.setSpan(
            ForegroundColorSpan(context.resources.getColor(R.color.white)),
            7, 14,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickSecondSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                openDontKillMyApp(context)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = context.resources.getColor(R.color.white)
                ds.isUnderlineText = true
            }
        }

        spannable.setSpan(
            clickSecondSpan,
            textView.length() - 10,
            textView.length() - 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        spannable.setSpan(
            ForegroundColorSpan(context.resources.getColor(R.color.white)),
            7, 14,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickSecondSubSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                openPowerSettings(context)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = context.resources.getColor(R.color.white)
                ds.isUnderlineText = true
            }
        }

        spannable.setSpan(
            clickSecondSubSpan,
            19,
            28,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }

    fun getSpanThree(textView: TextView, context: Context): SpannableString {
        val spannable = SpannableString(textView.text.toString())
        spannable.setSpan(
            ForegroundColorSpan(context.resources.getColor(R.color.white)),
            7, 14,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickSecondSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                openPrivacyPolicy(context)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = context.resources.getColor(R.color.white)
                ds.isUnderlineText = true
            }
        }

        spannable.setSpan(clickSecondSpan, 23, 37, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannable
    }
}