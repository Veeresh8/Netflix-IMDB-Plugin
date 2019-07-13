package com.droid.netflixIMDB

import adil.dev.lib.materialnumberpicker.dialog.NumberPickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import kotlinx.android.synthetic.main.activity_customize_rating_view.*
import kotlinx.android.synthetic.main.rating_view.*


class CustomizeRatingViewActivity : AppCompatActivity(), ColorPickerDialogListener {

    private val TAG: String = this.javaClass.simpleName
    private var which: ColorObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customize_rating_view)
        setClickListeners()
        checkForColorPrefs()
    }

    private fun checkForColorPrefs() {
        val titleColor = ColorPrefs.getTitleColor()
        val backgroundColor = ColorPrefs.getBackgroundColor()
        val iconColor = ColorPrefs.getIconColor()
        val timeout = ColorPrefs.getViewTimeout()

        if (titleColor != null && titleColor != 0) {
            tvTitle.setTextColor(titleColor)
            tvRating.setTextColor(titleColor)
        }

        if (backgroundColor != null && backgroundColor != 0) {
            constraintLayout.setBackgroundColor(backgroundColor)
        }

        if (iconColor != null && iconColor != 0) {
            ivClose.setColorFilter(iconColor)
        }

        tvTimeout.text = "$timeout" + "s"
    }

    private fun setClickListeners() {
        tvBackgroundColor.setOnClickListener {
            pickColor()
            which = ColorObject.BACKGROUND
        }

        tvTextColor.setOnClickListener {
            pickColor()
            which = ColorObject.TITLE
        }

        tvIconColor.setOnClickListener {
            pickColor()
            which = ColorObject.ICON
        }

        tvChangeRatingTimeout.setOnClickListener {
            val dialog = NumberPickerDialog(this, 3, 10,
                NumberPickerDialog.NumberPickerCallBack { value ->
                    ColorPrefs.setViewTimeout(value)
                    tvTimeout.text = "$value" + "s"
                })
            dialog.show()
        }
    }

    private fun pickColor() {
        ColorPickerDialog.newBuilder()
            .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
            .setAllowPresets(false)
            .setDialogId(0)
            .setColor(Color.BLACK)
            .setShowAlphaSlider(true)
            .show(this)
    }

    override fun onDialogDismissed(dialogId: Int) {
        Log.d(TAG, "onDialogDismissed() called with: dialogId = [$dialogId]")
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        Toast.makeText(this, "Selected Color: #" + Integer.toHexString(color), Toast.LENGTH_SHORT).show()
        when (which) {
            ColorObject.BACKGROUND -> {
                constraintLayout.setBackgroundColor(color)
                ColorPrefs.setBackgroundColor(color)
            }
            ColorObject.TITLE -> {
                tvTitle.setTextColor(color)
                tvRating.setTextColor(color)
                ColorPrefs.setTitleColor(color)
            }
            ColorObject.ICON -> {
                ivClose.setColorFilter(color)
                ColorPrefs.setIconColor(color)
            }
        }

    }

    enum class ColorObject {
        BACKGROUND, TITLE, ICON
    }
}
