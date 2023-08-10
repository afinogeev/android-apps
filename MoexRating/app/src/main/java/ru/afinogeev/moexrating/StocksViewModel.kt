package ru.afinogeev.moexrating

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.afinogeev.moexrating.model.StockItem
import ru.afinogeev.moexrating.repository.Repository

class StocksViewModel(private val repository: Repository): ViewModel() {

    val mResponse: MutableLiveData<List<StockItem>> = MutableLiveData()

    fun getStocks() {
        viewModelScope.launch {
            try {
                val response = repository.getStocks()
                mResponse.value = response
            } catch (e:Exception){
                Log.d("STOCKS","-error-")
            }

        }
    }
}