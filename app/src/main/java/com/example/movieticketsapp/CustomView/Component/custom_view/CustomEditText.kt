package com.example.movieticketsapp.CustomView.Component.custom_view

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.example.movieticketsapp.R

class CustomEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    private var isPasswordVisible = false
    private var visibleIcon: Drawable? = null
    private var hiddenIcon: Drawable? = null
    private var passIcon: Drawable? = null

    init {
        setupIcons()
        setPasswordVisibility(false)
    }

    private fun setupIcons() {
        visibleIcon = ContextCompat.getDrawable(context, R.drawable.custom_visible)
        hiddenIcon = ContextCompat.getDrawable(context, R.drawable.custom_disible)
        passIcon = ContextCompat.getDrawable(context, R.drawable.custom_pass_ic)
        setCompoundDrawablesWithIntrinsicBounds(null, null, hiddenIcon, null)
    }
    private fun setPasswordVisibility(visible: Boolean) {
        isPasswordVisible = visible
        inputType = if (visible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val icon = if (visible) visibleIcon else hiddenIcon
        setCompoundDrawablesWithIntrinsicBounds(passIcon, null, icon, null)
        setSelection(text?.length ?: 0)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            val drawableRight = compoundDrawables[2]
            if (drawableRight != null) {
                val touchAreaStart = width - paddingEnd - drawableRight.bounds.width()
                if (event.x >= touchAreaStart) {
                    performClick()
                    setPasswordVisibility(!isPasswordVisible)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
