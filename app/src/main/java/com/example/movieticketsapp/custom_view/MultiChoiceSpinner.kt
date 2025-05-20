package com.example.movieticketsapp.custom_view
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

@SuppressLint("AppCompatCustomView")
class MultiChoiceSpinner @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Spinner(context, attrs, defStyleAttr) {

    private var selectedItems = mutableListOf<String>()
    private var allItems = listOf<String>()

    init {
        // Set the default adapter
        val defaultAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listOf())
        adapter = defaultAdapter
    }

    // Method to set the items to display in the MultiChoiceSpinner
    fun setItems(items: List<String>) {
        allItems = items
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items)
        this.adapter = adapter
    }

    // Method to show the dialog and allow multiple choices
    fun showMultiChoiceDialog() {
        val selectedItemsCopy = selectedItems.toMutableList()
        val dialogBuilder = AlertDialog.Builder(context)
            .setTitle("Select Items")
            .setMultiChoiceItems(allItems.toTypedArray(), BooleanArray(allItems.size)) { _, which, isChecked ->
                if (isChecked) {
                    selectedItemsCopy.add(allItems[which])
                } else {
                    selectedItemsCopy.remove(allItems[which])
                }
            }
            .setPositiveButton("OK") { _, _ ->
                selectedItems = selectedItemsCopy
                (getChildAt(0) as TextView).text = selectedItems.joinToString(", ")
            }
            .setNegativeButton("Cancel", null)

        dialogBuilder.create().show()
    }

    // Get selected items
    fun getSelectedItems(): List<String> {
        return selectedItems
    }
}
