package ru.afinogeev.lettersreplacement

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import ru.afinogeev.lettersreplacement.databinding.WorkplaceFragmentBinding

class WorkplaceFragment : Fragment() {

    companion object {
        fun newInstance() = WorkplaceFragment()
    }

    private lateinit var viewModel: WorkplaceViewModel
    private lateinit var binding: WorkplaceFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = WorkplaceFragmentBinding.inflate(layoutInflater)
        val view = binding.root

        viewModel = ViewModelProvider(this).get(WorkplaceViewModel::class.java)

        viewModel.text.observe(viewLifecycleOwner, Observer { it ->
            setNewText(it)
        })

        binding.buttonProcess.setOnClickListener{
            val mode: Int = if (binding.switchMode.isChecked) 1 else 0
            val text: String = binding.textMain.text.toString()
            processText(text, mode)

            val sproc = resources.getString(R.string.button_process)
            val sunproc = resources.getString(R.string.button_unprocess)
            if(binding.buttonProcess.text == sproc)
                binding.buttonProcess.text = sunproc
            else if (binding.buttonProcess.text == sunproc)
                binding.buttonProcess.text = sproc
        }

        return view
    }

    private fun setNewText(text: String){
        binding.textMain.setText(text.toCharArray(),0,text.length)
    }

    private fun processText(text: String, mode: Int){
        try {
            viewModel.processText(text, mode)
        } catch (e: Exception){
            Log.d("Workplace", "error  process")
        }
    }
}