package ru.afinogeev.moexrating.api

import retrofit2.http.GET
import ru.afinogeev.moexrating.model.BondItem
import ru.afinogeev.moexrating.model.StockItem
import ru.afinogeev.moexrating.util.Constants.Companion.BONDS_URL
import ru.afinogeev.moexrating.util.Constants.Companion.STOCKS_URL

interface MoexRatingApi {
    @GET(STOCKS_URL)
    suspend fun getStocks(): List<StockItem>

    @GET(BONDS_URL)
    suspend fun getBonds(): List<BondItem>
}