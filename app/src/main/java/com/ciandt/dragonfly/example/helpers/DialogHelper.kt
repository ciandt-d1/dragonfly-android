package com.ciandt.dragonfly.example.helpers

import android.app.AlertDialog
import android.content.Context
import com.ciandt.dragonfly.example.R

object DialogHelper {

    fun showConfirmation(context: Context, title: String, message: String, onConfirmation: () -> Unit) {

        val dialog = AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(context.getString(R.string.dialog_confirmation_button_cancel), { dialog, _ ->
                    dialog.dismiss()
                })
                .setPositiveButton(context.getString(R.string.dialog_confirmation_button_confirm), { dialog, _ ->
                    dialog.dismiss()
                    onConfirmation()
                })
                .create()

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
}