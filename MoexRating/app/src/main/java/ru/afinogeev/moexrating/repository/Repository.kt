package ru.afinogeev.moexrating.repository

import ru.afinogeev.moexrating.api.RetrofitInstance
import ru.afinogeev.moexrating.model.BondItem
import ru.afinogeev.moexrating.model.StockItem

class Repository {

    suspend fun getStocks(): List<StockItem> {
        return RetrofitInstance.api.getStocks()
    }

    suspend fun getBonds(): List<BondItem> {
        return RetrofitInstance.api.getBonds()
    }
}