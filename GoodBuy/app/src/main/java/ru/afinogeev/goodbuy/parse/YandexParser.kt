package ru.afinogeev.goodbuy.parse
import android.util.Log
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import ru.afinogeev.goodbuy.Good

class YandexParser: GoodsParser() {

    companion object {
        val TAG = "Yandex"
    }

    override suspend fun get(text:String, order:String): ArrayList<Good>? {

        val secHeaders = listOf<Map<String, String>>(
            mapOf("accept-encoding" to "gzip, deflate, br"),
            mapOf("accept-language" to "ru,en;q=0.9"),
            mapOf("cache-control" to "max-age=0"),
            mapOf("downlink" to "10"),
            mapOf("dpr" to "1"),
            mapOf("ect" to "4g"),
            mapOf("rtt" to "50"),
            mapOf("sec-fetch-dest" to "document"),
            mapOf("sec-fetch-mode" to "navigate"),
            mapOf("sec-fetch-site" to "same-origin"),
            mapOf("upgrade-insecure-requests" to "1"),
        )

        headers= mapOf(
            "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
            "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36",
        ).toMutableMap()

        (headers as MutableMap<String, String>).putAll(secHeaders.random())

        sort = mapOf(
            "pa" to "aprice",
            "pd" to "dprice",
            "rd" to "dpop",
        )
        url = "https://yandex.ru/products/search"
        params = mapOf(
            "text" to text,
            "order" to sort[order]!!,
        )

        try {
            val doc: Document = Jsoup.connect(url)
                .timeout(TIMEOUT)
                .headers(headers)
                .data(params)
                .get()
            delay(DELAY_MS)

            if (doc.location().contains("yandex.ru/showcaptcha")){
                val url = doc.location()
                var price = 99999999
                if (order == "pd"){
                    price = 0
                }
                goods.add(Good("captcha", url, "", price, "error"))
            }
            else{
                val items: Elements = doc.select("div.ProductCardsList-Item")
                for(item in items){
                    try {
                        val link = "https://yandex.ru" + item.select("a.Link").first()?.attr("href").toString()
                        val name = item.select("div.ProductCard-Title").first()?.text().toString()
                        val img = "https:" + item.select("img.Thumbnail-Image").first()?.attr("src").toString()

                        var tPrice = ""
                        val prices = item.select("span.PriceValue-Thousands")
                        prices.forEach(){
                            tPrice+= it.text()
                        }
                        val price = tPrice.toInt()

                        goods.add(Good(name, link, img, price, "yandex.ru"))
                    }
                    catch (e:Exception) {
                        Log.d(TAG,e.message!!)
                    }
                }
            }

        }
        catch (e:Exception) {
            Log.d(TAG,e.message!!)
        }

        return goods
    }
}