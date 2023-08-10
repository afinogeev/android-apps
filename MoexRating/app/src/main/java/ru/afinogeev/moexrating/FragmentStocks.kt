package ru.afinogeev.moexrating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.util.Log
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.afinogeev.moexrating.model.StockItem
import ru.afinogeev.moexrating.repository.Repository
import ru.afinogeev.moexrating.util.FragmentBrowser


class FragmentStocks : Fragment(), FragmentBrowser {

    private lateinit var stocksViewModel: StocksViewModel

    private var  textTitle: TextView? = null
    private var tableLayout: TableLayout? = null
    private var colorLight:Int = 0
    private var colorDark:Int = 0
    private var colorBuy:Int = 0
    private var colorSell:Int = 0


    @RequiresApi(Build.VERSION_CODES.M)
        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view: View? = inflater!!.inflate(R.layout.fragment_tab, container, false)

        textTitle = view?.findViewById<TextView>(R.id.text_title) as TextView
        textTitle?.text = "---STOCKS---"


        colorLight = ContextCompat.getColor(requireContext(), R.color.light)
        colorDark = ContextCompat.getColor(requireContext(), R.color.dark)
        colorBuy = ContextCompat.getColor(requireContext(), R.color.buy)
        colorSell = ContextCompat.getColor(requireContext(), R.color.sell)

        tableLayout = view?.findViewById<TableLayout>(R.id.table_layout) as TableLayout

        getStocks()

        stocksViewModel.mResponse.observe(viewLifecycleOwner, Observer { response ->
            textTitle?.text = ""
            fillTable(response)
        })


        return view
    }

    private fun fillTable(response: List<StockItem>?) {
        val tableRow = TableRow(context)
        val textViewName = TextView(context)
        val textViewRating = TextView(context)
        val textViewGraph = TextView(context)
        val textViewFund = TextView(context)
        textViewName.setPadding(10)
        textViewRating.setPadding(10)
        textViewGraph.setPadding(10)
        textViewFund.setPadding(10)
        textViewName.setTextColor(colorBuy)
        textViewName.text = "Акция"
        textViewRating.setTextColor(colorBuy)
        textViewRating.text = "Оценка"
        textViewGraph.setTextColor(colorBuy)
        textViewGraph.text = "График"
        textViewFund.setTextColor(colorBuy)
        textViewFund.text = "Анализ"
        tableRow.addView(textViewName)
        tableRow.addView(textViewRating)
        tableRow.addView(textViewGraph)
        tableRow.addView(textViewFund)
        tableRow.gravity = Gravity.CENTER
        tableLayout?.addView(tableRow)
        for(item in response!!) {
            val tableRow = TableRow(context)
            tableRow.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            val textViewName = TextView(context)
            textViewName.setPadding(10)

            val textViewRating = TextView(context)
            textViewRating.setPadding(10)
            textViewRating.gravity = Gravity.CENTER

            var sName = item.name
//            if (item.name.length > 20)
//                sName = item.name.substring(0, 20)
            textViewName.text = sName
            textViewRating.text = item.rating.toString()


            val lPar = RelativeLayout.LayoutParams(50, 50)

            val buttonGraphLayout = RelativeLayout(context)
            val buttonGraph = Button(context)
            buttonGraph.setBackgroundResource(R.drawable.ic_graph)
            buttonGraph.layoutParams = lPar
            buttonGraph.gravity = Gravity.CENTER
            buttonGraphLayout.addView(buttonGraph)
            buttonGraphLayout.setPadding(10)
            buttonGraphLayout.gravity = Gravity.CENTER

            val buttonFundLayout = RelativeLayout(context)
            val buttonFund = Button(context)
            buttonFund.setBackgroundResource(R.drawable.ic_pie)
            buttonFund.layoutParams = lPar
            buttonFund.gravity = Gravity.CENTER
            buttonFundLayout.addView(buttonFund)
            buttonFundLayout.setPadding(10)
            buttonFundLayout.gravity = Gravity.CENTER

            buttonGraph.setOnClickListener {
                openBrowser(this,item.graph)
            }

            buttonFund.setOnClickListener {
                openBrowser(this,item.fundamental)
            }

            if(item.tech > 1) {

                tableRow.setBackgroundColor(colorBuy)
                textViewName.setTextColor(colorDark)
                textViewRating.setTextColor(colorDark)
            }
            else if (item.tech < -1) {
                tableRow.setBackgroundColor(colorSell)
                textViewName.setTextColor(colorDark)
                textViewRating.setTextColor(colorDark)
            }

            tableRow.addView(textViewName)
            tableRow.addView(textViewRating)
            tableRow.addView(buttonGraphLayout)
            tableRow.addView(buttonFundLayout)
            tableRow.gravity = Gravity.CENTER
            tableLayout?.addView(tableRow)
        }
    }


    private fun getStocks() {
        val repository = Repository()
//        stocksViewModel = StocksViewModel(repository)
        val stocksViewModelFactory = StocksViewModelFactory(repository)
        stocksViewModel = ViewModelProvider(this, stocksViewModelFactory).get(StocksViewModel::class.java)

        try {
            stocksViewModel.getStocks()
        } catch (e: Exception){
            Log.d("STOCKS:", "--error--")
            textTitle?.text = "Не удалось загрузить данные."
        }
    }
}