package com.vms.wizwarehouse.otp


import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText

class GenericTextWatcher(
    private val currentEditText: EditText,
    private val previousEditText: EditText?,
    private val nextEditText: EditText?,
    private val otpFields: Array<EditText>,
    private val hiddenEditText: EditText
) : TextWatcher, View.OnKeyListener {

    init {
        currentEditText.setOnKeyListener(this) // Set key listener for backspace detection
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // No action needed
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s?.length == 1) {
            nextEditText?.requestFocus()
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (hiddenEditText.text.length == 6) {
            val otp = hiddenEditText.text.toString()
            for (i in otp.indices) {
                if (i < otpFields.size) {
                    otpFields[i].setText(otp[i].toString())
                }
            }
        }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL &&
            currentEditText.text.isEmpty() && previousEditText != null
        ) {
            previousEditText.requestFocus()
            previousEditText.setSelection(previousEditText.text.length)
            return true
        }
        return false
    }
}
