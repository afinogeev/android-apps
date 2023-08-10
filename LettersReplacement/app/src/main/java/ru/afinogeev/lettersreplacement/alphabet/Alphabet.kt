package ru.afinogeev.lettersreplacement.alphabet

import android.content.Context
import android.util.Log
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

object Alphabet {

    const val filename: String = "alphabet.csv"
    private lateinit var data: MutableList<Symbol>

    fun getByI(i:Int): Symbol {
        return data[i]
    }

    fun getAll(): MutableList<Symbol> {
        return data
    }

    fun setAll(csvdata: MutableList<Symbol>){
        data = csvdata
    }

    fun saveNewLetter(context: Context, newletter: Symbol){
        data?.forEach {
            if (it.name == newletter.name) {
                it.std =  newletter.std!!
                it.ult =  newletter.ult!!
                writeFile(context!!)
            }
        }
    }

    fun getByLetter(letter: String): Symbol? {
        data.forEach {
            if (it.name == letter)
                return it
        }
        return null
    }

    fun readFile(context: Context) {
        try {
            GlobalScope.launch(Dispatchers.Main) {
                data = mutableListOf()
                val inFile = File(context.getExternalFilesDir(null), filename)
                csvReader().open(inFile) {
                    readAllAsSequence().forEach { row ->
                        Log.d("csv", String.format("row %s: %s, %s", row[0], row[1], row[2]));
                        data?.add(Symbol(row[0], row[1], row[2]))
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("csv", "Failed to read csv", e)
        }


    }

    fun writeFile(context: Context) {
        try {
            GlobalScope.launch(Dispatchers.Main) {
                val outFile = File(context.getExternalFilesDir(null), filename)
                csvWriter().open(outFile) {
                    data.forEach {
                        val syml = listOf(it.name, it.std, it.ult)
                        writeRow(syml)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("csv", "Failed to write csv", e)
        }

    }
}