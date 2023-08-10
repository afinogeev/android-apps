package ru.afinogeev.goodbuy.parse
import android.util.Log
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import ru.afinogeev.goodbuy.Good


class SravniParser: GoodsParser() {

    companion object {
        val TAG = "Sravni"
    }

    override suspend fun get(text:String, order:String): ArrayList<Good>? {
        headers= mapOf(
            "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
            "accept-language" to "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7",
            "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36",
        )

        val entext = convertStringCp1251(text)
        url = "https://sravni.com/index.php?action=search%2Findex&q=" + entext +"&search_submit=true"

        try {
            val doc: Document = Jsoup.connect(url)
                .timeout(TIMEOUT)
                .headers(headers)
                .get()
            delay(DELAY_MS)

            val items: Elements = doc.select("div.price-line")
            for(item in items){
                try {
                    val a = item.select("a.common").first()
                    val link = a?.attr("href").toString()
                    val name = a?.text().toString().trim()
                    val img = item.select("img").first()?.attr("src").toString()
                    val price = item.select("a.price").first()?.text()?.trim()?.split(" р")?.first()?.replace(" ","")?.toInt()

                    goods.add(Good(name, link, img, price!!, "sravni.com"))
                }
                catch (e:Exception) {
                    Log.d(TAG,e.message!!)
                }
            }
        }
        catch (e:Exception) {
            Log.d(TAG,e.message!!)
            return goods
        }

        return goods
    }

    private fun convertStringCp1251(text: String): String {

//        %F1%EC%E5%F1%E8%F2%E5%EB%FC+%E4%EB%FF+%E2%E0%ED%ED%FB
        var ns = ""
        val string = text.replace(" ", "+")
        for (i in 0..(string.length-1)){
            if (string[i] > 'А'){
                val symb = string[i].toByte().toInt() + 176
                ns+= "%" + Integer.toHexString(symb).uppercase()
            }
            else
                ns+= string[i]
        }
        return ns
    }
}