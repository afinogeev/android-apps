package ru.afinogeev.moexrating

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.afinogeev.moexrating.model.BondItem
import ru.afinogeev.moexrating.repository.Repository

class BondsViewModel(private val repository: Repository): ViewModel() {

    val mResponse: MutableLiveData<List<BondItem>> = MutableLiveData()

    fun getBonds() {
        viewModelScope.launch {
            try {
                val response = repository.getBonds()
                mResponse.value = response
            } catch (e:Exception){
                Log.d("Bonds","-error-")
            }

        }
    }
}