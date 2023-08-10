package ru.afinogeev.goodbuy.parse

import android.util.Log
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import ru.afinogeev.goodbuy.Good

class GoogleParser: GoodsParser() {

    companion object {
        val TAG = "Google"
    }

    override suspend fun get(text:String, order:String): ArrayList<Good>? {
        headers = mapOf(
            "user-agent" to "Chrome/96.0.4664.174",
            "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,image/png,image/jpeg,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
            "content-type" to "text/html,image/webp",
        )
        sort = mapOf(
            "pa" to "p",
            "pd" to "pd",
            "rd" to "rv",
        )
        url = "https://www.google.ru/search"
        params = mapOf(
            "q" to text,
            "tbm" to "shop",
            "tbs" to "p_ord:"+sort[order],
        )

        try {
            val doc: Document = Jsoup.connect(url)
                .timeout(TIMEOUT)
                .headers(headers)
                .data(params)
                .get()
            delay(DELAY_MS)

            val items: Elements = doc.select("div.xcR77")
            for(item in items){
                try {
                    val ln = item.select("div.rgHvZc").first()
                    val link = "https://google.com" + ln?.select("a")?.first()?.attr("href").toString()
                    val name = ln?.select("a")?.first()?.text().toString()
                    val img = item.select("img")?.first()?.attr("src").toString()
                    val price = item.select("span.HRLxBb").text().split(",")[0].replace(" ","").toInt()
                    goods.add(Good(name, link, img, price, "google.com"))
                }
                catch (e:Exception) {
                    Log.d(TAG,e.message!!)
                }
            }
        }
        catch (e:Exception) {
            Log.d(TAG,e.message!!)
            if (e.message!!.contains("https://www.google.com/sorry")){
                val url = e.message!!.split("URL=[")[1].trimEnd(']')
                var price = 99999999
                if (order == "pd"){
                    price = 0
                }
                goods.add(Good("captcha", url, "", price, "error"))
            }
        }

        return goods
    }
}