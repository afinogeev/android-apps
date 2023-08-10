package ru.afinogeev.goodbuy

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Observer

import com.squareup.picasso.Picasso
import ru.afinogeev.goodbuy.databinding.*
import java.lang.Exception

class GoodsFragment : Fragment() {

    companion object {
        fun newInstance() = GoodsFragment()
        val TAG = "GoodsFragment"
    }

    private lateinit var viewModel: GoodsViewModel
    private lateinit var binding: GoodsFragmentBinding
    private var order = "rd"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = GoodsFragmentBinding.inflate(inflater)
        val view = binding.root

        viewModel = ViewModelProvider(this).get(GoodsViewModel::class.java)

        setListeners()
        setObservers()
        showInfo(getString(R.string.welcome))

        val sharedPref = this.activity?.getPreferences(Context.MODE_PRIVATE)
        val defaultValue = "rd"
        order = sharedPref?.getString("sort", defaultValue)!!
        setSortIcon()

        return view
    }

    private fun setListeners(){
        binding.buttonSort.setOnClickListener(){
            val sortDialog = SortDialog()
            childFragmentManager.setFragmentResultListener("sortKey", this) { requestKey, bundle ->
                val result = bundle.getInt("sortType")
                when(result){
                    0 -> order = "pa"
                    1 -> order = "pd"
                    2 -> order = "rd"
                }
                setSortIcon()
                getGoods()
            }
            sortDialog.show(childFragmentManager,"sort")
        }

        binding.editTextSearch.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                getGoods()
                return@OnKeyListener true
            }
            false
        })

        binding.buttonSearch.setOnClickListener(){
            getGoods()
        }
    }

    private fun setObservers(){
        viewModel.goods.observe(viewLifecycleOwner, Observer {
            binding.layoutContent.removeAllViews()
            if (it.size == 0) {
                showInfo("Ничего не найдено")
            }
            else {
                val size = it.size
                var i = 0
                for(good in it){
                    if (i==0 || i==size/2)
                        showRate()
                    showGood(good)
                    if (i==size-1)
                        showRate()
                    i++
                }
            }
        })

        viewModel.infoMessage.observe(viewLifecycleOwner, Observer {
            showInfo(it)
        })
    }

    private fun showRate(){
        val bindingRate = RateLayoutBinding.inflate(layoutInflater)

        Picasso.get().load("https://www.gstatic.com/android/market_images/web/play_prism_hlock_2x.png").into(bindingRate.img)
        bindingRate.root.setOnClickListener {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.afinogeev.goodbuy"))
                startActivity(browserIntent)
            }
            catch (e:Exception){
                Log.d(TAG,e.message!!)
            }
        }

        binding.layoutContent.addView(bindingRate.root)
    }

    private fun getGoods(){
        try {
            val text = binding.editTextSearch.text.toString().trim()
            if (text.length > 2)
                viewModel.getGoods(text, order)
        } catch (e:Exception) {
            Log.d(TAG,"getTest error")
        }
    }

    private fun showGood(good: Good){

        if (good.src != "error"){
            val bindingCard = CardLayoutBinding.inflate(layoutInflater)

            Picasso.get().load(good.img).into(bindingCard.img)
            bindingCard.price.text = good.price.toString() + " р."
            bindingCard.name.text = good.name
            bindingCard.src.text = good.src
            bindingCard.root.setOnClickListener {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(good.link))
                    startActivity(browserIntent)
                }
                catch (e:Exception){
                    Log.d(TAG,e.message!!)
                }
            }

            binding.layoutContent.addView(bindingCard.root)
        }
        else{
            showWeb(good.link)
        }

    }

    private fun showInfo(message: String){
        binding.layoutContent.removeAllViews()
        val bindingInfo = InfoLayoutBinding.inflate(layoutInflater)
        bindingInfo.message.text = message
        bindingInfo.img.setBackgroundResource(R.drawable.ic_main)
        binding.layoutContent.addView(bindingInfo.root)
    }

    private fun setSortIcon(){
        when(order){
            "pa" -> binding.buttonSort.setBackgroundResource(R.drawable.ic_sort_pa)
            "pd" -> binding.buttonSort.setBackgroundResource(R.drawable.ic_sort_pd)
            "rd" -> binding.buttonSort.setBackgroundResource(R.drawable.ic_sort_rd)
        }

        val sharedPref = this.activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("sort", order)
            apply()
        }
    }

    private fun showWeb(url:String){

        val bindingCaptcha = WebLayoutBinding.inflate(layoutInflater)
        bindingCaptcha.web.webViewClient = CustomWebViewClient()
        bindingCaptcha.web.settings.javaScriptEnabled = true
        bindingCaptcha.web.loadUrl(url)
        binding.layoutContent.addView(bindingCaptcha.root)
    }

    //Кастомизация веб-клиента, чтобы открывался внутри приложения
    private class CustomWebViewClient : WebViewClient(){
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            view?.loadUrl(request?.url.toString())
            return true
        }
    }

}