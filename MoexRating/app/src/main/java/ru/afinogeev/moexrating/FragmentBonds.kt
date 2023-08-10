package ru.afinogeev.moexrating

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.afinogeev.moexrating.model.BondItem
import ru.afinogeev.moexrating.repository.Repository
import ru.afinogeev.moexrating.util.FragmentBrowser


class FragmentBonds : Fragment(), FragmentBrowser {

    private lateinit var bondsViewModel: BondsViewModel

    private var  textTitle: TextView? = null
    private var tableLayout: TableLayout? = null
    private var colorLight:Int = 0
    private var colorDark:Int = 0
    private var colorBuy:Int = 0
    private var colorSell:Int = 0
    private lateinit var tableBorder: Drawable


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view: View? = inflater!!.inflate(R.layout.fragment_tab, container, false)

        textTitle = view?.findViewById<TextView>(R.id.text_title) as TextView
        textTitle?.text = "---BONDS---"


        colorLight = ContextCompat.getColor(requireContext(), R.color.light)
        colorDark = ContextCompat.getColor(requireContext(), R.color.dark)
        colorBuy = ContextCompat.getColor(requireContext(), R.color.buy)
        colorSell = ContextCompat.getColor(requireContext(), R.color.sell)

        tableBorder = ContextCompat.getDrawable(requireContext(), R.drawable.border)!!

        tableLayout = view?.findViewById<TableLayout>(R.id.table_layout) as TableLayout

        getBonds()

        bondsViewModel.mResponse.observe(viewLifecycleOwner, Observer { response ->
            textTitle?.text = ""
            fillTable(response)
        })


        return view
    }

    private fun fillTable(response: List<BondItem>?) {
        val tableRow = TableRow(context)

        val textViewName = TextView(context)
        textViewName.setPadding(10)
        textViewName.setTextColor(colorBuy)
        textViewName.text = "Облигация"
        textViewName.gravity = Gravity.CENTER
        tableRow.addView(textViewName)

        val textViewProfit = TextView(context)
        textViewProfit.setPadding(10)
        textViewProfit.setTextColor(colorBuy)
        textViewProfit.gravity = Gravity.CENTER
        textViewProfit.text = "%"
        tableRow.addView(textViewProfit)

        val textViewDuration = TextView(context)
        textViewDuration.setPadding(10)
        textViewDuration.setTextColor(colorBuy)
        textViewDuration.gravity = Gravity.CENTER
        textViewDuration.text = "Дюрация"
        tableRow.addView(textViewDuration)

        val textViewPrice = TextView(context)
        textViewPrice.setPadding(10)
        textViewPrice.setTextColor(colorBuy)
        textViewPrice.gravity = Gravity.CENTER
        textViewPrice.text = "Цена"
        tableRow.addView(textViewPrice)

        val textViewCoupon = TextView(context)
        textViewCoupon.setPadding(10)
        textViewCoupon.setTextColor(colorBuy)
        textViewCoupon.gravity = Gravity.CENTER
        textViewCoupon.text = "Купон"
        tableRow.addView(textViewCoupon)

        val textViewClose = TextView(context)
        textViewClose.setPadding(10)
        textViewClose.setTextColor(colorBuy)
        textViewClose.gravity = Gravity.CENTER
        textViewClose.text = "Закрытие"
        tableRow.addView(textViewClose)

        tableRow.gravity = Gravity.CENTER
        tableLayout?.addView(tableRow)

        var irow:Int = 0
        for(item in response!!) {
            val tableRow = TableRow(context)
            tableRow.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            val textViewName = TextView(context)
            textViewName.setPadding(10)
            textViewName.gravity = Gravity.CENTER
            textViewName.text = item.name
            tableRow.addView(textViewName)

            val textViewProfit = TextView(context)
            textViewProfit.setPadding(10)
            textViewProfit.gravity = Gravity.CENTER
            textViewProfit.text = item.profit
            tableRow.addView(textViewProfit)

            val textViewDuration = TextView(context)
            textViewDuration.setPadding(10)
            textViewDuration.gravity = Gravity.CENTER
            textViewDuration.text = item.duration
            tableRow.addView(textViewDuration)

            val textViewPrice = TextView(context)
            textViewPrice.setPadding(10)
            textViewPrice.gravity = Gravity.CENTER
            textViewPrice.text = item.price
            tableRow.addView(textViewPrice)

            val textViewCoupon = TextView(context)
            textViewCoupon.setPadding(10)
            textViewCoupon.gravity = Gravity.CENTER
            textViewCoupon.text = item.coupon
            tableRow.addView(textViewCoupon)

            val textViewClose = TextView(context)
            textViewClose.setPadding(10)
            textViewClose.gravity = Gravity.CENTER
            textViewClose.text = item.close.replace("-",".")
            tableRow.addView(textViewClose)

            tableRow.setOnClickListener {
                openBrowser(this,item.link)
            }

            tableRow.gravity = Gravity.CENTER
            if(irow % 2 == 0)
                tableRow.setBackgroundColor(Color.BLACK)
            irow++

            tableLayout?.addView(tableRow)
        }
    }


    private fun getBonds() {
        val repository = Repository()
        val bondsViewModelFactory = BondsViewModelFactory(repository)
        bondsViewModel = ViewModelProvider(this, bondsViewModelFactory).get(BondsViewModel::class.java)

        try {
            bondsViewModel.getBonds()
        } catch (e: Exception){
            Log.d("BONDS:", "--error--")
            textTitle?.text = "Не удалось загрузить данные."
        }
    }
}