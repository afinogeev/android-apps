package ru.afinogeev.lettersreplacement

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.afinogeev.lettersreplacement.alphabet.Symbol
import ru.afinogeev.lettersreplacement.databinding.AlphabetFragmentBinding

class AlphabetFragment : Fragment() {

    companion object {
        fun newInstance() = AlphabetFragment()
    }

    private lateinit var viewModel: AlphabetViewModel
    private lateinit var binding: AlphabetFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = AlphabetFragmentBinding.inflate(inflater)
        val view = binding.root

        viewModel = ViewModelProvider(this).get(AlphabetViewModel::class.java)

        viewModel.alphabet.observe(viewLifecycleOwner, Observer { it ->
            fillTable(it)
        })

        viewModel.table.observe(viewLifecycleOwner, Observer { it ->
            binding.tableAlphabet.removeAllViews()
            binding.tableAlphabet.addView(it)
        })

        return view
    }

    override fun onResume() {
        super.onResume()

        val tableRow = TableRow(context)
        val textView = TextView(context)
        textView.text = resources.getString(R.string.text_wait)
        tableRow.addView(textView)
        tableRow.gravity = Gravity.CENTER
        binding.tableAlphabet.addView(tableRow)

        this.lifecycleScope.launch {
            delay(500)
            getAlphabet()
        }
    }

    override fun onPause() {
        super.onPause()

        binding.tableAlphabet.removeAllViews()
    }

    private fun getAlphabet(){
        try {
            viewModel.getAlphabet()
        } catch (e: Exception){
            Log.d("Alphabet", "error  get")
        }
    }

    private fun fillTable(data: MutableList<Symbol>) {
        try {
            viewModel.fillTable(requireContext())
        } catch (e: Exception){
            Log.d("Alphabet", "error  table")
        }
    }

}