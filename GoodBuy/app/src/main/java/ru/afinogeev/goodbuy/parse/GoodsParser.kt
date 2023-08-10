package ru.afinogeev.goodbuy.parse

import ru.afinogeev.goodbuy.Good

abstract class GoodsParser {

    companion object{
        val TIMEOUT = 10000
        val DELAY_MS: Long = 1000
    }

    val goods: ArrayList<Good> = ArrayList()

    lateinit var headers:Map<String, String>
    lateinit var sort:Map<String, String>
    lateinit var params:Map<String, String>
    lateinit var url:String

    abstract suspend fun get(text:String, order:String): ArrayList<Good>?

}