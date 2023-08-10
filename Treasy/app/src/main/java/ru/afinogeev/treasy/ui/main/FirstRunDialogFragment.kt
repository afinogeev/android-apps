package ru.afinogeev.treasy.ui.main
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.R
import android.view.View
import androidx.annotation.NonNull
import ru.afinogeev.treasy.databinding.FirstrunFragmentBinding
//import ru.afinogeev.treasy.databinding.MainFragmentBinding


class FirstRunDialogFragment : DialogFragment() {
    
    //private var _binding: FirstrunFragmentBinding? = null
    //private val binding get() = _binding!!
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)

            val view =  FirstrunFragmentBinding.inflate(layoutInflater).root

            builder.setView(view)
                .setPositiveButton("Понятно",
                    DialogInterface.OnClickListener { dialog, id ->
                        // FIRE ZE MISSILES!
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}