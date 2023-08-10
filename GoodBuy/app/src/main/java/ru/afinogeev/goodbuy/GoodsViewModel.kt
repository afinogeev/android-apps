package ru.afinogeev.goodbuy

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.afinogeev.goodbuy.parse.*

class GoodsViewModel : ViewModel() {

    companion object{
        val TAG = "GoodsViewModel"
    }

    //val testText: MutableLiveData<String> = MutableLiveData()
    val infoMessage: MutableLiveData<String> = MutableLiveData()
    val goods: MutableLiveData<ArrayList<Good>> = MutableLiveData()

    fun getGoods(text:String, order:String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val tmpGoods: ArrayList<Good> = ArrayList()

                tmpGoods.addAll(getGoogle(text, order))
                tmpGoods.addAll(getYandex(text, order))
                tmpGoods.addAll(getSravni(text, order))

                infoMessage.postValue("Подготовка данных")
                when (order){
                    "pa" -> tmpGoods.sortBy { it.price }
                    "pd" -> tmpGoods.sortByDescending { it.price }
                    "rd" -> tmpGoods.sortBy { it.price }
                }
                goods.postValue(tmpGoods)
            } catch (e:Exception){
                Log.d(TAG,"get error")
                infoMessage.postValue("Не найдено")
            }

        }
    }

    private suspend fun getGoogle(text:String, order:String): ArrayList<Good>{
        infoMessage.postValue("Поиск на google.com")
        val googleParser = GoogleParser()
        val goods:java.util.ArrayList<Good> = ArrayList()
        try {
            goods.addAll(googleParser.get(text, order)!!)
        }
        catch (e:Exception){
            Log.d(TAG,"google error")
        }
        return  goods
    }

    private suspend fun getYandex(text:String, order:String): ArrayList<Good>{
        infoMessage.postValue("Поиск на yandex.ru")
        val yandexParser = YandexParser()
        val goods:java.util.ArrayList<Good> = ArrayList()
        try {
            goods.addAll(yandexParser.get(text, order)!!)
        }
        catch (e:Exception){
            Log.d(TAG,"yandex error")
        }
        return  goods
    }

    private suspend fun getSravni(text:String, order:String): ArrayList<Good>{
        infoMessage.postValue("Поиск на sravni.com")
        val sravniParser = SravniParser()
        val goods:java.util.ArrayList<Good> = ArrayList()
        try {
            goods.addAll(sravniParser.get(text, order)!!)
        }
        catch (e:Exception){
            Log.d(TAG,"sravni error")
        }
        return  goods
    }

}