package ru.afinogeev.lettersreplacement

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.afinogeev.lettersreplacement.alphabet.Alphabet

class WorkplaceViewModel : ViewModel() {

    val text: MutableLiveData<String> = MutableLiveData()

    fun processText(oldText: String, mode:Int) {
        viewModelScope.launch {
            try {
                var tmpText = oldText
                val alphabet = Alphabet.getAll()
                alphabet.forEach{
                    var newSym:String = it.name
                    when (mode){
                        0 -> newSym = it.std
                        1 -> newSym = it.ult
                    }
                    if (tmpText.contains(it.name.toCharArray()[0])){
                        tmpText = tmpText.replace(it.name, newSym)
                        return@forEach
                    }
                    if (tmpText.contains(newSym.toCharArray()[0])){
                        tmpText = tmpText.replace(newSym, it.name)
                        return@forEach
                    }
                }
                text.value = tmpText
            } catch (e:Exception){
                Log.d("WorkplaceViewModel","process error")
            }

        }
    }
}