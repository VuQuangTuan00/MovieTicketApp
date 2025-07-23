package com.example.movieticketsapp.CustomView.Component.custom_view

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import java.util.*

class CustomCalendarEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    init {
        isFocusable = false
        setOnTouchListener { _, event -> handleTouch(event) }
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val DRAWABLE_END = 2
            val drawable = compoundDrawables[DRAWABLE_END]
            drawable?.let {
                val bounds: Rect = it.bounds
                val touchX = event.x.toInt()
                val width = width
                val paddingEnd = paddingEnd
                if (touchX >= width - bounds.width() - paddingEnd) {
                    showDatePicker()
                    performClick()
                    return true
                }
            }
        }
        return false
    }
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, y, m, d ->
            val date = "${d.toString().padStart(2, '0')}/" +
                    "${(m + 1).toString().padStart(2, '0')}/$y"
            setText(date)
        }, year, month, day).show()
    }
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
