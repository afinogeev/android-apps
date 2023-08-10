package ru.afinogeev.lettersreplacement

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.afinogeev.lettersreplacement.alphabet.Alphabet
import ru.afinogeev.lettersreplacement.alphabet.Symbol

class AlphabetViewModel : ViewModel() {

    val alphabet: MutableLiveData<MutableList<Symbol>> = MutableLiveData()
    val table: MutableLiveData<TableLayout> = MutableLiveData()

    fun getAlphabet() {
        viewModelScope.launch {
            try {
                val alp = Alphabet.getAll()
                alphabet.value = alp.sortedBy { it.name }.toMutableList()
            } catch (e:Exception){
                Log.d("AlphabetViewModel","get error")
            }

        }
    }

    fun fillTable(context: Context){
        val tableTmp = TableLayout(context)
        viewModelScope.launch {
            fillHeader(context, tableTmp)
            fillData(context, tableTmp)
        }
        table.value = tableTmp
    }

    private fun saveNewLetter(context: Context, newletter: Symbol){
        viewModelScope.launch {
            try {
                Alphabet.saveNewLetter(context, newletter)
            } catch (e:Exception){
                Log.d("AlphabetViewModel","save error")
            }

        }
    }

    private fun fillHeader(context: Context, tableTmp:TableLayout){
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(10)
        params.gravity = Gravity.CENTER
        val tableRow = TableRow(context)
        val names = listOf<String>("Cимвол", "Стандарт", "Ультра")
        names.forEach(){
            val layt = LinearLayout(context)
            val textView = TextView(context)
            textView.setPadding(10)
            textView.text = it
            textView.setBackgroundColor(Color.BLACK)
            textView.layoutParams = params
            textView.gravity = Gravity.CENTER
            layt.addView(textView)
            tableRow.addView(layt)
        }

        tableRow.gravity = Gravity.CENTER
        tableTmp.addView(tableRow)
    }

    private fun fillData(context: Context, tableTmp:TableLayout){
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(10)
        params.gravity = Gravity.CENTER
        val colorEdit = ContextCompat.getColor(context, R.color.edit)
        alphabet.value!!.forEach{
            var syms = it
            val tableRow = TableRow(context)
            for(i in 0..2){
                val layt = LinearLayout(context)
                if(i==0) {
                    val textView = TextView(context)
                    textView.setPadding(10)
                    textView.text = it.name
                    textView.setBackgroundColor(Color.BLACK)
                    textView.layoutParams = params
                    textView.gravity = Gravity.CENTER
                    layt.addView(textView)
                }
                else{
                    val editText = EditText(context)
                    editText.setPadding(10)
                    editText.setBackgroundColor(colorEdit)
                    editText.layoutParams = params
                    editText.gravity = Gravity.CENTER
                    when (i){
                        1 -> editText.setText(it.std.toCharArray(),0,1)
                        2 -> editText.setText(it.ult.toCharArray(),0,1)
                    }
                    editText.doAfterTextChanged {
                        when (i){
                            1 -> syms.std = editText.text.toString()
                            2 -> syms.ult = editText.text.toString()
                        }
                        saveNewLetter(context,syms)
                    }
                    layt.addView(editText)
                }
                tableRow.addView(layt)
            }

            tableRow.gravity = Gravity.CENTER
            tableTmp.addView(tableRow)
        }
    }
}