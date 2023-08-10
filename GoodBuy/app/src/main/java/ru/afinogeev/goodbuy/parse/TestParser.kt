package ru.afinogeev.goodbuy.parse

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class TestParser() {

    fun getHtml():String{

        var text:String = ""
        val stringBuilder = StringBuilder()
        try {
            val doc: Document = Jsoup.connect("http://afinogeev.ru/").get()
            val title: String = doc.title()
            val links: Elements = doc.select("a[href]")
            stringBuilder.append(title).append("\n")
            for (link in links) {
                stringBuilder.append("\n").append("Link :").append(link.attr("href")).append("\n").append("Text : ").append(link.text())
            }
        } catch (e: Exception) {
            stringBuilder.append("Error : ").append(e.message).append("\n")
            Log.d("test",e.message!!)
        }
        text = stringBuilder.toString()
        Log.d("test",stringBuilder.toString())

        return text
    }
}