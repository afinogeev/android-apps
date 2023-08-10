package ru.afinogeev.moexrating.model

data class StockItem (
    val name:String,
    val sector:String,
    val rating:Int,
    val graph:String,
    val fundamental:String,
    val ticker:String,
    val tech:Int,
)

