package ru.afinogeev.moexrating

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import kotlinx.coroutines.*


open class MainActivity : AppCompatActivity() {

    private lateinit var adapter: PagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val tabNames: Array<String> = arrayOf(
        "Акции",
        "Облигации",
    )

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        supportActionBar.hom
//
        if(isOnline(this@MainActivity)) {
            setContentView(R.layout.activity_main)
            adapter = PagerAdapter(this)
            viewPager = findViewById(R.id.pager)
            viewPager.adapter = adapter

            tabLayout = findViewById(R.id.tab_layout)
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = tabNames[position]
            }.attach()
        }
        else {
            setContentView(R.layout.nointernet)
            waitOnline()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_about -> {
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                    return true
                }
                R.id.menu_contacts -> {
                    val intent = Intent(this, ContactsActivity::class.java)
                    startActivity(intent)
                    return true
                }
                android.R.id.home-> {
                    finish()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    return true
                }

            }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun waitOnline(){
        GlobalScope.launch(Dispatchers.Main) {

            var loop = true
            while (loop){
                if(isOnline(this@MainActivity)) {
                    loop = false
                    finish()
                    val mIntent = Intent(this@MainActivity, MainActivity::class.java)
                    startActivity(mIntent)
                }
                delay(1000)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if(capabilities != null) return true
        return false
    }


}

class PagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when(position) {
            0 -> fragment = FragmentStocks()
            1 -> fragment = FragmentBonds()
        }
        return fragment!!
    }
}