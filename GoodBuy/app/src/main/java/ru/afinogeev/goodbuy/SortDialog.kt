package ru.afinogeev.goodbuy

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment

class SortDialog: DialogFragment() {

    private val sortNames = arrayOf("дешево", "дорого", "популярно")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Сортировка")
                .setItems(sortNames,
                    DialogInterface.OnClickListener(){ dialog, which ->
                        parentFragmentManager.setFragmentResult("sortKey", bundleOf("sortType" to which))
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}